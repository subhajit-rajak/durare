package com.subhajitrajak.durare.data.repositories

import android.content.Context
import com.subhajitrajak.durare.data.local.ChatDatabaseProvider
import com.subhajitrajak.durare.data.local.ChatMessageEntity

class ConversationsRepository(context: Context) {

    private val db = ChatDatabaseProvider.getDatabase(context)

    suspend fun getChatHistory(): List<ChatMessageEntity> = db.chatDao().getAllMessages()

    suspend fun clearChat() = db.chatDao().clearChat()
}