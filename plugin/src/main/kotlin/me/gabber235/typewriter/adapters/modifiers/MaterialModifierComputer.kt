package me.gabber235.typewriter.adapters.modifiers

import me.gabber235.typewriter.adapters.CustomField
import me.gabber235.typewriter.adapters.FieldInfo
import me.gabber235.typewriter.adapters.FieldModifier
import me.gabber235.typewriter.utils.failure
import me.gabber235.typewriter.utils.ok

enum class MaterialProperty {
    ITEM,
    BLOCK,
    SOLID,
    TRANSPARENT,
    FLAMMABLE,
    BURNABLE,
    EDIBLE,
    FUEL,
    INTRACTABLE,
    OCCLUDING,
    RECORD,
    TOOL,
    WEAPON,
    ARMOR,
    ORE,
}

@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.PROPERTY)
annotation class MaterialProperties(vararg val properties: MaterialProperty)


object MaterialPropertiesModifierComputer : StaticModifierComputer<MaterialProperties> {
    override val annotationClass: Class<MaterialProperties> = MaterialProperties::class.java

    override fun computeModifier(annotation: MaterialProperties, info: FieldInfo): Result<FieldModifier?> {
        // If the field is wrapped in a list or other container, we try if the inner type can be modified
        innerCompute(annotation, info)?.let { return ok(it) }

        if (info !is CustomField) {
            return failure("材料属性注释只能用于自定义字段")
        }
        if (info.editor != "material") {
            return failure("MaterialProperties 注解只能用于材质（不能在 ${info.editor} 上使用）")
        }

        return ok(FieldModifier.DynamicModifier(
            "material_properties",
            annotation.properties.joinToString(";") { it.name.lowercase() }
        ))
    }
}