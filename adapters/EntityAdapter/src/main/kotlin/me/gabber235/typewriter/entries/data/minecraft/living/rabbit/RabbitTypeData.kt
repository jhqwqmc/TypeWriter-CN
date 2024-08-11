package me.gabber235.typewriter.entries.data.minecraft.living.rabbit

import me.gabber235.typewriter.adapters.Colors
import me.gabber235.typewriter.adapters.Entry
import me.gabber235.typewriter.adapters.Tags
import me.gabber235.typewriter.adapters.modifiers.Help
import me.gabber235.typewriter.entry.entity.SinglePropertyCollectorSupplier
import me.gabber235.typewriter.entry.entries.EntityData
import me.gabber235.typewriter.entry.entries.EntityProperty
import me.gabber235.typewriter.extensions.packetevents.metas
import me.tofaa.entitylib.meta.mobs.passive.RabbitMeta
import me.tofaa.entitylib.wrapper.WrapperEntity
import org.bukkit.entity.Player
import java.util.*
import kotlin.reflect.KClass

@Entry("rabbit_type_data", "兔子的种类", Colors.RED, "mdi:rabbit")
@Tags("rabbit_data", "rabbit_type_data")
class RabbitTypeData (
    override val id: String = "",
    override val name: String = "",
    @Help("兔子的种类。")
    val rabbitType: RabbitMeta.Type = RabbitMeta.Type.BROWN,
    override val priorityOverride: Optional<Int> = Optional.empty(),
) : EntityData<RabbitTypeProperty> {
    override fun type(): KClass<RabbitTypeProperty> = RabbitTypeProperty::class

    override fun build(player: Player): RabbitTypeProperty = RabbitTypeProperty(rabbitType)
}

data class RabbitTypeProperty(val rabbitType: RabbitMeta.Type) : EntityProperty {
    companion object : SinglePropertyCollectorSupplier<RabbitTypeProperty>(RabbitTypeProperty::class)
}

fun applyRabbitTypeData(entity: WrapperEntity, property: RabbitTypeProperty) {
    entity.metas {
        meta<RabbitMeta> { type = property.rabbitType }
        error("无法将 RabbitTypeData 应用于 ${entity.entityType} 实体。")
    }
}