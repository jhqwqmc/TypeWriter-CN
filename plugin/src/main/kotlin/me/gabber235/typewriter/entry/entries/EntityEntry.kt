package me.gabber235.typewriter.entry.entries

import me.gabber235.typewriter.adapters.Tags
import me.gabber235.typewriter.adapters.modifiers.Help
import me.gabber235.typewriter.entry.StaticEntry
import me.gabber235.typewriter.utils.Sound

@Tags("entity")
interface EntityEntry : StaticEntry

@Tags("speaker")
interface SpeakerEntry : EntityEntry {
    @Help("将在聊天中显示的实体的名称（例如“Steve”或“Alex”）。")
    val displayName: String

    @Help("实体说话时将播放的声音。")
    val sound: Sound
}

@Tags("npc")
interface Npc : SpeakerEntry, SoundSourceEntry
