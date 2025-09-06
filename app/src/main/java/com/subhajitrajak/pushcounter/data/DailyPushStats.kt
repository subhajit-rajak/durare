package com.subhajitrajak.pushcounter.data

data class DailyPushStats(
    val date: String = "",
    val totalReps: Int = 0,
    val totalPushups: Int = 0,
    val totalActiveTimeMs: Long = 0L,
    val averagePushDurationMs: Long = 0L,
    val totalRestTimeMs: Long = 0L
)