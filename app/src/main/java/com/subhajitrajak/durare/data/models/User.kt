package com.subhajitrajak.durare.data.models

import com.subhajitrajak.durare.auth.UserData

data class User(
    val uid: String,
    val userData: UserData,
    val pushups: Long
)