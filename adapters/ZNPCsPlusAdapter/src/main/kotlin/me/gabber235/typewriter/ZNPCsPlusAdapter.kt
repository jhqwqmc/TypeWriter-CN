package me.gabber235.typewriter

import App
import me.gabber235.typewriter.adapters.Adapter
import me.gabber235.typewriter.adapters.TypewriterAdapter
import me.gabber235.typewriter.adapters.Untested

@Untested
@Deprecated("使用 EntityAdapter 代替")
@Adapter("ZNPCsPlus", "对于 ZNPCsPlus 插件", App.VERSION)
/**
 * The ZNPCsPlus adapter allows you to create custom interactions with NPCs.
 */
object ZNPCsPlusAdapter : TypewriterAdapter() {
    override fun initialize() {
        if (!plugin.server.pluginManager.isPluginEnabled("ZNPCsPlus")) {
            logger.warning("未找到 ZNPCsPlus 插件，请尝试安装它或禁用 ZNPCsPlus 适配器")
            return
        }
    }
}
