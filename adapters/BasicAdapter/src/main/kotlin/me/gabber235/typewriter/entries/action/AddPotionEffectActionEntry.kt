package me.gabber235.typewriter.entries.action

import me.gabber235.typewriter.adapters.Colors
import me.gabber235.typewriter.adapters.Entry
import me.gabber235.typewriter.adapters.modifiers.Help
import me.gabber235.typewriter.entry.Criteria
import me.gabber235.typewriter.entry.Modifier
import me.gabber235.typewriter.entry.Ref
import me.gabber235.typewriter.entry.TriggerableEntry
import me.gabber235.typewriter.entry.entries.ActionEntry
import me.gabber235.typewriter.utils.ThreadType
import me.gabber235.typewriter.utils.ThreadType.SYNC
import me.gabber235.typewriter.utils.toTicks
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.time.Duration

@Entry("add_potion_effect", "为玩家添加药水效果", Colors.RED, "fa6-solid:flask-vial")
/**
 * The `Add Potion Effect Action` is an action that adds a potion effect to the player.
 *
 * ## How could this be used?
 *
 * This action can be useful in a variety of situations. You can use it to provide players with buffs or debuffs, such as speed or slowness, or to create custom effects.
 */
class AddPotionEffectActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    @Help("要添加的药水效果。")
    val potionEffect: PotionEffectType = PotionEffectType.SPEED,
    @Help("药水效果的持续时间。")
    val duration: Duration = Duration.ofSeconds(1),
    @Help("药水效果的等级。")
    val amplifier: Int = 1,
    @Help("效果是否是环境效果")
    val ambient: Boolean = false,
    @Help("是否显示药水效果粒子。")
    val particles: Boolean = true,
    @Help("是否在玩家的物品栏中显示药水效果图标。")
    val icon: Boolean = true,
) : ActionEntry {
    override fun execute(player: Player) {
        super.execute(player)

        val potion = PotionEffect(potionEffect, duration.toTicks().toInt(), amplifier, ambient, particles, icon)
        SYNC.launch {
            player.addPotionEffect(potion)
        }
    }
}