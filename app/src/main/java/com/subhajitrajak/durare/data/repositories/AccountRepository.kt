package com.subhajitrajak.durare.data.repositories

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.subhajitrajak.durare.auth.UserData
import com.subhajitrajak.durare.utils.Constants
import com.subhajitrajak.durare.utils.Preferences
import kotlinx.coroutines.tasks.await

class AccountRepository (val context: Context) {
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val pref = Preferences.getInstance(context)

    val uid = FirebaseAuth.getInstance().currentUser?.uid
    val userDoc = uid?.let { db.collection(Constants.USERS).document(it) }
    val profileDoc = userDoc?.collection(Constants.USER_DATA)?.document(Constants.PROFILE)

    suspend fun updateWeight(weight: Double) {
        pref.setWeight(weight)
        profileDoc?.update(Constants.USER_WEIGHT, weight)?.await()
    }

    suspend fun getUserData(): Result<UserData?> = try {
        val snapshot = profileDoc?.get()?.await()
        if (snapshot != null && snapshot.exists()) {
            val data = snapshot.toObject(UserData::class.java)
            Result.success(data)
        } else {
            Result.success(null)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}