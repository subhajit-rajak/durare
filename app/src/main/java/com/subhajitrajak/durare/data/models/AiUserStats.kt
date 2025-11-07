package com.subhajitrajak.durare.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AiUserStats(
    val totalPushups: Int = 0,
    val averagePerDay: Float = 0f,
    val currentStreak: Int = 0,
    val highestStreak: Int = 0,
    val last7Days: List<Int> = emptyList(),
    val last30Days: List<Int> = emptyList()
) : Parcelable