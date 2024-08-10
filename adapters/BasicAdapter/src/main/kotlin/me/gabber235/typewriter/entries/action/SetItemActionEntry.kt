package me.gabber235.typewriter.entries.action

import me.gabber235.typewriter.adapters.Colors
import me.gabber235.typewriter.adapters.Entry
import me.gabber235.typewriter.adapters.modifiers.Help
import me.gabber235.typewriter.entry.Criteria
import me.gabber235.typewriter.entry.Modifier
import me.gabber235.typewriter.entry.Ref
import me.gabber235.typewriter.entry.TriggerableEntry
import me.gabber235.typewriter.entry.entries.ActionEntry
import me.gabber235.typewriter.utils.Item
import me.gabber235.typewriter.utils.ThreadType
import me.gabber235.typewriter.utils.ThreadType.SYNC
import org.bukkit.entity.Player

@Entry("set_item", "在特定槽位中设置物品", Colors.RED, "fluent:tray-item-add-24-filled")
/**
 * The `Set Item Action` is an action that sets an item in a specific slot in the player's inventory.
 *
 * ## How could this be used?
 *
 * This can be used to equip a player with an elytra, or give them a weapon.
 */
class SetItemActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria>,
    override val modifiers: List<Modifier>,
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    @Help("设置的物品。")
    val item: Item = Item.Empty,
    @Help("设置物品的槽位。")
    val slot: Int = 0,
) : ActionEntry {
    override fun execute(player: Player) {
        super.execute(player)

        SYNC.launch {
            player.inventory.setItem(slot, item.build(player))
        }
    }
}