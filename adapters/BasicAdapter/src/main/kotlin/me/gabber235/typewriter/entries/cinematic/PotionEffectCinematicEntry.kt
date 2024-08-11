package me.gabber235.typewriter.entries.cinematic

import me.gabber235.typewriter.adapters.Colors
import me.gabber235.typewriter.adapters.Entry
import me.gabber235.typewriter.adapters.modifiers.Help
import me.gabber235.typewriter.adapters.modifiers.Segments
import me.gabber235.typewriter.entry.Criteria
import me.gabber235.typewriter.entry.cinematic.SimpleCinematicAction
import me.gabber235.typewriter.entry.entries.CinematicAction
import me.gabber235.typewriter.entry.entries.CinematicEntry
import me.gabber235.typewriter.entry.entries.PrimaryCinematicEntry
import me.gabber235.typewriter.entry.entries.Segment
import me.gabber235.typewriter.utils.EffectStateProvider
import me.gabber235.typewriter.utils.PlayerState
import me.gabber235.typewriter.utils.ThreadType.SYNC
import me.gabber235.typewriter.utils.restore
import me.gabber235.typewriter.utils.state
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

@Entry(
    "potion_effect_cinematic",
    "在过场动画期间对玩家应用不同的药水效果",
    Colors.CYAN,
    "fa6-solid:flask-vial"
)
/**
 * The `PotionEffectCinematicEntry` is used to apply different potion effects to the player during a cinematic.
 *
 * ## How could this be used?
 * This can be used to dynamically apply effects like blindness, slowness, etc., at different times
 * during a cinematic, enhancing the storytelling or gameplay experience.
 */
class PotionEffectCinematicEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    @Segments(icon = "heroicons-solid:status-offline")
    val segments: List<PotionEffectSegment> = emptyList()
) : PrimaryCinematicEntry {
    override fun createSimulating(player: Player): CinematicAction? = null
    override fun create(player: Player): CinematicAction {
        return PotionEffectCinematicAction(
            player,
            this
        )
    }
}

data class PotionEffectSegment(
    override val startFrame: Int = 0,
    override val endFrame: Int = 0,
    @Help("要应用的药水效果类型")
    val potionEffectType: PotionEffectType = PotionEffectType.BLINDNESS,
    @Help("药水效果的强度")
    val strength: Int = 1,
    @Help("药水效果是否为环境效果")
    val ambient: Boolean = false,
    @Help("药水效果是否有粒子效果")
    val particles: Boolean = false,
    @Help("药水效果是否显示图标")
    val icon: Boolean = false,
) : Segment

class PotionEffectCinematicAction(
    private val player: Player,
    entry: PotionEffectCinematicEntry
) : SimpleCinematicAction<PotionEffectSegment>() {

    private var state: PlayerState? = null

    override val segments: List<PotionEffectSegment> = entry.segments

    override suspend fun startSegment(segment: PotionEffectSegment) {
        super.startSegment(segment)
        state = player.state(EffectStateProvider(segment.potionEffectType))

        SYNC.switchContext {
            player.addPotionEffect(
                PotionEffect(
                    segment.potionEffectType,
                    10000000,
                    segment.strength,
                    segment.ambient,
                    segment.particles,
                    segment.icon
                )
            )
        }
    }

    override suspend fun stopSegment(segment: PotionEffectSegment) {
        super.stopSegment(segment)
        restoreState()
    }

    private suspend fun restoreState() {
        val state = state ?: return
        this.state = null
        SYNC.switchContext {
            player.restore(state)
        }
    }

    override suspend fun teardown() {
        super.teardown()

        if (state != null) {
            restoreState()
        }
    }
}
