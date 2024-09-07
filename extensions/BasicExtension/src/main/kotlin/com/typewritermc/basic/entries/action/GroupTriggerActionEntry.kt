package com.typewritermc.basic.entries.action

import com.google.gson.annotations.SerializedName
import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.entries.emptyRef
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entries.CustomTriggeringActionEntry
import com.typewritermc.engine.paper.entry.entries.GroupEntry
import com.typewritermc.engine.paper.entry.entries.GroupId
import org.bukkit.entity.Player
import java.util.*

@Entry(
    "group_trigger_action",
    "触发与玩家同一组中每个人的下一个条目",
    Colors.RED,
    "fluent:globe-arrow-forward-16-filled"
)
/**
 * The `Group Trigger Action` is an action that triggers the next entries for everyone in the same group as the player.
 *
 * :::caution
 * The modifiers will only be applied to the player that triggered the action.
 * If you want to modify the other players, you will need to do it in the next entries.
 * :::
 *
 * ## How could this be used?
 * This could be used to trigger the next entries for everyone in the same group as the player,
 * when a player joins a faction, all the other players in the same faction could be notified.
 */
class GroupTriggerActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    @SerializedName("triggers")
    override val customTriggers: List<Ref<TriggerableEntry>> = emptyList(),
    val group: Ref<GroupEntry> = emptyRef(),
    @Help("需要触发下一个条目的组。如果没有设置，动作会触发该玩家所在的组。")
    val forceGroup: Optional<String> = Optional.empty(),
) : CustomTriggeringActionEntry {
    override fun execute(player: Player) {
        super.execute(player)

        val groupEntry = group.get() ?: return

        val group = forceGroup
            .map { groupEntry.group(GroupId(it)) }
            .orElseGet { groupEntry.group(player) } ?: return

        group.players.forEach {
            it.triggerCustomTriggers()
        }
    }
}