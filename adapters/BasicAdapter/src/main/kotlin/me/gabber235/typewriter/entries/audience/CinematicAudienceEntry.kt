package me.gabber235.typewriter.entries.audience

import com.google.gson.annotations.SerializedName
import me.gabber235.typewriter.adapters.Colors
import me.gabber235.typewriter.adapters.Entry
import me.gabber235.typewriter.adapters.modifiers.Help
import me.gabber235.typewriter.adapters.modifiers.Page
import me.gabber235.typewriter.entry.PageType
import me.gabber235.typewriter.entry.Ref
import me.gabber235.typewriter.entry.cinematic.isPlayingCinematic
import me.gabber235.typewriter.entry.entries.AudienceEntry
import me.gabber235.typewriter.entry.entries.AudienceFilter
import me.gabber235.typewriter.entry.entries.AudienceFilterEntry
import me.gabber235.typewriter.entry.entries.Invertible
import me.gabber235.typewriter.entry.ref
import me.gabber235.typewriter.events.AsyncCinematicEndEvent
import me.gabber235.typewriter.events.AsyncCinematicStartEvent
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler

@Entry(
    "cinematic_audience",
    "根据观众是否在过场动画中来过滤他们",
    Colors.MEDIUM_SEA_GREEN,
    "mdi:movie"
)
/**
 * The `Cinematic Audience` entry filters an audience based on if they are in a cinematic.
 *
 * If no cinematic is referenced, it will filter based on if any cinematic is active.
 *
 * ## How could this be used?
 * This could be used to hide the sidebar or boss bar when a cinematic is playing.
 */
class CinematicAudienceEntry(
    override val id: String = "",
    override val name: String = "",
    override val children: List<Ref<AudienceEntry>> = emptyList(),
    @Help("当未设置时，它将根据是否有任何过场动画处于活动状态进行过滤。")
    @Page(PageType.CINEMATIC)
    @SerializedName("cinematic")
    val pageId: String = "",
    override val inverted: Boolean = false
) : AudienceFilterEntry, Invertible {
    override fun display(): AudienceFilter = CinematicAudienceFilter(
        ref(),
        pageId,
    )
}

class CinematicAudienceFilter(
    ref: Ref<out AudienceFilterEntry>,
    private val pageId: String,
) : AudienceFilter(ref) {
    override fun filter(player: Player): Boolean {
        val inCinematic = if (pageId.isNotBlank()) player.isPlayingCinematic(pageId) else player.isPlayingCinematic()
        return inCinematic
    }

    @EventHandler
    fun onCinematicStart(event: AsyncCinematicStartEvent) {
        if (pageId.isNotBlank() && event.pageId != pageId) return
        event.player.updateFilter(true)
    }

    @EventHandler
    fun onCinematicEnd(event: AsyncCinematicEndEvent) {
        if (pageId.isNotBlank() && event.pageId != pageId) return
        event.player.updateFilter(false)
    }
}

