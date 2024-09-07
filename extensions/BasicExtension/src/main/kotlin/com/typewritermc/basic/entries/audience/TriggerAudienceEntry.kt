package com.typewritermc.basic.entries.audience

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.entries.Ref
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.core.entries.emptyRef
import com.typewritermc.engine.paper.entry.entries.AudienceDisplay
import com.typewritermc.engine.paper.entry.entries.AudienceEntry
import com.typewritermc.engine.paper.entry.triggerFor
import org.bukkit.entity.Player

@Entry(
    "trigger_audience",
    "当玩家进入或退出观众时触发一个序列",
    Colors.GREEN,
    "mdi:account-arrow-right"
)
/**
 * The `Trigger Audience` entry is an audience filter that triggers a sequence when the player enters or exits the audience.
 *
 * ## How could this be used?
 * This can be used to bridge the gap between audiences and sequence pages.
 */
class TriggerAudienceEntry(
    override val id: String = "",
    override val name: String = "",
    @Help("玩家进入观众时触发的序列。")
    val onEnter: Ref<TriggerableEntry> = emptyRef(),
    @Help("玩家退出观众时触发的序列。")
    val onExit: Ref<TriggerableEntry> = emptyRef(),
) : AudienceEntry {
    override fun display(): AudienceDisplay = TriggerAudienceDisplay(onEnter, onExit)
}

class TriggerAudienceDisplay(
    private val onEnter: Ref<TriggerableEntry>,
    private val onExit: Ref<TriggerableEntry>,
) : AudienceDisplay() {
    override fun onPlayerAdd(player: Player) {
        onEnter triggerFor player
    }

    override fun onPlayerRemove(player: Player) {
        onExit triggerFor player
    }
}