package me.gabber235.typewriter.entries.data.minecraft.living

import me.gabber235.typewriter.adapters.Colors
import me.gabber235.typewriter.adapters.Entry
import me.gabber235.typewriter.adapters.modifiers.Help
import me.gabber235.typewriter.entry.entity.SinglePropertyCollectorSupplier
import me.gabber235.typewriter.entry.entries.EntityProperty
import me.gabber235.typewriter.entry.entries.GenericEntityData
import me.gabber235.typewriter.extensions.packetevents.metas
import me.tofaa.entitylib.meta.types.LivingEntityMeta
import me.tofaa.entitylib.wrapper.WrapperEntity
import org.bukkit.entity.Player
import java.util.*
import kotlin.reflect.KClass

@Entry("potion_effect_color_data", "药水效果粒子的颜色", Colors.RED, "bi:droplet-fill")
class PotionEffectColorData(
    override val id: String = "",
    override val name: String = "",
    @Help("药水效果粒子的颜色。")
    val color: Int = 0,
    override val priorityOverride: Optional<Int> = Optional.empty(),
) : GenericEntityData<PotionEffectColorProperty> {
    override fun type(): KClass<PotionEffectColorProperty> = PotionEffectColorProperty::class

    override fun build(player: Player): PotionEffectColorProperty = PotionEffectColorProperty(color)
}

data class PotionEffectColorProperty(val color: Int) : EntityProperty {
    companion object : SinglePropertyCollectorSupplier<PotionEffectColorProperty>(PotionEffectColorProperty::class)
}

fun applyPotionEffectColorData(entity: WrapperEntity, property: PotionEffectColorProperty) {
    entity.metas {
        meta<LivingEntityMeta> { potionEffectColor = property.color }
        error("无法将 PotionEffectColorData 应用于 ${entity.entityType} 实体。")
    }
}