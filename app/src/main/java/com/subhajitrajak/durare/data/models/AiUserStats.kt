package com.subhajitrajak.durare.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AiUserStats(
    val totalPushups: Int,
    val averagePerDay: Float,
    val currentStreak: Int,
    val highestStreak: Int,
    val last7Days: List<Int>,
    val last30Days: List<Int>
) : Parcelable