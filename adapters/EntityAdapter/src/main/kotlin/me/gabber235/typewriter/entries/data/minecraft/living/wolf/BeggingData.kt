package me.gabber235.typewriter.entries.data.minecraft.living.wolf

import me.gabber235.typewriter.adapters.Colors
import me.gabber235.typewriter.adapters.Entry
import me.gabber235.typewriter.adapters.Tags
import me.gabber235.typewriter.adapters.modifiers.Help
import me.gabber235.typewriter.entry.entity.SinglePropertyCollectorSupplier
import me.gabber235.typewriter.entry.entries.EntityData
import me.gabber235.typewriter.entry.entries.EntityProperty
import me.gabber235.typewriter.extensions.packetevents.metas
import me.tofaa.entitylib.meta.mobs.tameable.WolfMeta
import me.tofaa.entitylib.wrapper.WrapperEntity
import org.bukkit.entity.Player
import java.util.*
import kotlin.reflect.KClass

@Entry("begging_data", "The begging state of the wolf", Colors.RED, "game-icons:sitting-dog")
@Tags("begging_data")
class BeggingData (
    override val id: String = "",
    override val name: String = "",
    @Help("The begging state of the wolf.")
    val begging: Boolean = false,
    override val priorityOverride: Optional<Int> = Optional.empty(),
) : EntityData<BeggingProperty> {
    override fun type(): KClass<BeggingProperty> = BeggingProperty::class

    override fun build(player: Player): BeggingProperty = BeggingProperty(begging)
}

data class BeggingProperty(val wolfBegging: Boolean) : EntityProperty {
    companion object : SinglePropertyCollectorSupplier<BeggingProperty>(BeggingProperty::class, BeggingProperty(false))
}

fun applyBeggingData(entity: WrapperEntity, property: BeggingProperty) {
    entity.metas {
        meta<WolfMeta> { isBegging = property.wolfBegging }
        error("Could not apply WolfBeggingData to ${entity.entityType} entity.")
    }
}