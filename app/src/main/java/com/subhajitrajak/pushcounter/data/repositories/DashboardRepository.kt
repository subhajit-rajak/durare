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

    suspend fun fetchThisMonthPushupCounts2(): List<Int> {
        val uid = auth.currentUser?.uid ?: throw Exception("User not logged in")

        val cal = Calendar.getInstance()
        val now = cal.time
        cal.time = now
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val startOfMonth = dateFormat.format(cal.time)
        cal.add(Calendar.MONTH, 1)
        cal.add(Calendar.DAY_OF_MONTH, -1)
        val endOfMonth = dateFormat.format(cal.time)

        val query = db.collection(USERS).document(uid)
            .collection(DAILY_PUSHUP_STATS)
            .whereGreaterThanOrEqualTo(DATE, startOfMonth)
            .whereLessThanOrEqualTo(DATE, endOfMonth)
            .get().await()

        return query.documents.map {
            it.toObject(DailyPushStats::class.java)?.totalPushups ?: 0
        }
    }

    suspend fun fetchThisMonthPushupCounts(): List<Int> {
        val uid = auth.currentUser?.uid ?: throw Exception("User not logged in")

        val cal = Calendar.getInstance()
        val now = cal.time
        cal.time = now

        // First day of this month
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val startOfMonth = dateFormat.format(cal.time)

        // Last day of this month
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        cal.set(Calendar.DAY_OF_MONTH, daysInMonth)
        val endOfMonth = dateFormat.format(cal.time)

        // Query all docs within this month
        val query = db.collection(USERS).document(uid)
            .collection(DAILY_PUSHUP_STATS)
            .whereGreaterThanOrEqualTo(DATE, startOfMonth)
            .whereLessThanOrEqualTo(DATE, endOfMonth)
            .get().await()

        // Initialize list of zeros for all days
        val pushupCounts = MutableList(daysInMonth) { 0 }

        for (doc in query.documents) {
            val stats = doc.toObject(DailyPushStats::class.java)
            if (stats != null) {
                // Assuming DATE field is stored as a string matching dateFormat (e.g. "yyyy-MM-dd")
                val docDate = dateFormat.parse(doc.getString(DATE)!!)
                val dayOfMonth = Calendar.getInstance().apply { time = docDate }.get(Calendar.DAY_OF_MONTH)

                pushupCounts[dayOfMonth - 1] = stats.totalPushups
            }
        }
        return pushupCounts
    }
}
