package com.typewritermc.basic.entries.event

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Query
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.*
import com.typewritermc.core.interaction.EntryContextKey
import com.typewritermc.core.utils.point.Position
import com.typewritermc.core.utils.point.toBlockPosition
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entries.CancelableEventEntry
import com.typewritermc.engine.paper.entry.entries.ConstVar
import com.typewritermc.engine.paper.entry.entries.EventEntry
import com.typewritermc.engine.paper.entry.entries.Var
import com.typewritermc.engine.paper.entry.entries.shouldCancel
import com.typewritermc.engine.paper.entry.startDialogueWithOrNextDialogue
import com.typewritermc.engine.paper.utils.item.Item
import com.typewritermc.engine.paper.utils.toPosition
import org.bukkit.Material
import org.bukkit.event.block.BlockBreakEvent
import java.util.*
import kotlin.reflect.KClass

@Entry("on_block_break", "当玩家破坏方块时", Colors.YELLOW, "mingcute:pickax-fill")
@ContextKeys(BlockBreakContextKeys::class)
/**
 *The `Block Break Event` is triggered when a player breaks a block.
 *
 * ## How could this be used?
 *
 * This could allow you to make custom ores with custom drops, give the player a reward after breaking a certain amount of blocks.
 */
class BlockBreakEventEntry(
    override val id: String = "",
    override val name: String = "",
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    @MaterialProperties(MaterialProperty.BLOCK)
    val block: Optional<Material> = Optional.empty(),
    val location: Optional<Var<Position>> = Optional.empty(),
    @Help("破坏方块时玩家必须持有的物品")
    val itemInHand: Var<Item> = ConstVar(Item.Empty),
    @Help("玩家必须持有该物品的手")
    val hand: HoldingHand = HoldingHand.BOTH,
    override val cancel: Var<Boolean> = ConstVar(false),
) : CancelableEventEntry

enum class BlockBreakContextKeys(override val klass: KClass<*>) : EntryContextKey {
    @KeyType(Material::class)
    TYPE(Material::class),

    @KeyType(Position::class)
    BLOCK_POSITION(Position::class),

    @KeyType(Position::class)
    CENTER_POSITION(Position::class)
}

@EntryListener(BlockBreakEventEntry::class, ignoreCancelled = true)
fun onBlockBreak(event: BlockBreakEvent, query: Query<BlockBreakEventEntry>) {
    val player = event.player
    val position = event.block.location.toPosition()
    val entries = query.findWhere { entry ->
        // Check if the player clicked on the correct location
        if (!entry.location.map { it.get(player) == position }.orElse(true)) return@findWhere false

        // Check if the player is holding the correct item
        if (!hasItemInHand(player, entry.hand, entry.itemInHand.get(player))) return@findWhere false

        // Check if block type is correct
        entry.block.map { it == event.block.type }.orElse(true)
    }.toList()
    entries.startDialogueWithOrNextDialogue(player) {
        BlockBreakContextKeys.TYPE += event.block.type
        BlockBreakContextKeys.BLOCK_POSITION += position.toBlockPosition()
        BlockBreakContextKeys.CENTER_POSITION += position.mid()
    }
    if (entries.shouldCancel(player)) event.isCancelled = true
}