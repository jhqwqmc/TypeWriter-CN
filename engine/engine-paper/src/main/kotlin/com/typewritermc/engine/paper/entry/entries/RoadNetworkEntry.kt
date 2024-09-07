package com.typewritermc.engine.paper.entry.entries

import com.github.retrooper.packetevents.util.Vector3f
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.typewritermc.core.entries.Entry
import com.typewritermc.core.entries.Query
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.entries.ref
import com.typewritermc.core.extension.annotations.ContentEditor
import com.typewritermc.core.extension.annotations.Tags
import com.typewritermc.core.utils.failure
import com.typewritermc.core.utils.ok
import com.typewritermc.core.utils.point.World
import com.typewritermc.engine.paper.content.ContentContext
import com.typewritermc.engine.paper.content.ContentMode
import com.typewritermc.engine.paper.content.components.bossBar
import com.typewritermc.engine.paper.content.components.exit
import com.typewritermc.engine.paper.content.components.nodes
import com.typewritermc.engine.paper.content.entryId
import com.typewritermc.engine.paper.content.fieldPath
import com.typewritermc.engine.paper.entry.fieldValue
import com.typewritermc.engine.paper.entry.roadnetwork.content.RoadNetworkEditorComponent
import com.typewritermc.engine.paper.entry.roadnetwork.content.material
import com.typewritermc.engine.paper.entry.triggerFor
import com.typewritermc.engine.paper.loader.serializers.WorldSerializer
import com.typewritermc.engine.paper.snippets.snippet
import com.typewritermc.engine.paper.utils.LocationSerializer
import com.typewritermc.engine.paper.utils.RuntimeTypeAdapterFactory
import com.typewritermc.engine.paper.utils.playSound
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

val roadNetworkMaxDistance by snippet("road_network.distance.max", 30.0)

@Tags("road-network")
interface RoadNetworkEntry : ArtifactEntry {
    suspend fun loadRoadNetwork(gson: Gson): RoadNetwork
    suspend fun saveRoadNetwork(gson: Gson, network: RoadNetwork)
}

@Tags("road-network-node")
interface RoadNodeEntry : Entry {
    val roadNetwork: Ref<RoadNetworkEntry>

    @ContentEditor(SelectRoadNodeContentMode::class)
    val nodeId: RoadNodeId
}

@Tags("road-network-node-collection")
interface RoadNodeCollectionEntry : Entry {
    val roadNetwork: Ref<RoadNetworkEntry>

    @ContentEditor(SelectRoadNodeCollectionContentMode::class)
    val nodes: List<RoadNodeId>
}

data class RoadNetwork(
    val nodes: List<RoadNode> = emptyList(),
    val edges: List<RoadEdge> = emptyList(),
    val modifications: List<RoadModification> = emptyList(),
    val negativeNodes: List<RoadNode> = emptyList(),
)

@JvmInline
value class RoadNodeId(val id: Int) {
    override fun toString(): String = id.toString()
}

data class RoadNode(
    val id: RoadNodeId,
    val location: Location,
    val radius: Double,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RoadNode

        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}

data class RoadEdge(
    val start: RoadNodeId,
    val end: RoadNodeId,
    val weight: Double,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RoadEdge

        if (start != other.start) return false
        if (end != other.end) return false

        return true
    }

    override fun hashCode(): Int {
        var result = start.hashCode()
        result = 31 * result + end.hashCode()
        return result
    }
}

fun Collection<RoadEdge>.containsEdge(start: RoadNodeId, end: RoadNodeId): Boolean =
    any { it.start == start && it.end == end }

sealed interface RoadModification {
    sealed interface EdgeModification : RoadModification {
        val start: RoadNodeId
        val end: RoadNodeId
    }

    data class EdgeAddition(
        override val start: RoadNodeId,
        override val end: RoadNodeId,
        val weight: Double
    ) :
        EdgeModification {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as EdgeAddition

            if (start != other.start) return false
            if (end != other.end) return false

            return true
        }

        override fun hashCode(): Int {
            var result = start.hashCode()
            result = 31 * result + end.hashCode()
            return result
        }
    }

    data class EdgeRemoval(override val start: RoadNodeId, override val end: RoadNodeId) :
        EdgeModification
}

fun Collection<RoadModification>.containsRemoval(start: RoadNodeId, end: RoadNodeId): Boolean =
    any { it is RoadModification.EdgeRemoval && it.start == start && it.end == end }

fun Collection<RoadModification>.containsAddition(start: RoadNodeId, end: RoadNodeId): Boolean =
    any { it is RoadModification.EdgeAddition && it.start == start && it.end == end }

fun createRoadNetworkParser(): Gson = GsonBuilder()
    .registerTypeAdapter(Location::class.java, LocationSerializer())
    .registerTypeAdapter(World::class.java, WorldSerializer())
    .registerTypeAdapterFactory(
        RuntimeTypeAdapterFactory.of(RoadModification::class.java)
            .registerSubtype(RoadModification.EdgeAddition::class.java)
            .registerSubtype(RoadModification.EdgeRemoval::class.java)
    )
    .create()

class SelectRoadNodeContentMode(context: ContentContext, player: Player) : ContentMode(context, player) {
    private lateinit var editorComponent: RoadNetworkEditorComponent
    private val network: RoadNetwork
        get() = editorComponent.network

    private var cycle = 0

    override suspend fun setup(): Result<Unit> {
        val fieldPath = context.fieldPath ?: return failure(Exception("未找到字段路径"))
        val entryId = context.entryId ?: return failure(Exception("未找到条目 ID"))

        val entry = Query.findById<RoadNodeEntry>(entryId)
            ?: return failure(Exception("未找到 ID 为 $entryId 的道路节点"))

        val roadNetworkRef = entry.roadNetwork

        if (!roadNetworkRef.isSet) {
            return failure("未找到与道路节点 $entryId 关联的 ID 为 ${entry.roadNetwork.id} 的道路网络")
        }

        editorComponent = RoadNetworkEditorComponent(roadNetworkRef)

        exit(doubleShiftExits = true)
        bossBar {
            title = "选择道路节点"
            color = BossBar.Color.WHITE
        }

        nodes({ network.nodes }, ::showingLocation) { node ->
            item = ItemStack(node.material(network.modifications))
            glow = NamedTextColor.WHITE
            scale = Vector3f(0.5f, 0.5f, 0.5f)

            onInteract {
                val value = node.id.id
                entry.ref().fieldValue(fieldPath, value)
                SystemTrigger.CONTENT_POP triggerFor player
            }
        }

        return ok(Unit)
    }

    override suspend fun tick() {
        super.tick()
        cycle++
    }

    private fun showingLocation(node: RoadNode): Location = node.location.clone().apply {
        yaw = (cycle % 360).toFloat()
    }
}

class SelectRoadNodeCollectionContentMode(context: ContentContext, player: Player) : ContentMode(context, player) {
    private lateinit var editorComponent: RoadNetworkEditorComponent
    private val network: RoadNetwork
        get() = editorComponent.network

    private var cycle = 0

    private var nodes: List<RoadNodeId> = emptyList()

    override suspend fun setup(): Result<Unit> {
        val fieldPath = context.fieldPath ?: return failure(Exception("未找到字段路径"))
        val entryId = context.entryId ?: return failure(Exception("未找到条目 ID"))

        val entry = Query.findById<RoadNodeCollectionEntry>(entryId)
            ?: return failure(Exception("未找到 ID 为 $entryId 的道路节点集合"))

        nodes = entry.nodes
        val ref = entry.ref()

        val roadNetworkRef = entry.roadNetwork

        if (!roadNetworkRef.isSet) {
            return failure("未找到与道路节点集合 $entryId 关联的 ID 为 ${entry.roadNetwork.id} 的道路网络")
        }


        editorComponent = RoadNetworkEditorComponent(roadNetworkRef)

        exit(doubleShiftExits = true)
        bossBar {
            title = "选择道路节点 <gray>(${nodes.size})"
            color = BossBar.Color.WHITE
        }

        nodes({ network.nodes }, ::showingLocation) { node ->
            item = ItemStack(node.material(network.modifications))
            glow = when {
                nodes.any { it.id == node.id.id } -> NamedTextColor.BLUE
                else -> NamedTextColor.WHITE
            }
            scale = Vector3f(0.5f, 0.5f, 0.5f)

            onInteract {
                val value = node.id.id
                val newNodes = if (nodes.any { it.id == value }) {
                    nodes.filter { it.id != value }
                } else {
                    nodes + RoadNodeId(value)
                }
                ref.fieldValue(fieldPath, newNodes)
                nodes = newNodes
                player.playSound("ui.button.click")
            }
        }

        return ok(Unit)
    }

    override suspend fun tick() {
        super.tick()
        cycle++
    }

    private fun showingLocation(node: RoadNode): Location = node.location.clone().apply {
        yaw = (cycle % 360).toFloat()
    }
}