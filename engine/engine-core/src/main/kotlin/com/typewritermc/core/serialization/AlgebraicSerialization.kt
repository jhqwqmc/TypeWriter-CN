package com.typewritermc.core.serialization

import com.google.gson.*
import com.google.gson.internal.Streams
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.typewritermc.core.extension.annotations.AlgebraicTypeInfo
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotations

class AlgebraicSerializationFactory : TypeAdapterFactory {
    override fun <T : Any> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {
        val klass = type.rawType.kotlin as? KClass<T> ?: return null
        if (!klass.isSealed) return null
        if (!klass.java.isInterface) return null
        return AlgebraicTypeAdapter(gson, klass, klass.sealedSubclasses)
    }
}

private class AlgebraicTypeAdapter<T : Any>(
    private val gson: Gson,
    private val klass: KClass<T>,
    private val subclasses: List<KClass<out T>>,
) : TypeAdapter<T>() {
    override fun write(out: JsonWriter, value: T) {
        out.beginObject()
        out.name("case")
        val klass = value::class
        if (klass !in subclasses) {
            throw IllegalArgumentException("${klass.qualifiedName} 类型的值不是 ${this.klass.qualifiedName} 的有效值")
        }
        val name = klass.typeName
        out.value(name)
        out.name("value")
        Streams.write(gson.toJsonTree(value), out)
        out.endObject()
    }

    override fun read(`in`: JsonReader): T {
        val element = Streams.parse(`in`)
        if (element !is JsonObject) throw IllegalArgumentException("预期是 JsonObject 但得到的是 ${element::class.qualifiedName}")
        val case = element.get("case")
            ?: throw IllegalArgumentException("预期是 case 但得到的是 null（在 $element 中）")
        if (case !is JsonPrimitive) throw IllegalArgumentException("预期是 JsonPrimitive 但得到的是 ${case::class.qualifiedName}")
        val name = case.asString
        val klass = subclasses.firstOrNull { it.typeName == name }
            ?: throw IllegalArgumentException("找不到 $name 的子类")
        val value = element.get("value") ?: throw IllegalArgumentException("预期有值但得到的是 null")
        return gson.fromJson(value, klass.java)
    }

    private val KClass<*>.typeName: String
        get() {
            return findAnnotations(AlgebraicTypeInfo::class).firstOrNull()?.name
                ?: throw IllegalArgumentException("找不到 ${this.qualifiedName} 的 `@AlgebraicTypeInfo` 注解")
        }
}