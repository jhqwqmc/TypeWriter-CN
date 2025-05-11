package com.typewritermc.engine.paper.entry.dialogue

import com.typewritermc.engine.paper.plugin
import com.typewritermc.engine.paper.utils.config
import com.typewritermc.engine.paper.utils.reloadable
import java.time.Duration

private val typingDurationTypeString by config(
    "typingDurationType", TypingDurationType.TOTAL.name, comment = """
    |应该使用的打字持续时间类型。
    |可选值: ${TypingDurationType.entries.joinToString(", ") { it.name }}
""".trimMargin()
)

val typingDurationType: TypingDurationType by reloadable {
    val type = TypingDurationType.fromString(typingDurationTypeString)
    if (type == null) {
        plugin.logger.warning("无效的打字持续时间类型'$typingDurationTypeString'。将使用默认类型'${TypingDurationType.TOTAL.name}'替代。")
        return@reloadable TypingDurationType.TOTAL
    }
    type
}

enum class TypingDurationType {
    TOTAL,
    CHARACTER,
    ;

    fun calculatePercentage(playTime: Duration, duration: Duration, text: String): Double {
        return playTime.toMillis().toDouble() / totalDuration(text, duration).toMillis()
    }

    fun totalDuration(text: String, duration: Duration): Duration {
        return when (this) {
            TOTAL -> duration
            CHARACTER -> Duration.ofMillis(text.length * duration.toMillis())
        }
    }

    companion object {
        fun fromString(string: String): TypingDurationType? {
            return entries.find { it.name.equals(string, true) }
        }
    }
}
