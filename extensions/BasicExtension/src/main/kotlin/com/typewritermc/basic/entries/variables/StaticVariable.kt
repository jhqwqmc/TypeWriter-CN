package com.typewritermc.basic.entries.variables

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.utils.Generic
import com.typewritermc.engine.paper.entry.entries.VarContext
import com.typewritermc.engine.paper.entry.entries.VariableEntry

@Entry(
    "static_variable",
    "具有单一静态值的变量",
    Colors.GREEN,
    "streamline:setting-line"
)
/**
 * The `Static Variable` entry is a variable that has a single static value.
 *
 * ## How could this be used?
 * If you have the same value for multiple entries, you can use this to avoid repeating the value and allow for easier changes.
 */
class StaticVariable(
    override val id: String = "",
    override val name: String = "",
    val value: Generic = Generic.Empty,
) : VariableEntry {
    override fun <T : Any> get(context: VarContext<T>): T {
        return value.get(context.klass)
            ?: throw IllegalStateException("找不到静态值：${value.data} 绑定到 ${context.klass.qualifiedName} 的值")
    }
}