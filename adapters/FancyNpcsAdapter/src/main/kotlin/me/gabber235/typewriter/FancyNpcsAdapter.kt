package me.gabber235.typewriter

import App
import me.gabber235.typewriter.adapters.Adapter
import me.gabber235.typewriter.adapters.TypewriteAdapter
import me.gabber235.typewriter.adapters.Untested

@Untested
@Adapter("FancyNpcs", "对于 FancyNpcs 插件", App.VERSION)
/**
 * The FancyNpcs adapter allows you to create custom interactions with NPCs.
 */
object FancyNpcsAdapter : TypewriteAdapter() {
    override fun initialize() {
        if (!plugin.server.pluginManager.isPluginEnabled("FancyNpcs")) {
            logger.warning("未找到 FancyNpcs 插件，请尝试安装它或禁用 FancyNpcs 适配器")
            return
        }
    }
}
