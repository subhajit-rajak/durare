package com.subhajitrajak.durare.data.models

data class DashboardStats(
    val todayPushups: Int,
    val last7Pushups: Int,
    val last30Pushups: Int,
    val lifetimePushups: Int,
    val allUsersTotal: Int
)
