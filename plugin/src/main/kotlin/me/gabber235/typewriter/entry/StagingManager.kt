package me.gabber235.typewriter.entry

import com.google.gson.*
import lirand.api.extensions.events.listen
import me.gabber235.typewriter.entry.StagingState.*
import me.gabber235.typewriter.events.PublishedBookEvent
import me.gabber235.typewriter.events.StagingChangeEvent
import me.gabber235.typewriter.events.TypewriterReloadEvent
import me.gabber235.typewriter.logger
import me.gabber235.typewriter.plugin
import me.gabber235.typewriter.ui.ClientSynchronizer
import me.gabber235.typewriter.utils.*
import me.gabber235.typewriter.utils.ThreadType.DISPATCHERS_ASYNC
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import org.koin.java.KoinJavaComponent
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.seconds

interface StagingManager {
    val stagingState: StagingState

    fun initialize()
    fun fetchPages(): Map<String, JsonObject>
    fun createPage(data: JsonObject): Result<String>
    fun renamePage(oldName: String, newName: String): Result<String>
    fun changePageValue(pageId: String, path: String, value: JsonElement): Result<String>
    fun deletePage(name: String): Result<String>
    fun moveEntry(entryId: String, fromPage: String, toPage: String): Result<String>
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

    override fun initialize() {
        loadState()

        plugin.listen<TypewriterReloadEvent> { loadState() }
    }

    private fun loadState() {
        stagingState = if (stagingDir.exists()) {
            // Migrate staging directory to use the new format
            stagingDir.migrateIfNecessary()

            val stagingPages = fetchPages(stagingDir)
            val publishedPages = fetchPages(publishedDir)

            if (stagingPages == publishedPages) PUBLISHED else STAGING
        } else PUBLISHED

        // Read the pages from the file
        val dir =
            if (stagingState == STAGING) stagingDir else publishedDir
        pages.putAll(fetchPages(dir))
    }

    private fun fetchPages(dir: File): Map<String, JsonObject> {
        val pages = mutableMapOf<String, JsonObject>()
        dir.pages().forEach { file ->
            val page = file.readText()
            val pageName = file.nameWithoutExtension
            val pageJson = gson.fromJson(page, JsonObject::class.java)
            // Sometimes the name of the page is out of sync with the file name, so we need to update it
            pageJson.addProperty("name", pageName)
            pages[pageName] = pageJson
        }
        return pages
    }

    override fun fetchPages(): Map<String, JsonObject> {
        return pages
    }

    override fun createPage(data: JsonObject): Result<String> {
        if (!data.has("name")) return failure("名称为必填项")
        val nameJson = data["name"]
        if (!nameJson.isJsonPrimitive || !nameJson.asJsonPrimitive.isString) return failure("名称必须是字符串")
        val name = nameJson.asString

        if (pages.containsKey(name)) return failure("具有该名称的页面已存在")

        // Add the version of this page to track migrations
        data.addProperty("version", plugin.pluginMeta.version)

        pages[name] = data
        autoSaver()
        return ok("已成功创建名为 $name 的页面")
    }

    override fun renamePage(oldName: String, newName: String): Result<String> {
        if (pages.containsKey(newName)) return failure("具有该名称的页面已存在")
        val oldPage = pages.remove(oldName) ?: return failure("页面“$oldName”不存在")

        oldPage.addProperty("name", newName)

        pages[newName] = oldPage
        autoSaver()
        return ok("已成功将页面从 $oldName 重命名为 $newName")
    }

    override fun changePageValue(pageId: String, path: String, value: JsonElement): Result<String> {
        val page = getPage(pageId) onFail { return it }

        page.changePathValue(path, value)

        autoSaver()
        return ok("已成功更新字段")
    }

    override fun deletePage(name: String): Result<String> {
        pages.remove(name) ?: return failure("页面不存在")

        autoSaver()
        return ok("已成功删除名为 $name 的页面")
    }

    override fun moveEntry(entryId: String, fromPage: String, toPage: String): Result<String> {
        val from = pages[fromPage] ?: return failure("页面 '$fromPage' 不存在")
        val to = pages[toPage] ?: return failure("页面 '$toPage' 不存在")

        val entry = from["entries"].asJsonArray.find { it.asJsonObject["id"].asString == entryId }
            ?: return failure("条目在页面 '$fromPage' 中不存在")

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

        pages.forEach { (name, page) ->
            val file = dir["$name.json"]
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

            try {
                pages.forEach { (name, page) ->
                    val file = publishedDir["$name.json"]
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
                PublishedBookEvent().callEvent()
                logger.info("发布暂存状态")
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

fun Ref<out Entry>.fieldValue(path: String, value: Any) {
    val stagingManager = KoinJavaComponent.get<StagingManager>(StagingManager::class.java)
    val gson = KoinJavaComponent.get<Gson>(Gson::class.java, named("entryParser"))

    val pageId = pageId
    if (pageId == null) {
        logger.warning("未找到 $this 的 pageId 。你是否忘记发布了？")
        return
    }

    val json = gson.toJsonTree(value)
    val result = stagingManager.updateEntryField(pageId, id, path, json)
    if (result.isFailure) {
        logger.warning("更新字段失败：${result.exceptionOrNull()}")
        return
    }

    val clientSynchronizer = KoinJavaComponent.get<ClientSynchronizer>(ClientSynchronizer::class.java)
    clientSynchronizer.sendEntryFieldUpdate(pageId, id, path, json)
}