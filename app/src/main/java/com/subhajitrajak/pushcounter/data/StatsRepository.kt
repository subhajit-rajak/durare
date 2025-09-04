package com.subhajitrajak.pushcounter.data

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore

class StatsRepository {
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    fun saveOrAccumulateDaily(uid: String, date: String, stats: DailyPushStats): Task<Void> {
        val docRef = db.collection("users").document(uid)
            .collection("dailyPushStats").document(date)

        return db.runTransaction { txn ->
            val snapshot = txn.get(docRef)
            if (snapshot.exists()) {
                val existing = snapshot.toObject(DailyPushStats::class.java)
                val merged = existing?.copy(
                    totalReps = existing.totalReps + stats.totalReps,
                    totalPushups = existing.totalPushups + stats.totalPushups,
                    totalActiveTimeMs = existing.totalActiveTimeMs + stats.totalActiveTimeMs,
                    averagePushDurationMs = if (existing.totalPushups + stats.totalPushups > 0)
                        ((existing.averagePushDurationMs * existing.totalPushups) + (stats.averagePushDurationMs * stats.totalPushups)) / (existing.totalPushups + stats.totalPushups)
                    else 0L,
                    totalRestTimeMs = existing.totalRestTimeMs + stats.totalRestTimeMs
                )
                    ?: stats
                txn.set(docRef, merged)
            } else {
                txn.set(docRef, stats)
            }
            null
        }
    }
}


