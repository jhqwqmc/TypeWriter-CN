package me.ahdg6.typewriter.mythicmobs.entries.fact

import io.lumine.mythic.bukkit.MythicBukkit
import me.gabber235.typewriter.adapters.Colors
import me.gabber235.typewriter.adapters.Entry
import me.gabber235.typewriter.adapters.modifiers.Help
import me.gabber235.typewriter.entry.Ref
import me.gabber235.typewriter.entry.emptyRef
import me.gabber235.typewriter.entry.entries.GroupEntry
import me.gabber235.typewriter.entry.entries.ReadableFactEntry
import me.gabber235.typewriter.facts.FactData
import org.bukkit.entity.Player

@Entry(
    "mythic_mob_count_fact",
    "计算指定类型的活跃MythicMobs的生物的数量",
    Colors.PURPLE,
    "mingcute:counter-fill"
)
/**
 * A [fact](/docs/creating-stories/facts) that represents how many specific MythicMobs mob are in the world.
 *
 * <fields.ReadonlyFactInfo />
 *
 * ## How could this be used?
 *
 * This fact could be used to change dialogue sent by an NPC or mob when a boss exists. It could also be used in conjunction with the Spawn Mob action to spawn specific mobs if one or more mobs exist/doesn't exist.
 */
class MobCountFact(
    override val id: String = "",
    override val name: String = "",
    override val comment: String = "",
    override val group: Ref<GroupEntry> = emptyRef(),
    @Help("要计数的生物的ID")
    val mobName: String = "",
) : ReadableFactEntry {
    override fun readSinglePlayer(player: Player): FactData {
        val mob = MythicBukkit.inst().mobManager.getMythicMob(mobName)
        if (!mob.isPresent) return FactData(0)

        var count = 0
        for (activeMob in MythicBukkit.inst().mobManager.activeMobs) {
            if (activeMob.type == mob.get()) {
                count++
            }
        }

        return FactData(count)
    }
}