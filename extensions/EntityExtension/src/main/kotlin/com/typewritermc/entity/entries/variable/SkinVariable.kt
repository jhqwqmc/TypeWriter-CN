package com.typewritermc.entity.entries.variable

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.GenericConstraint
import com.typewritermc.core.extension.annotations.Placeholder
import com.typewritermc.core.extension.annotations.VariableData
import com.typewritermc.engine.paper.entry.entity.SkinProperty
import com.typewritermc.engine.paper.entry.entity.skin
import com.typewritermc.engine.paper.entry.entries.VarContext
import com.typewritermc.engine.paper.entry.entries.VariableEntry
import com.typewritermc.engine.paper.entry.entries.getData
import com.typewritermc.engine.paper.extensions.placeholderapi.parsePlaceholders
import com.typewritermc.engine.paper.logger
import lirand.api.extensions.server.server
import java.util.*
import kotlin.reflect.safeCast

@Entry(
    "skin_variable",
    "基于UUID返回玩家皮肤的变量",
    Colors.GREEN,
    "ant-design:skin-filled"
)
@GenericConstraint(SkinProperty::class)
@VariableData(SkinVariableData::class)
/**
 * The `SkinVariable` is a variable that returns a players skin based on the uuid.
 *
 * The UUID must be a valid UUID, or a placeholder that returns a valid UUID.
 *
 * ## How could this be used?
 * This could be used to show NPCs of players on a leaderboard.
 */
class SkinVariable(
    override val id: String = "",
    override val name: String = "",
) : VariableEntry {
    override fun <T : Any> get(context: VarContext<T>): T {
        val data = context.getData<SkinVariableData>() ?: throw IllegalStateException("找不到${context.klass}的数据，条目${id}的数据：${context.data}")
        val possibleUUID = data.uuid.parsePlaceholders(context.player)
        val uuid = try {
            UUID.fromString(possibleUUID)
        } catch (e: IllegalArgumentException) {
            logger.warning("无法为条目${id}解析UUID'$possibleUUID'，将改用玩家UUID")
            context.player.uniqueId
        }
        val skin = server.getOfflinePlayer(uuid).skin
        return context.klass.safeCast(skin) ?: throw IllegalStateException("无法将皮肤转换为${context.klass}，SkinProperty仅兼容SkinProperty类型字段")
    }
}

data class SkinVariableData(
    @Placeholder
    val uuid: String = "",
)