package com.subhajitrajak.durare.data.repositories

import android.content.Context
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.subhajitrajak.durare.data.local.ChatMessageEntity
import com.subhajitrajak.durare.data.local.ChatDatabaseProvider
import com.subhajitrajak.durare.data.models.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AiChatRepository(context: Context) {

    private val db = ChatDatabaseProvider.getDatabase(context)

    suspend fun getChatHistory(): List<ChatMessageEntity> = db.chatDao().getAllMessages().takeLast(7)

    suspend fun saveMessage(role: String, content: String) {
        db.chatDao().insertMessage(ChatMessageEntity(role = role, content = content))
    }

    suspend fun clearChat() = db.chatDao().clearChat()

    suspend fun askAi(
        userPrompt: String,
        model: String,
        userData: String,
        useStats: Boolean,
        remember: Boolean
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val messages = mutableListOf<Message>()

                // system message
                val systemContent = buildSystemMessage(userData, useStats, remember)
                messages.add(Message("system", systemContent))

                // add chat history if remember = true
                if (remember) {
                    val historyMessages = getChatHistory().map {
                        Message(it.role, it.content)
                    }
                    messages.addAll(historyMessages)
                }

                // add current user message
                messages.add(Message("user", userPrompt))

                val prompt = messages.joinToString("\n\n") { "${it.role.uppercase()}: ${it.content}" }

                val model = Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(model)
                val resp = model.generateContent(prompt)
                val result = resp.text

                if (result != null) {
                    saveMessage("user", userPrompt)
                    saveMessage("assistant", result)

                    Result.success(result)
                } else {
                    Result.failure(Exception("No reply, please try again."))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private fun buildSystemMessage(userData: String, useStats: Boolean, remember: Boolean): String {
        return buildString {
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
                append("Always base your insights and feedback on this data.\nuseStats = true\n")
            }

            if (remember) {
                append("Include context from previous messages if relevant.\nremember = true\n")
            }
        }
    }
}