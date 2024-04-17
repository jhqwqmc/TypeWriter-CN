package me.gabber235.typewriter.adapters.modifiers

import me.gabber235.typewriter.adapters.FieldInfo
import me.gabber235.typewriter.adapters.FieldModifier
import me.gabber235.typewriter.adapters.PrimitiveField
import me.gabber235.typewriter.adapters.PrimitiveFieldType
import me.gabber235.typewriter.logger

@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER)
annotation class Colored

object ColoredModifierComputer : StaticModifierComputer<Colored> {
    override val annotationClass: Class<Colored> = Colored::class.java

    override fun computeModifier(annotation: Colored, info: FieldInfo): FieldModifier {
        // If the field is wrapped in a list or other container we try if the inner type can be modified
        innerCompute(annotation, info)?.let { return it }

        if (info !is PrimitiveField) {
            logger.warning("彩色注释只能用于字符串（包括列表或映射）！")
            return FieldModifier.StaticModifier("colored")
        }

        if (info.type != PrimitiveFieldType.STRING) {
            logger.warning("彩色注释只能用于字符串（包括列表或映射）！")
            return FieldModifier.StaticModifier("colored")
        }

        return FieldModifier.StaticModifier("colored")
    }
}