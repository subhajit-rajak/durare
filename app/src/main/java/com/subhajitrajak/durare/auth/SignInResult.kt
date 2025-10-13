package com.subhajitrajak.durare.auth

data class SignInResult(
    val data: UserData?,
    val errorMessage: String?
)

data class UserData(
    val userId: String = "",
    val username: String? = null,
    val userEmail: String? = null,
    val profilePictureUrl: String? = null,
    val isAnonymous: Boolean = false
)