package me.gabber235.typewriter.entry

import com.google.gson.annotations.SerializedName
import me.gabber235.typewriter.adapters.Tags
import me.gabber235.typewriter.adapters.modifiers.*
import me.gabber235.typewriter.entry.entries.*
import me.gabber235.typewriter.facts.Fact

interface Entry {
	val id: String
	val name: String
}

@Tags("static")
interface StaticEntry : Entry

@Tags("trigger")
interface TriggerEntry : Entry {
	@Triggers
	@EntryIdentifier(TriggerableEntry::class)
	@Help("在此条目之后将被激发的条目。")
	val triggers: List<String>
}

@Tags("triggerable")
interface TriggerableEntry : TriggerEntry {
	@Help("触发此条目之前必须满足的条件")
	val criteria: List<Criteria>

	@Help("触发此条目时将应用的修饰符")
	val modifiers: List<Modifier>
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
}

data class Criteria(
	@Help("在触发该条目之前要检查的变量。")
	@EntryIdentifier(ReadableFactEntry::class)
	val fact: String = "",
	@Help("将变量的值与标准值进行比较时使用的运算符")
	val operator: CriteriaOperator = CriteriaOperator.EQUALS,
	@Help("与变量的值进行比较的值")
	val value: Int = 0,
) {
	fun isValid(fact: Fact?): Boolean {
		val value = fact?.value ?: 0
		return when (operator) {
			CriteriaOperator.EQUALS                -> value == this.value
			CriteriaOperator.LESS_THAN             -> value < this.value
			CriteriaOperator.GREATER_THAN          -> value > this.value
			CriteriaOperator.LESS_THAN_OR_EQUALS   -> value <= this.value
			CriteriaOperator.GREATER_THAN_OR_EQUAL -> value >= this.value
		}
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
	@EntryIdentifier(WritableFactEntry::class)
	val fact: String = "",
	@Help("修改变量的值的时使用的运算符")
	val operator: ModifierOperator = ModifierOperator.ADD,
	@Help("修改变量的值")
	val value: Int = 0,
)
