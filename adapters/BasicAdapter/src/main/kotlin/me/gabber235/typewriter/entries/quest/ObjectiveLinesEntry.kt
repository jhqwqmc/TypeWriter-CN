package me.gabber235.typewriter.entries.quest

import me.gabber235.typewriter.adapters.Colors
import me.gabber235.typewriter.adapters.Entry
import me.gabber235.typewriter.adapters.modifiers.Colored
import me.gabber235.typewriter.adapters.modifiers.Help
import me.gabber235.typewriter.adapters.modifiers.MultiLine
import me.gabber235.typewriter.adapters.modifiers.Placeholder
import me.gabber235.typewriter.entry.entries.LinesEntry
import me.gabber235.typewriter.entry.entries.trackedShowingObjectives
import me.gabber235.typewriter.extensions.placeholderapi.parsePlaceholders
import me.gabber235.typewriter.utils.asMini
import me.gabber235.typewriter.utils.asMiniWithResolvers
import org.bukkit.entity.Player
import java.util.*

@Entry(
    "objective_lines",
    "显示所有当前目标",
    Colors.ORANGE_RED,
    "fluent:clipboard-task-list-ltr-24-filled"
)
/**
 * The `ObjectiveLinesEntry` is a display that shows all the current objectives.
 *
 * ## How could this be used?
 * This could be used to show a list of tracked objectives
 */
class ObjectiveLinesEntry(
    override val id: String = "",
    override val name: String = "",
    @Help("行的格式。使用&lt;objective&gt;替换目标名称。")
    @Colored
    @Placeholder
    @MultiLine
    val format: String = "<objective>",
    override val priorityOverride: Optional<Int> = Optional.empty(),
) : LinesEntry {
    override fun lines(player: Player): String {
        return player.trackedShowingObjectives().joinToString("\n") {
            format.parsePlaceholders(player).asMiniWithResolvers(
                net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.parsed(
                    "objective",
                    it.display(player)
                )
            ).asMini()
        }
    }
}