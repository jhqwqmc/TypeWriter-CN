package me.gabber235.typewriter.adapters.modifiers

import me.gabber235.typewriter.adapters.CustomField
import me.gabber235.typewriter.adapters.FieldInfo
import me.gabber235.typewriter.adapters.FieldModifier
import me.gabber235.typewriter.logger

@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER)
annotation class WithRotation

object WithRotationModifierComputer : StaticModifierComputer<WithRotation> {
    override val annotationClass: Class<WithRotation> = WithRotation::class.java

    override fun computeModifier(annotation: WithRotation, info: FieldInfo): FieldModifier? {
        // If the field is wrapped in a list or other container we try if the inner type can be modified
        innerCompute(annotation, info)?.let { return it }

        if (info !is CustomField) {
            logger.warning("WithRotation 注释只能用于位置（包括列表或映射）！")
            return null
        }
        if (info.editor != "location") {
            logger.warning("WithRotation 注释只能用于位置（包括列表或映射）！")
            return null
        }

        return FieldModifier.StaticModifier("with_rotation")
    }
}