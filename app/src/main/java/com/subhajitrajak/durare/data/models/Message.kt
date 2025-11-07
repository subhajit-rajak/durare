package com.subhajitrajak.durare.data.models

data class Message(
    val role: Role = Role.USER,
    val content: String = ""
)