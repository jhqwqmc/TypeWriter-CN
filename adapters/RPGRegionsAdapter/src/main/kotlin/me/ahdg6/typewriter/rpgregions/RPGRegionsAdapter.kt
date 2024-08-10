package me.ahdg6.typewriter.rpgregions

import App
import lirand.api.extensions.server.server
import me.gabber235.typewriter.adapters.Adapter
import me.gabber235.typewriter.adapters.TypewriterAdapter
import me.gabber235.typewriter.adapters.Untested
import me.gabber235.typewriter.logger

@Untested
@Adapter("RPGRegions", "对于使用 RPGRegions", App.VERSION)
/**
 * The RPGRegions Adapter is an adapter for the RPGRegions plugin. It allows you to use RPGRegions's discovery system in your dialogue.
 */
object RPGRegionsAdapter : TypewriterAdapter() {

    override fun initialize() {
        if (!server.pluginManager.isPluginEnabled("RPGRegions")) {
            logger.warning("未找到 RPGRegions 插件，请尝试安装它或禁用适配器")
            return
        }
    }

}