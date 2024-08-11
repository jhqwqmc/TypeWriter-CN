package me.gabber235.typewriter.entries.data.minecraft.living.horse

import me.gabber235.typewriter.adapters.Colors
import me.gabber235.typewriter.adapters.Entry
import me.gabber235.typewriter.adapters.Tags
import me.gabber235.typewriter.adapters.modifiers.Help
import me.gabber235.typewriter.entry.entity.SinglePropertyCollectorSupplier
import me.gabber235.typewriter.entry.entries.EntityData
import me.gabber235.typewriter.entry.entries.EntityProperty
import me.gabber235.typewriter.extensions.packetevents.metas
import me.tofaa.entitylib.meta.mobs.horse.BaseHorseMeta
import me.tofaa.entitylib.wrapper.WrapperEntity
import org.bukkit.entity.Player
import java.util.*
import kotlin.reflect.KClass

@Entry("eating_data", "实体是否在进食。", Colors.RED, "mdi:horse")
@Tags("eating_data")
class HorseEatingData(
    override val id: String = "",
    override val name: String = "",
    @Help("实体是否在进食。")
    val eating: Boolean = false,
    override val priorityOverride: Optional<Int> = Optional.empty(),
) : EntityData<EatingProperty> {
    override fun type(): KClass<EatingProperty> = EatingProperty::class

    override fun build(player: Player): EatingProperty = EatingProperty(eating)
}

data class EatingProperty(val eating: Boolean) : EntityProperty {
    companion object : SinglePropertyCollectorSupplier<EatingProperty>(EatingProperty::class, EatingProperty(false))
}

fun applyHorseEatingData(entity: WrapperEntity, property: EatingProperty) {
    entity.metas {
        meta<BaseHorseMeta> { isEating = property.eating }
        error("无法将 BaseHorseEatingData 应用于 ${entity.entityType} 实体。")
    }
}