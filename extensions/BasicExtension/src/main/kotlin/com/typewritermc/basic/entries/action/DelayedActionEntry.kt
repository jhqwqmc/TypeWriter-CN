package com.typewritermc.basic.entries.action

import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.delay
import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier
import com.typewritermc.core.entries.Ref
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entries.CustomTriggeringActionEntry
import com.typewritermc.engine.paper.utils.ThreadType.DISPATCHERS_ASYNC
import org.bukkit.entity.Player
import java.time.Duration

@Entry("delayed_action", "将动作延迟一定时间", Colors.RED, "fa-solid:hourglass")
/**
 * The `Delayed Action Entry` is an entry that fires its triggers after a specified duration. This entry provides you with the ability to create time-based actions and events.
 *
 * ## How could this be used?
 *
 * This entry can be useful in a variety of situations where you need to delay an action or event.
 * You can use it to create countdown timers, to perform actions after a certain amount of time has elapsed, or to schedule events in the future.
 */
class DelayedActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    @SerializedName("triggers")
    override val customTriggers: List<Ref<TriggerableEntry>> = emptyList(),
    @Help("下一次触发器触发之前的持续时间。")
    private val duration: Duration = Duration.ZERO,
) : CustomTriggeringActionEntry {

    override fun execute(player: Player) {
        DISPATCHERS_ASYNC.launch {
            delay(duration.toMillis())
            super.execute(player)
            player.triggerCustomTriggers()
        }
    }
}