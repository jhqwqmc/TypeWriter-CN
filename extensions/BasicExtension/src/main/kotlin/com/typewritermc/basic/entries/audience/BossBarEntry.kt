package com.typewritermc.basic.entries.audience

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Colored
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.extension.annotations.Placeholder
import com.typewritermc.engine.paper.entry.entries.AudienceDisplay
import com.typewritermc.engine.paper.entry.entries.AudienceEntry
import com.typewritermc.engine.paper.entry.entries.TickableDisplay
import com.typewritermc.engine.paper.extensions.placeholderapi.parsePlaceholders
import com.typewritermc.engine.paper.utils.asMini
import net.kyori.adventure.bossbar.BossBar
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Entry("boss_bar", "Boss栏", Colors.GREEN, "carbon:progress-bar")
/**
 * The `BossBarEntry` is a display that shows a bar at the top of the screen.
 *
 * ## How could this be used?
 * This could be used to show objectives in a quest, or to show the progress of a task.
 */
class BossBarEntry(
    override val id: String = "",
    override val name: String = "",
    @Colored
    @Placeholder
    @Help("boss栏的标题")
    val title: String = "",
    @Help("boss栏的进度条。0.0 为空，1.0 为满。")
    val progress: Double = 1.0,
    @Help("boss栏的颜色")
    val color: BossBar.Color = BossBar.Color.WHITE,
    @Help("boss栏可能有的样式")
    val style: BossBar.Overlay = BossBar.Overlay.PROGRESS,
    @Help("任何适用于boss栏的标志")
    val flags: List<BossBar.Flag> = emptyList(),
) : AudienceEntry {
    override fun display(): AudienceDisplay {
        return BossBarDisplay(title, progress, color, style, flags)
    }
}

class BossBarDisplay(
    private val title: String,
    private val progress: Double,
    private val color: BossBar.Color,
    private val style: BossBar.Overlay,
    private val flags: List<BossBar.Flag>,
) : AudienceDisplay(), TickableDisplay {
    private val bars = ConcurrentHashMap<UUID, BossBar>()

    override fun tick() {
        for ((id, bar) in bars) {
            bar.name(title.parsePlaceholders(id).asMini())
        }
    }

    override fun onPlayerAdd(player: Player) {
        val bar = BossBar.bossBar(
            title.parsePlaceholders(player).asMini(),
            progress.toFloat(),
            color,
            style,
            flags.toSet()
        )
        bars[player.uniqueId] = bar
        player.showBossBar(bar)
    }

    override fun onPlayerRemove(player: Player) {
        bars.remove(player.uniqueId)?.let { player.hideBossBar(it) }
    }
}