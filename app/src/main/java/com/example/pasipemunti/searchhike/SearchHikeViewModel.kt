package com.example.pasipemunti.searchhike

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SearchHikeViewModel : ViewModel() {
    var start by mutableStateOf("")
    var end by mutableStateOf("")
    var routePoints by mutableStateOf<List<GeoPoint>>(emptyList())

    private val nominatim = Retrofit.Builder()
        .baseUrl("https://nominatim.openstreetmap.org/")
        .addConverterFactory(GsonConverterFactory.create())
        .build().create(NominatimService::class.java)

    private val ors = Retrofit.Builder()
        .baseUrl("https://api.openrouteservice.org/")
        .addConverterFactory(GsonConverterFactory.create())
        .build().create(ORSService::class.java)

    fun fetchRoute(apiKey: String) {
        viewModelScope.launch {
            try {
                val startResult = nominatim.search(start).firstOrNull()
                val endResult = nominatim.search(end).firstOrNull()

                if (startResult != null && endResult != null) {
                    val startCoord = listOf(startResult.lon.toDouble(), startResult.lat.toDouble())
                    val endCoord = listOf(endResult.lon.toDouble(), endResult.lat.toDouble())
                    val response = ors.getRoute(ORSRequest(listOf(startCoord, endCoord)), apiKey)

                    routePoints = response.features
                        .flatMap { it.geometry.coordinates }
                        .map { GeoPoint(it[1], it[0]) }
                }
            } catch (e: Exception) {
                Log.e("Route", "Error: ${e.message}")
            }
        }
    }
}
