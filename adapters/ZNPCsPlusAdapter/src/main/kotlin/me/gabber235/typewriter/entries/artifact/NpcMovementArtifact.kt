package me.gabber235.typewriter.entries.artifact

import me.gabber235.typewriter.adapters.Colors
import me.gabber235.typewriter.adapters.Entry
import me.gabber235.typewriter.adapters.Tags
import me.gabber235.typewriter.entry.entries.ArtifactEntry
import me.gabber235.typewriter.extensions.packetevents.ArmSwing
import org.bukkit.Location
import org.bukkit.inventory.ItemStack

@Deprecated("使用 EntityAdapter 代替")
@Entry("znpc_movement_artifact", "NPC 的移动数据", Colors.PINK, "fa6-solid:person-walking")
@Tags("npc_movement_artifact")
/**
 * The `Npc Movement Artifact` is an artifact that stores the movement data of an NPC.
 * There is no reason to create this on its own.
 * It should always be connected to another entry
 */
class ZNpcMovementArtifact(
    override val id: String = "",
    override val name: String = "",
    override val artifactId: String = "",
) : ArtifactEntry

data class NpcFrame(
    val location: Location?,
    val sneaking: Boolean?,
    val swing: ArmSwing?,
    val mainHand: ItemStack?,
    val offHand: ItemStack?,
    val helmet: ItemStack?,
    val chestplate: ItemStack?,
    val leggings: ItemStack?,
    val boots: ItemStack?,
)