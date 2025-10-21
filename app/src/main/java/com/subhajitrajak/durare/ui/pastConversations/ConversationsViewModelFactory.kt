package com.subhajitrajak.durare.ui.pastConversations

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.subhajitrajak.durare.data.repositories.ConversationsRepository

class ConversationsViewModelFactory(
    val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ConversationsViewModel::class.java)) {
            val repository = ConversationsRepository(context)
            @Suppress("UNCHECKED_CAST")
            return ConversationsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}