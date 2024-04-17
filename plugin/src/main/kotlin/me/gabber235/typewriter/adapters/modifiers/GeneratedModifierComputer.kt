package me.gabber235.typewriter.adapters.modifiers

import me.gabber235.typewriter.adapters.FieldInfo
import me.gabber235.typewriter.adapters.FieldModifier
import me.gabber235.typewriter.adapters.PrimitiveField
import me.gabber235.typewriter.adapters.PrimitiveFieldType
import me.gabber235.typewriter.logger

@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER)
annotation class Generated

object GeneratedModifierComputer : StaticModifierComputer<Generated> {
    override val annotationClass: Class<Generated> = Generated::class.java

    override fun computeModifier(annotation: Generated, info: FieldInfo): FieldModifier? {
        // If the field is wrapped in a list or other container we try if the inner type can be modified
        innerCompute(annotation, info)?.let { return it }

        if (info !is PrimitiveField) {
            logger.warning("生成的注释只能用于字符串（包括列表或映射）！")
            return null
        }

        if (info.type != PrimitiveFieldType.STRING) {
            logger.warning("生成的注释只能用于字符串（包括列表或映射）！")
            return null
        }

        return FieldModifier.StaticModifier("generated")
    }
}