package com.subhajitrajak.pushcounter.data.repositories

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.subhajitrajak.pushcounter.data.models.DailyPushStats
import com.subhajitrajak.pushcounter.utils.Constants.DAILY_PUSHUP_STATS
import com.subhajitrajak.pushcounter.utils.Constants.USERS
import kotlinx.coroutines.tasks.await

class DailyStatsRepository {
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    private fun dailyStatsRef(uid: String) =
        db.collection(USERS).document(uid).collection(DAILY_PUSHUP_STATS)

    suspend fun fetchAllDailyStats(): List<DailyPushStats> {
        val uid = auth.currentUser?.uid ?: throw Exception("User not logged in")

        val snapshot = dailyStatsRef(uid)
            .orderBy("date", Query.Direction.DESCENDING)
            .get()
            .await()

        return snapshot.documents.mapNotNull { it.toObject(DailyPushStats::class.java) }
    }
}