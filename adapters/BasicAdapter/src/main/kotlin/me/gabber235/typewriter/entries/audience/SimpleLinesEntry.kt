package me.gabber235.typewriter.entries.audience

import me.gabber235.typewriter.adapters.Colors
import me.gabber235.typewriter.adapters.Entry
import me.gabber235.typewriter.adapters.modifiers.Colored
import me.gabber235.typewriter.adapters.modifiers.Help
import me.gabber235.typewriter.adapters.modifiers.MultiLine
import me.gabber235.typewriter.adapters.modifiers.Placeholder
import me.gabber235.typewriter.entry.entries.LinesEntry
import org.bukkit.entity.Player
import java.util.*

@Entry("simple_lines", "静态确定的文本行", Colors.ORANGE_RED, "bi:layout-text-sidebar")
/**
 * The `SimpleSidebarLinesEntry` is a display that shows lines.
 *
 * Separating lines with a newline character will display them on separate lines.
 *
 * ## How could this be used?
 * This could be used to show a list of objectives, or the region a player is in.
 */
class SimpleLinesEntry(
    override val id: String = "",
    override val name: String = "",
    @Help("要在侧边栏上显示的行。使用换行符分隔行。")
    @Colored
    @Placeholder
    @MultiLine
    val lines: String = "",
    override val priorityOverride: Optional<Int> = Optional.empty(),
) : LinesEntry {
    override fun lines(player: Player): String = lines
}