package com.example.pasipemunti.searchhike

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.math.roundToInt

class SearchHikeViewModel : ViewModel() {
    var start by mutableStateOf("")
    var end by mutableStateOf("")
    var routePoints by mutableStateOf<List<GeoPoint>>(emptyList())
    var isNavigating by mutableStateOf(false)
    var currentLocation by mutableStateOf<GeoPoint?>(null)
    var locationPermissionGranted by mutableStateOf(false)

    // New navigation stats
    var distanceTraveled by mutableStateOf(0.0) // in meters
    var distanceRemaining by mutableStateOf(0.0) // in meters
    var currentAltitude by mutableStateOf(0.0) // in meters
    var elapsedTimeMillis by mutableStateOf(0L) // in milliseconds

    private val nominatim = Retrofit.Builder()
        .baseUrl("https://nominatim.openstreetmap.org/")
        .addConverterFactory(GsonConverterFactory.create())
        .build().create(NominatimService::class.java)
    private val ors = Retrofit.Builder()
        .baseUrl("https://api.openrouteservice.org/")
        .addConverterFactory(GsonConverterFactory.create())
        .build().create(ORSService::class.java)

    private var fusedLocationClient: FusedLocationProviderClient? = null
    private lateinit var locationCallback: LocationCallback
    private var timerJob: Job? = null
    private var lastLocationUpdateTime: Long = 0L

    fun fetchRoute(apiKey: String) {
        viewModelScope.launch {
            try {
                val startResult = nominatim.search(start).firstOrNull()
                val endResult = nominatim.search(end).firstOrNull()
                if (startResult != null && endResult != null) {
                    val startCoord = listOf(startResult.lon.toDouble(), startResult.lat.toDouble())
                    val endCoord = listOf(endResult.lon.toDouble(), endResult.lat.toDouble())

                    Log.d("RouteDebug", "Nominatim Start: ${startResult.lat}, ${startResult.lon}")
                    Log.d("RouteDebug", "Nominatim End: ${endResult.lat}, ${endResult.lon}")
                    Log.d("RouteDebug", "ORS Request Coords: Start=$startCoord, End=$endCoord")

                    val response = ors.getRoute(ORSRequest(listOf(startCoord, endCoord)), apiKey)
                    routePoints = response.features
                        .flatMap { it.geometry.coordinates }
                        .map { GeoPoint(it[1], it[0]) }

                    // Initialize distance remaining with total route length
                    distanceRemaining = calculateTotalRouteLength(routePoints)
                }
                else {
                    Log.e("RouteDebug", "Nominatim search failed for start or end location.")
                    if (startResult == null) Log.e(
                        "RouteDebug",
                        "Start result is null for '$start'"
                    )
                    if (endResult == null) Log.e("RouteDebug", "End result is null for '$end'")
                }
            } catch (e: Exception) {
                Log.e("Route", "Error: ${e.message}")
            }
        }
    }

    fun setFusedLocationClient(client: FusedLocationProviderClient) {
        fusedLocationClient = client
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    val newLocation = GeoPoint(location.latitude, location.longitude)
                    currentAltitude = location.altitude // Update altitude

                    if (currentLocation != null) {
                        // Calculate distance traveled since last update
                        val distance = currentLocation!!.distanceToAsDouble(newLocation)
                        distanceTraveled += distance
                        distanceRemaining = (distanceRemaining - distance).coerceAtLeast(0.0) // Ensure it doesn't go negative
                    }
                    currentLocation = newLocation
                    Log.d("LocationUpdate", "New location: ${currentLocation?.latitude}, ${currentLocation?.longitude}, Alt: ${currentAltitude}, DistTraveled: ${distanceTraveled}, DistRemaining: ${distanceRemaining}")

                    lastLocationUpdateTime = System.currentTimeMillis() // Update last update time
                }
            }
        }
    }

    fun startNavigation() {
        if (locationPermissionGranted && fusedLocationClient != null) {
            isNavigating = true
            // Reset stats when starting new navigation
            distanceTraveled = 0.0
            elapsedTimeMillis = 0L
            currentAltitude = 0.0 // Reset or get initial altitude

            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000) // Update every 5 seconds
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(3000) // Minimum 3 seconds interval
                .build()

            try {
                fusedLocationClient?.requestLocationUpdates(locationRequest, locationCallback, null)
                startTimer() // Start the timer
            } catch (e: SecurityException) {
                Log.e("SearchHikeViewModel", "Location permission not granted: ${e.message}")
            }
        } else {
            Log.e("SearchHikeViewModel", "Cannot start navigation: Location permission not granted or FusedLocationClient not set.")
        }
    }

    fun stopNavigation() {
        isNavigating = false
        fusedLocationClient?.removeLocationUpdates(locationCallback)
        timerJob?.cancel() // Stop the timer
        currentLocation = null // Clear current location when stopping
        // Optionally, reset other stats here if you want them cleared on stop
        // distanceTraveled = 0.0
        // distanceRemaining = 0.0 // Re-calculate or reset if needed
        // currentAltitude = 0.0
        // elapsedTimeMillis = 0L
    }

    // No longer need setLocationPermissionGranted function, direct assignment is used

    private fun startTimer() {
        timerJob?.cancel() // Cancel any existing timer
        timerJob = viewModelScope.launch {
            while (isNavigating) {
                delay(1000) // Update every second
                elapsedTimeMillis += 1000
            }
        }
    }

    private fun calculateTotalRouteLength(points: List<GeoPoint>): Double {
        if (points.size < 2) return 0.0
        var totalDistance = 0.0
        for (i in 0 until points.size - 1) {
            totalDistance += points[i].distanceToAsDouble(points[i+1])
        }
        return totalDistance
    }


    override fun onCleared() {
        super.onCleared()
        stopNavigation() // Ensure location updates and timer are stopped when ViewModel is cleared
    }
}