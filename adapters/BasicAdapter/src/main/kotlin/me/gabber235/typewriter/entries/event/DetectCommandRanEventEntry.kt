package me.gabber235.typewriter.entries.event

import me.gabber235.typewriter.adapters.Colors
import me.gabber235.typewriter.adapters.Entry
import me.gabber235.typewriter.adapters.modifiers.Help
import me.gabber235.typewriter.adapters.modifiers.Regex
import me.gabber235.typewriter.entry.*
import me.gabber235.typewriter.entry.entries.EventEntry
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import kotlin.text.Regex as KotlinRegex

@Entry("on_detect_command_ran", "当玩家运行现有命令时", Colors.YELLOW, "mdi:account-eye")
/**
 * The `Detect Command Ran Event` event is triggered when an **already existing** command is ran.
 *
 * :::caution
 * This event only works if the command already exists. If you are trying to make a new command that does not exist already, use the [`Run Command Event`](on_run_command) instead.
 * :::
 *
 * ## How could this be used?
 *
 * This event could be used to trigger a response when a specific command has been run.
 * For example, you could have a command that sends a message to a channel when a command has been run,
 * which could be used as an audit log for your admins.
 */
class DetectCommandRanEventEntry(
    override val id: String = "",
    override val name: String = "",
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    @Regex
    @Help("运行的命令。 可以是正则表达式。")
    /**
     * The command to listen for.
     * This can be partial, so if you wanted to listen for any warp command,
     * you could use <code>warp</code> as the command.
     * However, this can also include parameters, so if you
     * wanted to listen if the player warps to spawn, you could use
     * <code>warp spawn</code> as the command.
     * <br />
     * <Admonition type="caution">
     *     Do not include the <code>/</code> at the start of the command.
     *     This will be added automatically.
     * </Admonition>
     */
    val command: String = "",
    /**
     * Cancel the event when triggered.
     * It will only cancel the event if all the criteria are met.
     * If set to false, it will not modify the event.
     *
     * <Admonition type="tip">
     *     You should always set this to true if any dialog is triggered after this.
     *     To prevent the dialog from immediately being closed.
     * </Admonition>
     */
    @Help("触发时取消事件")
    val cancel: Boolean = false,
) : EventEntry

@EntryListener(DetectCommandRanEventEntry::class)
fun onRunCommand(event: PlayerCommandPreprocessEvent, query: Query<DetectCommandRanEventEntry>) {
    val message = event.message.removePrefix("/")

    val entries = query.findWhere { KotlinRegex(it.command).matches(message) }.toList()
    if (entries.isEmpty()) return
    entries triggerAllFor event.player
    if (entries.any { it.cancel }) event.isCancelled = true
}