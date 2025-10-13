package com.subhajitrajak.durare.data.repositories

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.subhajitrajak.durare.auth.UserData
import com.subhajitrajak.durare.utils.Constants.PROFILE
import com.subhajitrajak.durare.utils.Constants.USERS
import com.subhajitrajak.durare.utils.Constants.USER_DATA
import kotlinx.coroutines.tasks.await

class OnBoardingRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    private fun getUid(): String {
        return auth.currentUser?.uid ?: throw Exception("User not logged in")
    }

    private fun profileDoc() =
        db.collection(USERS)
            .document(getUid())
            .collection(USER_DATA)
            .document(PROFILE)

    // Save or update user profile
    suspend fun saveUserData(userData: UserData) {
        profileDoc().set(userData, SetOptions.merge()).await()
    }

    // Fetch user profile
    suspend fun getUserData(): UserData? {
        val snapshot = profileDoc().get().await()
        return snapshot.toObject(UserData::class.java)
    }
}