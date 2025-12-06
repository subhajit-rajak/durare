package com.subhajitrajak.durare.data.repositories

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.subhajitrajak.durare.auth.UserData
import com.subhajitrajak.durare.utils.Constants
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

    private fun parentDocRef() =
        db.collection(USERS)
            .document(getUid())

    private fun profileDocRef() =
        parentDocRef().collection(USER_DATA)
            .document(PROFILE)

    // Save or update user profile
    suspend fun saveUserData(userData: UserData) {
        // profile data
        val dataToSave = mutableMapOf<String, Any?>()
        if (userData.userId.isNotEmpty()) dataToSave[Constants.USER_ID] = userData.userId
        if (!userData.username.isNullOrEmpty()) dataToSave[Constants.USER_NAME] = userData.username
        if (!userData.userEmail.isNullOrEmpty()) dataToSave[Constants.USER_EMAIL] = userData.userEmail
        if (!userData.profilePictureUrl.isNullOrEmpty()) dataToSave[Constants.PROFILE_PICTURE_URL] = userData.profilePictureUrl
        if (userData.isAnonymous) dataToSave[Constants.IS_ANONYMOUS] = true
        if (userData.userWeight != null && userData.userWeight != 0.0) {
            dataToSave[Constants.USER_WEIGHT] = userData.userWeight
        }

        // parent data
        val parentUpdates = mutableMapOf<String, Any?>()
        if (userData.userId.isNotEmpty()) parentUpdates[Constants.USER_ID] = userData.userId
        if (!userData.username.isNullOrEmpty()) parentUpdates[Constants.USER_NAME] = userData.username
        if (!userData.profilePictureUrl.isNullOrEmpty()) parentUpdates[Constants.PROFILE_PICTURE_URL] = userData.profilePictureUrl

        if (dataToSave.isNotEmpty()) {
            db.runBatch { batch ->
                // Update detailed profile
                batch.set(profileDocRef(), dataToSave, SetOptions.merge())

                // Update parent doc
                if (parentUpdates.isNotEmpty()) {
                    batch.set(parentDocRef(), parentUpdates, SetOptions.merge())
                }
            }.await()
        }
    }

    // Fetch user profile
    suspend fun getUserData(): UserData? {
        val snapshot = profileDocRef().get().await()
        return snapshot.toObject(UserData::class.java)
    }
}