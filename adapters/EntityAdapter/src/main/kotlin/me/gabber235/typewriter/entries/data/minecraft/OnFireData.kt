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

@Entry("on_fire_data", "实体是否着火", Colors.RED, "bi:fire")
class OnFireData(
    override val id: String = "",
    override val name: String = "",
    @Help("实体是否着火。")
    val onFire: Boolean = false,
    override val priorityOverride: Optional<Int> = Optional.empty(),
) : GenericEntityData<OnFireProperty> {
    override fun type(): KClass<OnFireProperty> = OnFireProperty::class

    override fun build(player: Player): OnFireProperty = OnFireProperty(onFire)
}

data class OnFireProperty(val onFire: Boolean = false) : EntityProperty {
    companion object : SinglePropertyCollectorSupplier<OnFireProperty>(OnFireProperty::class, OnFireProperty(false))
}

fun applyOnFireData(entity: WrapperEntity, property: OnFireProperty) {
    entity.metas {
        meta<EntityMeta> { isOnFire = property.onFire }
        error("无法将 OnFireData 应用于 ${entity.entityType} 实体。")
    }
}