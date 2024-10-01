package com.typewritermc.basic.entries.audience

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.entries.Ref
import com.typewritermc.engine.paper.entry.entries.*
import com.typewritermc.core.entries.ref
import com.typewritermc.engine.paper.utils.item.Item
import org.bukkit.entity.Player

@Entry(
    "item_in_slot_audience",
    "根据玩家在特定槽位中是否拥有特定的物品来过滤观众",
    Colors.MEDIUM_SEA_GREEN,
    "mdi:hand"
)
/**
 * The `Item In Slot Audience` entry filters an audience based on if they have a specific item in a specific slot.
 *
 * ## How could this be used?
 * It can be used to have magnet boots which allow players to move in certain areas.
 */
class ItemInSlotAudienceEntry(
    override val id: String = "",
    override val name: String = "",
    override val children: List<Ref<AudienceEntry>> = emptyList(),
    @Help("要检查的物品。")
    val item: Item = Item.Empty,
    @Help("要检查的槽位。")
    val slot: Int = 0,
    override val inverted: Boolean = false,
) : AudienceFilterEntry, Invertible {
    override fun display(): AudienceFilter = ItemInSlotAudienceFilter(ref(), item, slot)
}

class ItemInSlotAudienceFilter(
    ref: Ref<out AudienceFilterEntry>,
    private val item: Item,
    private val slot: Int,
) : AudienceFilter(ref), TickableDisplay {
    override fun filter(player: Player): Boolean {
        val itemInSlot = player.inventory.getItem(slot) ?: return false
        return item.isSameAs(player, itemInSlot)
    }

    override fun tick() {
        consideredPlayers.forEach { it.refresh() }
    }
}