package com.typewritermc.basic.entries.action

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entries.ActionEntry
import com.typewritermc.engine.paper.entry.entries.ActionTrigger
import com.typewritermc.engine.paper.entry.entries.Var
import com.typewritermc.engine.paper.utils.SoundId
import net.kyori.adventure.sound.SoundStop
import java.util.*

@Entry("stop_sound", "停止玩家当前播放的某个或所有音效", Colors.RED, "teenyicons:sound-off-solid")
/**
 * The `Stop Sound` action is used to stop a or all sounds for a player.
 *
 * ## How could this be used?
 *
 * This action can be useful in situations where you want to stop a sound for a player.
 * For example, when leaving a certain area, you might want to stop the music that was playing.
 */
class StopSoundActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    @Help("要停止的音效名称（留空则停止所有音效）")
    val sound: Optional<Var<SoundId>> = Optional.empty(),
) : ActionEntry {
    override fun ActionTrigger.execute() {
        if (sound.isPresent) {
            val sound = sound.get().get(player, context)
            val soundStop = sound.namespacedKey?.let { SoundStop.named(it) } ?: return

            player.stopSound(soundStop)
        } else {
            player.stopAllSounds()
        }
    }
}