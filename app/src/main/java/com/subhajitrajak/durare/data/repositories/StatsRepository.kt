package com.subhajitrajak.durare.data.repositories

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.subhajitrajak.durare.data.models.DailyPushStats
import com.subhajitrajak.durare.utils.Constants

class StatsRepository {
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    fun saveOrAccumulateDaily(uid: String, date: String, stats: DailyPushStats): Task<Void> {
        val userDoc = db.collection(Constants.USERS).document(uid)
        val docRef = userDoc.collection(Constants.DAILY_PUSHUP_STATS).document(date)

        return db.runTransaction { txn ->
            // Read everything first
            val dailySnap = txn.get(docRef)
            val userSnap = txn.get(userDoc)
            var pushupIncrement: Int

            // Handle daily stats
            if (dailySnap.exists()) {
                val existing = dailySnap.toObject(DailyPushStats::class.java)
                val merged = existing?.copy(
                    totalReps = existing.totalReps + stats.totalReps,
                    totalPushups = existing.totalPushups + stats.totalPushups,
                    totalActiveTimeMs = existing.totalActiveTimeMs + stats.totalActiveTimeMs,
                    averagePushDurationMs = if (existing.totalPushups + stats.totalPushups > 0)
                        ((existing.averagePushDurationMs * existing.totalPushups) + (stats.averagePushDurationMs * stats.totalPushups)) / (existing.totalPushups + stats.totalPushups)
                    else 0L,
                    totalRestTimeMs = existing.totalRestTimeMs + stats.totalRestTimeMs
                ) ?: stats
                txn.set(docRef, merged)
                pushupIncrement = merged.totalPushups - existing!!.totalPushups
            } else {
                txn.set(docRef, stats)
                pushupIncrement = stats.totalPushups
            }

            // Handle lifetime stats
            if (userSnap.exists()) {
                txn.update(userDoc, Constants.LIFETIME_TOTAL_PUSHUPS, FieldValue.increment(pushupIncrement.toLong()))
            } else {
                txn.set(userDoc, mapOf(Constants.LIFETIME_TOTAL_PUSHUPS to pushupIncrement))
            }

            null
        }
    }
}