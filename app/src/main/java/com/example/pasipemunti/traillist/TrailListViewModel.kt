package com.example.pasipemunti.ui

import android.util.Log
import androidx.compose.runtime.State // Importă State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue // Rămâne pentru _selectedTrail.value = ...
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pasipemunti.data.GpxTrailDao
import com.example.pasipemunti.data.GPXTrail
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class TrailListViewModel(private val gpxTrailDao: GpxTrailDao) : ViewModel() {

    val allTrails: Flow<List<GPXTrail>> = gpxTrailDao.getAllTrails().map { entities ->
        entities.map { it.toGPXTrail() }
    }

    // Proprietatea privată MutableState care deține valoarea
    private val _selectedTrail = mutableStateOf<GPXTrail?>(null)

    // Proprietatea publică read-only, de tip State<GPXTrail?>
    // UI-ul va observa direct acest obiect State și va accesa valoarea cu .value
    val selectedTrail: State<GPXTrail?> get() = _selectedTrail

    // Funcția pentru a seta traseul selectat, care modifică _selectedTrail
    fun selectTrail(trail: GPXTrail) {
        _selectedTrail.value = trail // Modifică valoarea State-ului
    }

    suspend fun getTrailById(id: Int): GPXTrail? {
        return gpxTrailDao.getTrailById(id)?.toGPXTrail()
    }

    suspend fun getTrailByResourceId(resourceId: String): GPXTrail? {
        return gpxTrailDao.getTrailByResourceId(resourceId)?.toGPXTrail()
    }

    fun getTrailsByZone(zoneName: String): Flow<List<GPXTrail>> {
        return gpxTrailDao.getTrailsByZone(zoneName).map { entities ->
            entities.map { it.toGPXTrail() }
        }
    }
}

fun debugTrailData(trail: GPXTrail) {
    Log.d("TrailDebug", "=== Trail Debug Info ===")
    Log.d("TrailDebug", "Name: ${trail.name}")
    Log.d("TrailDebug", "Points count: ${trail.points.size}")

    if (trail.points.isNotEmpty()) {
        Log.d("TrailDebug", "First point: lat=${trail.points.first().latitude}, lon=${trail.points.first().longitude}")
        Log.d("TrailDebug", "Last point: lat=${trail.points.last().latitude}, lon=${trail.points.last().longitude}")

        // Check if coordinates are valid (not 0,0 or extreme values)
        val validPoints = trail.points.filter { point ->
            point.latitude != 0.0 && point.longitude != 0.0 &&
                    point.latitude >= -90 && point.latitude <= 90 &&
                    point.longitude >= -180 && point.longitude <= 180
        }
        Log.d("TrailDebug", "Valid points: ${validPoints.size}/${trail.points.size}")

        if (validPoints.isEmpty()) {
            Log.e("TrailDebug", "ERROR: No valid coordinates found!")
        }
    } else {
        Log.e("TrailDebug", "ERROR: No points in trail!")
    }
    Log.d("TrailDebug", "========================")
}

class TrailListViewModelFactory(private val gpxTrailDao: GpxTrailDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TrailListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TrailListViewModel(gpxTrailDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}