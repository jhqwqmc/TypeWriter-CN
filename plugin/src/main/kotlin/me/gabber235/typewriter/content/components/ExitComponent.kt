package me.gabber235.typewriter.content.components

import lirand.api.extensions.events.unregister
import lirand.api.extensions.inventory.meta
import lirand.api.extensions.server.registerEvents
import me.gabber235.typewriter.content.ContentMode
import me.gabber235.typewriter.content.inLastContentMode
import me.gabber235.typewriter.entry.entries.SystemTrigger
import me.gabber235.typewriter.entry.triggerFor
import me.gabber235.typewriter.plugin
import me.gabber235.typewriter.utils.loreString
import me.gabber235.typewriter.utils.name
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.bukkit.inventory.ItemStack
import java.util.*

fun ContentMode.exit(doubleShiftExits: Boolean = false) = +ExitComponent(doubleShiftExits)
class ExitComponent(
    private val doubleShiftExits: Boolean,
) : ItemComponent, Listener {
    private var playerId: UUID? = null
    private var lastShift = 0L

    override suspend fun initialize(player: Player) {
        super.initialize(player)
        if (!doubleShiftExits) return
        plugin.registerEvents(this)
        playerId = player.uniqueId
    }

    @EventHandler
    private fun onShift(event: PlayerToggleSneakEvent) {
        if (event.player.uniqueId != playerId) return
        // Only count shifting down
        if (!event.isSneaking) return
        if (System.currentTimeMillis() - lastShift < 500) {
            SystemTrigger.CONTENT_POP triggerFor event.player
        }
        lastShift = System.currentTimeMillis()
    }

    override suspend fun dispose(player: Player) {
        super.dispose(player)
        unregister()
    }

    override fun item(player: Player): Pair<Int, IntractableItem> {
        val sneakingLine = if (doubleShiftExits) {
            "<line> <gray>双击Shift键退出"
        } else {
            ""
        }
        val item = if (player.inLastContentMode) {
            ItemStack(Material.BARRIER).meta {
                name = "<red><bold>退出编辑器"
                loreString = """
                    |
                    |<line> <gray>点击以退出编辑器。
                    |$sneakingLine
                """.trimMargin()
            }
        } else {
            ItemStack(Material.END_CRYSTAL).meta {
                name = "<yellow><bold>上一个编辑器"
                loreString = """
                    |
                    |<line> <gray>点击返回上一个编辑器。
                    |$sneakingLine
                """.trimMargin()
            }
        }

        return 8 to item {
            SystemTrigger.CONTENT_POP triggerFor player
        }
    }
}