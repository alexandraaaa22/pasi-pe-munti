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

    // Mapping of GPX files to images - real implementation would use proper image references
    private val trailImageMapping = mapOf(
        R.raw.babarunca_poiana_zanoaga_culmea_bratocea to R.drawable.home_screen_background, // Example mapping
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
                        R.raw.babarunca_poiana_zanoaga_culmea_bratocea,  // These are placeholders - create these resources
                        R.raw.cabana_m_rosu_saua_gropsoarele_curmatura_stanii,
                        R.raw.cheia_cabana_m_rosu_cabana_vf_ciucas,
                        R.raw.cheia_pasul_bratocea,
                        R.raw.cheia_v_berii_cabana_vf_ciucas,
                        R.raw.dalghiu_poiana_teslei,
                        R.raw.pasul_bratocea_vf_ciucas_pasul_tabla_butii,
                        R.raw.poiana_stanii_pasul_boncuta,
                        R.raw.poiana_stanii_pasul_tabla_butii,
                        R.raw.traseul_ta_v_paraului_alb_la_rascruce,
                        R.raw.traseul_tr_saua_teslei,
                        R.raw.v_berii_cabana_m_rosu
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