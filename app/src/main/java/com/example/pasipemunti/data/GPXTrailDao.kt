package com.example.pasipemunti.data

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GpxTrailDao {
    @Query("SELECT * FROM gpx_trails")
    fun getAllTrails(): Flow<List<GPXTrailEntity>>

    @Query("SELECT * FROM gpx_trails WHERE id = :trailId")
    suspend fun getTrailById(trailId: Int): GPXTrailEntity?

    @Query("SELECT * FROM gpx_trails WHERE resource_id = :resourceId LIMIT 1")
    suspend fun getTrailByResourceId(resourceId: String): GPXTrailEntity?

    @Query("SELECT * FROM gpx_trails WHERE zone = :zoneName")
    fun getTrailsByZone(zoneName: String): Flow<List<GPXTrailEntity>>

    // Poți adăuga insert, update, delete dacă vei modifica datele din aplicație
    // @Insert
    // suspend fun insertTrail(trail: GPXTrailEntity)
}