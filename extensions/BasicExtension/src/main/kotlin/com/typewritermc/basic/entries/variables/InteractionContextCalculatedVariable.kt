package com.typewritermc.basic.entries.variables

import com.mthaler.aparser.arithmetic.Expression
import com.mthaler.aparser.arithmetic.tryEval
import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.extension.annotations.*
import com.typewritermc.core.interaction.InteractionContextKey
import com.typewritermc.engine.paper.entry.entries.VarContext
import com.typewritermc.engine.paper.entry.entries.VariableEntry
import com.typewritermc.engine.paper.entry.entries.getData
import com.typewritermc.engine.paper.extensions.placeholderapi.parsePlaceholders
import com.typewritermc.engine.paper.logger

@Entry(
    "interaction_context_calculated_variable",
    "使用交互上下文值进行计算的变量",
    Colors.GREEN,
    "fa6-solid:calculator"
)
@GenericConstraint(Int::class)
@GenericConstraint(Double::class)
@VariableData(InteractionContextCalculatedVariableData::class)
class InteractionContextCalculatedVariable(
    override val id: String = "",
    override val name: String = "",
) : VariableEntry {
    override fun <T : Any> get(context: VarContext<T>): T {
        val data = context.getData<InteractionContextCalculatedVariableData>()
            ?: throw IllegalStateException("找不到 ${context.klass} 的数据，数据：${context.data}（条目ID：$id）")

        // First replace interaction context values
        var expression = data.expression.parsePlaceholders(context.player)

        val interactionContext = context.interactionContext
        expression = if (interactionContext == null) {
            // Use default values if no context is available
            data.keys.foldIndexed(expression) { index, acc, keyValue ->
                acc.replace("<${index + 1}>", keyValue.default.toString())
            }
        } else {
            // Replace with actual values from context
            data.keys.foldIndexed(expression) { index, acc, keyValue ->
                val value = interactionContext[keyValue.key]?.toString() ?: keyValue.default.toString()
                acc.replace("<${index + 1}>", value)
            }
        }

        if (expression.isBlank()) {
            return 0.0.cast<T>(context.klass)
        }

        // Evaluate the expression
        val value = when (val result = Expression(expression.trim()).tryEval()) {
            is com.mthaler.aparser.util.Try.Success -> result.value
            is com.mthaler.aparser.util.Try.Failure -> {
                logger.warning("无法为玩家 ${context.player.name} 的变量 $id 计算表达式 '$expression'")
                return 0.0.cast<T>(context.klass)
            }
        }
        return value.cast<T>(context.klass)
    }
}

private data class InteractionContextCalculatedVariableData(
    val keys: List<CalculatedKeyValue> = emptyList(),
    @Colored
    @Placeholder
    @Help("使用 <1> 在数学表达式中插入第一个变量的值")
    val expression: String = "",
)

private data class CalculatedKeyValue(
    val key: InteractionContextKey<*> = InteractionContextKey.Empty,
    @Help("当键不在上下文中时用于计算的值")
    val default: Double = 0.0,
)