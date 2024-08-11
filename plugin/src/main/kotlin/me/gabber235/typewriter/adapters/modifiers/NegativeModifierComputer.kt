package me.gabber235.typewriter.adapters.modifiers

import me.gabber235.typewriter.adapters.FieldInfo
import me.gabber235.typewriter.adapters.FieldModifier
import me.gabber235.typewriter.adapters.PrimitiveField
import me.gabber235.typewriter.adapters.PrimitiveFieldType
import me.gabber235.typewriter.utils.failure
import me.gabber235.typewriter.utils.ok

@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER)
annotation class Negative

object NegativeModifierComputer : StaticModifierComputer<Negative> {
    override val annotationClass: Class<Negative> = Negative::class.java

    override fun computeModifier(annotation: Negative, info: FieldInfo): Result<FieldModifier?> {
        // If the field is wrapped in a list or other container, we try if the inner type can be modified
        innerCompute(annotation, info)?.let { return ok(it) }

        if (info !is PrimitiveField) {
            return failure("负注解只能用于数字（包括列表或映射中的数字）！")
        }

        if (info.type != PrimitiveFieldType.INTEGER && info.type != PrimitiveFieldType.DOUBLE) {
            return failure("负注解只能用于数字（包括列表或映射中的数字）！")
        }

        return ok(FieldModifier.StaticModifier("negative"))
    }
}