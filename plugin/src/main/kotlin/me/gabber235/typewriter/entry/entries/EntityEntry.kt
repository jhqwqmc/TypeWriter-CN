package me.gabber235.typewriter.entry.entries

import me.gabber235.typewriter.adapters.Tags
import me.gabber235.typewriter.adapters.modifiers.Help
import me.gabber235.typewriter.adapters.modifiers.WithRotation
import me.gabber235.typewriter.entry.*
import me.gabber235.typewriter.entry.entity.*
import me.gabber235.typewriter.utils.Sound
import org.bukkit.Location
import org.bukkit.entity.Player
import org.checkerframework.checker.units.qual.A
import kotlin.reflect.KClass

@Tags("speaker")
interface SpeakerEntry : PlaceholderEntry {
    @Help("将在聊天中显示的实体的名称（例如“Steve”或“Alex”）。")
    val displayName: String

    @Help("实体说话时将播放的声音。")
    val sound: Sound

    override fun display(player: Player?): String? = displayName
}

/**
 * Must override equals and hashCode to compare the data.
 *
 * Must have a companion object that implements `PropertyCollectorSupplier`.
 */
interface EntityProperty

interface PropertyCollectorSupplier<P : EntityProperty> {
    val type: KClass<P>
    fun collector(suppliers: List<PropertySupplier<out P>>): PropertyCollector<P>
}

interface PropertySupplier<P : EntityProperty> {
    fun type(): KClass<P>
    fun build(player: Player): P
    fun canApply(player: Player): Boolean
}

interface PropertyCollector<P : EntityProperty> {
    val type: KClass<P>
    fun collect(player: Player): P?
}

@Tags("entity_data")
interface EntityData<P : EntityProperty> : AudienceEntry, PropertySupplier<P>, PriorityEntry {
    override fun canApply(player: Player): Boolean = player.inAudience(this)
    override fun display(): AudienceDisplay = PassThroughDisplay()
}

@Tags("generic_entity_data")
interface GenericEntityData<P : EntityProperty> : EntityData<P>


@Tags("living_entity_data")
interface LivingEntityData<P : EntityProperty> : EntityData<P>


@Tags("entity_definition")
interface EntityDefinitionEntry : ManifestEntry, SpeakerEntry, EntityCreator {
    val data: List<Ref<EntityData<*>>>
}

@Tags("entity_instance")
interface EntityInstanceEntry : AudienceFilterEntry {
    val definition: Ref<out EntityDefinitionEntry>

    @WithRotation
    val spawnLocation: Location
}

@Tags("entity_activity")
interface EntityActivityEntry : ActivityCreator, ManifestEntry

@Tags("shared_entity_activity")
interface SharedEntityActivityEntry : EntityActivityEntry {
    override fun create(
        context: ActivityContext,
        currentLocation: LocationProperty
    ): EntityActivity<ActivityContext> {
        if (context !is SharedActivityContext) throw WrongActivityContextException(context, SharedActivityContext::class, this)
        return create(context, currentLocation) as EntityActivity<ActivityContext>
    }
    fun create(context: SharedActivityContext, currentLocation: LocationProperty): EntityActivity<SharedActivityContext>
}

@Tags("individual_entity_activity")
interface IndividualEntityActivityEntry : EntityActivityEntry {
    override fun create(
        context: ActivityContext,
        currentLocation: LocationProperty
    ): EntityActivity<ActivityContext> {
        if (context !is IndividualActivityContext) throw WrongActivityContextException(context, IndividualActivityContext::class, this)
        return create(context, currentLocation) as EntityActivity<ActivityContext>
    }
    fun create(context: IndividualActivityContext, currentLocation: LocationProperty): EntityActivity<IndividualActivityContext>
}

@Tags("generic_entity_activity")
interface GenericEntityActivityEntry : SharedEntityActivityEntry, IndividualEntityActivityEntry {
    override fun create(
        context: ActivityContext,
        currentLocation: LocationProperty
    ): EntityActivity<ActivityContext>

    override fun create(
        context: SharedActivityContext,
        currentLocation: LocationProperty
    ): EntityActivity<SharedActivityContext> {
        return create(context as ActivityContext, currentLocation) as EntityActivity<SharedActivityContext>
    }

    override fun create(
        context: IndividualActivityContext,
        currentLocation: LocationProperty
    ): EntityActivity<IndividualActivityContext> {
        return create(context as ActivityContext, currentLocation) as EntityActivity<IndividualActivityContext>
    }
}

class WrongActivityContextException(context: ActivityContext, expected: KClass<out ActivityContext>, entry: EntityActivityEntry) : IllegalStateException("""
    |${entry.name}的活动上下文不是预期的类型。
    |预期类型：$expected
    |实际类型：$context
    |
    |当你尝试混合共享和个体活动时会发生这种情况。
    |例如，你不能在个体实体上使用共享活动。
    |同样，你也不能在共享实体上使用个体活动。
    |
    |要解决此问题，你需要确保活动与实体的可见性匹配。
    |如果你需要更多帮助，请加入TypeWriter Discord！https://discord.gg/gs5QYhfv9x
""".trimMargin())