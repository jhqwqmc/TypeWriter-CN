package com.typewritermc.basic.entries.dialogue

import com.typewritermc.core.entries.*
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Colored
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.extension.annotations.Placeholder
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entries.DialogueEntry
import com.typewritermc.engine.paper.entry.entries.SpeakerEntry
import java.time.Duration

@Entry(
    "random_spoken",
    "向玩家显示随机选择的动画消息",
    "#1E88E5",
    "mingcute:message-4-fill"
)
/**
 * The `Random Spoken Dialogue` action displays a randomly selected animated message to the player.
 *
 * ## How could this be used?
 *
 * Let's say you have an NPC in your game who tells jokes to the player.
 * You could use the Random Spoken Dialogue action
 * to randomly select a joke from a list of possible jokes and have the NPC "tell"
 * it to the player using an animated message.
 */
class RandomSpokenDialogueEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    override val speaker: Ref<SpeakerEntry> = emptyRef(),
    @Placeholder
    @Colored
    @Help("显示给玩家的文本。 将随机选择一个。")
    // A list of messages to display to the player. Every time the dialogue is triggered, one of these messages will be picked at random.
    val messages: List<String> = emptyList(),
    @Help("输出消息所需的时间。")
    val duration: Duration = Duration.ZERO,
) : DialogueEntry