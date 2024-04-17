package me.gabber235.typewriter.adapters.modifiers

import com.google.gson.JsonObject
import me.gabber235.typewriter.adapters.*
import me.gabber235.typewriter.adapters.FieldModifier.DynamicModifier
import me.gabber235.typewriter.adapters.FieldModifier.InnerListModifier
import me.gabber235.typewriter.logger
import me.gabber235.typewriter.utils.Icons

@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class Segments(val color: String = Colors.CYAN, val icon: Icons = Icons.STAR)

object SegmentModifierComputer : StaticModifierComputer<Segments> {
    override val annotationClass: Class<Segments> = Segments::class.java

    override fun computeModifier(annotation: Segments, info: FieldInfo): FieldModifier? {
        if (info !is ListField) {
            logger.warning("段注释只能用在列表上")
            return null
        }
        val type = info.type
        if (type !is ObjectField) {
            logger.warning("段注释只能用在对象列表上")
            return null
        }
        val startFrame = type.fields["startFrame"]
        val endFrame = type.fields["endFrame"]
        if (startFrame !is PrimitiveField || endFrame !is PrimitiveField) {
            logger.warning("段注释只能用于具有 startFrame 和 endFrame 字段的对象列表")
            return null
        }

        if (startFrame.type != PrimitiveFieldType.INTEGER || endFrame.type != PrimitiveFieldType.INTEGER) {
            logger.warning("段注释只能用在具有 int 类型的 startFrame 和 endFrame 字段的对象列表上")
            return null
        }

        val color = annotation.color
        val icon = annotation.icon

        val data = JsonObject().apply {
            addProperty("color", color)
            addProperty("icon", icon.id)
        }

        return InnerListModifier(DynamicModifier("segment", data))
    }
}

