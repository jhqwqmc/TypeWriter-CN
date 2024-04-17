package me.gabber235.typewriter

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.IntegerArgumentType.integer
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import lirand.api.dsl.command.builders.LiteralDSLBuilder
import lirand.api.dsl.command.builders.command
import lirand.api.dsl.command.types.PlayerType
import lirand.api.dsl.command.types.WordType
import lirand.api.dsl.command.types.exceptions.ChatCommandExceptionType
import lirand.api.dsl.command.types.extensions.readUnquoted
import me.gabber235.typewriter.entry.*
import me.gabber235.typewriter.entry.PageType.CINEMATIC
import me.gabber235.typewriter.entry.entries.CinematicStartTrigger
import me.gabber235.typewriter.entry.entries.EntryTrigger
import me.gabber235.typewriter.entry.entries.FactEntry
import me.gabber235.typewriter.entry.entries.SystemTrigger.CINEMATIC_END
import me.gabber235.typewriter.events.TypewriterReloadEvent
import me.gabber235.typewriter.facts.FactDatabase
import me.gabber235.typewriter.facts.formattedName
import me.gabber235.typewriter.interaction.chatHistory
import me.gabber235.typewriter.ui.CommunicationHandler
import me.gabber235.typewriter.utils.asMini
import me.gabber235.typewriter.utils.msg
import me.gabber235.typewriter.utils.sendMini
import net.kyori.adventure.inventory.Book
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.java.KoinJavaComponent.get
import java.time.format.DateTimeFormatter
import java.util.concurrent.CompletableFuture
import kotlin.reflect.KClass

fun Plugin.typeWriterCommand() = command("typewriter") {
    alias("tw")

    reloadCommands()

    factsCommands()

    clearChatCommand()

    connectCommand()

    cinematicCommand()

    triggerCommand()
}

private fun LiteralDSLBuilder.reloadCommands() {
    literal("reload") {
        requiresPermissions("typewriter.reload")
        executes {
            source.msg("正在重新加载配置...")
            TypewriterReloadEvent().callEvent()
            source.msg("配置已重新加载！")
        }
    }
}

private fun LiteralDSLBuilder.factsCommands() {
    val factDatabase: FactDatabase = get(FactDatabase::class.java)

    literal("facts") {
        requiresPermissions("typewriter.facts")
        fun Player.listCachedFacts(source: CommandSender) {
            val facts = factDatabase.listCachedFacts(uniqueId)
            if (facts.isEmpty()) {
                source.msg("$name 没有变量。")
            } else {
                source.sendMini("\n\n")
                val formatter = DateTimeFormatter.ofPattern("HH:mm:ss yyyy/MM/dd")
                source.msg("$name 有以下变量：\n")
                facts.map { it to Query.findById<FactEntry>(it.id) }.forEach { (fact, entry) ->
                    if (entry == null) return@forEach

                    source.sendMini(
                        "<hover:show_text:'${
                            entry.comment.replace(
                                Regex(" +"),
                                " "
                            )
                        }\n\n<gray><i>点击修改'><click:suggest_command:'/tw facts $name set ${entry.name} ${fact.value}'><gray> - </gray><blue>${entry.formattedName}:</blue> ${fact.value} <gray><i>(${
                            formatter.format(
                                fact.lastUpdate
                            )
                        })</i></gray>"
                    )
                }
            }
        }


        argument("player", PlayerType) { player ->
            executes {
                player.get().listCachedFacts(source)
            }

            literal("set") {
                argument("fact", entryType<FactEntry>()) { fact ->
                    argument("value", integer(0)) { value ->
                        executes {
                            factDatabase.modify(player.get().uniqueId) {
                                set(fact.get().id, value.get())
                            }
                            source.msg("将 ${player.get().name} 的 <blue>${fact.get().formattedName}</blue> 设置为 ${value.get()}")
                        }
                    }
                }
            }

            literal("reset") {
                executes {
                    val p = player.get()
                    factDatabase.modify(p.uniqueId) {
                        factDatabase.listCachedFacts(p.uniqueId).forEach { (id, _) ->
                            set(id, 0)
                        }
                    }
                    source.msg("${p.name} 的所有变量均已重置。")
                }
            }
        }

        executesPlayer {
            source.listCachedFacts(source)
        }
        literal("set") {
            argument("fact", entryType<FactEntry>()) { fact ->
                argument("value", integer(0)) { value ->
                    executesPlayer {
                        factDatabase.modify(source.uniqueId) {
                            set(fact.get().id, value.get())
                        }
                        source.msg("变量 <blue>${fact.get().formattedName}</blue> 设置为 ${value.get()}。")
                    }
                }
            }
        }

        literal("reset") {
            executesPlayer {
                factDatabase.modify(source.uniqueId) {
                    factDatabase.listCachedFacts(source.uniqueId).forEach { (id, _) ->
                        set(id, 0)
                    }
                }
                source.msg("你所有的变量都已被重置。")
            }
        }
    }
}

private fun LiteralDSLBuilder.clearChatCommand() {
    literal("clearChat") {
        requiresPermissions("typewriter.clearChat")
        executesPlayer {
            source.chatHistory.let {
                it.clear()
                it.resendMessages(source)
            }
        }
    }
}

private fun LiteralDSLBuilder.connectCommand() {
    val communicationHandler: CommunicationHandler = get(CommunicationHandler::class.java)
    literal("connect") {
        requiresPermissions("typewriter.connect")
        executesConsole {
            if (communicationHandler.server == null) {
                source.msg("服务器未托管 websocket。 尝试在配置中启用它。")
                return@executesConsole
            }

            val url = communicationHandler.generateUrl(playerId = null)
            source.msg("连接到<blue> $url </blue>以启动连接。")
        }
        executesPlayer {
            if (communicationHandler.server == null) {
                source.msg("服务器未托管 websocket。 尝试在配置中启用它。")
                return@executesPlayer
            }

            val url = communicationHandler.generateUrl(source.uniqueId)

            val bookTitle = "<blue>连接到服务器</blue>".asMini()
            val bookAuthor = "<blue>Typewriter</blue>".asMini()

            val bookPage = """
				|<blue><bold>连接到面板</bold></blue>
				|
				|<#3e4975>点击下面的链接连接到面板。 连接后，您就可以开始编写了。</#3e4975>
				|
				|<hover:show_text:'<gray>点击打开链接'><click:open_url:'$url'><blue>[Link]</blue></click></hover>
				|
				|<gray><i>由于安全原因，此链接将在 5 分钟后过期。</i></gray>
			""".trimMargin().asMini()

            val book = Book.book(bookTitle, bookAuthor, bookPage)
            source.openBook(book)
        }
    }
}

private fun LiteralDSLBuilder.cinematicCommand() = literal("cinematic") {
    literal("stop") {
        requiresPermissions("typewriter.cinematic.stop")
        executesPlayer {
            CINEMATIC_END triggerFor source
        }

        argument("player", PlayerType) { player ->
            executes {
                CINEMATIC_END triggerFor player.get()
            }
        }
    }

    literal("start") {
        requiresPermissions("typewriter.cinematic.start")

        argument("cinematic", CinematicType) { cinematicId ->
            executesPlayer {
                CinematicStartTrigger(cinematicId.get(), emptyList(), override = true) triggerFor source
            }

            argument("player", PlayerType) { player ->
                executes {
                    CinematicStartTrigger(cinematicId.get(), emptyList(), override = true) triggerFor player.get()
                }
            }
        }
    }

    literal("simulate") {
        requiresPermissions("typewriter.cinematic.start")

        argument("cinematic", CinematicType) { cinematicId ->
            executesPlayer {
                CinematicStartTrigger(
                    cinematicId.get(),
                    emptyList(),
                    override = true,
                    simulate = true
                ) triggerFor source
            }

            argument("player", PlayerType) { player ->
                executes {
                    CinematicStartTrigger(
                        cinematicId.get(),
                        emptyList(),
                        override = true,
                        simulate = true
                    ) triggerFor player.get()
                }
            }
        }
    }
}

private fun LiteralDSLBuilder.triggerCommand() = literal("trigger") {
    requiresPermissions("typewriter.trigger")
    argument("entry", entryType<TriggerableEntry>()) { entry ->
        executesPlayer {
            EntryTrigger(entry.get()) triggerFor source
        }

        argument("player", PlayerType) { player ->
            executes {
                EntryTrigger(entry.get()) triggerFor player.get()
            }
        }

    }
}

inline fun <reified E : Entry> entryType() = EntryType(E::class)

open class EntryType<E : Entry>(
    val type: KClass<E>,
    open val notFoundExceptionType: ChatCommandExceptionType = PlayerType.notFoundExceptionType
) : WordType<E>, KoinComponent {
    override fun parse(reader: StringReader): E {
        val arg = reader.readUnquoted()
        return Query.findById(type, arg) ?: Query.findByName(type, arg) ?: throw notFoundExceptionType.create(arg)
    }


    override fun <S> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {

        Query.findWhere(type) { it.name.startsWith(builder.remaining, true) }.forEach {
            builder.suggest(it.name)
        }

        return builder.buildFuture()
    }

    override fun getExamples(): Collection<String> = emptyList()
}

open class CinematicType(
    open val notFoundExceptionType: ChatCommandExceptionType = PlayerType.notFoundExceptionType
) : WordType<String>, KoinComponent {
    private val entryDatabase: EntryDatabase by inject()

    companion object Instance : CinematicType()

    override fun parse(reader: StringReader): String {
        val name = reader.readString()
        if (name !in entryDatabase.getPageNames(CINEMATIC)) throw notFoundExceptionType.create(name)
        return name
    }

    override fun <S> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {

        entryDatabase.getPageNames(CINEMATIC).filter { it.startsWith(builder.remaining, true) }.forEach {
            builder.suggest(it)
        }

        return builder.buildFuture()
    }

    override fun getExamples(): Collection<String> = listOf("test.cinematic", "key.some_cinematic")
}