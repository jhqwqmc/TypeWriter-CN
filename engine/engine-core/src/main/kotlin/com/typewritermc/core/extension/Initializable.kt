package com.typewritermc.core.extension

import com.typewritermc.core.utils.Reloadable
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.logging.Logger

interface Initializable {
    suspend fun initialize()
    suspend fun shutdown()
}

class InitializableManager : KoinComponent, Reloadable {
    private val logger: Logger by inject()

    private var initializables: List<Initializable> = emptyList()

    override suspend fun load() {
        initializables = getKoin().getAll<Initializable>()

        initializables.forEach {
            try {
                it.initialize()
            } catch (e: Exception) {
                logger.severe("初始化 ${it.javaClass.simpleName} 失败: ${e.message}")
            }
        }
    }

    override suspend fun unload() {
        initializables.forEach {
            try {
                it.shutdown()
            } catch (e: Exception) {
                logger.severe("关闭 ${it.javaClass.simpleName} 失败: ${e.message}")
            }
        }
        initializables = emptyList()
    }
}