package me.gabber235.typewriter.adapters.modifiers

import me.gabber235.typewriter.adapters.FieldInfo
import me.gabber235.typewriter.adapters.FieldModifier
import me.gabber235.typewriter.capture.Capturer
import me.gabber235.typewriter.capture.CapturerCreator
import me.gabber235.typewriter.logger
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.jvmName

@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.PROPERTY)
annotation class Capture(val capturer: KClass<out Capturer<*>>)

object CaptureModifierComputer : StaticModifierComputer<Capture> {
    override val annotationClass: Class<Capture> = Capture::class.java

    override fun computeModifier(annotation: Capture, info: FieldInfo): FieldModifier? {
        val capturer = annotation.capturer
        val name = capturer.qualifiedName

        if (name == null) {
            logger.warning("捕获器 ${capturer.jvmName} 没有限定名称！ 它必须是非本地非匿名类。")
            return null
        }

        if (capturer.companionObject == null) {
            logger.warning("捕获器 ${capturer.jvmName} 需要有一个扩展 CapturerCreator<${capturer.simpleName}> 的伴生对象！ 它没有伴生对象。")
            return null
        }

        if (capturer.companionObject?.isSubclassOf(CapturerCreator::class) != true) {
            logger.warning("捕获器 ${capturer.jvmName} 需要有一个扩展 CapturerCreator<${capturer.simpleName}> 的伴生对象！ 忘记扩展 CapturerCreator？")
            return null
        }

        return FieldModifier.DynamicModifier("capture", name)
    }
}