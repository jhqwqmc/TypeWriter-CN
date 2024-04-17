package me.ahdg6.typewriter.mythicmobs.entries.event

import io.lumine.mythic.bukkit.events.MythicMobInteractEvent
import me.gabber235.typewriter.adapters.Colors
import me.gabber235.typewriter.adapters.Entry
import me.gabber235.typewriter.adapters.modifiers.Help
import me.gabber235.typewriter.entry.EntryListener
import me.gabber235.typewriter.entry.Query
import me.gabber235.typewriter.entry.entries.EventEntry
import me.gabber235.typewriter.entry.startDialogueWithOrNextDialogue
import me.gabber235.typewriter.utils.Icons

@Entry("mythicmobs_interact_event", "MythicMob 交互事件", Colors.YELLOW, Icons.DRAGON)
/**
 * The `MythicMob Interact Event` is fired when a player interacts with a MythicMob.
 *
 * ## How could this be used?
 *
 * This can be used to create a variety of interactions that can occur between a MythicMob and a player. For example, you could create a MythicMob that gives the player an item when they interact with it.
 */
class MythicMobInteractEventEntry(
    override val id: String,
    override val name: String,
    override val triggers: List<String>,
    @Help("要监听的特定 MythicMob 类型")
    val mobName: String,
) : EventEntry

@EntryListener(MythicMobInteractEventEntry::class)
fun onMythicMobInteractEvent(event: MythicMobInteractEvent, query: Query<MythicMobInteractEventEntry>) {
    query.findWhere {
        it.mobName == event.activeMobType.internalName
    } startDialogueWithOrNextDialogue event.player
}