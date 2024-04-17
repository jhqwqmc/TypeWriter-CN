package me.gabber235.typewriter.entries.action

import me.gabber235.typewriter.adapters.Colors
import me.gabber235.typewriter.adapters.Entry
import me.gabber235.typewriter.adapters.modifiers.Help
import me.gabber235.typewriter.entry.Criteria
import me.gabber235.typewriter.entry.Modifier
import me.gabber235.typewriter.entry.entries.ActionEntry
import me.gabber235.typewriter.utils.Icons
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.entity.Player
import java.util.*

@Entry("spawn_particles", "在指定位置生成粒子", Colors.RED, Icons.FIRE_FLAME_SIMPLE)
/**
 * The `Spawn Particle Action` is an action that spawns a specific particle at a given location. This action provides you with the ability to spawn particles with a specified type, count, and location.
 *
 * ## How could this be used?
 *
 * This action can be useful in a variety of situations. You can use it to create visual effects in response to specific events, such as explosions or magical spells. The possibilities are endless!
 */
class SpawnParticleActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<String> = emptyList(),
    @Help("生成粒子的位置。 （默认为玩家所在位置）")
    val location: Optional<Location> = Optional.empty(),
    @Help("要生成的粒子。")
    val particle: Particle = Particle.SMOKE_NORMAL,
    @Help("生成的粒子数量。")
    val count: Int = 1,
    @Help("距 X 轴位置的偏移量。")
    val offsetX: Double = 0.0,
    @Help("距 Y 轴位置的偏移量。")
    val offsetY: Double = 0.0,
    @Help("距 Z 轴位置的偏移量。")
    val offsetZ: Double = 0.0,
) : ActionEntry {
    override fun execute(player: Player) {
        super.execute(player)

        if (location.isPresent) {
            location.get().world?.spawnParticle(particle, location.get(), count, offsetX, offsetY, offsetZ)
        } else {
            player.world.spawnParticle(particle, player.location, count, offsetX, offsetY, offsetZ)
        }

    }
}