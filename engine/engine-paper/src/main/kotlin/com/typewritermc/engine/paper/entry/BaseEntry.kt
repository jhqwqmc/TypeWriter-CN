package com.typewritermc.engine.paper.entry

import com.google.gson.annotations.SerializedName
import com.typewritermc.core.entries.Entry
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.entries.emptyRef
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.extension.annotations.Negative
import com.typewritermc.core.extension.annotations.Tags
import com.typewritermc.engine.paper.entry.entries.ReadableFactEntry
import com.typewritermc.engine.paper.entry.entries.WritableFactEntry
import com.typewritermc.engine.paper.facts.FactData
import org.bukkit.entity.Player

@Tags("static")
interface StaticEntry : Entry

@Tags("manifest")
interface ManifestEntry : Entry


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

infix fun Iterable<Criteria>.matches(player: Player): Boolean = all {
    val entry = it.fact.get()
    val fact = entry?.readForPlayersGroup(player)
    it.isValid(fact)
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
