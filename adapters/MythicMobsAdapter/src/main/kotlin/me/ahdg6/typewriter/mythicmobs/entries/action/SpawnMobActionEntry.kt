package me.ahdg6.typewriter.mythicmobs.entries.action

import io.lumine.mythic.api.mobs.entities.SpawnReason
import io.lumine.mythic.bukkit.BukkitAdapter
import io.lumine.mythic.bukkit.MythicBukkit
import me.gabber235.typewriter.adapters.Colors
import me.gabber235.typewriter.adapters.Entry
import me.gabber235.typewriter.adapters.modifiers.Help
import me.gabber235.typewriter.adapters.modifiers.Placeholder
import me.gabber235.typewriter.adapters.modifiers.WithRotation
import me.gabber235.typewriter.entry.Criteria
import me.gabber235.typewriter.entry.Modifier
import me.gabber235.typewriter.entry.Ref
import me.gabber235.typewriter.entry.TriggerableEntry
import me.gabber235.typewriter.entry.entries.ActionEntry
import me.gabber235.typewriter.extensions.placeholderapi.parsePlaceholders
import me.gabber235.typewriter.plugin
import me.gabber235.typewriter.utils.ThreadType.SYNC
import org.bukkit.Location
import org.bukkit.entity.Player


@Entry("spawn_mythicmobs_mob", "从 MythicMobs 中生成一个生物", Colors.ORANGE, "fa6-solid:dragon")
/**
 * The `Spawn Mob Action` action spawn MythicMobs mobs to the world.
 *
 * ## How could this be used?
 *
 * This action could be used in a plethora of scenarios. From simple quests requiring you to kill some spawned mobs, to complex storylines that simulate entire battles, this action knows no bounds!
 */
class SpawnMobActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    @Help("生物的名字")
    @Placeholder
    private val mobName: String = "",
    @Help("生物的等级")
    private val level: Double = 1.0,
    @Help("该生物是否只能被玩家看到")
    private val onlyVisibleForPlayer: Boolean = false,
    @Help("生物生成的位置")
    @WithRotation
    private var spawnLocation: Location,
) : ActionEntry {
    override fun execute(player: Player) {
        super.execute(player)

        val mob = MythicBukkit.inst().mobManager.getMythicMob(mobName.parsePlaceholders(player))
        if (!mob.isPresent) return

        SYNC.launch {
            mob.get().spawn(BukkitAdapter.adapt(spawnLocation), level, SpawnReason.OTHER) {
                if (onlyVisibleForPlayer) {
                    it.isVisibleByDefault = false
                    player.showEntity(plugin, it)
                }
            }
        }
    }
}