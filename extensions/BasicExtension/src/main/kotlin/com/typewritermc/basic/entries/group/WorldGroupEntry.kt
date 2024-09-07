package com.typewritermc.basic.entries.group

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.engine.paper.entry.entries.GroupEntry
import com.typewritermc.engine.paper.entry.entries.GroupId
import org.bukkit.entity.Player

@Entry("world_group", "整个世界的组", Colors.MYRTLE_GREEN, "bx:world")
/**
 * The `World Group` is a group that includes all the players in a world.
 *
 * ## How could this be used?
 * This could be used to have facts that are specific to a world, like the state of a world event or the state of a world boss.
 */
class WorldGroupEntry(
    override val id: String = "",
    override val name: String = "",
) : GroupEntry {
    override fun groupId(player: Player): GroupId = GroupId(player.world.uid)
}