package com.subhajitrajak.durare.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.subhajitrajak.durare.data.models.Role

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val role: Role,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)