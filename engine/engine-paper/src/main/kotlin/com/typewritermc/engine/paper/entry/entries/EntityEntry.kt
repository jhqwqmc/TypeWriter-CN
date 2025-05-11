package com.typewritermc.engine.paper.entry.entries

import com.typewritermc.core.entries.PriorityEntry
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.entries.ref
import com.typewritermc.core.extension.annotations.Colored
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.extension.annotations.Placeholder
import com.typewritermc.core.extension.annotations.Tags
import com.typewritermc.engine.paper.entry.*
import com.typewritermc.engine.paper.entry.entity.*
import com.typewritermc.engine.paper.utils.Sound
import org.bukkit.entity.Player
import kotlin.reflect.KClass

@Tags("speaker")
interface SpeakerEntry : PlaceholderEntry {
    @Colored
    @Placeholder
    @Help("将在聊天中显示的实体名称（例如'Steve'或'Alex'）")
    val displayName: Var<String>

    @Help("该实体说话时将播放的音效")
    val sound: Sound

    override fun parser(): PlaceholderParser = placeholderParser {
        supply { player -> displayName.get(player) }
    }
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
    override suspend fun display(): AudienceDisplay = PassThroughDisplay()
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
interface EntityInstanceEntry : AudienceFilterEntry, SoundSourceEntry, SpeakerEntry {
    val definition: Ref<out EntityDefinitionEntry>

    override val displayName: Var<String>
        get() = ComputeVar { player, context ->
            (ref().findDisplay<AudienceDisplay>() as? AudienceEntityDisplay)
                ?.property<DisplayNameProperty>(player.uniqueId)
                ?.displayName
                ?.get(player, context)
                ?: definition.get()?.displayName?.get(player, context)
                ?: ""
        }

    override val sound: Sound
        get() = definition.get()?.sound ?: Sound.EMPTY

    override fun getEmitter(player: Player): SoundEmitter {
        val display = ref().findDisplay() as? AudienceEntityDisplay ?: return SoundEmitter(player.entityId)
        val entityId = display.entityId(player.uniqueId)
        return SoundEmitter(entityId)
    }

    override fun parser(): PlaceholderParser = placeholderParser {
        include(super<AudienceFilterEntry>.parser())
        include(super<SpeakerEntry>.parser())
    }
}

@Tags("entity_activity")
interface EntityActivityEntry : ActivityCreator, ManifestEntry

@Tags("shared_entity_activity")
interface SharedEntityActivityEntry : EntityActivityEntry {
    override fun create(
        context: ActivityContext,
        currentLocation: PositionProperty
    ): EntityActivity<ActivityContext> {
        if (context !is SharedActivityContext) throw WrongActivityContextException(
            context,
            SharedActivityContext::class,
            this
        )
        return create(context, currentLocation) as EntityActivity<ActivityContext>
    }

    fun create(context: SharedActivityContext, currentLocation: PositionProperty): EntityActivity<SharedActivityContext>
}

@Tags("individual_entity_activity")
interface IndividualEntityActivityEntry : EntityActivityEntry {
    override fun create(
        context: ActivityContext,
        currentLocation: PositionProperty
    ): EntityActivity<ActivityContext> {
        if (context !is IndividualActivityContext) throw WrongActivityContextException(
            context,
            IndividualActivityContext::class,
            this
        )
        return create(context, currentLocation) as EntityActivity<ActivityContext>
    }

    fun create(
        context: IndividualActivityContext,
        currentLocation: PositionProperty
    ): EntityActivity<IndividualActivityContext>
}

@Tags("generic_entity_activity")
interface GenericEntityActivityEntry : SharedEntityActivityEntry, IndividualEntityActivityEntry {
    override fun create(
        context: ActivityContext,
        currentLocation: PositionProperty
    ): EntityActivity<ActivityContext>

    override fun create(
        context: SharedActivityContext,
        currentLocation: PositionProperty
    ): EntityActivity<SharedActivityContext> {
        return create(context as ActivityContext, currentLocation) as EntityActivity<SharedActivityContext>
    }

    override fun create(
        context: IndividualActivityContext,
        currentLocation: PositionProperty
    ): EntityActivity<IndividualActivityContext> {
        return create(context as ActivityContext, currentLocation) as EntityActivity<IndividualActivityContext>
    }
}

class WrongActivityContextException(
    context: ActivityContext,
    expected: KClass<out ActivityContext>,
    entry: EntityActivityEntry
) : IllegalStateException(
    """
    |${entry.name}的活动上下文不符合预期类型
    |预期类型: $expected
    |实际类型: $context
    |
    |当您尝试混合使用共享活动和个体活动时会出现此问题
    |例如：不能在个体实体上使用共享活动
    |也不能在共享实体上使用个体活动
    |
    |解决方法：确保活动类型与实体可见性匹配
    |如需更多帮助，请加入TypeWriter Discord！https://discord.gg/gs5QYhfv9x
""".trimMargin()
)