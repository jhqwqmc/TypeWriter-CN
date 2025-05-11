package com.typewritermc.basic.entries.variables

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.extension.annotations.*
import com.typewritermc.core.interaction.InteractionContextKey
import com.typewritermc.engine.paper.entry.entries.VarContext
import com.typewritermc.engine.paper.entry.entries.VariableEntry
import com.typewritermc.engine.paper.entry.entries.getData
import com.typewritermc.engine.paper.extensions.placeholderapi.parsePlaceholders
import kotlin.reflect.cast

@Entry(
    "interaction_context_text_variable",
    "使用交互变量构建文本的方法",
    Colors.GREEN,
    "material-symbols:text-compare-rounded"
)
@GenericConstraint(String::class)
@VariableData(InteractionContextTextVariableData::class)
class InteractionContextTextVariable(
    override val id: String = "",
    override val name: String = "",
) : VariableEntry {
    override fun <T : Any> get(context: VarContext<T>): T {
        val data = context.getData<InteractionContextTextVariableData>() ?: throw IllegalStateException("找不到 ${context.klass} 的数据，数据：${context.data}（条目ID：$id）")

        val text = data.text.parsePlaceholders(context.player)
        val keys = data.keys

        val interactionContext = context.interactionContext
        if (interactionContext == null) {
            val defaultText = keys.foldIndexed(text) { index, acc, keyValue ->
                acc.replace("<${index + 1}>", keyValue.default)
            }
            return context.klass.cast(defaultText)
        }

        val replacedText = keys.foldIndexed(text) { index, acc, keyValue ->
            val value = interactionContext[keyValue.key]?.toString() ?: keyValue.default
            acc.replace("<${index + 1}>", value)
        }
        return context.klass.cast(replacedText)
    }
}

private data class InteractionContextTextVariableData(
    val keys: List<TextKeyValue> = emptyList(),
    @Colored
    @Placeholder
    @Help("使用 <1> 插入第一个变量的文本")
    val text: String = "",
)

private data class TextKeyValue(
    @IgnoreContextKeyBlueprint
    val key: InteractionContextKey<*> = InteractionContextKey.Empty,
    @Help("当键不在上下文中时显示的内容")
    val default: String = "",
)