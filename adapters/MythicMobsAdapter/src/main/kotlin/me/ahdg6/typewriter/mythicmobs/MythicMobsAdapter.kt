package me.ahdg6.typewriter.mythicmobs

import App
import lirand.api.extensions.server.server
import me.gabber235.typewriter.adapters.Adapter
import me.gabber235.typewriter.adapters.TypewriteAdapter
import me.gabber235.typewriter.adapters.Untested
import me.gabber235.typewriter.logger

@Untested
@Adapter("MythicMobs", "对于使用 MythicMobs", App.VERSION)
/**
 * The MythicMobs Adapter is an adapter for the MythicMobs plugin. It allows you handle mob-related things in TypeWriter.
 */
object MythicMobsAdapter : TypewriteAdapter() {

    override fun initialize() {
        if (!server.pluginManager.isPluginEnabled("MythicMobs")) {
            logger.warning("未找到 MythicMobs 插件，请尝试安装它或禁用 MythicMobs 适配器")
            return
        }
    }
}