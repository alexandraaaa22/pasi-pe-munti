package com.example.pasipemunti.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

// Clasa singleton care creeaza si ofera acces la baza de date Room

@Database(entities = [GPXTrailEntity::class], version = 1, exportSchema = false)
@TypeConverters(GeoPointListConverter::class, DateConverter::class)
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
                    "app_gpx_database.db"
                )
                    .createFromAsset("app_gpx_database.db")
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}