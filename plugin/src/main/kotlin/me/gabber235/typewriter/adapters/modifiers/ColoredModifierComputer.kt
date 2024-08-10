package me.gabber235.typewriter.adapters.modifiers

import me.gabber235.typewriter.adapters.FieldInfo
import me.gabber235.typewriter.adapters.FieldModifier
import me.gabber235.typewriter.adapters.PrimitiveField
import me.gabber235.typewriter.adapters.PrimitiveFieldType
import me.gabber235.typewriter.utils.failure
import me.gabber235.typewriter.utils.ok

@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER)
annotation class Colored

object ColoredModifierComputer : StaticModifierComputer<Colored> {
    override val annotationClass: Class<Colored> = Colored::class.java

    override fun computeModifier(annotation: Colored, info: FieldInfo): Result<FieldModifier?> {
        // If the field is wrapped in a list or other container, we try if the inner type can be modified
        innerCompute(annotation, info)?.let { return ok(it) }

        if (info !is PrimitiveField) {
            return failure("彩色注释只能用于字符串（包括列表或映射）！")
        }

        if (info.type != PrimitiveFieldType.STRING) {
            return failure("彩色注释只能用于字符串（包括列表或映射）！")
        }

        return ok(FieldModifier.StaticModifier("colored"))
    }
}