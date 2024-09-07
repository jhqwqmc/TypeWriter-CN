package com.typewritermc.entity.entries.quest

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.entries.emptyRef
import com.typewritermc.engine.paper.entry.entries.AudienceEntry
import com.typewritermc.engine.paper.entry.entries.EntityDefinitionEntry
import com.typewritermc.engine.paper.entry.entries.ObjectiveEntry
import com.typewritermc.engine.paper.entry.entries.QuestEntry
import com.typewritermc.engine.paper.snippets.snippet
import java.util.*

private val displayTemplate by snippet("quest.objective.interact_entity", "Interact with <entity>")

@Entry("interact_entity_objective", "与实体交互", Colors.BLUE_VIOLET, "ph:hand-tap-fill")
/**
 * The `InteractEntityObjective` class is an entry that represents an objective to interact with an entity.
 * When such an objective is active, it will show an icon above any NPC.
 */
class InteractEntityObjective(
    override val id: String = "",
    override val name: String = "",
    override val children: List<Ref<out AudienceEntry>> = emptyList(),
    override val quest: Ref<QuestEntry> = emptyRef(),
    override val criteria: List<Criteria> = emptyList(),
    @Help("玩家需要与之互动的实体。")
    val entity: Ref<out EntityDefinitionEntry> = emptyRef(),
    @Help("向玩家显示的目标显示。使用 &lt;实体&gt; 替换实体名称。")
    val overrideDisplay: Optional<String> = Optional.empty(),
    override val priorityOverride: Optional<Int> = Optional.empty(),
) : ObjectiveEntry {
    override val display: String
        get() = overrideDisplay.orElseGet { displayTemplate }.run {
            val entityName = entity.get()?.displayName ?: ""
            replace("<实体>", entityName)
        }
}