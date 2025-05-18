package com.example.pasipemunti.traillist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.pasipemunti.R

class TrailListViewModel : ViewModel() {

    // MutableState list to hold the trails
    val trails = mutableStateListOf<GPXTrail>()

     //Mapping of GPX files to images - real implementation would use proper image references
    private val trailImageMapping = mapOf(
        R.raw.busteni_cabana_padina_pietrosita to R.drawable.home_screen_background, // Example mapping
        // Add other mappings as needed
    )

    // Function to load GPX files from resources
    fun loadTrails(context: Context) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                // Get all available raw resources - in a production app you might want to filter for GPX files specifically
                try {
                    // For this example, we're just adding hardcoded resource IDs
                    // In a real app, you'd dynamically find all GPX files in the raw directory
                    val gpxResources = listOf(
                        R.raw.busteni_cascada_urlatoarea,
                        R.raw.busteni_cabana_piatra_arsa,
                        R.raw.busteni_cabana_poiana_izvoarelor_cabana_omu,
                        R.raw.busteni_cascada_urlatoarea,
                        R.raw.busteni_poiana_costilei_cabana_omu,
                        R.raw.busteni_poiana_pichetul_rosu_cabana_malaiesti,
                        R.raw.busteni_saua_baiului_cabana_diham,
                        R.raw.poiana_tapului_cascada_urlatoarea,
                        R.raw.predeal_valea_iadului_saua_baiului,
                        R.raw.sinaia_cabana_omu_rasnov
                    )

                    val parser = GPXParser()

                    // Clear existing trails
                    trails.clear()

                    // Parse each GPX file
                    gpxResources.forEach { resourceId ->
                        val imageResId = trailImageMapping[resourceId] ?: R.drawable.home_screen_background
                        val trail = parser.parse(context, resourceId, imageResId)
                        if (trail != null) {
                            trails.add(trail)
                        }
                    }
                } catch (e: Exception) {
                    // Handle error loading resources
                    e.printStackTrace()
                }
            }
        }
    }

    // Selected trail for showing on map
    private var _selectedTrail: GPXTrail? = null
    val selectedTrail: GPXTrail? get() = _selectedTrail

    fun selectTrail(trail: GPXTrail) {
        _selectedTrail = trail
    }

    fun clearSelectedTrail() {
        _selectedTrail = null
    }
}