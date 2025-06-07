package com.example.pasipemunti.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [GPXTrailEntity::class], version = 1, exportSchema = false)
@TypeConverters(GeoPointListConverter::class, DateConverter::class) // Specifică TypeConverters și aici
abstract class LocalDatabase : RoomDatabase() {
    abstract fun gpxTrailDao(): GpxTrailDao

    companion object {
        @Volatile
        private var INSTANCE: LocalDatabase? = null

        fun getDatabase(context: Context): LocalDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LocalDatabase::class.java,
                    "app_gpx_database.db" // Numele bazei de date generate de Python
                )
                    .createFromAsset("app_gpx_database.db") // AICI SE SPECIFICĂ FIȘIERUL PRE-POPULAT
                    // .fallbackToDestructiveMigration() // Folosește asta DOAR în faza de dezvoltare, dacă schimbi schema
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}