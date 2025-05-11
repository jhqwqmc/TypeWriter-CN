@file:Suppress("UnstableApiUsage")

package com.typewritermc.engine.paper.command

import com.typewritermc.core.books.pages.PageType
import com.typewritermc.core.entries.Query
import com.typewritermc.core.entries.formattedName
import com.typewritermc.core.interaction.context
import com.typewritermc.engine.paper.command.dsl.*
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.audienceState
import com.typewritermc.engine.paper.entry.entries.*
import com.typewritermc.engine.paper.entry.inAudience
import com.typewritermc.engine.paper.entry.temporal.temporalCommand
import com.typewritermc.engine.paper.entry.triggerFor
import com.typewritermc.engine.paper.interaction.chatHistory
import com.typewritermc.engine.paper.plugin
import com.typewritermc.engine.paper.ui.CommunicationHandler
import com.typewritermc.engine.paper.utils.ThreadType
import com.typewritermc.engine.paper.utils.asMini
import com.typewritermc.engine.paper.utils.msg
import com.typewritermc.engine.paper.utils.sendMini
import com.typewritermc.loader.ExtensionLoader
import io.papermc.paper.command.brigadier.CommandSourceStack
import net.kyori.adventure.inventory.Book
import org.bukkit.entity.Player
import org.koin.java.KoinJavaComponent.get
import java.time.format.DateTimeFormatter

fun typewriterCommand() = command<CommandSourceStack>("typewriter", "tw") {
    reloadCommand()
    factsCommand()
    clearChatCommand()
    connectCommand()
    triggerCommand()
    manifestCommand()
    temporalCommand()

    registerDynamicCommands()
}

fun CommandTree.registerDynamicCommands() {
    val extensionLoader = get<ExtensionLoader>(ExtensionLoader::class.java)
    extensionLoader.extensions.flatMap { it.typewriterCommands }
        .map {
            val clazz = extensionLoader.loadClass(it.className)
            clazz.getMethod(it.methodName, CommandTree::class.java)
        }
        .forEach {
            it.invoke(null, this)
        }
}

private fun CommandTree.reloadCommand() = literal("reload") {
    withPermission("typewriter.reload")
    executes {
        sender.msg("重新加载配置中...")
        ThreadType.DISPATCHERS_ASYNC.launch {
            plugin.reload()
            sender.msg("配置已重新加载！")
        }
    }
}

private fun CommandTree.factsCommand() = literal("facts") {
    withPermission("typewriter.facts")


    literal("set") {
        withPermission("typewriter.facts.set")
        entry<WritableFactEntry>("fact") { fact ->
            int("value") { value ->
                executePlayerOrTarget { target ->
                    fact().write(target, value())
                    sender.msg("将<blue>${fact().formattedName}</blue>持久化变量设置为${value()}（目标：${target.name}）。")
                }
            }
        }
    }

    literal("reset") {
        withPermission("typewriter.facts.reset")
        executePlayerOrTarget { target ->
            val entries = Query.find<WritableFactEntry>().toList()
            if (entries.isEmpty()) {
                sender.msg("当前没有可用的持久化变量。")
                return@executePlayerOrTarget
            }

            for (entry in entries) {
                entry.write(target, 0)
            }
            sender.msg("<green>${target.name}</green>的所有持久化变量已重置。")
        }
    }

    literal("query") {
        entry<ReadableFactEntry>("fact") { fact ->
            executePlayerOrTarget { target ->
                sender.sendMini("<green>${target.name}</green>的持久化变量：")
                sender.sendMini(fact().format(target))
            }
        }
    }

    literal("inspect") {
        page("page", PageType.STATIC) { page ->
            executePlayerOrTarget { target ->
                val facts = page().entries.filterIsInstance<ReadableFactEntry>().sortedBy { it.name }
                sender.sendMini("页面<blue>${page().name}</blue>上关于<green>${target.name}</green>的持久化变量：")

                if (facts.isEmpty()) {
                    sender.msg("该页面没有持久化变量记录。")
                    return@executePlayerOrTarget
                }

                for (fact in facts) {
                    sender.sendMini(fact.format(target))
                }
            }
        }
    }

    executePlayerOrTarget { target ->
        val factEntries = Query.find<ReadableFactEntry>().toList()
        if (factEntries.isEmpty()) {
            sender.msg("当前没有可用的持久化变量。")
            return@executePlayerOrTarget
        }

        sender.sendMini("\n\n")
        sender.msg("<green>${target.name}</green>拥有以下持久化变量：\n")

        for (entry in factEntries.take(10)) {
            sender.sendMini(entry.format(target))
        }

        val remaining = factEntries.size - 10
        if (remaining > 0) {
            sender.sendMini(
                """
                    |<gray><i>以及另外${remaining}个...
                    |
                    |<gray>使用<white>/tw facts query [fact_id] </white>查询特定持久化变量
                    |<gray>使用<white>/tw facts inspect [page_name] </white>检查页面所有持久化变量
                    """.trimMargin()
            )
        }
    }
}

private val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")
private fun ReadableFactEntry.format(player: Player): String {
    val data = readForPlayersGroup(player)
    return "<hover:show_text:'${
        comment.replace(
            Regex(" +"),
            " "
        ).replace("'", "\\'")
    }\n\n<gray><i>点击修改'><click:suggest_command:'/tw facts set $name ${data.value} ${player.name}'><gray> - </gray><blue>${formattedName}:</blue> ${data.value} <gray><i>(${
        formatter.format(
            data.lastUpdate
        )
    })</i></gray>"
}

private fun CommandTree.clearChatCommand() = literal("clearChat") {
    withPermission("typewriter.clearChat")
    executePlayerOrTarget { player ->
        player.chatHistory.let {
            it.clear()
            it.allowedMessageThrough()
            it.resendMessages(player)
        }
    }
}


private fun CommandTree.connectCommand() = literal("connect") {
    val communicationHandler: CommunicationHandler = get(CommunicationHandler::class.java)
    withPermission("typewriter.connect")
    executes {
        if (communicationHandler.server == null) {
            sender.msg("服务器未启用WebSocket服务，请尝试在配置中启用。")
            return@executes
        }

        val player = (source.executor as? Player) ?: (sender as? Player)
        val url = communicationHandler.generateUrl(player?.uniqueId)

        if (player == null) {
            sender.msg("连接到<blue> $url </blue>以建立连接。")
            return@executes
        }

        val bookTitle = "<blue>Connect to the server</blue>".asMini()
        val bookAuthor = "<blue>Typewriter</blue>".asMini()

        val bookPage = """
				|<blue><bold>连接控制面板</bold></blue>
				|
				|<#3e4975>点击下方链接连接控制面板。连接后即可开始编写。</#3e4975>
				|
				|<hover:show_text:'<gray>点击打开链接'><click:open_url:'$url'><blue>[链接]</blue></click></hover>
				|
				|<gray><i>出于安全考虑，此链接将在5分钟后失效。</i></gray>
			""".trimMargin().asMini()

        val book = Book.book(bookTitle, bookAuthor, bookPage)
        player.openBook(book)
    }
}

private fun CommandTree.triggerCommand() = literal("trigger") {
    withPermission("typewriter.trigger")
    entry<TriggerableEntry>("entry") { entry ->
        executePlayerOrTarget { target ->
            EntryTrigger(entry()).triggerFor(target, context())
        }
    }
}

private fun CommandTree.manifestCommand() = literal("manifest") {
    withPermission("typewriter.manifest")
    literal("inspect") {
        withPermission("typewriter.manifest.inspect")
        executePlayerOrTarget { target ->
            val inEntries = Query.findWhere<AudienceEntry> { target.inAudience(it) }
                .sortedBy { it.name }
                .toList()

            if (inEntries.none()) {
                sender.msg("您不属于任何受众条目。")
                return@executePlayerOrTarget
            }

            sender.sendMini("\n\n")
            sender.msg("您属于以下受众条目：")
            for (entry in inEntries) {
                sender.sendMini(
                    "<hover:show_text:'<gray>${entry.id}'><click:copy_to_clipboard:${entry.id}><gray> - </gray><blue>${entry.formattedName}</blue></click></hover>"
                )
            }
        }
    }

    literal("page") {
        withPermission("typewriter.manifest.page")
        page("page", PageType.MANIFEST) { page ->
            executePlayerOrTarget { target ->
                val audienceEntries = page().entries
                    .filterIsInstance<AudienceEntry>()
                    .sortedBy { it.name }
                    .toList()

                if (audienceEntries.isEmpty()) {
                    sender.msg("在${page().name}页面未找到受众条目")
                    return@executePlayerOrTarget
                }

                val entryStates = audienceEntries.groupBy { target.audienceState(it) }

                sender.sendMini("\n\n")
                sender.msg("以下是页面<i>${page().name}</i>上的受众条目：")
                for (state in AudienceDisplayState.entries) {
                    val entries = entryStates[state] ?: continue
                    val color = state.color
                    sender.sendMini("\n<b><$color>${state.displayName}</$color></b>")

                    for (entry in entries) {
                        sender.sendMini(
                            "<hover:show_text:'<gray>${entry.id}'><click:copy_to_clipboard:${entry.id}><gray> - </gray><$color>${entry.formattedName}</$color></click></hover>"
                        )
                    }
                }
            }
        }
    }
}