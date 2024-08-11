package me.gabber235.typewriter.entries.data.minecraft

import com.github.retrooper.packetevents.protocol.entity.pose.EntityPose
import me.gabber235.typewriter.adapters.Colors
import me.gabber235.typewriter.adapters.Entry
import me.gabber235.typewriter.adapters.modifiers.Help
import me.gabber235.typewriter.entry.entity.SinglePropertyCollectorSupplier
import me.gabber235.typewriter.entry.entries.EntityProperty
import me.gabber235.typewriter.entry.entries.GenericEntityData
import me.gabber235.typewriter.extensions.packetevents.metas
import me.tofaa.entitylib.meta.EntityMeta
import me.tofaa.entitylib.wrapper.WrapperEntity
import net.kyori.adventure.text.BlockNBTComponent.Pos
import org.bukkit.entity.Player
import org.bukkit.entity.Pose
import java.util.*
import kotlin.reflect.KClass

@Entry("pose_data", "实体的旋转角度", Colors.RED, "bi:person-arms-up")
class PoseData(
    override val id: String = "",
    override val name: String = "",
    @Help("实体的旋转角度。")
    val pose: EntityPose = EntityPose.STANDING,
    override val priorityOverride: Optional<Int> = Optional.empty(),
) : GenericEntityData<PoseProperty> {
    override fun type(): KClass<PoseProperty> = PoseProperty::class

    override fun build(player: Player): PoseProperty = PoseProperty(pose)
}

data class PoseProperty(val pose: EntityPose) : EntityProperty {
    companion object : SinglePropertyCollectorSupplier<PoseProperty>(PoseProperty::class, PoseProperty(EntityPose.STANDING))
}

fun Pose.toEntityPose() = when (this) {
    Pose.SNEAKING -> EntityPose.CROUCHING
    else -> EntityPose.valueOf(this.name)
}

fun EntityPose.toBukkitPose() = when (this) {
    EntityPose.CROUCHING -> Pose.SNEAKING
    else -> Pose.valueOf(this.name)
}

fun applyPoseData(entity: WrapperEntity, property: PoseProperty) {
    entity.metas {
        meta<EntityMeta> { pose = property.pose }
        error("无法将 PoseData 应用于 ${entity.entityType} 实体。")
    }
}