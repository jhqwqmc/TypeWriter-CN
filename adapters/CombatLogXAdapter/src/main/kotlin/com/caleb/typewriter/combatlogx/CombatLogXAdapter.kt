package com.caleb.typewriter.combatlogx

import App
import com.github.sirblobman.combatlogx.api.ICombatLogX
import lirand.api.extensions.server.server
import me.gabber235.typewriter.adapters.Adapter
import me.gabber235.typewriter.adapters.TypewriteAdapter
import me.gabber235.typewriter.adapters.Untested
import me.gabber235.typewriter.logger
import org.bukkit.Bukkit

@Untested
@Adapter("CombatLogX", "对于使用 CombatLogX", App.VERSION)
/**
 * The CombatLogX Adapter allows you to create entries that are triggered when a player enters or leaves combat.
 */
object CombatLogXAdapter : TypewriteAdapter() {

    override fun initialize() {
        if (!server.pluginManager.isPluginEnabled("CombatLogX")) {
            logger.warning("未找到 CombatLogX 插件，请尝试安装它或禁用 CombatLogX 适配器")
            return
        }
    }

    fun getAPI(): ICombatLogX? {
        val pluginManager = Bukkit.getPluginManager()
        val plugin = pluginManager.getPlugin("CombatLogX")
        return plugin as? ICombatLogX
    }
}