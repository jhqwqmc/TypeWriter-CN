package me.gabber235.typewriter.entries.data.minecraft.display.text

import me.gabber235.typewriter.adapters.Colors
import me.gabber235.typewriter.adapters.Entry
import me.gabber235.typewriter.adapters.Tags
import me.gabber235.typewriter.adapters.modifiers.Help
import me.gabber235.typewriter.entry.entity.SinglePropertyCollectorSupplier
import me.gabber235.typewriter.entry.entries.EntityProperty
import me.gabber235.typewriter.extensions.packetevents.metas
import me.tofaa.entitylib.meta.display.TextDisplayMeta
import me.tofaa.entitylib.wrapper.WrapperEntity
import org.bukkit.entity.Player
import java.util.*
import kotlin.reflect.KClass

@Entry("text_see_through_data", "文本展示实体是否透明。", Colors.RED, "mdi:texture")
@Tags("text_see_through_data")

class TextSeeThroughData(
    override val id: String = "",
    override val name: String = "",
    @Help("文字是否透明。")
    val seeThrough: Boolean = false,
    override val priorityOverride: Optional<Int> = Optional.empty(),
) : TextDisplayEntityData<SeeThroughProperty> {
    override fun type(): KClass<SeeThroughProperty> = SeeThroughProperty::class

    override fun build(player: Player): SeeThroughProperty =
        SeeThroughProperty(seeThrough)
}

data class SeeThroughProperty(val seeThrough: Boolean) : EntityProperty {
    companion object : SinglePropertyCollectorSupplier<SeeThroughProperty>(SeeThroughProperty::class, SeeThroughProperty(false))
}

fun applySeeThroughData(entity: WrapperEntity, property: SeeThroughProperty) {
    entity.metas {
        meta<TextDisplayMeta> { isSeeThrough = property.seeThrough }
        error("无法将 SeeThroughData 应用于 ${entity.entityType} 实体。")
    }
}
