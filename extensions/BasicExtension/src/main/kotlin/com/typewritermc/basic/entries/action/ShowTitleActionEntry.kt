package com.typewritermc.basic.entries.action

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Colored
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.extension.annotations.Placeholder
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier
import com.typewritermc.core.entries.Ref
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entries.ActionEntry
import com.typewritermc.engine.paper.extensions.placeholderapi.parsePlaceholders
import com.typewritermc.engine.paper.utils.asMini
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
    val title: String = "",
    @Placeholder
    @Colored
    val subtitle: String = "",
    @Help("可选的标题持续时间设置。标题持续时间：淡入、停留时间、淡出。")
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