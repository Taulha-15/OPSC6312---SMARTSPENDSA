package com.example.smartspendsa.data

import android.content.Context
import androidx.room.Room

object DatabaseProvider {
    @Volatile private var db: SmartSpendDatabase? = null

    fun get(context: Context): SmartSpendDatabase =
        db ?: synchronized(this) {
            db ?: Room.databaseBuilder(
                context.applicationContext,
                SmartSpendDatabase::class.java,
                "smartspend.db"
            ).build().also { db = it }
        }
}
