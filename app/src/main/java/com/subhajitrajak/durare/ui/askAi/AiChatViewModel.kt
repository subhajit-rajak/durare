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

    private var selectedModel = "gemini-2.5-flash-lite"

    fun setModel(model: String) {
        selectedModel = model
    }

    fun askAI(prompt: String, userData: String, useStats: Boolean, remember: Boolean) {
        viewModelScope.launch {
            val result = repository.askAi(
                userPrompt = prompt,
                model = selectedModel,
                userData = userData,
                useStats = useStats,
                remember = remember
            )
            result.onSuccess {
                _response.postValue(it)
            }.onFailure { error ->
                _response.postValue(error.message)
            }
        }
    }
}
