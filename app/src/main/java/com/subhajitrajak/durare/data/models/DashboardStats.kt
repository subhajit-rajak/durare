package com.subhajitrajak.durare.data.models

data class DashboardStats(
    val todayPushups: Int = 0,
    val last7Pushups: Int = 0,
    val last30Pushups: Int = 0,
    val lifetimePushups: Int = 0,
    val allUsersTotal: Int = 0
)
