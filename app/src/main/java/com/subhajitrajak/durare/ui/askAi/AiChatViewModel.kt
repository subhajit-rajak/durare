package com.subhajitrajak.durare.ui.askAi

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subhajitrajak.durare.data.repositories.AiChatRepository
import kotlinx.coroutines.launch

class AiChatViewModel(
    private val repository: AiChatRepository
) : ViewModel() {

    private val _response = MutableLiveData<String>()
    val response: LiveData<String> = _response

    private var selectedModel = "deepseek/deepseek-r1:free"

    fun setModel(model: String) {
        selectedModel = model
    }

    fun askAI(prompt: String, userData: String) {
        viewModelScope.launch {
            _response.postValue("Thinking...")
            val result = repository.askAI(prompt, selectedModel, userData)
            result.onSuccess {
                _response.postValue(it)
            }.onFailure { error ->
                val message = when (error.message) {
                    "400" -> "Request error, please try again."
                    "401" -> "API key issue. Please verify configuration."
                    "429" -> "Too many requests, slow down."
                    else -> "Something went wrong (${error.message})."
                }
                _response.postValue(message)
            }
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.clearChat()
        }
    }
}
