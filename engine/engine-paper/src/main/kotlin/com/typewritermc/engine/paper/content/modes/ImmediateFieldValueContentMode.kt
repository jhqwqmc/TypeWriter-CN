package com.typewritermc.engine.paper.content.modes

import com.github.shynixn.mccoroutine.bukkit.launch
import com.typewritermc.core.entries.Entry
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.utils.failure
import com.typewritermc.core.utils.ok
import com.typewritermc.engine.paper.content.ContentContext
import com.typewritermc.engine.paper.content.ContentMode
import com.typewritermc.engine.paper.content.entryId
import com.typewritermc.engine.paper.content.fieldPath
import com.typewritermc.engine.paper.entry.entries.InteractionEndTrigger
import com.typewritermc.engine.paper.entry.fieldValue
import com.typewritermc.engine.paper.entry.triggerFor
import com.typewritermc.core.interaction.context
import com.typewritermc.engine.paper.logger
import com.typewritermc.engine.paper.plugin
import kotlinx.coroutines.delay
import org.bukkit.entity.Player
import java.lang.reflect.Type
import kotlin.time.Duration.Companion.milliseconds

abstract class ImmediateFieldValueContentMode<T : Any>(context: ContentContext, player: Player) :
    ContentMode(context, player) {
    abstract val type: Type

    override suspend fun setup(): Result<Unit> {
        val entryId = context.entryId
            ?: return failure("未找到 ${this::class.simpleName} 的 entryId。这是一个错误，请报告此问题。")

        val fieldPath = context.fieldPath
            ?: return failure("未找到 ${this::class.simpleName} 的 fieldPath。这是一个错误，请报告此问题。")

        // Needs to complete the initialisation so that we can properly get the value and end the content mode
        plugin.launch {
            delay(200.milliseconds)
            try {
                val value = value()
                Ref(entryId, Entry::class).fieldValue(fieldPath, value, type)
            } catch (e: Exception) {
                logger.severe("未能为 ${this::class.simpleName} 设置字段值，上下文：$context。这是一个错误，请报告此问题。")
                e.printStackTrace()
            } finally {
                InteractionEndTrigger.triggerFor(player, context())
            }
        }

        return ok(Unit)
    }

    abstract fun value(): T

}