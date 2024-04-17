package me.gabber235.typewriter.entries.action

import com.google.gson.annotations.SerializedName
import me.gabber235.typewriter.adapters.Colors
import me.gabber235.typewriter.adapters.Entry
import me.gabber235.typewriter.adapters.modifiers.Help
import me.gabber235.typewriter.adapters.modifiers.Page
import me.gabber235.typewriter.entry.*
import me.gabber235.typewriter.entry.entries.CinematicStartTrigger
import me.gabber235.typewriter.entry.entries.CustomTriggeringActionEntry
import me.gabber235.typewriter.utils.Icons
import org.bukkit.entity.Player


@Entry("cinematic", "开始新的过场动画", Colors.RED, Icons.CAMERA_RETRO)
/**
 * The `Cinematic` action is used to start a new cinematic.
 *
 * ## How could this be used?
 *
 * This action can be useful in situations where you want to start a cinematic.
 * See the [Cinematic](/docs/first-cinematic) tutorial for more information.
 */
class CinematicEntry(
    override val id: String = "",
    override val name: String = "",
    @SerializedName("triggers")
    override val customTriggers: List<String> = emptyList(),
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    @SerializedName("page")
    @Page(PageType.CINEMATIC)
    @Help("过场动画页面开始。")
    val pageId: String = "",
    @Help("如果玩家已经在过场动画中，是否应该更换过场动画？")
    val override: Boolean = false
) : CustomTriggeringActionEntry {
    override fun execute(player: Player) {
        super.execute(player)

        CinematicStartTrigger(pageId, customTriggers, override) triggerFor player
    }
}