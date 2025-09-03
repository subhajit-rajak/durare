package com.subhajitrajak.pushcounter.auth

data class SignInState(
    val isSignInSuccessful: Boolean = false,
    val signInError: String? = null
)