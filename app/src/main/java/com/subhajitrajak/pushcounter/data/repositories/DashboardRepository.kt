package com.subhajitrajak.pushcounter.data.repositories

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.subhajitrajak.pushcounter.data.models.DailyPushStats
import com.subhajitrajak.pushcounter.data.models.DashboardStats
import com.subhajitrajak.pushcounter.utils.Constants.DAILY_PUSHUP_STATS
import com.subhajitrajak.pushcounter.utils.Constants.DATE
import com.subhajitrajak.pushcounter.utils.Constants.DATE_FORMAT
import com.subhajitrajak.pushcounter.utils.Constants.LIFETIME_TOTAL_PUSHUPS
import com.subhajitrajak.pushcounter.utils.Constants.USERS
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class DashboardRepository(context: Context) {
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.US)

    suspend fun fetchDashboardStats(): DashboardStats {
        val uid = auth.currentUser?.uid ?: throw Exception("User not logged in")

        // Date boundaries
        val today = dateFormat.format(Date())

        val cal = Calendar.getInstance()
        val now = cal.time

        cal.time = now
        cal.add(Calendar.DAY_OF_YEAR, -6) // last 7 days (including today)
        val last7Date = dateFormat.format(cal.time)

        cal.time = now
        cal.add(Calendar.DAY_OF_YEAR, -29) // last 30 days
        val last30Date = dateFormat.format(cal.time)

        // today's pushups
        val todayDoc = db.collection(USERS).document(uid)
            .collection(DAILY_PUSHUP_STATS).document(today)
            .get().await()
        val todayPushups = todayDoc.toObject(DailyPushStats::class.java)?.totalPushups ?: 0

        // last 7 days
        val last7Query = db.collection(USERS).document(uid)
            .collection(DAILY_PUSHUP_STATS)
            .whereGreaterThanOrEqualTo(DATE, last7Date)
            .get().await()
        val last7Pushups = last7Query.documents.sumOf {
            it.toObject(DailyPushStats::class.java)?.totalPushups ?: 0
        }

        // last 30 days
        val last30Query = db.collection(USERS).document(uid)
            .collection(DAILY_PUSHUP_STATS)
            .whereGreaterThanOrEqualTo(DATE, last30Date)
            .get().await()
        val last30Pushups = last30Query.documents.sumOf {
            it.toObject(DailyPushStats::class.java)?.totalPushups ?: 0
        }

        // lifetime (all time)
        val allDocs = db.collection(USERS).document(uid)
            .collection(DAILY_PUSHUP_STATS)
            .get().await()
        val lifetimePushups = allDocs.documents.sumOf {
            it.toObject(DailyPushStats::class.java)?.totalPushups ?: 0
        }

        // pushups of all users
        val allUsers = db.collection(USERS).get().await()
        val allUsersTotal = allUsers.documents.sumOf {
            it.getLong(LIFETIME_TOTAL_PUSHUPS)?.toInt() ?: 0
        }

        return DashboardStats(
            todayPushups,
            last7Pushups,
            last30Pushups,
            lifetimePushups,
            allUsersTotal
        )
    }

    suspend fun fetchDailyStats(): DailyPushStats {
        val uid = auth.currentUser?.uid ?: throw Exception("User not logged in")
        
        val today = dateFormat.format(Date())
        val todayDoc = db.collection(USERS).document(uid)
            .collection(DAILY_PUSHUP_STATS).document(today)
            .get().await()
        return todayDoc.toObject(DailyPushStats::class.java) ?: throw Exception("No stats found for today")
    }
}
