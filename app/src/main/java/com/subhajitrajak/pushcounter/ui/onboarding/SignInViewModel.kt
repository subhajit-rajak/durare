package com.subhajitrajak.pushcounter.ui.onboarding

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.subhajitrajak.pushcounter.auth.SignInResult
import com.subhajitrajak.pushcounter.auth.SignInState
import com.subhajitrajak.pushcounter.auth.UserData
import com.subhajitrajak.pushcounter.data.repositories.OnBoardingRepository

class SignInViewModel(
    private val repository: OnBoardingRepository = OnBoardingRepository()
) : ViewModel() {

    private val _state = MutableLiveData(SignInState())
    val state: LiveData<SignInState> = _state

    fun onSignInResult(result: SignInResult) {
        _state.value = SignInState(
            isSignInSuccessful = result.data != null,
            signInError = result.errorMessage
        )
    }

    fun resetState() {
        _state.value = SignInState()
    }

    fun saveUserData(userData: UserData) = liveData {
        try {
            repository.saveUserData(userData)
            emit(Result.success("User data saved successfully"))
        } catch (e: Exception) {
            emit(Result.failure<String>(e))
        }
    }

    fun getUserData() = liveData {
        try {
            val data = repository.getUserData()
            emit(Result.success(data))
        } catch (e: Exception) {
            emit(Result.failure<UserData?>(e))
        }
    }
}