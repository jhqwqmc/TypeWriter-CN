package me.gabber235.typewriter.entry.entries

import me.gabber235.typewriter.adapters.Tags
import me.gabber235.typewriter.adapters.modifiers.Help
import me.gabber235.typewriter.entry.Ref
import me.gabber235.typewriter.entry.TriggerableEntry

@Tags("dialogue")
interface DialogueEntry : TriggerableEntry {
    @Help("对话的发言者")
    val speaker: Ref<SpeakerEntry>

    val speakerDisplayName: String
        get() = speaker.get()?.displayName ?: ""
}

