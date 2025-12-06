package com.subhajitrajak.durare.data.repositories

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import com.subhajitrajak.durare.auth.UserData
import com.subhajitrajak.durare.data.models.DailyPushStats
import com.subhajitrajak.durare.data.models.DashboardStats
import com.subhajitrajak.durare.data.models.User
import com.subhajitrajak.durare.utils.Constants.DAILY_PUSHUP_STATS
import com.subhajitrajak.durare.utils.Constants.DATE
import com.subhajitrajak.durare.utils.Constants.DATE_FORMAT
import com.subhajitrajak.durare.utils.Constants.LIFETIME_TOTAL_PUSHUPS
import com.subhajitrajak.durare.utils.Constants.PROFILE_PICTURE_URL
import com.subhajitrajak.durare.utils.Constants.USERS
import com.subhajitrajak.durare.utils.Constants.USER_ID
import com.subhajitrajak.durare.utils.Constants.USER_NAME
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DashboardRepository(context: Context) {
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.US)

    private fun dailyStatsRef(uid: String) =
        db.collection(USERS).document(uid).collection(DAILY_PUSHUP_STATS)

    private fun todayString() = dateFormat.format(Date())

    suspend fun fetchDashboardStats(): DashboardStats {
        val uid = auth.currentUser?.uid ?: throw Exception("User not logged in")

        val today = todayString()
        val cal = Calendar.getInstance()
        val now = cal.time

        cal.time = now
        cal.add(Calendar.DAY_OF_YEAR, -6) // last 7 days (including today)
        val last7Date = dateFormat.format(cal.time)

        cal.time = now
        cal.add(Calendar.DAY_OF_YEAR, -29) // last 30 days
        val last30Date = dateFormat.format(cal.time)

        // Fetch all user docs once
        val allDocs = dailyStatsRef(uid).get().await()
        val statsByDate = allDocs.documents.mapNotNull { doc ->
            val dateStr = doc.getString(DATE)
            val stats = doc.toObject(DailyPushStats::class.java)
            if (dateStr != null && stats != null) dateStr to stats.totalPushups else null
        }.toMap()

        val todayPushups = statsByDate[today] ?: 0

        val last7Pushups = statsByDate.filterKeys { it >= last7Date }.values.sum()
        val last30Pushups = statsByDate.filterKeys { it >= last30Date }.values.sum()
        val lifetimePushups = statsByDate.values.sum()

        // For all users lifetime (optional optimization: store this separately)
        val allUsersTotal = db.collection(USERS).get().await()
            .documents.sumOf { it.getLong(LIFETIME_TOTAL_PUSHUPS)?.toInt() ?: 0 }

        return DashboardStats(
            todayPushups,
            last7Pushups,
            last30Pushups,
            lifetimePushups,
            allUsersTotal
        )
    }

    suspend fun fetchLast30DaysPushupCounts(): List<Int> {
        val uid = auth.currentUser?.uid ?: throw Exception("User not logged in")

        val cal = Calendar.getInstance()
        val today = cal.time
        cal.add(Calendar.DAY_OF_YEAR, -29) // last 30 days includes today
        val startDate = dateFormat.format(cal.time)
        val endDate = dateFormat.format(today)

        // Fetch stats from Firestore
        val query = dailyStatsRef(uid)
            .whereGreaterThanOrEqualTo(DATE, startDate)
            .whereLessThanOrEqualTo(DATE, endDate)
            .get().await()

        val pushupMap = query.documents.mapNotNull { doc ->
            val dateStr = doc.getString(DATE)
            val stats = doc.toObject(DailyPushStats::class.java)
            if (dateStr != null && stats != null) dateStr to stats.totalPushups else null
        }.toMap()

        // Build result list
        val result = MutableList(30) { 0 }
        cal.time = today
        for (i in 29 downTo 0) {
            val dayCal = Calendar.getInstance()
            dayCal.time = today
            dayCal.add(Calendar.DAY_OF_YEAR, -i)
            val key = dateFormat.format(dayCal.time)
            result[29 - i] = pushupMap[key] ?: 0
        }

        return result
    }

    suspend fun fetchStreak(): Pair<Int, Int> {
        val uid = auth.currentUser?.uid ?: throw Exception("User not logged in")

        val allDocs = dailyStatsRef(uid).get().await()
        if (allDocs.isEmpty) return 0 to 0

        val datePushups = allDocs.documents.mapNotNull { doc ->
            val dateStr = doc.getString(DATE)
            val stats = doc.toObject(DailyPushStats::class.java)
            if (dateStr != null && stats != null) {
                val date = dateFormat.parse(dateStr)
                if (date != null) date to stats.totalPushups else null
            } else null
        }.sortedBy { it.first }

        if (datePushups.isEmpty()) return 0 to 0

        val dateMap = datePushups.associate { dateFormat.format(it.first) to it.second }

        // Highest streak
        var highestStreak = 0
        var tempStreak = 0
        val calPrev = Calendar.getInstance()
        for ((i, entry) in datePushups.withIndex()) {
            val (date, pushups) = entry
            if (pushups > 0) {
                if (i > 0) {
                    val prevDate = datePushups[i - 1].first
                    calPrev.time = prevDate
                    val calCurr = Calendar.getInstance().apply { time = date }
                    calPrev.add(Calendar.DAY_OF_YEAR, 1)
                    if (calPrev.get(Calendar.YEAR) == calCurr.get(Calendar.YEAR) &&
                        calPrev.get(Calendar.DAY_OF_YEAR) == calCurr.get(Calendar.DAY_OF_YEAR)
                    ) {
                        tempStreak++
                    } else {
                        tempStreak = 1
                    }
                } else tempStreak = 1
                highestStreak = maxOf(highestStreak, tempStreak)
            } else tempStreak = 0
        }

        // Current streak
        val cal = Calendar.getInstance()
        var currentStreak = 0
        while (true) {
            val dateStr = dateFormat.format(cal.time)
            val pushups = dateMap[dateStr] ?: 0
            if (pushups > 0) {
                currentStreak++
                cal.add(Calendar.DAY_OF_YEAR, -1)
            } else break
        }

        return currentStreak to highestStreak
    }

    fun fetchLeaderboard(): Flow<List<User>> = flow {
        // sending cached data first
        try {
            val cachedUsers = fetchUsersFromSource(Source.CACHE)
            if (cachedUsers.isNotEmpty()) {
                emit(cachedUsers)
            }
        } catch (_: Exception) {}

        // fetch from SERVER and send fresh data
        try {
            val serverUsers = fetchUsersFromSource(Source.SERVER)
            emit(serverUsers)
        } catch (e: Exception) {
            throw e
        }
    }

    private suspend fun fetchUsersFromSource(source: Source): List<User> {
        val snapshot = db.collection(USERS)
            .orderBy(LIFETIME_TOTAL_PUSHUPS, Query.Direction.DESCENDING) // server-side sort
            .limit(100) // limited to 100 for speed & cost savings
            .get(source)
            .await()

        return snapshot.documents.mapNotNull { doc ->
            val userId = doc.getString(USER_ID) ?: doc.id
            val username = doc.getString(USER_NAME)
            val photoUrl = doc.getString(PROFILE_PICTURE_URL)
            val pushups = doc.getLong(LIFETIME_TOTAL_PUSHUPS) ?: 0L

            if (username != null) {
                val userData = UserData(
                    userId = userId,
                    username = username,
                    profilePictureUrl = photoUrl ?: ""
                )
                User(userId, userData, pushups)
            } else {
                null
            }
        }
    }
}