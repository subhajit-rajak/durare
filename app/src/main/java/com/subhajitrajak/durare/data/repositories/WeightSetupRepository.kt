package com.subhajitrajak.durare.data.repositories

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.subhajitrajak.durare.utils.Constants
import com.subhajitrajak.durare.utils.Preferences
import kotlinx.coroutines.tasks.await

class WeightSetupRepository (val context: Context) {
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    val pref = Preferences.getInstance(context)

    val uid = FirebaseAuth.getInstance().currentUser?.uid
    val userDoc = uid?.let { db.collection(Constants.USERS).document(it) }
    val profileDoc = userDoc?.collection(Constants.USER_DATA)?.document(Constants.PROFILE)

    suspend fun saveData(weight: Double) {
        pref.setWeight(weight)
        profileDoc?.update(Constants.USER_WEIGHT, weight)?.await()
    }

    suspend fun isDataSaved() : Boolean {
        val profileSnapshot = profileDoc?.get()?.await()
        if (profileSnapshot!=null && profileSnapshot.exists()) {
            val weight = profileSnapshot.getDouble(Constants.USER_WEIGHT) ?: 0.0
            return weight > 0.0
        }
        return false
    }

    suspend fun updateLocalFromRemote() {
        val profileSnapshot = profileDoc?.get()?.await()
        if (profileSnapshot!=null && profileSnapshot.exists()) {
            val weight = profileSnapshot.getDouble(Constants.USER_WEIGHT)
            if (weight != null) {
                pref.setWeight(weight)
            }
        }
    }
}