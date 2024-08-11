package me.gabber235.typewriter.entries.data.minecraft

import me.gabber235.typewriter.adapters.Colors
import me.gabber235.typewriter.adapters.Entry
import me.gabber235.typewriter.adapters.modifiers.Help
import me.gabber235.typewriter.entry.entity.SinglePropertyCollectorSupplier
import me.gabber235.typewriter.entry.entries.EntityProperty
import me.gabber235.typewriter.entry.entries.GenericEntityData
import me.gabber235.typewriter.extensions.packetevents.metas
import me.tofaa.entitylib.meta.EntityMeta
import me.tofaa.entitylib.wrapper.WrapperEntity
import org.bukkit.entity.Player
import java.util.*
import kotlin.reflect.KClass

@Entry("glowing_effect_data", "实体是否发光", Colors.RED, "bi:lightbulb-fill")
class GlowingEffectData(
    override val id: String = "",
    override val name: String = "",
    @Help("实体是否发光。")
    val glowing: Boolean = false,
    override val priorityOverride: Optional<Int> = Optional.empty(),
) : GenericEntityData<GlowingEffectProperty> {
    override fun type(): KClass<GlowingEffectProperty> = GlowingEffectProperty::class

    override fun build(player: Player): GlowingEffectProperty = GlowingEffectProperty(glowing)
}

data class GlowingEffectProperty(val glowing: Boolean = false) : EntityProperty {
    companion object : SinglePropertyCollectorSupplier<GlowingEffectProperty>(GlowingEffectProperty::class, GlowingEffectProperty(false))
}

fun applyGlowingEffectData(entity: WrapperEntity, property: GlowingEffectProperty) {
    entity.metas {
        meta<EntityMeta> { setHasGlowingEffect(property.glowing) }
        error("无法将 GlowingEffectData 应用于 ${entity.entityType} 实体。")
    }
}