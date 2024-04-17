package me.gabber235.typewriter.entries.dialogue

import me.gabber235.typewriter.adapters.Entry
import me.gabber235.typewriter.adapters.modifiers.*
import me.gabber235.typewriter.entry.Criteria
import me.gabber235.typewriter.entry.Modifier
import me.gabber235.typewriter.entry.TriggerableEntry
import me.gabber235.typewriter.entry.entries.DialogueEntry
import me.gabber235.typewriter.utils.Icons

@Entry("option", "向玩家显示选项列表", "#4CAF50", Icons.LIST_UL)
/**
 * The `Option Dialogue` action displays a list of options to the player to choose from. This action provides you with the ability to give players choices that affect the outcome of the game.
 *
 * ## How could this be used?
 *
 * This action can be useful in a variety of situations, such as presenting the player with dialogue choices that determine the course of a story or offering the player a choice of rewards for completing a quest.
 */
class OptionDialogueEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<String> = emptyList(),
    override val speaker: String = "",
    @Placeholder
    @Colored
    @Help("显示给玩家的文本。")
    val text: String = "",
    @Help("供玩家选择的选项。")
    val options: List<Option> = emptyList(),
) : DialogueEntry

data class Option(
    @Help("此选项的文本。")
    val text: String = "",
    @Help("显示此选项必须满足的条件。")
    val criteria: List<Criteria> = emptyList(),
    @Help("选择此选项时要应用的修饰符。")
    val modifiers: List<Modifier> = emptyList(),
    @Triggers
    @EntryIdentifier(TriggerableEntry::class)
    @Help("选择此选项时触发的触发器。")
    val triggers: List<String> = emptyList()
)