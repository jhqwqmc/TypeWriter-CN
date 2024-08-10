package me.gabber235.typewriter.entries.entity

import me.gabber235.typewriter.adapters.Colors
import me.gabber235.typewriter.adapters.Entry
import me.gabber235.typewriter.adapters.Tags
import me.gabber235.typewriter.adapters.modifiers.Help
import me.gabber235.typewriter.utils.Sound
import net.kyori.adventure.sound.Sound as AdventureSound


@Deprecated("Use the EntityAdapter instead")
@Tags("znpc_reference_npc")
@Entry("znpc_reference_npc", "当npc不受TypeWriter管理时", Colors.ORANGE, "fa-solid:user-tie")
/**
 * An identifier that references an NPC in the ZNPCsPlus plugin. But does not manage the NPC.
 *
 * ## How could this be used?
 *
 * This can be used to reference an NPC which is already in the world. This could be used to create a quest that requires the player to talk to an NPC.
 */
class ReferenceNpcEntry(
    override val id: String = "",
    override val name: String = "",
    override val displayName: String = "",
    override val sound: Sound,
    @Help("ZNPCsPlus 插件中 NPC 的 id。")
    override val npcId: String = "",
) : ZNPC {
    override fun getEmitter(): AdventureSound.Emitter {
        // Since the zNPCsPlus npcs are all done using packets. We can't get the emitter, since there is none.
        return AdventureSound.Emitter.self()
    }
}