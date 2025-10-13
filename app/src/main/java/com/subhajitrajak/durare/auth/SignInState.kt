package com.subhajitrajak.durare.auth

data class SignInState(
    val isSignInSuccessful: Boolean = false,
    val signInError: String? = null
)