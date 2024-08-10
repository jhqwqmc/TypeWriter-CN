package me.gabber235.typewriter.entries.action

import me.gabber235.typewriter.adapters.Colors
import me.gabber235.typewriter.adapters.Entry
import me.gabber235.typewriter.adapters.modifiers.Colored
import me.gabber235.typewriter.adapters.modifiers.Help
import me.gabber235.typewriter.adapters.modifiers.Placeholder
import me.gabber235.typewriter.entry.Criteria
import me.gabber235.typewriter.entry.Modifier
import me.gabber235.typewriter.entry.Ref
import me.gabber235.typewriter.entry.TriggerableEntry
import me.gabber235.typewriter.entry.entries.ActionEntry
import me.gabber235.typewriter.extensions.placeholderapi.parsePlaceholders
import me.gabber235.typewriter.utils.asMini
import net.kyori.adventure.title.Title
import org.bukkit.entity.Player
import java.time.Duration
import java.util.*

@Entry("show_title", "向玩家显示标题", Colors.RED, "fluent:align-center-vertical-32-filled")
/**
 * The `Show Title Action` is an action that shows a title to a player. You can specify the subtitle, and durations if needed.
 *
 * ## How could this be used?
 *
 * This action can be useful in a variety of situations. You can use it to create text effects in response to specific events, such as completing questions or anything else. The possibilities are endless!
 */
class ShowTitleActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    @Placeholder
    @Colored
    @Help("要显示的标题文本。")
    val title: String = "",
    @Placeholder
    @Colored
    @Help("要显示的副标题文字。")
    val subtitle: String = "",
    @Help("标题的可选持续时间设置。")
    // Duration of the title: Fade in, how long it stays, fade out.
    val durations: Optional<TitleDurations> = Optional.empty(),
) : ActionEntry {
    override fun execute(player: Player) {
        super.execute(player)

        val adventureTitle: Title = durations.map { durations ->
            Title.title(
                title.parsePlaceholders(player).asMini(),
                subtitle.parsePlaceholders(player).asMini(),

                Title.Times.times(
                    Duration.ofMillis(durations.fadeIn.toMillis()),
                    Duration.ofMillis(durations.stay.toMillis()),
                    Duration.ofMillis(durations.fadeOut.toMillis())
                )
            )
        }.orElseGet {
            Title.title(
                title.parsePlaceholders(player).asMini(),
                subtitle.parsePlaceholders(player).asMini(),
            )
        }

        player.showTitle(adventureTitle)
    }
}

data class TitleDurations(
    @Help("淡入效果的时间。")
    val fadeIn: Duration,
    @Help("保持显示的时间。")
    val stay: Duration,
    @Help("淡出效果的时间。")
    val fadeOut: Duration
)