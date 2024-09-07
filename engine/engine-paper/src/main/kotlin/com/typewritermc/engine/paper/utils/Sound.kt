package com.typewritermc.engine.paper.utils

import com.typewritermc.core.entries.Query
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.utils.point.Position
import com.typewritermc.engine.paper.entry.entries.SoundIdEntry
import com.typewritermc.engine.paper.entry.entries.SoundSourceEntry
import com.typewritermc.engine.paper.logger
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.sound.SoundStop
import org.bukkit.NamespacedKey
import net.kyori.adventure.sound.Sound as AdventureSound


data class Sound(
    @Help("要播放的声音。")
    val soundId: SoundId = SoundId.EMPTY,
    @Help("播放声音的位置的来源。 （默认为玩家所在位置）")
    val soundSource: SoundSource = SelfSoundSource,
    @Help("播放声音的曲目。 （对应于 Minecraft 声音类别）")
    val track: AdventureSound.Source = AdventureSound.Source.MASTER,
    @Help("声音的音量。 值 1.0 是正常音量。")
    val volume: Float = 1.0f,
    @Help("声音的音高。 值 1.0 是正常音高。")
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
                    logger.warning("找不到 ID 为 $entryId 的声源条目")
                    return
                }
                val emitter = entry.getEmitter()
                audience.playSound(sound, emitter)
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
                logger.warning("找不到 ID 为 $entryId 的声音条目")
                return null
            }
            return NamespacedKey.fromString(entry.soundId)
        }
}

sealed interface SoundSource

data object SelfSoundSource : SoundSource
class EmitterSoundSource(val entryId: String) : SoundSource

class LocationSoundSource(val position: Position) : SoundSource
