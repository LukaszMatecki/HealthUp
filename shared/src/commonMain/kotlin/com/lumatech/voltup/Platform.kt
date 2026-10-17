package com.lumatech.voltup

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect fun currentTimeMillis(): Long

fun getDayOfWeekIndex(timestampMs: Long = currentTimeMillis()): Int {
    val epochDays = timestampMs / 86400000L
    val dayIdx = ((epochDays + 3) % 7).toInt()
    return if (dayIdx < 0) (dayIdx + 7) % 7 else dayIdx
}
