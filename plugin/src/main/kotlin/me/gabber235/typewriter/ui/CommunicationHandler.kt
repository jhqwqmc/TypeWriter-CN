package me.gabber235.typewriter.ui

import com.corundumstudio.socketio.Configuration
import com.corundumstudio.socketio.HandshakeData
import com.corundumstudio.socketio.SocketIOClient
import com.corundumstudio.socketio.SocketIOServer
import lirand.api.extensions.events.listen
import lirand.api.extensions.server.server
import me.gabber235.typewriter.entry.StagingManager
import me.gabber235.typewriter.events.StagingChangeEvent
import me.gabber235.typewriter.logger
import me.gabber235.typewriter.plugin
import me.gabber235.typewriter.utils.config
import me.gabber235.typewriter.utils.logErrorIfNull
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.Instant
import java.util.*
import kotlin.collections.set


class CommunicationHandler : KoinComponent {
    private val clientSynchronizer: ClientSynchronizer by inject()
    private val writers: Writers by inject()
    private val stagingManager: StagingManager by inject()
    private val panelHost: PanelHost by inject()

    private val hostName: String by config(
        "hostname",
        "127.0.0.1",
        comment = "服务器的主机名。 将其更改为您的服务器 IP。"
    )
    private val panelPort: Int by config(
        "panel.port",
        8080,
        comment = "Web面板的端口。 确保此端口已打开。"
    )
    private val BASE_URL get() = "http://${hostName}:${panelPort}/#"
    private val enabled: Boolean by config(
        "enabled",
        false,
        comment = "是否启用 Web 面板和 Web 套接字。"
    )
    private val port: Int by config(
        "websocket.port",
        9092,
        comment = "Websocket 服务器的端口。 确保此端口已打开。"
    )
    private val auth: String by config(
        "websocket.auth", "session", comment = """
        |使用的身份验证。 如果您不知道自己在做什么，请保持不变。
        |可能的值：none（不推荐）、session
    """.trimMargin()
    ) // Possible values: none, session
    val authenticationEnabled: Boolean
        get() = auth == "session"

    private val sessionTokens: MutableMap<UUID, SessionData> = mutableMapOf()

    var server: SocketIOServer? = null
        private set

    fun initialize() {
        if (!enabled) return
        logger.warning("Websocket 服务器已启用。 不建议对生产服务器这样做。")
        panelHost.initialize()
        val config = Configuration().apply {
            hostname = "0.0.0.0"
            port = this@CommunicationHandler.port
            setAuthorizationListener(this@CommunicationHandler::authenticate)
        }

        server = SocketIOServer(config)

        server?.addEventListener("fetch", String::class.java, clientSynchronizer::handleFetchRequest)
        server?.addEventListener("createPage", String::class.java, clientSynchronizer::handleCreatePage)
        server?.addEventListener("renamePage", String::class.java, clientSynchronizer::handleRenamePage)
        server?.addEventListener("changePageValue", String::class.java, clientSynchronizer::handleChangePageValue)
        server?.addEventListener("deletePage", String::class.java, clientSynchronizer::handleDeletePage)
        server?.addEventListener("createEntry", String::class.java, clientSynchronizer::handleCreateEntry)
        server?.addEventListener("updateEntry", String::class.java, clientSynchronizer::handleEntryFieldUpdate)
        server?.addEventListener(
            "updateCompleteEntry",
            String::class.java,
            clientSynchronizer::handleEntryUpdate
        )
        server?.addEventListener("reorderEntry", String::class.java, clientSynchronizer::handleReorderEntry)
        server?.addEventListener("deleteEntry", String::class.java, clientSynchronizer::handleDeleteEntry)

        server?.addEventListener("publish", String::class.java, clientSynchronizer::handlePublish)

        server?.addEventListener("updateWriter", String::class.java, clientSynchronizer::handleUpdateWriter)
        server?.addEventListener("captureRequest", String::class.java, clientSynchronizer::handleCaptureRequest)

        server?.addConnectListener { socket ->
            logger.info("客户端连接：${socket.remoteAddress}")
            socket.sendEvent("stagingState", stagingManager.stagingState.name.lowercase())

            val token = getSessionToken(socket.handshakeData)
            if (token != null) {
                sessionTokens.computeIfPresent(token) { _, session ->
                    session.copy(lastConnectionId = socket.sessionId)
                }
            }

            val iconUrl = getIconUrl(token)

            writers.addWriter(socket.sessionId.toString(), iconUrl)
            server.broadcastWriters(writers)
        }

        server?.addDisconnectListener {
            logger.info("客户端已断开连接：${it.remoteAddress}")
            server?.broadcastOperations?.sendEvent("disconnectWriter", it, it.sessionId.toString())

            writers.removeWriter(it.sessionId.toString())
            server.broadcastWriters(writers)
        }

        server?.start()


        plugin.listen<StagingChangeEvent> { (newState) ->
            server?.broadcastOperations?.sendEvent("stagingState", newState.name.lowercase())
        }
    }

    private fun authenticate(data: HandshakeData): Boolean {
        if (auth == "none") return true
        if (auth == "session") {
            val token = getSessionToken(data)
                .logErrorIfNull("${data.address} 尝试在没有令牌的情况下连接到套接字！") ?: return false
            val session = sessionTokens[token] ?: return false
            return session.isValid
        }
        return false
    }

    private fun getSessionToken(data: HandshakeData): UUID? {
        if (auth == "none") return null
        if (auth == "session") {
            val token = data.getSingleUrlParam("token") ?: return null
            return UUID.fromString(token)
        }
        return null
    }

    private fun generateSessionToken(playerId: UUID?): UUID {
        // If there already is a token for this player, use that one.
        val existingToken = sessionTokens.filter { it.value.playerId == playerId }.keys.firstOrNull()
        if (existingToken != null) return existingToken

        // Otherwise, create a new token.
        val token = UUID.randomUUID()
        sessionTokens[token] = SessionData.create(playerId)
        return token
    }

    fun generateUrl(playerId: UUID?): String {
        if (auth == "none") return "$BASE_URL/connect?host=$hostName&port=$port"
        if (auth == "session") {
            val token = generateSessionToken(playerId)
            return "$BASE_URL/connect?host=$hostName&port=$port&token=$token"
        }
        return ""
    }

    fun getPlayer(client: SocketIOClient): Player? {
        val token = getSessionToken(client.handshakeData) ?: return null
        val session = sessionTokens[token] ?: return null
        return session.playerId?.let { plugin.server.getPlayer(it) }
    }

    fun getIconUrl(sessionId: String): String? {
        val client = server?.getClient(UUID.fromString(sessionId)) ?: return null
        val token = getSessionToken(client.handshakeData) ?: return null
        return getIconUrl(token)
    }

    private fun getIconUrl(token: UUID?): String? {
        val session = sessionTokens[token] ?: return null
        return session.playerId?.let {
            "https://crafatar.com/avatars/$it?size=64&overlay"
        }
    }

    fun shutdown() {
        if (!enabled) return
        server?.stop()
        panelHost.dispose()
        writers.dispose()
    }
}

data class SessionData(
    val playerId: UUID?,
    val created: Instant,
    val lastConnectionId: UUID? = null,
) {
    companion object {
        fun create(playerId: UUID?) = SessionData(playerId, Instant.now())
    }

    /**
     * Check if the session is still valid.
     *
     * A session is valid if the player is still online.
     *
     * @return true if the session is still valid, false otherwise.
     */
    val isValid: Boolean
        get() = playerId?.let { server.getPlayer(it) != null } ?: true
}