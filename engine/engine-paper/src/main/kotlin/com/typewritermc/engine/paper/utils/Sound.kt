package com.typewritermc.engine.paper.utils

import com.github.retrooper.packetevents.protocol.sound.SoundCategory
import com.github.retrooper.packetevents.protocol.sound.StaticSound
import com.github.retrooper.packetevents.resources.ResourceLocation
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntitySoundEffect
import com.typewritermc.core.entries.Query
import com.typewritermc.core.extension.annotations.Default
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.utils.point.Position
import com.typewritermc.engine.paper.entry.entries.SoundIdEntry
import com.typewritermc.engine.paper.entry.entries.SoundSourceEntry
import com.typewritermc.engine.paper.extensions.packetevents.sendPacketTo
import com.typewritermc.engine.paper.logger
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.audience.ForwardingAudience
import net.kyori.adventure.sound.SoundStop
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import net.kyori.adventure.sound.Sound as AdventureSound

data class Sound(
    val soundId: SoundId = SoundId.EMPTY,
    @Help("播放音效的位置来源（默认为玩家当前位置）")
    val soundSource: SoundSource = SelfSoundSource,
    @Help("对应Minecraft的音效分类")
    val track: AdventureSound.Source = AdventureSound.Source.MASTER,
    @Help("1.0为正常音量值")
    @Default("1.0")
    val volume: Float = 1.0f,
    @Help("1.0为正常音高值")
    @Default("1.0")
    val pitch: Float = 1.0f,
) {
    companion object {
        val EMPTY = Sound()
    }

    val soundStop: SoundStop?
        get() = soundId.namespacedKey?.let { SoundStop.named(it) }

    fun play(audience: Audience) {
        val key = this.soundId.namespacedKey ?: return
        val sound = AdventureSound.sound(key, track, volume, pitch)

        when (soundSource) {
            is SelfSoundSource -> audience.playSound(sound)
            is EmitterSoundSource -> {
                val entryId = soundSource.entryId
                val entry = Query.findById<SoundSourceEntry>(entryId)
                if (entry == null) {
                    logger.warning("找不到ID为${entryId}的音源条目")
                    return
                }
                audience.viewers.forEach { viewer ->
                    val emitter = entry.getEmitter(viewer)
                    val packetSound = StaticSound(ResourceLocation(key.namespace, key.key), 16f)
                    val category = SoundCategory.fromId(track.ordinal)
                    WrapperPlayServerEntitySoundEffect(
                        packetSound,
                        category,
                        emitter.entityId,
                        volume,
                        pitch
                    ) sendPacketTo viewer
                }
            }

            is LocationSoundSource -> {
                val location = soundSource.position
                audience.playSound(sound, location.x, location.y, location.z)
            }
        }
    }
}

fun Audience.playSound(sound: Sound) = sound.play(this)
fun Audience.stopSound(sound: Sound) = sound.soundStop?.let { this.stopSound(it) }

sealed interface SoundId {
    companion object {
        val EMPTY = DefaultSoundId(null)
    }

    val namespacedKey: NamespacedKey?
}

class DefaultSoundId(override val namespacedKey: NamespacedKey?) : SoundId {
    constructor(key: String) : this(if (key.isEmpty()) null else NamespacedKey.fromString(key))
}

class EntrySoundId(val entryId: String) : SoundId {
    override val namespacedKey: NamespacedKey?
        get() {
            val entry = Query.findById<SoundIdEntry>(entryId)
            if (entry == null) {
                logger.warning("找不到ID为${entryId}的音效条目")
                return null
            }
            return NamespacedKey.fromString(entry.soundId)
        }
}

sealed interface SoundSource

data object SelfSoundSource : SoundSource
class EmitterSoundSource(val entryId: String) : SoundSource

class LocationSoundSource(val position: Position) : SoundSource

val Audience.viewers: List<Player>
    get() = when (this) {
        is Player -> listOf(this)
        is ForwardingAudience -> audiences().flatMap { it.viewers }
        else -> throw IllegalArgumentException("无法从${this::class.simpleName}类型的受众获取观众")
    }