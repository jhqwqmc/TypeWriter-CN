package me.gabber235.typewriter.entries.data.minecraft.living.parrot

import me.gabber235.typewriter.adapters.Colors
import me.gabber235.typewriter.adapters.Entry
import me.gabber235.typewriter.adapters.Tags
import me.gabber235.typewriter.adapters.modifiers.Help
import me.gabber235.typewriter.entry.entity.SinglePropertyCollectorSupplier
import me.gabber235.typewriter.entry.entries.EntityData
import me.gabber235.typewriter.entry.entries.EntityProperty
import me.gabber235.typewriter.entry.entries.GenericEntityData
import me.gabber235.typewriter.extensions.packetevents.metas
import me.tofaa.entitylib.meta.mobs.tameable.ParrotMeta
import me.tofaa.entitylib.wrapper.WrapperEntity
import org.bukkit.entity.Player
import java.util.*
import kotlin.reflect.KClass

@Entry("parrot_color_data", "鹦鹉的颜色", Colors.RED, "ph:bird-fill")
@Tags("parrot_data", "parrot_color_data")
class ParrotColorData (
    override val id: String = "",
    override val name: String = "",
    @Help("鹦鹉的颜色。")
    val parrotColor: ParrotMeta.Color  = ParrotMeta.Color.RED_BLUE,
    override val priorityOverride: Optional<Int> = Optional.empty(),
) : EntityData<ParrotColorProperty> {
    override fun type(): KClass<ParrotColorProperty> = ParrotColorProperty::class

    override fun build(player: Player): ParrotColorProperty = ParrotColorProperty(parrotColor)
}

data class ParrotColorProperty(val parrotColor: ParrotMeta.Color ) : EntityProperty {
    companion object : SinglePropertyCollectorSupplier<ParrotColorProperty>(ParrotColorProperty::class)
}

fun applyParrotColorData(entity: WrapperEntity, property: ParrotColorProperty) {
    entity.metas {
        meta<ParrotMeta> { color = property.parrotColor }
        error("无法将 ParrotColorData 应用于 ${entity.entityType} 实体。")
    }
}