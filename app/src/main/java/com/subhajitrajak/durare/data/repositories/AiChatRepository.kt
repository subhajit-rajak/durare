package com.subhajitrajak.durare.data.repositories

import android.content.Context
import androidx.room.Room
import com.subhajitrajak.durare.BuildConfig
import com.subhajitrajak.durare.data.local.ChatDatabase
import com.subhajitrajak.durare.data.local.ChatMessageEntity
import com.subhajitrajak.durare.data.models.OpenRouterMessage
import com.subhajitrajak.durare.data.models.OpenRouterRequest
import com.subhajitrajak.durare.network.OpenRouterApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AiChatRepository(context: Context) {

    private val api: OpenRouterApiService
    private val db: ChatDatabase

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://openrouter.ai/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        api = retrofit.create(OpenRouterApiService::class.java)

        db = Room.databaseBuilder(
            context.applicationContext,
            ChatDatabase::class.java,
            "chat_db"
        ).build()
    }

    suspend fun getChatHistory(): List<ChatMessageEntity> = db.chatDao().getAllMessages().takeLast(7)

    suspend fun saveMessage(role: String, content: String) {
        db.chatDao().insertMessage(ChatMessageEntity(role = role, content = content))
    }

    suspend fun clearChat() = db.chatDao().clearChat()

    suspend fun askAI(userPrompt: String, model: String, userData: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                // Build message context (system + chat history)
                val historyEntities = getChatHistory()
                val historyMessages = historyEntities.map {
                    OpenRouterMessage(it.role, it.content)
                }

                val systemMessage = OpenRouterMessage(
                    "system",
                    """
    You are **Durare**, an AI-powered personal push-up assistant and coach built into a fitness app.
    Your role is to help users understand and improve their push-up performance using their historical data.

    ### Your Goals
    1. Analyze the user's push-up records and provide data-driven insights.
    2. Motivate and guide the user with positive, conversational, and concise advice.
    3. Compare trends — weekly, monthly, or total progress.
    4. When possible, summarize performance (averages, streaks, totals, improvements).
    5. Remember previous questions and context within this chat session.
    6. If you don’t have enough data to answer, politely explain and suggest what to track.

    ### Tone & Style
    - Supportive and conversational, like a friendly fitness coach.
    - Keep explanations short and clear.
    - Include motivational comments when possible (e.g., “Nice streak!” or “You’re improving fast!”).
    - Avoid giving any medical advice or extreme fitness recommendations.

    ### Available Data
    The following information describes the user’s push-up performance:
    {USER_PUSHUP_DATA}

    Always base your insights and feedback on this data and the previous messages in the chat.
    If the user asks unrelated questions, gently redirect them to fitness or progress-related topics.
    """.trimIndent()
                )

                val systemPrompt = systemMessage.copy(
                    content = systemMessage.content.replace("{USER_PUSHUP_DATA}", userData)
                )

                val messages = listOf(systemPrompt) +
                        historyMessages +
                        OpenRouterMessage("user", userPrompt)

                val request = OpenRouterRequest(model, messages)

                val result = api.chatWithAI(request, "Bearer ${BuildConfig.OPENROUTER_API_KEY}")
                if (result.isSuccessful) {
                    val reply =
                        result.body()?.choices?.firstOrNull()?.message?.content ?: "No reply"
                    saveMessage("user", userPrompt)
                    saveMessage("assistant", reply)
                    Result.success(reply)
                } else {
                    Result.failure(Exception(result.code().toString()))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
