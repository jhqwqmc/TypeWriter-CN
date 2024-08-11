package me.gabber235.typewriter.entries.data.minecraft.living.villager

import com.github.retrooper.packetevents.protocol.entity.villager.VillagerData
import com.github.retrooper.packetevents.protocol.entity.villager.profession.VillagerProfession
import com.github.retrooper.packetevents.protocol.entity.villager.profession.VillagerProfessions
import com.github.retrooper.packetevents.protocol.entity.villager.type.VillagerType
import com.github.retrooper.packetevents.protocol.entity.villager.type.VillagerTypes
import me.gabber235.typewriter.adapters.Colors
import me.gabber235.typewriter.adapters.Entry
import me.gabber235.typewriter.adapters.Tags
import me.gabber235.typewriter.adapters.modifiers.Help
import me.gabber235.typewriter.entry.entity.SinglePropertyCollectorSupplier
import me.gabber235.typewriter.entry.entries.EntityData
import me.gabber235.typewriter.entry.entries.EntityProperty
import me.gabber235.typewriter.extensions.packetevents.metas
import me.tofaa.entitylib.meta.mobs.monster.zombie.ZombieVillagerMeta
import me.tofaa.entitylib.meta.mobs.villager.VillagerMeta
import me.tofaa.entitylib.wrapper.WrapperEntity
import org.bukkit.entity.Player
import java.util.*
import kotlin.reflect.KClass

@Entry("villager_data", "村民数据", Colors.RED, "material-symbols:diamond")
@Tags("villager_data", "zombie_villager_data")
class VillagerData(
    override val id: String = "",
    override val name: String = "",
    @Help("村民的类型")
    val villagerType: VillagerTypeData = VillagerTypeData.PLAINS,
    @Help("村民的职业")
    val profession: VillagerProfessionData = VillagerProfessionData.NONE,
    @Help("村民的等级")
    val level: VillagerMeta.Level = VillagerMeta.Level.NOVICE,
    override val priorityOverride: Optional<Int> = Optional.empty(),
) : EntityData<VillagerProperty> {
    override fun type(): KClass<VillagerProperty> = VillagerProperty::class

    override fun build(player: Player): VillagerProperty = VillagerProperty(villagerType, profession, level)
}

data class VillagerProperty(
    val villagerType: VillagerTypeData,
    val profession: VillagerProfessionData,
    val level: VillagerMeta.Level,
) : EntityProperty {
    companion object : SinglePropertyCollectorSupplier<VillagerProperty>(VillagerProperty::class)
}

fun applyVillagerData(entity: WrapperEntity, property: VillagerProperty) {
    entity.metas {
        val villagerData = VillagerData(
            property.villagerType.type,
            property.profession.profession,
            property.level.ordinal + 1
        )
        meta<VillagerMeta> {
            this.villagerData = villagerData
        }
        meta<ZombieVillagerMeta> {
            this.villagerData = villagerData
        }
        error("无法将 VillagerData 应用于 ${entity.entityType} 实体。")
    }
}

enum class VillagerTypeData(val type: VillagerType) {
    PLAINS(VillagerTypes.PLAINS),
    DESERT(VillagerTypes.DESERT),
    JUNGLE(VillagerTypes.JUNGLE),
    SAVANNA(VillagerTypes.SAVANNA),
    SNOW(VillagerTypes.SNOW),
    SWAMP(VillagerTypes.SWAMP),
    TAIGA(VillagerTypes.TAIGA),
}

enum class VillagerProfessionData(val profession: VillagerProfession) {
    NONE(VillagerProfessions.NONE),
    ARMORER(VillagerProfessions.ARMORER),
    BUTCHER(VillagerProfessions.BUTCHER),
    CARTOGRAPHER(VillagerProfessions.CARTOGRAPHER),
    CLERIC(VillagerProfessions.CLERIC),
    FARMER(VillagerProfessions.FARMER),
    FISHERMAN(VillagerProfessions.FISHERMAN),
    FLETCHER(VillagerProfessions.FLETCHER),
    LEATHERWORKER(VillagerProfessions.LEATHERWORKER),
    LIBRARIAN(VillagerProfessions.LIBRARIAN),
    MASON(VillagerProfessions.MASON),
    NITWIT(VillagerProfessions.NITWIT),
    SHEPHERD(VillagerProfessions.SHEPHERD),
    TOOLSMITH(VillagerProfessions.TOOLSMITH),
    WEAPONSMITH(VillagerProfessions.WEAPONSMITH),
}