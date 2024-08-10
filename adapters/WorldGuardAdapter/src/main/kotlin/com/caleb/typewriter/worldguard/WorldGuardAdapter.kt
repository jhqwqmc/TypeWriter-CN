package com.caleb.typewriter.worldguard

import App
import com.sk89q.worldguard.WorldGuard
import lirand.api.extensions.server.server
import me.gabber235.typewriter.adapters.Adapter
import me.gabber235.typewriter.adapters.TypewriterAdapter
import me.gabber235.typewriter.logger

@Adapter("WorldGuard", "对于使用 WorldGuard", App.VERSION)
/**
 * The WorldGuard Adapter allows you to create dialogue that is triggered by WorldGuard regions.
 */
object WorldGuardAdapter : TypewriterAdapter() {

    override fun initialize() {
        if (!server.pluginManager.isPluginEnabled("WorldGuard")) {
            logger.warning("未找到 WorldGuard 插件，请尝试安装它或禁用 WorldGuard 适配器")
            return
        }

        val worldGuard = WorldGuard.getInstance()

        val registered = worldGuard.platform.sessionManager.registerHandler(WorldGuardHandler.Factory(), null)

        if (!registered) {
            logger.warning("无法注册 WorldGuardHandler。 这是一个错误，请在 Typewriter Discord 上报告。")
        }
    }
}