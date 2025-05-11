package com.typewritermc.core.utils

import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

fun Duration.formatCompact(): String {
    val days = this.inWholeDays
    val hours = (this - days.days).inWholeHours
    val minutes = (this - days.days - hours.hours).inWholeMinutes
    val seconds = (this - days.days - hours.hours - minutes.minutes).inWholeSeconds

    return buildString {
        if (days > 0) append("${days}天")
        if (hours > 0) append("${hours}小时")
        if (minutes > 0) append("${minutes}分钟")
        if (seconds > 0 || this.isEmpty()) append("${seconds}秒")
    }.trim()
}