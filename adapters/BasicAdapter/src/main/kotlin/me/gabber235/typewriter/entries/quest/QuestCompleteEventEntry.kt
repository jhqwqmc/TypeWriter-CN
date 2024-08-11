package me.gabber235.typewriter.entries.quest

import me.gabber235.typewriter.adapters.Colors
import me.gabber235.typewriter.adapters.Entry
import me.gabber235.typewriter.adapters.modifiers.Help
import me.gabber235.typewriter.entry.*
import me.gabber235.typewriter.entry.entries.EventEntry
import me.gabber235.typewriter.entry.entries.QuestEntry
import me.gabber235.typewriter.entry.quest.QuestStatus
import me.gabber235.typewriter.events.AsyncQuestStatusUpdate

@Entry("quest_complete_event", "当玩家完成任务时触发", Colors.YELLOW, "mdi:notebook-check")
/**
 * The `Quest Complete Event` entry is triggered when a quest is completed for a player.
 *
 * When no quest is referenced, it will trigger for all quests.
 *
 * ## How could this be used?
 * This could be used to show a title or notification to a player when a quest is completed.
 */
class QuestCompleteEventEntry(
    override val id: String = "",
    override val name: String = "",
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    @Help("如果未设置，它将对所有任务触发。")
    val quest: Ref<QuestEntry> = emptyRef()
) : EventEntry

@EntryListener(QuestCompleteEventEntry::class)
fun onQuestComplete(event: AsyncQuestStatusUpdate, query: Query<QuestCompleteEventEntry>) {
    if (event.to != QuestStatus.COMPLETED) return

    query.findWhere {
        !it.quest.isSet || it.quest == event.quest
    } triggerAllFor event.player
}