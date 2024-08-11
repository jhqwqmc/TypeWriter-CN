package me.gabber235.typewriter.entries.data.minecraft.living

import me.gabber235.typewriter.adapters.Colors
import me.gabber235.typewriter.adapters.Entry
import me.gabber235.typewriter.adapters.Tags
import me.gabber235.typewriter.adapters.modifiers.Help
import me.gabber235.typewriter.entry.entity.SinglePropertyCollectorSupplier
import me.gabber235.typewriter.entry.entries.EntityData
import me.gabber235.typewriter.entry.entries.EntityProperty
import me.gabber235.typewriter.extensions.packetevents.metas
import me.tofaa.entitylib.meta.mobs.FrogMeta
import me.tofaa.entitylib.wrapper.WrapperEntity
import org.bukkit.entity.Player
import java.util.*
import kotlin.reflect.KClass

@Entry("frog_variant_data", "青蛙变种数据", Colors.GREEN, "ph:frog-fill")
@Tags("frog_data")
class FrogData (
    override val id: String = "",
    override val name: String = "",
    @Help("实体是否是一只青蛙。")
    val frogVariant: FrogMeta.Variant = FrogMeta.Variant.TEMPERATE,
    override val priorityOverride: Optional<Int> = Optional.empty(),
) : EntityData<FrogVariantProperty> {
    override fun type(): KClass<FrogVariantProperty> = FrogVariantProperty::class

    override fun build(player: Player): FrogVariantProperty = FrogVariantProperty(frogVariant)
}

data class FrogVariantProperty(val frogVariant: FrogMeta.Variant) : EntityProperty {
    companion object : SinglePropertyCollectorSupplier<FrogVariantProperty>(FrogVariantProperty::class)
}

fun applyFrogVariantData(entity: WrapperEntity, property: FrogVariantProperty) {
    entity.metas {
        meta<FrogMeta> { variant = property.frogVariant }
        error("无法将 FrogVariantData 应用于 ${entity.entityType} 实体。")
    }
}