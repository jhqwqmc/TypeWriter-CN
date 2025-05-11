package com.typewritermc.basic.entries.action

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.Colored
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.extension.annotations.Placeholder
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entries.ActionEntry
import com.typewritermc.engine.paper.entry.entries.ActionTrigger
import com.typewritermc.engine.paper.entry.entries.ConstVar
import com.typewritermc.engine.paper.entry.entries.Var
import com.typewritermc.engine.paper.extensions.placeholderapi.parsePlaceholders
import com.typewritermc.engine.paper.utils.asMini
import net.kyori.adventure.title.Title
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
    val title: Var<String> = ConstVar(""),
    @Placeholder
    @Colored
    val subtitle: Var<String> = ConstVar(""),
    @Help("可选的标题持续时间设置：淡入时长、保持时长、淡出时长。")
    val durations: Optional<TitleDurations> = Optional.empty(),
) : ActionEntry {
    override fun ActionTrigger.execute() {
        val adventureTitle: Title = durations.map { durations ->
            Title.title(
                title.get(player, context).parsePlaceholders(player).asMini(),
                subtitle.get(player, context).parsePlaceholders(player).asMini(),

                Title.Times.times(
                    Duration.ofMillis(durations.fadeIn.toMillis()),
                    Duration.ofMillis(durations.stay.toMillis()),
                    Duration.ofMillis(durations.fadeOut.toMillis())
                )
            )
        }.orElseGet {
            Title.title(
                title.get(player, context).parsePlaceholders(player).asMini(),
                subtitle.get(player, context).parsePlaceholders(player).asMini()
            )
        }

        player.showTitle(adventureTitle)
    }
}

data class TitleDurations(
    @Help("淡入效果的持续时间。")
    val fadeIn: Duration = Duration.ofSeconds(1),
    @Help("标题保持显示的持续时间。")
    val stay: Duration = Duration.ofSeconds(1),
    @Help("淡出效果的持续时间。")
    val fadeOut: Duration = Duration.ofSeconds(1),
)