package me.gabber235.typewriter.adapters.modifiers

import me.gabber235.typewriter.adapters.FieldInfo
import me.gabber235.typewriter.adapters.FieldModifier
import me.gabber235.typewriter.adapters.PrimitiveField
import me.gabber235.typewriter.adapters.PrimitiveFieldType
import me.gabber235.typewriter.entry.PageType
import me.gabber235.typewriter.utils.failure
import me.gabber235.typewriter.utils.ok

@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class Page(val type: PageType)

object PageModifierComputer : StaticModifierComputer<Page> {
    override val annotationClass: Class<Page> = Page::class.java

    override fun computeModifier(annotation: Page, info: FieldInfo): Result<FieldModifier?> {
        innerCompute(annotation, info)?.let { return ok(it) }

        if (info !is PrimitiveField) {
            return failure("页面注释只能用在原始字段上")
        }

        if (info.type != PrimitiveFieldType.STRING) {
            return failure("页面注释只能用于字符串字段")
        }

        return ok(FieldModifier.DynamicModifier("page", annotation.type.id))
    }
}