package com.typewritermc.entity.entries.data.minecraft.living.guardian

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.extension.annotations.Default
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Tags
import com.typewritermc.engine.paper.entry.entity.SinglePropertyCollectorSupplier
import com.typewritermc.engine.paper.entry.entries.EntityData
import com.typewritermc.engine.paper.entry.entries.EntityProperty
import com.typewritermc.engine.paper.extensions.packetevents.metas
import me.tofaa.entitylib.meta.mobs.monster.GuardianMeta
import me.tofaa.entitylib.wrapper.WrapperEntity
import org.bukkit.entity.Player
import java.util.*
import kotlin.reflect.KClass

@Entry("retracting_spikes_data", "守卫者是否收回尖刺", Colors.RED, "game-icons:spikes")
@Tags("retracting_spikes_data", "guardian_data")
class RetractingSpikesData(
    override val id: String = "",
    override val name: String = "",
    @Default("true")
    val retractingSpikes: Boolean = true,
    override val priorityOverride: Optional<Int> = Optional.empty(),
) : EntityData<RetractingSpikesProperty> {
    override fun type(): KClass<RetractingSpikesProperty> = RetractingSpikesProperty::class

    override fun build(player: Player): RetractingSpikesProperty = RetractingSpikesProperty(retractingSpikes)
}

data class RetractingSpikesProperty(val retractingSpikes: Boolean) : EntityProperty {
    companion object : SinglePropertyCollectorSupplier<RetractingSpikesProperty>(RetractingSpikesProperty::class, RetractingSpikesProperty(false))
}

fun applyRetractingSpikesData(entity: WrapperEntity, property: RetractingSpikesProperty) {
    entity.metas {
        meta<GuardianMeta> { isRetractingSpikes = property.retractingSpikes }
        error("无法将RetractingSpikesData应用到${entity.entityType}实体")
    }
}