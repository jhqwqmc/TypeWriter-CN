package com.typewritermc.basic.entries.event

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Query
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.ContextKeys
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.EntryListener
import com.typewritermc.core.extension.annotations.KeyType
import com.typewritermc.core.interaction.EntryContextKey
import com.typewritermc.core.utils.point.Position
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entries.EventEntry
import com.typewritermc.engine.paper.entry.triggerAllFor
import com.typewritermc.engine.paper.utils.toPosition
import org.bukkit.event.player.PlayerTeleportEvent
import kotlin.reflect.KClass

@Entry("teleport_event", "当玩家传送时", Colors.YELLOW, "mdi:teleport")
@ContextKeys(TeleportEventContextKeys::class)
/**
 * The `TeleportEventEntry` class represents an event triggered when a player teleports.
 *
 * ## How could this be used?
 *
 * This could be used to create immersive gameplay experiences such as triggering a special event or dialogue when a player teleports to a specific location.
 */
class TeleportEventEntry(
    override val id: String = "",
    override val name: String = "",
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
) : EventEntry

enum class TeleportEventContextKeys(override val klass: KClass<*>) : EntryContextKey {
    @KeyType(Position::class)
    FROM_POSITION(Position::class),

    @KeyType(Position::class)
    TO_POSITION(Position::class),
}

@EntryListener(TeleportEventEntry::class)
fun onPlayerTeleport(event: PlayerTeleportEvent, query: Query<TeleportEventEntry>) {
    query.find().triggerAllFor(event.player) {
        TeleportEventContextKeys.FROM_POSITION += event.from.toPosition()
        TeleportEventContextKeys.TO_POSITION += event.to.toPosition()
    }
}