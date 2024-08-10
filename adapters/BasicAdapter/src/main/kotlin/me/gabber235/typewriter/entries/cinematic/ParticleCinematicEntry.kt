package me.gabber235.typewriter.entries.cinematic

import me.gabber235.typewriter.adapters.Colors
import me.gabber235.typewriter.adapters.Entry
import me.gabber235.typewriter.adapters.modifiers.Help
import me.gabber235.typewriter.adapters.modifiers.Negative
import me.gabber235.typewriter.adapters.modifiers.Segments
import me.gabber235.typewriter.entry.Criteria
import me.gabber235.typewriter.entry.entries.*
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.entity.Player

@Entry("particle_cinematic", "为过场动画生成粒子", Colors.CYAN, "fa6-solid:fire-flame-simple")
/**
 * The `Particle Cinematic` entry is used to spawn particles for a cinematic.
 *
 * ## How could this be used?
 *
 * This can be used to add dramatic effects to a cinematic.
 * Like, blowing up a building and spawning a bunch of particles.
 * Or, adding focus to a certain area by spawning particles around it.
 */
class ParticleCinematicEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    @Help("生成粒子的位置。")
    val location: Location = Location(null, 0.0, 0.0, 0.0),
    @Help("要生成的粒子。")
    val particle: Particle = Particle.FLAME,
    @Help("生成的粒子数量。")
    val count: Int = 1,
    @Negative
    @Help("距 X 轴位置的偏移量。")
    val offsetX: Double = 0.0,
    @Negative
    @Help("距 Y 轴位置的偏移量。")
    val offsetY: Double = 0.0,
    @Negative
    @Help("距 Z 轴位置的偏移量。")
    val offsetZ: Double = 0.0,
    @Help("粒子的速度。")
    // The speed of the particles. For some particles, this is the "extra" data value to control particle behavior.
    val speed: Double = 0.0,
    @Segments(icon = "fa6-solid:fire-flame-simple")
    val segments: List<ParticleSegment> = emptyList(),
) : CinematicEntry {
    override fun create(player: Player): CinematicAction {
        return ParticleCinematicAction(
            player,
            this,
        )
    }
}


data class ParticleSegment(
    override val startFrame: Int = 0,
    override val endFrame: Int = 0,
) : Segment

class ParticleCinematicAction(
    private val player: Player,
    private val entry: ParticleCinematicEntry,
) : CinematicAction {
    override suspend fun tick(frame: Int) {
        super.tick(frame)
        (entry.segments activeSegmentAt frame) ?: return

        player.spawnParticle(
            entry.particle,
            entry.location,
            entry.count,
            entry.offsetX,
            entry.offsetY,
            entry.offsetZ,
            entry.speed
        )

    }

    override fun canFinish(frame: Int): Boolean = entry.segments canFinishAt frame
}
