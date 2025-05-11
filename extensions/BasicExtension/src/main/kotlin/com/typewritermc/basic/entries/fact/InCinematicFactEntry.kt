package com.typewritermc.basic.entries.fact

import com.google.gson.annotations.SerializedName
import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.books.pages.PageType
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.entries.emptyRef
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.extension.annotations.Page
import com.typewritermc.engine.paper.entry.entries.GroupEntry
import com.typewritermc.engine.paper.entry.entries.ReadableFactEntry
import com.typewritermc.engine.paper.entry.temporal.isPlayingTemporal
import com.typewritermc.engine.paper.facts.FactData
import org.bukkit.entity.Player

@Entry("in_cinematic_fact", "判断玩家是否处于过场动画中", Colors.PURPLE, "eos-icons:storage-class")
/**
 * The 'In Cinematic Fact' is a fact that returns 1 if the player has an active cinematic, and 0 if not.
 *
 * If no cinematic is referenced, it will filter based on if any cinematic is active.
 *
 * <fields.ReadonlyFactInfo />
 *
 * ## How could this be used?
 * With this fact, it is possible to make an entry only take action if the player does not have an active cinematic.
 */
class InCinematicFactEntry(
    override val id: String = "",
    override val name: String = "",
    override val comment: String = "",
    override val group: Ref<GroupEntry> = emptyRef(),
    @Help("未设置时，将根据是否有任何过场动画处于活动状态进行判断")
    @Page(PageType.CINEMATIC)
    @SerializedName("cinematic")
    val pageId: String = "",
) : ReadableFactEntry {
    override fun readSinglePlayer(player: Player): FactData {
        val inCinematic = if (pageId.isNotBlank())
            player.isPlayingTemporal(pageId)
        else
            player.isPlayingTemporal()

        return FactData(inCinematic.toInt())
    }
}
