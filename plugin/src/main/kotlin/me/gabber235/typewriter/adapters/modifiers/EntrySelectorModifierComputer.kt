package me.gabber235.typewriter.adapters.modifiers

import me.gabber235.typewriter.adapters.*
import me.gabber235.typewriter.adapters.FieldModifier.DynamicModifier
import me.gabber235.typewriter.entry.Entry
import me.gabber235.typewriter.logger
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.PROPERTY)
annotation class EntryIdentifier(val entry: KClass<out Entry>)


object EntrySelectorModifierComputer : StaticModifierComputer<EntryIdentifier> {
    override val annotationClass: Class<EntryIdentifier> = EntryIdentifier::class.java

    override fun computeModifier(annotation: EntryIdentifier, info: FieldInfo): FieldModifier? {
        // If the field is wrapped in a list or other container we try if the inner type can be modified
        innerComputeForMap(annotation, info)?.let { return it }
        innerComputeForMap(annotation, info)?.let { return it }

        val entry = annotation.entry
        // Get the tag from the entry with annotation @Tags
        val tag = entry.annotations.find { it is Tags }?.let { (it as Tags).tags.firstOrNull() }
        if (tag == null) {
            logger.warning("警告：条目 ${entry.simpleName} 没有标签。 StaticEntryIdentifier 需要它")
            return null
        }

        // If it is a list we want to add a modifier to the list itself so the interface knows
        // that it should be a list of entries and allow the user to select multiple entries
        innerComputeForList(annotation, info)?.let {
            return FieldModifier.MultiModifier(it, DynamicModifier("entry-list", tag))
        }

        if (info !is PrimitiveField) {
            logger.warning("StaticEntryIdentifier 只能用于字符串字段")
            return null
        }
        if (info.type != PrimitiveFieldType.STRING) {
            logger.warning("StaticEntryIdentifier 只能用于字符串字段")
            return null
        }

        return DynamicModifier("entry", tag)
    }
}