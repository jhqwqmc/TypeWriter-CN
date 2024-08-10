package com.caleb.typewriter.superiorskyblock

import App
import lirand.api.extensions.server.server
import me.gabber235.typewriter.adapters.Adapter
import me.gabber235.typewriter.adapters.TypewriterAdapter
import me.gabber235.typewriter.adapters.Untested
import me.gabber235.typewriter.logger

@Untested
@Adapter("SuperiorSkyblock", "对于SuperiorSkyblock2，由Caleb (Sniper)制作", App.VERSION)
/**
 * The Superior Skyblock Adapter allows you to use the Superior Skyblock plugin with TypeWriter.
 * It includes many events for you to use in your dialogue, as well as a few actions and conditions.
 */
object SuperiorSkyblockAdapter : TypewriterAdapter() {
    override fun initialize() {
        if (!server.pluginManager.isPluginEnabled("SuperiorSkyblock2")) {
            logger.warning("未找到 SuperiorSkyblock2 插件，请尝试安装它或禁用 SuperiorSkyblock2 适配器")
        }

    }
}