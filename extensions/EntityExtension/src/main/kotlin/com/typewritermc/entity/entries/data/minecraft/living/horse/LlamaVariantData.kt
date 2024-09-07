package com.typewritermc.entity.entries.data.minecraft.living.horse

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Tags
import com.typewritermc.engine.paper.entry.entity.SinglePropertyCollectorSupplier
import com.typewritermc.engine.paper.entry.entries.EntityData
import com.typewritermc.engine.paper.entry.entries.EntityProperty
import com.typewritermc.engine.paper.extensions.packetevents.metas
import me.tofaa.entitylib.meta.mobs.horse.LlamaMeta
import me.tofaa.entitylib.wrapper.WrapperEntity
import org.bukkit.entity.Player
import java.util.*
import kotlin.reflect.KClass

@Entry("llama_variant_data", "羊驼的变种。", Colors.RED, "mdi:llama")
@Tags("llama_data", "variant_data")
class LlamaVariantData(
    override val id: String = "",
    override val name: String = "",
    val variant: LlamaMeta.Variant = LlamaMeta.Variant.CREAMY,
    override val priorityOverride: Optional<Int> = Optional.empty(),
) : EntityData<LlamaVariantProperty> {
    override fun type(): KClass<LlamaVariantProperty> = LlamaVariantProperty::class

    override fun build(player: Player): LlamaVariantProperty = LlamaVariantProperty(variant)
}

data class LlamaVariantProperty(val variant: LlamaMeta.Variant) : EntityProperty {
    companion object : SinglePropertyCollectorSupplier<LlamaVariantProperty>(LlamaVariantProperty::class)
}

fun applyLlamaVariantData(entity: WrapperEntity, property: LlamaVariantProperty) {
    entity.metas {
        meta<LlamaMeta> { variant = property.variant }
        error("无法将 LlamaVariantData 应用于 ${entity.entityType} 实体。")
    }
}