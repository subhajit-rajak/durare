package com.subhajitrajak.durare.ui.pastConversations

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subhajitrajak.durare.data.local.ChatMessageEntity
import com.subhajitrajak.durare.data.repositories.ConversationsRepository
import kotlinx.coroutines.launch

class ConversationsViewModel(
    private val repository: ConversationsRepository
) : ViewModel() {

    private val _chats = MutableLiveData<List<ChatMessageEntity>>()
    val chats: LiveData<List<ChatMessageEntity>> get() = _chats

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    fun getAllConversations() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val result = repository.getChatHistory()
                _chats.value = result
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            _loading.value = true
            try {
                repository.clearChat()
                _chats.value = emptyList()
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }
}
