package com.subhajitrajak.durare.data.repositories

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.subhajitrajak.durare.data.models.DailyPushStats
import com.subhajitrajak.durare.utils.Constants.DAILY_PUSHUP_STATS
import com.subhajitrajak.durare.utils.Constants.DATE
import com.subhajitrajak.durare.utils.Constants.DATE_FORMAT
import com.subhajitrajak.durare.utils.Constants.USERS
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DailyStatsRepository {
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.US)

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

    suspend fun fetchThisMonthPushupCounts(): List<Int> {
        val uid = auth.currentUser?.uid ?: throw Exception("User not logged in")

        val cal = Calendar.getInstance()
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        cal.set(Calendar.DAY_OF_MONTH, 1)
        val startOfMonth = dateFormat.format(cal.time)

        cal.set(Calendar.DAY_OF_MONTH, daysInMonth)
        val endOfMonth = dateFormat.format(cal.time)

        val query = dailyStatsRef(uid)
            .whereGreaterThanOrEqualTo(DATE, startOfMonth)
            .whereLessThanOrEqualTo(DATE, endOfMonth)
            .get().await()

        val pushupMap = query.documents.mapNotNull { doc ->
            val dateStr = doc.getString(DATE)
            val stats = doc.toObject(DailyPushStats::class.java)
            if (dateStr != null && stats != null) dateStr to stats.totalPushups else null
        }.toMap()

        val result = MutableList(daysInMonth) { 0 }
        for (day in 1..daysInMonth) {
            cal.set(Calendar.DAY_OF_MONTH, day)
            val key = dateFormat.format(cal.time)
            result[day - 1] = pushupMap[key] ?: 0
        }
        return result
    }
}