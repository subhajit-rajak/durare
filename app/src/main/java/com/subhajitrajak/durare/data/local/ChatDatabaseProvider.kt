package com.subhajitrajak.durare.data.local

import android.content.Context
import androidx.room.Room

object ChatDatabaseProvider {
    @Volatile
    private var INSTANCE: ChatDatabase? = null

    fun getDatabase(context: Context): ChatDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                ChatDatabase::class.java,
                "chat_db"
            ).build()
            INSTANCE = instance
            instance
        }
    }
}