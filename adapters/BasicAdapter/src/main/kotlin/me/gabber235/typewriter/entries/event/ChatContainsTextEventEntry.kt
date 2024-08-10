package me.gabber235.typewriter.entries.event

import io.papermc.paper.event.player.AsyncChatEvent
import me.gabber235.typewriter.adapters.Colors
import me.gabber235.typewriter.adapters.Entry
import me.gabber235.typewriter.adapters.modifiers.Help
import me.gabber235.typewriter.adapters.modifiers.Regex
import me.gabber235.typewriter.entry.*
import me.gabber235.typewriter.entry.entries.EventEntry
import me.gabber235.typewriter.utils.plainText
import kotlin.text.Regex as KotlinRegex

@Entry(
    "on_message_contains_text",
    "当玩家发送包含特定文本的聊天消息时",
    Colors.YELLOW,
    "fluent:note-48-filled"
)
/**
 * The `Chat Contains Text Event` is called when a player sends a chat message containing certain text.
 *
 * ## How could this be used?
 *
 * When a player mentions something, you could display dialogue to them.
 */
class ChatContainsTextEventEntry(
    override val id: String = "",
    override val name: String = "",
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    @Regex
    @Help("要在消息中查找的文本。")
    val text: String = "",
    @Help("文本是否应该完全匹配。")
    val exactSame: Boolean = false
) : EventEntry


@EntryListener(ChatContainsTextEventEntry::class)
fun onChat(event: AsyncChatEvent, query: Query<ChatContainsTextEventEntry>) {
    val message = event.message().plainText()
    query findWhere {
        if (it.exactSame)
            KotlinRegex(it.text).matches(message)
        else
            KotlinRegex(it.text).containsMatchIn(message)
    } triggerAllFor event.player
}