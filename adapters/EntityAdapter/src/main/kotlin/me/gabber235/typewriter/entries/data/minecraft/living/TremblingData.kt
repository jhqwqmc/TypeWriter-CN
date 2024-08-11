package me.gabber235.typewriter.entries.data.minecraft.living

import me.gabber235.typewriter.adapters.Colors
import me.gabber235.typewriter.adapters.Entry
import me.gabber235.typewriter.adapters.Tags
import me.gabber235.typewriter.adapters.modifiers.Help
import me.gabber235.typewriter.entry.entity.SinglePropertyCollectorSupplier
import me.gabber235.typewriter.entry.entries.EntityData
import me.gabber235.typewriter.entry.entries.EntityProperty
import me.gabber235.typewriter.extensions.packetevents.metas
import me.tofaa.entitylib.meta.mobs.HoglinMeta
import me.tofaa.entitylib.meta.mobs.monster.piglin.BasePiglinMeta
import me.tofaa.entitylib.wrapper.WrapperEntity
import org.bukkit.entity.Player
import java.util.*
import kotlin.reflect.KClass

@Entry("trembling_data", "使下界生物颤抖", Colors.RED, "game-icons:eye-monster")
@Tags("trembling_data")
class TremblingData(
    override val id: String = "",
    override val name: String = "",
    @Help("设置实体是否在颤抖。")
    val trembling: Boolean = false,
    override val priorityOverride: Optional<Int> = Optional.empty(),
) : EntityData<TremblingProperty> {
    override fun type(): KClass<TremblingProperty> = TremblingProperty::class

    override fun build(player: Player): TremblingProperty = TremblingProperty(trembling)
}

data class TremblingProperty(val trembling: Boolean) : EntityProperty {
    companion object : SinglePropertyCollectorSupplier<TremblingProperty>(TremblingProperty::class, TremblingProperty(false))
}

fun applyTremblingData(entity: WrapperEntity, property: TremblingProperty) {
    entity.metas {
        meta<BasePiglinMeta> { isImmuneToZombification = property.trembling.not() }
        meta<HoglinMeta> { isImmuneToZombification = property.trembling.not() }
        error("无法将 TremblingData 应用于 ${entity.entityType} 实体。")
    }
}