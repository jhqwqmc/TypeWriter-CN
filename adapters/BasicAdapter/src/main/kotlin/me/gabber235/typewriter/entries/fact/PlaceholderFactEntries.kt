package me.gabber235.typewriter.entries.fact

import me.gabber235.typewriter.adapters.Colors
import me.gabber235.typewriter.adapters.Entry
import me.gabber235.typewriter.adapters.modifiers.Help
import me.gabber235.typewriter.adapters.modifiers.Placeholder
import me.gabber235.typewriter.adapters.modifiers.Regex
import me.gabber235.typewriter.entry.Ref
import me.gabber235.typewriter.entry.emptyRef
import me.gabber235.typewriter.entry.entries.GroupEntry
import me.gabber235.typewriter.entry.entries.ReadableFactEntry
import me.gabber235.typewriter.extensions.placeholderapi.isPlaceholder
import me.gabber235.typewriter.extensions.placeholderapi.parsePlaceholders
import me.gabber235.typewriter.facts.FactData
import me.gabber235.typewriter.logger
import org.bukkit.entity.Player

@Entry("number_placeholder", "占位符数字的计算变量", Colors.PURPLE, "ph:placeholder-fill")
/**
 * A [fact](/docs/creating-stories/facts) that is computed from a placeholder.
 * This placeholder is evaluated when the fact is read and must return a number or boolean.
 *
 * <fields.ReadonlyFactInfo />
 *
 * ## How could this be used?
 *
 * Make sure the player has a high enough level.
 * Then allow them to start a quest or enter a dungeon.
 */
class NumberPlaceholderFactEntry(
    override val id: String = "",
    override val name: String = "",
    override val comment: String = "",
    override val group: Ref<GroupEntry> = emptyRef(),
    @Placeholder
    @Help("要解析的占位符（例如 %player_level%） - 仅支持返回数字或布尔值的占位符！")
    /**
     * The placeholder to parse.
     * For example %player_level%.
     *
     * <Admonition type="caution">
     *      Only placeholders that return a number or boolean are supported!
     *      If you want to use a placeholder that returns a string,
     *      use the <Link to='value_placeholder'>ValuePlaceholderFactEntry</Link> instead.
     * </Admonition>
     */
    private val placeholder: String = "",
) : ReadableFactEntry {
    override fun readSinglePlayer(player: Player): FactData {
        if (!placeholder.isPlaceholder) {
            logger.warning("占位符“$placeholder”不是有效的占位符！ 确保它只是一个以 % 开头和结尾的占位符")
            return FactData(0)
        }
        val value = placeholder.parsePlaceholders(player)
        return FactData(value.toIntOrNull() ?: value.toBooleanStrictOrNull()?.toInt() ?: 0)
    }
}

fun Boolean.toInt() = if (this) 1 else 0

@Entry("value_placeholder", "占位符值的变量", Colors.PURPLE, "fa6-solid:user-tag")
/**
 * A [fact](/docs/creating-stories/facts) that is computed from a placeholder.
 * This placeholder is evaluated when the fact is read and can return anything.
 * The value will be computed based on the `values` specified.
 * <fields.ReadonlyFactInfo/>
 *
 * ## How could this be used?
 *
 * If you only want to run certain actions if the player is in creative mode.
 * Or depending on the weather, change the dialogue of the NPC.
 */
class ValuePlaceholderFactEntry(
    override val id: String = "",
    override val name: String = "",
    override val comment: String = "",
    override val group: Ref<GroupEntry> = emptyRef(),
    @Placeholder
    @Help("要解析的占位符（例如％player_gamemode％）")
    private val placeholder: String = "",
    @Regex
    @Help("与占位符匹配的值及其相应的变量值。 支持正则表达式。")
    /**
     * The values to match the placeholder with and their corresponding fact value.
     *
     * An example would be:
     * ```yaml
     * values:
     *  SURVIVAL: 0
     *  CREATIVE: 1
     *  ADVENTURE: 2
     *  SPECTATOR: 3
     * ```
     * If the placeholder returns `CREATIVE`, the fact value will be `1`.
     * If no value matches, the fact value will be `0`.
     *
     * Values can have placeholders inside them.
     */
    private val values: Map<String, Int> = mapOf()
) : ReadableFactEntry {
    override fun readSinglePlayer(player: Player): FactData {
        if (!placeholder.isPlaceholder) {
            logger.warning("占位符“$placeholder”不是有效的占位符！ 确保它只是一个以 % 开头和结尾的占位符")
            return FactData(0)
        }
        val parsed = placeholder.parsePlaceholders(player)
        val value = values[parsed] ?: values.entries.firstOrNull { it.key.parsePlaceholders(player).toRegex().matches(parsed) }?.value ?: 0
        return FactData(value)
    }
}