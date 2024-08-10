package me.gabber235.typewriter.entry

import com.google.gson.annotations.SerializedName
import me.gabber235.typewriter.adapters.Tags
import me.gabber235.typewriter.adapters.modifiers.Help
import me.gabber235.typewriter.adapters.modifiers.Negative
import me.gabber235.typewriter.entry.entries.ReadableFactEntry
import me.gabber235.typewriter.entry.entries.WritableFactEntry
import me.gabber235.typewriter.facts.FactData
import org.bukkit.entity.Player
import java.util.*

val Entry.formattedName: String
    get() = name.split(".")
        .joinToString(" | ") { part -> part.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() } }
        .split("_")
        .joinToString(" ") { part -> part.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() } }

interface Entry {
    val id: String
    val name: String
}

interface PriorityEntry : Entry {
    /**
     * Normally, the priority of an entry is determined by the priority of the page it is on.
     * Subtypes may want to allow the user to override the priority for that specific entry.
     * This is useful when entries need to have fine-grained control over their priority.
     */
    @Help("条目的优先级。如果未设置，则将使用页面的优先级。")
    val priorityOverride: Optional<Int>
}

@Tags("static")
interface StaticEntry : Entry

@Tags("manifest")
interface ManifestEntry : Entry

@Tags("trigger")
interface TriggerEntry : Entry {
    @Help("在此条目之后将被触发的条目。")
    val triggers: List<Ref<TriggerableEntry>>
}

@Tags("triggerable")
interface TriggerableEntry : TriggerEntry {
    @Help("触发此条目之前必须满足的条件")
    val criteria: List<Criteria>

    @Help("触发此条目时将应用的修饰符")
    val modifiers: List<Modifier>
}

@Tags("placeholder")
interface PlaceholderEntry : Entry {
    fun display(player: Player?): String?
}

enum class CriteriaOperator {
    @SerializedName("==")
    EQUALS,

    @SerializedName("<")
    LESS_THAN,

    @SerializedName(">")
    GREATER_THAN,

    @SerializedName("<=")
    LESS_THAN_OR_EQUALS,

    @SerializedName(">=")
    GREATER_THAN_OR_EQUAL,

    @SerializedName("!=")
    NOT_EQUALS

    ;

    fun isValid(value: Double, criteria: Double): Boolean {
        return when (this) {
            EQUALS -> value == criteria
            LESS_THAN -> value < criteria
            GREATER_THAN -> value > criteria
            LESS_THAN_OR_EQUALS -> value <= criteria
            GREATER_THAN_OR_EQUAL -> value >= criteria
            NOT_EQUALS -> value != criteria
        }
    }
}

data class Criteria(
    @Help("在触发该条目之前要检查的变量")
    val fact: Ref<ReadableFactEntry> = emptyRef(),
    @Help("将变量值与条件值进行比较时要使用的运算符")
    val operator: CriteriaOperator = CriteriaOperator.EQUALS,
    @Help("与变量值进行比较的值")
    @Negative
    val value: Int = 0,
) {
    fun isValid(fact: FactData?): Boolean {
        val value = fact?.value ?: 0
        return operator.isValid(value.toDouble(), this.value.toDouble())
    }
}

enum class ModifierOperator {
    @SerializedName("=")
    SET,

    @SerializedName("+")
    ADD;
}

data class Modifier(
    @Help("触发条目时要修改的变量")
    val fact: Ref<WritableFactEntry> = emptyRef(),
    @Help("修改变量值的时使用的运算符")
    val operator: ModifierOperator = ModifierOperator.ADD,
    @Help("修改变量值的值")
    @Negative
    val value: Int = 0,
)
