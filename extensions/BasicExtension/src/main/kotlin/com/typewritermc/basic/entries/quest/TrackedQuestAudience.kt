package com.typewritermc.basic.entries.quest

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.entries.emptyRef
import com.typewritermc.engine.paper.entry.entries.*
import com.typewritermc.engine.paper.entry.quest.isQuestTracked
import com.typewritermc.core.entries.ref
import com.typewritermc.engine.paper.events.AsyncTrackedQuestUpdate
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler

@Entry(
    "tracked_quest_audience",
    "根据玩家是否跟踪了任务来过滤观众",
    Colors.MEDIUM_SEA_GREEN,
    "mdi:notebook-heart"
)
/**
 * The `Tracked Quest Audience` entry filters an audience based on if they have a quest tracked.
 *
 * If no quest is referenced, it will filter based on if any quest is tracked.
 *
 * ## How could this be used?
 *
 * This could be used to show a boss bar or sidebar based on if a player has a quest tracked.
 */
class TrackedQuestAudience(
    override val id: String = "",
    override val name: String = "",
    override val children: List<Ref<AudienceEntry>> = emptyList(),
    @Help("如果未设置，它将根据是否跟踪了任何任务进行过滤。")
    val quest: Ref<QuestEntry> = emptyRef(),
    override val inverted: Boolean = false,
) : AudienceFilterEntry, Invertible {
    override fun display(): AudienceFilter = TrackedQuestAudienceFilter(
        ref(),
        quest
    )
}

class TrackedQuestAudienceFilter(
    ref: Ref<out AudienceFilterEntry>,
    private val quest: Ref<QuestEntry>
) : AudienceFilter(ref) {
    override fun filter(player: Player): Boolean = player.isQuestTracked(quest)

    @EventHandler
    private fun onTrackedQuestUpdate(event: AsyncTrackedQuestUpdate) {
        if (quest.isSet) {
            event.player.updateFilter(event.to == quest)
            return
        }

        event.player.updateFilter(event.to != null)
    }
}