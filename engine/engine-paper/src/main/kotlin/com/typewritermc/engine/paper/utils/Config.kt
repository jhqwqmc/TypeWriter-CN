package com.typewritermc.engine.paper.utils

import com.typewritermc.engine.paper.logger
import com.typewritermc.engine.paper.plugin
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.safeCast


inline fun <reified T : Any> config(key: String, default: T, comment: String? = null) =
    ConfigPropertyDelegate(key, T::class, default, comment)

class ConfigPropertyDelegate<T : Any>(
    private val key: String,
    private val klass: KClass<T>,
    private val default: T,
    private val comments: String?,
) {
    operator fun getValue(thisRef: Nothing?, property: KProperty<*>): T {
        val value = plugin.config.get(key)
        if (value == null) {
            plugin.config.set(key, default)
            if (comments != null) {
                plugin.config.setComments(key, comments.lines())
            }
            plugin.saveConfig()
            return default
        }
        val t = klass.safeCast(value)
        if (t == null) {
            logger.warning("配置键 '$key' 的值无效，应为 ${klass.simpleName}，但得到 ${value::class.simpleName}")
            return default
        }
        return t
    }

    operator fun getValue(thisRef: Any, property: KProperty<*>): T = getValue(null, property)
}