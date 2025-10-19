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

    suspend fun askAI(
        userPrompt: String,
        model: String,
        userData: String,
        useStats: Boolean,
        remember: Boolean
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val messages = mutableListOf<OpenRouterMessage>()

                // system message
                val systemContent = buildString {
                    append(
                        """
                    You are **Durare**, an AI-powered personal push-up assistant and coach.
                    Your role is to help users improve their push-up performance.
                    
                    Tone & Style:
                    - Supportive, concise, and motivational.
                    - Avoid giving medical advice.
                    
                    Instructions:
                    - If `useStats = true`, you have access to the user's push-up stats (provided below) and should base your insights and feedback on this data.
                    - If `useStats = false`, politely remind the user to click the "📊 Use my stats" button if they ask questions about performance or trends.
                    - If `remember = true`, you have access to previous conversations and should include relevant context from them in your answers.
                    - If `remember = false`, politely remind the user to click the "🧠 Remember" button if they ask questions that depend on past conversations.
                    """.trimIndent()
                    )
                    append("\n\n")

                    if (useStats) {
                        append("### User Push-up Data:\n$userData\n\n")
                        append("Always base your insights and feedback on this data.\n")
                    }

                    if (remember) {
                        append("Include context from previous messages if relevant.\n")
                    }
                }

                messages.add(OpenRouterMessage("system", systemContent))

                // add chat history if remember = true
                if (remember) {
                    val historyEntities = getChatHistory()
                    val historyMessages = historyEntities.map {
                        OpenRouterMessage(it.role, it.content)
                    }
                    messages.addAll(historyMessages)
                }

                // add current user message
                messages.add(OpenRouterMessage("user", userPrompt))

                // build request
                val request = OpenRouterRequest(model, messages)
                val result = api.chatWithAI(request, "Bearer ${BuildConfig.OPENROUTER_API_KEY}")

                if (result.isSuccessful) {
                    val reply = result.body()?.choices?.firstOrNull()?.message?.content ?: "No reply"

                    // Save messages conditionally
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
