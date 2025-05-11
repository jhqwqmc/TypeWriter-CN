package com.typewritermc.entity.entries.data.minecraft

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Tags
import com.typewritermc.engine.paper.entry.entity.SinglePropertyCollectorSupplier
import com.typewritermc.engine.paper.entry.entries.EntityData
import com.typewritermc.engine.paper.entry.entries.EntityProperty
import com.typewritermc.engine.paper.extensions.packetevents.metas
import me.tofaa.entitylib.extras.DyeColor
import me.tofaa.entitylib.meta.mobs.golem.ShulkerMeta
import me.tofaa.entitylib.meta.mobs.horse.LlamaMeta
import me.tofaa.entitylib.meta.mobs.tameable.CatMeta
import me.tofaa.entitylib.meta.mobs.tameable.WolfMeta
import me.tofaa.entitylib.wrapper.WrapperEntity
import org.bukkit.entity.Player
import java.util.*
import kotlin.reflect.KClass

@Entry(
    "dye_color_data",
    "使用染料为实体局部染色（不同实体可使用其他染色方式）",
    Colors.RED,
    "fluent:paint-bucket-16-filled"
)
@Tags("dye_color_data", "cat_data", "wolf_data", "llama_data", "shulker_data")
class DyeColorData(
    override val id: String = "",
    override val name: String = "",
    val color: DyeColor = DyeColor.RED,
    override val priorityOverride: Optional<Int> = Optional.empty(),
) : EntityData<DyeColorProperty> {
    override fun type(): KClass<DyeColorProperty> = DyeColorProperty::class

    override fun build(player: Player): DyeColorProperty = DyeColorProperty(color)
}

data class DyeColorProperty(val color: DyeColor) : EntityProperty {
    companion object : SinglePropertyCollectorSupplier<DyeColorProperty>(DyeColorProperty::class)
}

fun applyDyeColorData(entity: WrapperEntity, property: DyeColorProperty) {
    entity.metas {
        meta<CatMeta> { collarColor = property.color }
        meta<WolfMeta> { collarColor = property.color.ordinal }
        meta<LlamaMeta> { carpetColor = property.color.ordinal }
        meta<ShulkerMeta> { color = property.color.ordinal.toByte() }
        error("无法将CatCollarColorData应用到${entity.entityType}实体")
    }
}
