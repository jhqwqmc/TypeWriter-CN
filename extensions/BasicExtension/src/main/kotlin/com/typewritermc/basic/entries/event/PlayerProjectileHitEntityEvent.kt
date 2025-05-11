package com.typewritermc.basic.entries.event

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Query
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.EntryListener
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.interaction.context
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entries.EventEntry
import com.typewritermc.engine.paper.entry.triggerAllFor
import com.typewritermc.engine.paper.entry.triggerEntriesFor
import com.typewritermc.engine.paper.utils.item.Item
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.entity.ProjectileHitEvent
import java.util.*

@Entry(
    "player_projectile_hit_entity_event",
    "当玩家的抛射物击中实体时触发",
    Colors.YELLOW,
    "mdi:target-account"
)
/**
 * The `Player Projectile Hit Entity Event` is triggered when a player's projectile hits an entity.
 *
 * ## How could this be used?
 * This could be used to make a hit sound play when the player hits an entity.
 */
class PlayerProjectileHitEntityEventEntry(
    override val id: String = "",
    override val name: String = "",
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    @Help("玩家被抛射物击中时运行的触发器")
    val targetTriggers: List<Ref<TriggerableEntry>> = emptyList(),
    val projectileType: Optional<EntityType> = Optional.empty(),
    val hitEntityType: Optional<EntityType> = Optional.empty(),
    val itemInHand: Optional<Item> = Optional.empty(),
    val holdingHand: HoldingHand = HoldingHand.BOTH,
) : EventEntry

@EntryListener(PlayerProjectileHitEntityEventEntry::class)
fun onPlayerProjectileHitEntity(event: ProjectileHitEvent, query: Query<PlayerProjectileHitEntityEventEntry>) {
    val shooter = event.entity.shooter
    if (shooter !is Player) return
    val hitEntity = event.hitEntity ?: return

    val entries = query.findWhere { entry ->
        if (entry.projectileType.isPresent && entry.projectileType.get() != event.entityType) return@findWhere false
        if (entry.hitEntityType.isPresent && entry.hitEntityType.get() != hitEntity.type) return@findWhere false
        if (entry.itemInHand.isPresent && hasItemInHand(shooter, entry.holdingHand, entry.itemInHand.get())) return@findWhere false
        true
    }.toList()

    entries.triggerAllFor(shooter, context())
    if (hitEntity !is Player) return
    entries.flatMap { it.targetTriggers }.triggerEntriesFor(hitEntity, context())
}