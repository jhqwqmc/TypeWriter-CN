package me.gabber235.typewriter.entries.activity

import me.gabber235.typewriter.adapters.Colors
import me.gabber235.typewriter.adapters.Entry
import me.gabber235.typewriter.adapters.modifiers.Help
import me.gabber235.typewriter.entry.Ref
import me.gabber235.typewriter.entry.emptyRef
import me.gabber235.typewriter.entry.entity.*
import me.gabber235.typewriter.entry.entries.EntityActivityEntry
import me.gabber235.typewriter.entry.entries.GenericEntityActivityEntry
import java.time.Duration
import java.util.*

@Entry("player_close_by_activity", "一个附近有玩家的活动", Colors.PALATINATE_BLUE, "material-symbols-light:frame-person")
/**
 * The `PlayerCloseByActivityEntry` is an activity that activates child activities when a viewer is close by.
 *
 * The activity will only activate when the viewer is within the defined range.
 *
 * When the maximum idle duration is reached, the activity will deactivate.
 * If the maximum idle duration is set to 0, then it won't use the timer.
 *
 * ## How could this be used?
 * When the player has to follow the NPC and walks away, let the NPC wander around (or stand still) around the point the player walked away. When the player returns, resume its path.
 *
 * When the npc is walking, and a player comes in range Stand still.
 */
class PlayerCloseByActivityEntry(
    override val id: String = "",
    override val name: String = "",
    @Help("玩家必须在该范围内才能激活活动。")
    val range: Double = 10.0,
    @Help("玩家在同一范围内空闲的最大持续时间，超过此时间活动将停用。")
    val maxIdleDuration: Duration = Duration.ofSeconds(30),
    @Help("当有玩家在附近时将要使用的活动。")
    val closeByActivity: Ref<out EntityActivityEntry> = emptyRef(),
    @Help("当没有玩家在附近时将要使用的活动。")
    val idleActivity: Ref<out EntityActivityEntry> = emptyRef(),
) : GenericEntityActivityEntry {
    override fun create(
        context: ActivityContext,
        currentLocation: LocationProperty
    ): EntityActivity<in ActivityContext> {
        return PlayerCloseByActivity(range, maxIdleDuration, closeByActivity, idleActivity, currentLocation)
    }
}

class PlayerCloseByActivity(
    private val range: Double,
    private val maxIdleDuration: Duration,
    private val closeByActivity: Ref<out EntityActivityEntry>,
    private val idleActivity: Ref<out EntityActivityEntry>,
    startLocation: LocationProperty,
) : SingleChildActivity<ActivityContext>(startLocation) {
    private var trackers = mutableMapOf<UUID, PlayerLocationTracker>()

    override fun currentChild(context: ActivityContext): Ref<out EntityActivityEntry> {
        val closeByPlayers = context.viewers
            .filter { it.isValid }
            .filter { (it.location.toProperty().distanceSqrt(currentLocation) ?: Double.MAX_VALUE) < range * range }

        trackers.keys.removeAll { uuid -> closeByPlayers.none { it.uniqueId == uuid } }

        closeByPlayers.forEach { player ->
            trackers.computeIfAbsent(player.uniqueId) { PlayerLocationTracker(player.location.toProperty()) }
                .update(player.location.toProperty())
        }

        val isActive = trackers.any { (_, tracker) -> tracker.isActive(maxIdleDuration) }
        return if (isActive) {
            closeByActivity
        } else {
            idleActivity
        }
    }

    private class PlayerLocationTracker(
        var location: LocationProperty,
        var lastSeen: Long = System.currentTimeMillis()
    ) {
        fun update(location: LocationProperty) {
            if ((this.location.distanceSqrt(location) ?: Double.MAX_VALUE) < 0.1) return
            this.location = location
            lastSeen = System.currentTimeMillis()
        }

        fun isActive(maxIdleDuration: Duration): Boolean {
            if (maxIdleDuration.isZero) return true
            return System.currentTimeMillis() - lastSeen < maxIdleDuration.toMillis()
        }
    }
}