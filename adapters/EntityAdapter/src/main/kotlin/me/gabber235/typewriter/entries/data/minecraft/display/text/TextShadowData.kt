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

@Entry("text_shadow_data", "文本展示实体中的文字是否有阴影。", Colors.RED, "mdi:box-shadow")
@Tags("text_shadow_data")
class TextShadowData(
    override val id: String = "",
    override val name: String = "",
    @Help("文字是否有阴影。")
    val shadow: Boolean = false,
    override val priorityOverride: Optional<Int> = Optional.empty(),
) : TextDisplayEntityData<ShadowProperty> {
    override fun type(): KClass<ShadowProperty> = ShadowProperty::class

    override fun build(player: Player): ShadowProperty =
        ShadowProperty(shadow)
}

data class ShadowProperty(val shadow: Boolean) : EntityProperty {
    companion object : SinglePropertyCollectorSupplier<ShadowProperty>(ShadowProperty::class, ShadowProperty(false))
}

fun applyShadowData(entity: WrapperEntity, property: ShadowProperty) {
    entity.metas {
        meta<TextDisplayMeta> { isShadow = property.shadow }
        error("无法将 ShadowData 应用于 ${entity.entityType} 实体。")
    }
}