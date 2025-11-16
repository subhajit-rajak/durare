package com.subhajitrajak.durare.ui.settings.general.account

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subhajitrajak.durare.auth.UserData
import com.subhajitrajak.durare.data.repositories.AccountRepository
import kotlinx.coroutines.launch

sealed class UserDataState {
    object Loading : UserDataState()
    data class Success(val data: UserData?) : UserDataState()
    data class Error(val message: String) : UserDataState()
}

class AccountViewModel(
    private val repository: AccountRepository
) : ViewModel() {

    private val _userDataState = MutableLiveData<UserDataState>()
    val userDataState: LiveData<UserDataState> get() = _userDataState

    fun fetchUserData() {
        viewModelScope.launch {
            _userDataState.value = UserDataState.Loading
            val result = repository.getUserData()
            _userDataState.value = result.fold(
                onSuccess = { data -> UserDataState.Success(data) },
                onFailure = { e -> UserDataState.Error(e.localizedMessage ?: "Unknown error") }
            )
        }
    }

    fun updateWeight(weight: Float) {
        viewModelScope.launch {
            try {
                repository.updateWeight(weight)
                fetchUserData() // refresh data after update
            } catch (e: Exception) {
                _userDataState.value = UserDataState.Error(e.localizedMessage ?: "Update failed")
            }
        }
    }
}
