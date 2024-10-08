package me.gabber235.typewriter.content.modes

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import lirand.api.extensions.events.unregister
import lirand.api.extensions.inventory.meta
import lirand.api.extensions.server.registerEvents
import me.gabber235.typewriter.content.*
import me.gabber235.typewriter.content.components.*
import me.gabber235.typewriter.entry.AssetManager
import me.gabber235.typewriter.entry.entries.*
import me.gabber235.typewriter.entry.forceTriggerFor
import me.gabber235.typewriter.entry.triggerFor
import me.gabber235.typewriter.interaction.startBlockingActionBar
import me.gabber235.typewriter.interaction.stopBlockingActionBar
import me.gabber235.typewriter.plugin
import me.gabber235.typewriter.utils.ThreadType.SYNC
import me.gabber235.typewriter.utils.failure
import me.gabber235.typewriter.utils.loreString
import me.gabber235.typewriter.utils.name
import me.gabber235.typewriter.utils.ok
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set
import kotlin.reflect.KClass

inline fun <reified T : Any> ComponentContainer.recordingCinematic(
    context: ContentContext,
    slot: Int,
    noinline frameFetcher: () -> Int,
    noinline modeCreator: (ContentContext, Player, Int, KClass<T>) -> RecordingCinematicContentMode<T>,
) = +RecordingCinematicComponent(context, slot, frameFetcher, modeCreator, T::class)

class RecordingCinematicComponent<T : Any>(
    private val context: ContentContext,
    val slot: Int,
    val frameFetcher: () -> Int,
    val modeCreator: (ContentContext, Player, Int, KClass<T>) -> RecordingCinematicContentMode<T>,
    val klass: KClass<T>,
) : ContentComponent, ItemsComponent {
    override suspend fun initialize(player: Player) {}
    override suspend fun tick(player: Player) {}
    override suspend fun dispose(player: Player) {}

    override fun items(player: Player): Map<Int, IntractableItem> {
        if (frameFetcher() > (context.endFrame ?: 0)) {
            val item = ItemStack(Material.BARRIER).meta {
                name = "<red><b>无法开始录制"
                loreString = """
                    |<line> <gray>无法开始录制
                    |<line> <gray>因为帧超出了范围。
                    |
                    |<line> <gray>请确保过场动画帧
                    |<line> <gray>在片段的结束帧之前。
                """.trimMargin()
            } onInteract {}
            return mapOf(slot to item)
        }

        val item = ItemStack(Material.BOOK).meta {
            name = "<green><b>开始录制"
            loreString = "<line> <gray>点击开始录制过场动画。"
        } onInteract {
            ContentModeTrigger(context, modeCreator(context, player, frameFetcher(), klass)) triggerFor player
        }

        return mapOf(slot to item)
    }
}

abstract class RecordingCinematicContentMode<T : Any>(
    context: ContentContext,
    player: Player,
    private val initialFrame: Int = 0,
    private val klass: KClass<T>,
) : ContentMode(context, player), Listener, KoinComponent {
    private val gson: Gson by inject(named("bukkitDataParser"))
    private val assetManager: AssetManager by inject()

    private var asset: AssetEntry? = null
    private lateinit var actions: List<CinematicAction>
    private var frame = initialFrame
    private lateinit var frames: IntRange

    private var tape = mutableTapeOf<T>()

    override suspend fun setup(): Result<Unit> {
        val startFrame = context.startFrame
        val endFrame = context.endFrame
        if (startFrame == null || endFrame == null) {
            return failure(
                """
                |上下文中缺少startFrame或endFrame。
                |上下文：$context
                |
                |RecordingCinematicContentMode只能用于过场动画的片段。
                |请将此问题报告给适配器开发者。
            """.trimMargin()
            )
        }

        frames = startFrame..endFrame

        val result = getAssetFromFieldValue(context.fieldValue)
        if (result.isFailure) {
            return failure(
                """
                |无法从字段值获取资源（${context.fieldValue}）：
                |${result.exceptionOrNull()?.message}
                |
                |很可能是你在使用内容模式前忘记发布资源了。
            """.trimMargin()
            )
        }

        asset = result.getOrThrow()


        val page =
            findCinematicPageById(context.pageId) ?: return failure("未找到ID为${context.pageId}的过场动画页面")

        val entryId = context.entryId

        actions = page.entries.filterIsInstance<CinematicEntry>().mapNotNull {
            if (it.id == entryId) {
                it.createRecording(player)
            } else {
                it.createSimulating(player)
            }
        }

        bossBar {
            color = BossBar.Color.YELLOW
            if (frame < startFrame) {
                val secondsLeft = (startFrame - frame) / 20
                val color = when {
                    secondsLeft <= 1 -> "red"
                    secondsLeft <= 3 -> "#de751f"
                    secondsLeft <= 5 -> "yellow"
                    else -> "green"
                }

                title = "将在<$color><bold>$secondsLeft</bold></$color>后开始录制"
                progress = 1f - (frame - initialFrame) / (startFrame - initialFrame).toFloat()
                return@bossBar
            }

            val secondsLeft = (endFrame - frame) / 20
            title = "录制将在<bold>$secondsLeft</bold>后结束"
            progress = (frame - frames.first) / (frames.last - frames.first).toFloat()
        }
        return ok(Unit)
    }

    override suspend fun initialize() {
        plugin.registerEvents(this)
        super.initialize()

        // Load in the old tape if it exists
        val asset = asset
            ?: throw IllegalStateException("在设置后未找到用于录制过场动画的资源，这不应该发生。资源：'${context.fieldValue}'")
        val oldTapeData = if (assetManager.containsAsset(asset)) assetManager.fetchAsset(asset) else null
        if (oldTapeData != null) {
            tape = gson.fromJson(oldTapeData, tape.javaClass)
        }
        // If we are starting from the middle of the segment, apply the state
        if (frame > frames.first) {
            applyStartingState()
        }

        player.startBlockingActionBar()

        actions.forEach {
            try {
                it.setup()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override suspend fun tick() {
        frame++
        coroutineScope {
            actions.map {
                launch {
                    try {
                        it.tick(frame.coerceAtLeast(0))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }.joinAll()
        }
        super.tick()

        if (frame in frames) {
            recordFrame()
        } else if (frame > frames.last) {
            saveStore()
            SystemTrigger.CONTENT_POP forceTriggerFor player
        } else if (frame < frames.first) {
            preStart(frame)
        }
    }

    private fun preStart(frame: Int) {
        val secondsLeft = (frames.first - frame) / 20

        if (secondsLeft > 5) return
        if ((frames.first - frame) % 20 != 0) return
        player.playSound(
            Sound.sound(
                Key.key("block.note_block.bell"),
                Sound.Source.MASTER,
                1f,
                1f - (secondsLeft * 0.1f)
            )
        )
    }

    abstract fun captureFrame(): T
    abstract fun applyState(value: T)

    private suspend fun applyStartingState() {
        // We want to get the applied values. Since the tape is optimized, we can't just get the previous value
        // We need to look back through all the previous frames and apply the latest values for all keys
        val json = gson.toJsonTree(tape)
        if (!json.isJsonObject) return
        val obj = json.asJsonObject
        val frames = obj.keySet().mapNotNull { it.toIntOrNull() }.sorted()
        if (frames.isEmpty()) return
        val previousValues = JsonObject()
        for (frame in (frames.first()..frame).reversed()) {
            val dataElement = obj[frame.toString()] ?: continue
            if (!dataElement.isJsonObject) continue
            val data = dataElement.asJsonObject

            data.entrySet().filter { !previousValues.has(it.key) }.associate { it.key to it.value }
                .forEach { (key, value) ->
                    previousValues.add(key, value)
                }
        }

        val value = gson.fromJson(previousValues, klass.java)
        SYNC.switchContext {
            applyState(value)
        }
    }

    private fun recordFrame() {
        val relativeFrame = frame - frames.first
        val value = captureFrame()
        tape[relativeFrame] = value
    }

    private suspend fun saveStore() {
        val asset = asset ?: return
        val json = gson.toJsonTree(tape)
        optimizeTape(json)
        assetManager.storeAsset(asset, json.toString())
    }

    override suspend fun dispose() {
        unregister()
        player.stopBlockingActionBar()
        super.dispose()
        if (!::actions.isInitialized) return
        actions.forEach {
            try {
                it.teardown()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun optimizeTape(json: JsonElement) {
        if (!json.isJsonObject) return
        val obj = json.asJsonObject
        val frames = obj.keySet().mapNotNull { it.toIntOrNull() }.sorted()
        val previousValues = mutableMapOf<String, JsonElement>()
        for (frame in frames) {
            // If the previous value is the same as the current value, remove the current value
            val dataElement = obj[frame.toString()]
            if (!dataElement.isJsonObject) continue
            val data = dataElement.asJsonObject

            for (key in data.keySet().toList()) {
                val value = data[key]
                val previousValue = previousValues[key]
                if (previousValue != null && previousValue == value) {
                    data.remove(key)
                    continue
                }
                previousValues[key] = value
            }

            // If nothing changed in the frame, remove the frame
            if (data.size() == 0) {
                obj.remove(frame.toString())
            }
        }
    }
}