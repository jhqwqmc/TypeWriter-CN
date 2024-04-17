package me.gabber235.typewriter.adapters.modifiers

import me.gabber235.typewriter.adapters.FieldInfo
import me.gabber235.typewriter.adapters.FieldModifier
import me.gabber235.typewriter.adapters.PrimitiveField
import me.gabber235.typewriter.adapters.PrimitiveFieldType
import me.gabber235.typewriter.logger

@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER)
annotation class Regex
object RegexModifierComputer : StaticModifierComputer<Regex> {
    override val annotationClass: Class<Regex> = Regex::class.java

    override fun computeModifier(annotation: Regex, info: FieldInfo): FieldModifier {
        // If the field is wrapped in a list or other container, we try if the inner type can be modified
        innerCompute(annotation, info)?.let { return it }

        if (info !is PrimitiveField) {
            logger.warning("正则表达式注释只能用于字符串（包括列表或映射）！")
            return FieldModifier.StaticModifier("regex")
        }

        if (info.type != PrimitiveFieldType.STRING) {
            logger.warning("正则表达式注释只能用于字符串（包括列表或映射）！")
            return FieldModifier.StaticModifier("regex")
        }

        return FieldModifier.StaticModifier("regex")
    }
}