package com.typewritermc.basic.entries.event

import io.papermc.paper.event.player.AsyncChatEvent
import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Query
import com.typewritermc.core.entries.Ref
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.EntryListener
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.extension.annotations.Regex
import com.typewritermc.engine.paper.entry.*
import com.typewritermc.engine.paper.entry.entries.EventEntry
import com.typewritermc.engine.paper.utils.plainText
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
    @Help("文本是否应该完全匹配或者是否应该是子字符串。")
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