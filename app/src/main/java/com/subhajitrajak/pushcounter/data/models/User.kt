package com.subhajitrajak.pushcounter.data.models

import com.subhajitrajak.pushcounter.auth.UserData

data class User(
    val uid: String,
    val userData: UserData,
    val pushups: Long
)