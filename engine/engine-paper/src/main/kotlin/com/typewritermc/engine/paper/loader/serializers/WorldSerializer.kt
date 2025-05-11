package com.typewritermc.engine.paper.loader.serializers

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.typewritermc.core.utils.point.World
import com.typewritermc.core.serialization.DataSerializer
import com.typewritermc.engine.paper.utils.logErrorIfNull
import lirand.api.extensions.server.server
import java.lang.reflect.Type

class WorldSerializer : DataSerializer<World> {
    override val type: Type = World::class.java

    override fun serialize(src: World, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        return JsonPrimitive(src.identifier)
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): World {
        val world = json.asString

        val bukkitWorld = server.getWorld(world)
            ?: server.worlds.firstOrNull { it.name.equals(world, true) }
                .logErrorIfNull("找不到标识符为'$world'的世界，可选世界: ${server.worlds.map { it.name }}。将选择${server.worlds.firstOrNull()?.name}作为默认值。")
            ?: server.worlds.firstOrNull()
            ?: throw IllegalArgumentException("找不到位置对应的世界'$world'，且无默认世界可用。")

        return World(bukkitWorld.uid.toString())
    }
}