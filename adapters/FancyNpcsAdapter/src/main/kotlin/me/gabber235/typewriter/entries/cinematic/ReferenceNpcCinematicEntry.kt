package me.gabber235.typewriter.entries.cinematic

import me.gabber235.typewriter.adapters.Colors
import me.gabber235.typewriter.adapters.Entry
import me.gabber235.typewriter.adapters.modifiers.Help
import me.gabber235.typewriter.entries.entity.ReferenceNpcEntry
import me.gabber235.typewriter.entry.Criteria
import me.gabber235.typewriter.entry.Ref
import me.gabber235.typewriter.entry.emptyRef
import me.gabber235.typewriter.entry.entries.CinematicAction
import org.bukkit.entity.Player

@Deprecated("使用 EntityAdapter 代替")
@Entry(
    "fancy_reference_npc_cinematic",
    "对现有 npc 的引用，专门用于过场动画",
    Colors.PINK,
    "fa-solid:user-tie"
)
/**
 * The `Reference NPC Cinematic` entry that plays a recorded animation back on a reference NPC.
 * When active, the original NPC will be hidden and a clone will be spawned in its place.
 *
 * ## How could this be used?
 *
 * This could be used to create a cinematic where the player is talking to an NPC.
 * Like going in to a store and talking to the shopkeeper.
 */
class ReferenceNpcCinematicEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val recordedSegments: List<NpcRecordedSegment> = emptyList(),
    @Help("选择一个NPC进行克隆")
    val referenceNpc: Ref<ReferenceNpcEntry> = emptyRef(),
) : NpcCinematicEntry {
    override fun create(player: Player): CinematicAction {
        if (!referenceNpc.isSet) throw Exception("未设置 $id ($name) 的引用NPC")
        val referenceNpc =
            this.referenceNpc.get()
                ?: throw Exception("未找到引用NPC‘$referenceNpc’")

        return NpcCinematicAction(
            player,
            this,
            ReferenceNpcData(referenceNpc.npcId),
        )
    }
}
