package me.gabber235.typewriter.ui


import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import me.gabber235.typewriter.logger
import me.gabber235.typewriter.utils.config
import org.koin.core.component.KoinComponent
import java.io.File

class PanelHost : KoinComponent {
    private val enabled: Boolean by config("panel.enabled", false)
    private val port: Int by config("panel.port", 8080)

    private var server: ApplicationEngine? = null
    fun initialize() {
        if (!enabled) {
            // If we are developing the ui we don't want to start the server
            logger.warning("当 websocket 启用时，面板被禁用。 这仅用于开发目的。 请同时启用或都不启用。")
            return
        }
        server = embeddedServer(io.ktor.server.netty.Netty, port) {
            routing {
                static {
                    serveResources("web")
                    defaultResource("index.html", "web")
                }
            }
        }.start(wait = false)
    }

    fun dispose() {
        server?.stop()
    }
}

fun Route.serveResources(resourcePackage: String? = null) {
    get("{static-resources...}/") {
        call.serve(resourcePackage)
    }

    get("{static-resources...}") {
        call.serve(resourcePackage)
    }
}

suspend fun ApplicationCall.serve(resourcePackage: String? = null) {
    val relativePath = parameters.getAll("static-resources")?.joinToString(File.separator) ?: return

    // This is key part. We either resolve some resource or resolve index.html using path from the request
    val content = resolveResource(relativePath, resourcePackage)
        ?: resolveResource("$relativePath/index.html", resourcePackage)

    if (content != null) {
        respond(content)
    }
}