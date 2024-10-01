package com.typewritermc.engine.paper.entry

import com.google.gson.*
import com.typewritermc.core.entries.Entry
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.entries.pageId
import com.typewritermc.core.utils.failure
import com.typewritermc.core.utils.ok
import com.typewritermc.core.utils.onFail
import com.typewritermc.engine.paper.entry.StagingState.*
import com.typewritermc.engine.paper.events.StagingChangeEvent
import com.typewritermc.engine.paper.logger
import com.typewritermc.engine.paper.plugin
import com.typewritermc.engine.paper.ui.ClientSynchronizer
import com.typewritermc.engine.paper.utils.ThreadType.DISPATCHERS_ASYNC
import com.typewritermc.engine.paper.utils.Timeout
import com.typewritermc.engine.paper.utils.get
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import org.koin.java.KoinJavaComponent
import java.io.File
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set
import kotlin.time.Duration.Companion.seconds

interface StagingManager {
    val stagingState: StagingState

    fun loadState()
    fun unload()
    fun fetchPages(): Map<String, JsonObject>
    fun createPage(data: JsonObject): Result<String>
    fun renamePage(pageId: String, newName: String): Result<String>
    fun changePageValue(pageId: String, path: String, value: JsonElement): Result<String>
    fun deletePage(pageId: String): Result<String>
    fun moveEntry(entryId: String, fromPageId: String, toPageId: String): Result<String>
    fun createEntry(pageId: String, data: JsonObject): Result<String>
    fun updateEntryField(pageId: String, entryId: String, path: String, value: JsonElement): Result<String>
    fun updateEntry(pageId: String, data: JsonObject): Result<String>
    fun reorderEntry(pageId: String, entryId: String, newIndex: Int): Result<String>
    fun deleteEntry(pageId: String, entryId: String): Result<String>

    fun findEntryPage(entryId: String): Result<String>
    suspend fun publish(): Result<String>
    fun shutdown()
}

class StagingManagerImpl : StagingManager, KoinComponent {
    private val gson: Gson by inject(named("bukkitDataParser"))

    private val pages = ConcurrentHashMap<String, JsonObject>()

    private val autoSaver =
        Timeout(
            3.seconds,
            ::saveStaging,
            immediateRunnable = {
                // When called to save, we immediately want to update the staging state
                if (stagingState != PUBLISHING) stagingState = STAGING
            })

    private val stagingDir
        get() = plugin.dataFolder["staging"]

    private val publishedDir
        get() = plugin.dataFolder["pages"]

    override var stagingState = PUBLISHED
        private set(value) {
            field = value
            StagingChangeEvent(value).callEvent()
        }

    override fun loadState() {
        stagingState = if (stagingDir.exists()) STAGING else PUBLISHED

        // Read the pages from the file
        val dir = if (stagingState == STAGING) stagingDir else publishedDir
        pages.clear()
        pages.putAll(fetchPages(dir))
    }

    private fun fetchPages(dir: File): Map<String, JsonObject> {
        val pages = mutableMapOf<String, JsonObject>()
        dir.listFiles()?.filter { it.extension == "json" }?.forEach { file ->
            val page = file.readText()
            val pageId = file.nameWithoutExtension
            val pageJson = gson.fromJson(page, JsonObject::class.java)
            // Always force the id to be the same as the file name
            pageJson.addProperty("id", pageId)
            pages[pageId] = pageJson
        }
        return pages
    }

    override fun fetchPages(): Map<String, JsonObject> {
        return pages
    }

    override fun unload() {
        autoSaver.force()
    }

    override fun createPage(data: JsonObject): Result<String> {
        if (!data.has("id")) return failure("ID为必填项")
        if (!data.has("name")) return failure("名称为必填项")
        val idJson = data["id"]
        if (!idJson.isJsonPrimitive || !idJson.asJsonPrimitive.isString) return failure("ID必须是字符串")
        val id = idJson.asString

        if (pages.containsKey(id)) return failure("具有该 ID 的页面已存在")

        // Add the version of this page to track migrations
        data.addProperty("version", plugin.pluginMeta.version)

        pages[id] = data
        autoSaver()
        return ok("成功创建名为 $id 的页面")
    }

    override fun renamePage(pageId: String, newName: String): Result<String> {
        val page = getPage(pageId) onFail { return it }

        page.addProperty("name", newName)

        autoSaver()
        return ok("已成功将页面从 $pageId 重命名为 $newName")
    }

    override fun changePageValue(pageId: String, path: String, value: JsonElement): Result<String> {
        val page = getPage(pageId) onFail { return it }

        page.changePathValue(path, value)

        autoSaver()
        return ok("已成功更新字段")
    }

    override fun deletePage(pageId: String): Result<String> {
        pages.remove(pageId) ?: return failure("页面不存在")

        autoSaver()
        return ok("成功删除名为 $pageId 的页面")
    }

    override fun moveEntry(entryId: String, fromPageId: String, toPageId: String): Result<String> {
        val from = pages[fromPageId] ?: return failure("页面 '$fromPageId' 不存在")
        val to = pages[toPageId] ?: return failure("页面 '$toPageId' 不存在")

        val entry = from["entries"].asJsonArray.find { it.asJsonObject["id"].asString == entryId }
            ?: return failure("页面 '$fromPageId' 中不存在条目")

        from["entries"].asJsonArray.remove(entry)
        to["entries"].asJsonArray.add(entry)

        autoSaver()
        return ok("成功移动条目")
    }

    override fun createEntry(pageId: String, data: JsonObject): Result<String> {
        val page = getPage(pageId) onFail { return it }
        val entries = page["entries"] as? JsonArray ?: JsonArray()

        entries.add(data)
        page.add("entries", entries)

        autoSaver()
        return ok("已成功创建 ID 为 ${data["id"]} 的条目")
    }

    override fun updateEntryField(
        pageId: String,
        entryId: String,
        path: String,
        value: JsonElement
    ): Result<String> {
        // Update the page
        val page = getPage(pageId) onFail { return it }
        val entries = page["entries"].asJsonArray
        val entry = entries.find { it.asJsonObject["id"].asString == entryId } ?: return failure("条目不存在")

        // Update the entry
        entry.changePathValue(path, value)

        autoSaver()
        return ok("已成功更新字段")
    }

    override fun updateEntry(pageId: String, data: JsonObject): Result<String> {
        val page = getPage(pageId) onFail { return it }
        val entries = page["entries"] as? JsonArray ?: return failure("页面没有任何条目")
        val entryId = data["id"]?.asString ?: return failure("条目没有 id")

        entries.removeAll { entry -> entry.asJsonObject["id"]?.asString == entryId }
        entries.add(data)

        autoSaver()
        return ok("已成功更新 ID 为 ${data["id"]} 的条目")
    }

    override fun reorderEntry(pageId: String, entryId: String, newIndex: Int): Result<String> {
        val page = getPage(pageId) onFail { return it }
        val entries = page["entries"].asJsonArray
        val oldIndex = entries.indexOfFirst { it.asJsonObject["id"].asString == entryId }

        if (oldIndex == -1) return failure("条目不存在")
        if (oldIndex == newIndex) return ok("条目已位于正确的索引处")

        val correctIndex = if (oldIndex < newIndex) newIndex - 1 else newIndex

        val entryAtNewIndex = entries[correctIndex]
        entries[correctIndex] = entries[oldIndex]
        entries[oldIndex] = entryAtNewIndex

        autoSaver()
        return ok("已成功重新排序条目")
    }

    override fun deleteEntry(pageId: String, entryId: String): Result<String> {
        val page = getPage(pageId) onFail { return it }
        val entries = page["entries"].asJsonArray
        val entry = entries.find { it.asJsonObject["id"].asString == entryId } ?: return failure("条目不存在")

        entries.remove(entry)

        autoSaver()
        return ok("已成功删除 ID 为 $entryId 的条目")
    }

    override fun findEntryPage(entryId: String): Result<String> {
        val page = pages.values.find { page ->
            page["entries"].asJsonArray.any { entry -> entry.asJsonObject["id"].asString == entryId }
        } ?: return failure("条目不存在")

        return ok(page["name"].asString)
    }

    private fun getPage(id: String): Result<JsonObject> {
        val page = pages[id] ?: return failure("页面“$id”不存在")
        return ok(page)
    }

    private fun saveStaging() {
        // If we are already publishing, we don't want to save the staging
        if (stagingState == PUBLISHING) return
        val dir = stagingDir

        pages.forEach { (id, page) ->
            val file = dir["$id.json"]
            if (!file.exists()) {
                file.parentFile.mkdirs()
                file.createNewFile()
            }
            file.writeText(page.toString())
        }

        dir.listFiles()?.filter { it.nameWithoutExtension !in pages.keys }?.forEach { it.delete() }

        stagingState = STAGING
    }

    // Save the page to the file
    override suspend fun publish(): Result<String> {
        if (stagingState != STAGING) return failure("只能在暂存时发布")
        autoSaver.cancel()
        return DISPATCHERS_ASYNC.switchContext {
            stagingState = PUBLISHING

            if (!publishedDir.exists()) publishedDir.mkdirs()

            try {
                pages.forEach { (name, page) ->
                    val file = publishedDir["$name.json"]
                    file.createNewFile()
                    file.writeText(page.toString())
                }

                val stagingPages = pages.keys
                val publishedFiles = publishedDir.listFiles()?.toList() ?: emptyList()

                val deletedPages = publishedFiles.filter { it.nameWithoutExtension !in stagingPages }
                if (deletedPages.isNotEmpty()) {
                    logger.info(
                        "删除 ${deletedPages.size} 页面，因为它们不再处于暂存状态。 (${
                            deletedPages.joinToString(
                                ", "
                            ) { it.nameWithoutExtension }
                        })"
                    )
                    deletedPages.backup()
                    deletedPages.forEach { it.delete() }
                }

                // Delete the staging folder
                stagingDir.deleteRecursively()
                plugin.reload()
                stagingState = PUBLISHED
                ok("成功发布暂存状态")
            } catch (e: Exception) {
                e.printStackTrace()
                stagingState = STAGING
                failure("无法发布暂存状态")
            }
        }
    }

    override fun shutdown() {
        if (stagingState == STAGING) saveStaging()
    }
}

fun JsonElement.changePathValue(path: String, value: JsonElement) {
    val pathParts = path.split(".")
    var current: JsonElement = this
    pathParts.forEachIndexed { index, key ->
        if (index == pathParts.size - 1) {
            if (current.isJsonObject) {
                current.asJsonObject.add(key, value)
            } else if (current.isJsonArray) {
                val i = Integer.parseInt(key)
                while (current.asJsonArray.size() <= i) {
                    current.asJsonArray.add(JsonNull.INSTANCE)
                }
                current.asJsonArray[i] = value
            }
        } else if (current.isJsonObject) {
            if (!current.asJsonObject.has(key)) {
                current.asJsonObject.add(key, JsonObject())
            }
            current = current.asJsonObject[key]
        } else if (current.isJsonArray) {
            val i = Integer.parseInt(key)
            while (current.asJsonArray.size() <= i) {
                current.asJsonArray.add(JsonObject())
            }
            current = current.asJsonArray[i]
        }
    }
}

enum class StagingState {
    PUBLISHING,
    STAGING,
    PUBLISHED
}

inline fun <reified T : Any> Ref<out Entry>.fieldValue(path: String, value: T) = fieldValue(path, value, T::class.java)
fun Ref<out Entry>.fieldValue(path: String, value: Any, type: Type) {
    val stagingManager = KoinJavaComponent.get<StagingManager>(StagingManager::class.java)
    val gson = KoinJavaComponent.get<Gson>(Gson::class.java, named("dataSerializer"))

    val pageId = pageId
    if (pageId == null) {
        logger.warning("未找到 $this 的 pageId 。你是否忘记发布了？")
        return
    }

    val json = gson.toJsonTree(value, type)
    val result = stagingManager.updateEntryField(pageId, id, path, json)
    if (result.isFailure) {
        logger.warning("更新字段失败：${result.exceptionOrNull()}")
        return
    }

    val clientSynchronizer = KoinJavaComponent.get<ClientSynchronizer>(ClientSynchronizer::class.java)
    clientSynchronizer.sendEntryFieldUpdate(pageId, id, path, json)
}

fun List<File>.backup() {
    if (isEmpty()) {
        // Nothing to back up
        return
    }
    val date = Date()
    val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss")

    val backupFolder = plugin.dataFolder["backup/${dateFormat.format(date)}"]
    backupFolder.mkdirs()
    forEach {
        val file = it
        val backupFile = File(backupFolder, file.name)
        file.copyTo(backupFile, overwrite = true)
    }
}