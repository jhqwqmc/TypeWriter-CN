package me.gabber235.typewriter.entries.data.minecraft.living.scheep

import me.gabber235.typewriter.adapters.Colors
import me.gabber235.typewriter.adapters.Entry
import me.gabber235.typewriter.adapters.Tags
import me.gabber235.typewriter.adapters.modifiers.Help
import me.gabber235.typewriter.entry.entity.SinglePropertyCollectorSupplier
import me.gabber235.typewriter.entry.entries.EntityData
import me.gabber235.typewriter.entry.entries.EntityProperty
import me.gabber235.typewriter.extensions.packetevents.metas
import me.tofaa.entitylib.meta.mobs.passive.SheepMeta
import me.tofaa.entitylib.wrapper.WrapperEntity
import org.bukkit.entity.Player
import java.util.*
import kotlin.reflect.KClass

@Entry("sheared_data", "实体是否被修剪。", Colors.RED, "mdi:sheep")
@Tags("sheared_data")
class ShearedData (
    override val id: String = "",
    override val name: String = "",
    @Help("实体是否被修剪。")
    val sheared: Boolean = false,
    override val priorityOverride: Optional<Int> = Optional.empty(),
) : EntityData<ShearedProperty> {
    override fun type(): KClass<ShearedProperty> = ShearedProperty::class

    override fun build(player: Player): ShearedProperty = ShearedProperty(sheared)
}

data class ShearedProperty(val sheared: Boolean) : EntityProperty {
    companion object : SinglePropertyCollectorSupplier<ShearedProperty>(ShearedProperty::class, ShearedProperty(false))
}

fun applySheepShearedData(entity: WrapperEntity, property: ShearedProperty) {
    entity.metas {
        meta<SheepMeta> { isSheared = property.sheared }
        error("无法将 SheepShearedData 应用于 ${entity.entityType} 实体。")
    }
}