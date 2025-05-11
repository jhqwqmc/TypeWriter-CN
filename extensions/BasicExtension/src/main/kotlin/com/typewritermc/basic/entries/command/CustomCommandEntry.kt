package com.typewritermc.basic.entries.command

import com.mojang.brigadier.arguments.ArgumentType
import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.extension.annotations.Tags
import com.typewritermc.core.interaction.InteractionContextBuilder
import com.typewritermc.engine.paper.command.dsl.CommandTree
import com.typewritermc.engine.paper.command.dsl.DslCommand
import com.typewritermc.engine.paper.command.dsl.ExecutionContext
import com.typewritermc.engine.paper.command.dsl.requiresPlayer
import com.typewritermc.engine.paper.command.dsl.sender
import com.typewritermc.engine.paper.entry.ManifestEntry
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entries.CustomCommandEntry
import com.typewritermc.engine.paper.entry.triggerEntriesFor
import com.typewritermc.engine.paper.utils.msg
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.entity.Player
import kotlin.reflect.KClass

@Entry(
    "custom_command",
    "创建带参数的自定义命令",
    Colors.YELLOW,
    "heroicons:command-line-16-solid"
)
/**
 * The `Custom Command` entry is used to create custom commands with parameters.
 *
 * It allows you to create highly customizable commands with different parameters.
 * Parameters will be passed as context to any triggers.
 *
 * ## How could this be used?
 */
class CustomCommandEntry(
    override val id: String = "",
    override val name: String = "",
    @Help("要注册的命令（不要包含开头的斜杠）")
    val command: String = "",
    val aliases: List<String> = emptyList(),
    val arguments: List<Ref<CommandArgumentEntry>> = emptyList(),
    val triggers: List<Ref<TriggerableEntry>> = emptyList(),
) : ManifestEntry, CustomCommandEntry {
    @Suppress("UnstableApiUsage")
    override fun command(): DslCommand<CommandSourceStack> = com.typewritermc.engine.paper.command.dsl.command(command, *aliases.toTypedArray()) {
        arguments.applyTo(this, CustomCommandArgumentBuilder(emptyList()))
        triggers.applyTo(this, CustomCommandArgumentBuilder(emptyList()))
    }
}

typealias ContextBuilder = InteractionContextBuilder.(ExecutionContext<CommandSourceStack>) -> Unit
class CustomCommandArgumentBuilder(
    val contextBuilders: List<ContextBuilder> = emptyList(),
) {
    fun copyWith(contextBuilder: ContextBuilder): CustomCommandArgumentBuilder {
        return CustomCommandArgumentBuilder(contextBuilders + contextBuilder)
    }

    fun apply(builder: InteractionContextBuilder, context: ExecutionContext<CommandSourceStack>) {
        contextBuilders.forEach { it(builder, context) }
    }
}

@Tags("command_argument")
sealed interface CommandArgumentEntry : ManifestEntry {
    val children: List<Ref<CommandArgumentEntry>>
    val triggers: List<Ref<TriggerableEntry>>

    fun build(tree: CommandTree, builder: CustomCommandArgumentBuilder)
}

@Entry("literal_command_argument", "字面命令参数", Colors.BLUE, "material-symbols:format-color-text-rounded")
/**
 * The `Literal Command Argument` entry is a command argument that is a literal.
 *
 * The name of the entry is used as the literal.
 */
class LiteralCommandArgument(
    override val id: String = "",
    override val name: String = "",
    override val children: List<Ref<CommandArgumentEntry>> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
) : CommandArgumentEntry {
    override fun build(tree: CommandTree, builder: CustomCommandArgumentBuilder) {
        tree.literal(name) {
            requiresPlayer()
            children.applyTo(this, builder)
            triggers.applyTo(this, builder)
        }
    }
}

interface ArgumentCommandArgument<T : Any> : CommandArgumentEntry {
    val type: ArgumentType<T>
    val klass: KClass<T>

    override fun build(tree: CommandTree, builder: CustomCommandArgumentBuilder) {
        tree.argument(name, type, klass) {
            val builder = builder.copyWith {
                val value = it.getArgument<T>(name, klass)
                this.apply(value)
            }
            requiresPlayer()
            children.applyTo(this, builder)
            triggers.applyTo(this, builder)
        }
    }

    fun InteractionContextBuilder.apply(value: T)
}

@JvmName("applyCommandArgumentsToTree")
fun List<Ref<CommandArgumentEntry>>.applyTo(
    tree: CommandTree,
    builder: CustomCommandArgumentBuilder,
) {
    forEach { it.get()?.build(tree, builder) }
}

@JvmName("applyTriggersToTree")
fun List<Ref<TriggerableEntry>>.applyTo(
    tree: CommandTree,
    builder: CustomCommandArgumentBuilder,
) {
    if (isEmpty()) return
    tree.executes {
        val player = (source.executor as? Player) ?: (sender as? Player)
        if (player == null) {
            sender.msg("执行此命令必须是玩家身份")
            return@executes
        }
        val interactionContextBuilder = InteractionContextBuilder()
        builder.apply(interactionContextBuilder, this)
        val context = interactionContextBuilder.build()
        this@applyTo.triggerEntriesFor(player, context)
    }
}