package com.subhajitrajak.durare.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DailyPushStats(
    val date: String = "",
    val totalReps: Int = 0,
    val totalPushups: Int = 0,
    val totalActiveTimeMs: Long = 0L,
    val averagePushDurationMs: Long = 0L,
    val totalRestTimeMs: Long = 0L
) : Parcelable