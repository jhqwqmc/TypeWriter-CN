package me.gabber235.typewriter.adapters.modifiers

import me.gabber235.typewriter.adapters.FieldInfo
import me.gabber235.typewriter.adapters.FieldModifier
import me.gabber235.typewriter.adapters.FieldModifier.StaticModifier
import me.gabber235.typewriter.adapters.PrimitiveField
import me.gabber235.typewriter.adapters.PrimitiveFieldType
import me.gabber235.typewriter.utils.failure
import me.gabber235.typewriter.utils.ok

@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.PROPERTY)
annotation class MultiLine

object MultiLineModifierComputer : StaticModifierComputer<MultiLine> {
    override val annotationClass: Class<MultiLine> = MultiLine::class.java

    override fun computeModifier(annotation: MultiLine, info: FieldInfo): Result<FieldModifier?> {
        // If the field is wrapped in a list or other container, we try if the inner type can be modified
        innerCompute(annotation, info)?.let { return ok(it) }

        if (info !is PrimitiveField) {
            return failure("MultiLine 注释只能用于字符串")
        }
        if (info.type != PrimitiveFieldType.STRING) {
            return failure("MultiLine 注释只能用于字符串")
        }

        return ok(StaticModifier("multiline"))
    }
}