package com.example.pasipemunti.searchhike

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pasipemunti.home.HikeRequest
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class SearchHikeViewModel : ViewModel() {
    // UI State
    var start by mutableStateOf("")
    var end by mutableStateOf("")
    var routePoints by mutableStateOf<List<GeoPoint>>(emptyList())
    var isNavigating by mutableStateOf(false)
    var currentLocation by mutableStateOf<GeoPoint?>(null)
    var locationPermissionGranted by mutableStateOf(false)

    // Navigation stats
    var distanceTraveled by mutableStateOf(0.0) // in meters
    var distanceRemaining by mutableStateOf(0.0) // in meters
    var currentAltitude by mutableStateOf(0.0) // in meters
    var elapsedTimeMillis by mutableStateOf(0L) // in milliseconds

    // Hike tracking variables
    private var hikeStartTime: Long = 0L
    private var startLocationName: String = ""
    private var endLocationName: String = ""
    private var elevationGain: Int = 0
    private var minAltitude: Double = Double.MAX_VALUE
    private var maxAltitude: Double = Double.MIN_VALUE

    // API Services
    private val nominatim = Retrofit.Builder()
        .baseUrl("https://nominatim.openstreetmap.org/")
        .addConverterFactory(GsonConverterFactory.create())
        .build().create(NominatimService::class.java)

    private val ors = Retrofit.Builder()
        .baseUrl("https://api.openrouteservice.org/")
        .addConverterFactory(GsonConverterFactory.create())
        .build().create(ORSService::class.java)

    private val apiService = ApiClient.apiService

    // Location tracking
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
                    val newAltitude = location.altitude

                    // Track elevation changes
                    if (newAltitude < minAltitude) minAltitude = newAltitude
                    if (newAltitude > maxAltitude) maxAltitude = newAltitude
                    elevationGain = (maxAltitude - minAltitude).toInt()
                    currentAltitude = newAltitude

                    if (currentLocation != null) {
                        // Calculate distance traveled since last update
                        val distance = currentLocation!!.distanceToAsDouble(newLocation)
                        distanceTraveled += distance
                        distanceRemaining = (distanceRemaining - distance).coerceAtLeast(0.0)
                    }

                    currentLocation = newLocation
                    Log.d("LocationUpdate", "New location: ${currentLocation?.latitude}, ${currentLocation?.longitude}, Alt: ${currentAltitude}, DistTraveled: ${distanceTraveled}, DistRemaining: ${distanceRemaining}")

                    lastLocationUpdateTime = System.currentTimeMillis()
                }
            }
        }
    }

    fun startNavigation() {
        if (locationPermissionGranted && fusedLocationClient != null) {
            isNavigating = true

            // Initialize hike tracking
            hikeStartTime = System.currentTimeMillis()
            startLocationName = start
            endLocationName = end
            minAltitude = Double.MAX_VALUE
            maxAltitude = Double.MIN_VALUE
            elevationGain = 0

            // Reset navigation stats
            distanceTraveled = 0.0
            elapsedTimeMillis = 0L
            currentAltitude = 0.0

            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(3000)
                .build()

            try {
                fusedLocationClient?.requestLocationUpdates(locationRequest, locationCallback, null)
                startTimer()
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
        timerJob?.cancel()
        currentLocation = null
    }

    fun finishHike(userId: Int, difficulty: String = "moderate", weatherCondition: String = "sunny") {
        if (!isNavigating) {
            Log.w("FinishHike", "Drumeția nu este activă, nu se poate finaliza.")
            return
        }

        val hikeEndTime = System.currentTimeMillis()
        val totalDurationMinutes = ((hikeEndTime - hikeStartTime) / 60000).toInt()

        val startPoint = routePoints.firstOrNull()
        val endPoint = routePoints.lastOrNull()

        val hikeData = HikeRequest(
            user_id = userId,
            date = getCurrentDate(),
            distance_km = (distanceTraveled / 1000).toFloat().coerceAtLeast(0.0f),
            duration_minutes = totalDurationMinutes.coerceAtLeast(1),
            elevation_gain = elevationGain,
            start_latitude = startPoint?.latitude,
            start_longitude = startPoint?.longitude,
            end_latitude = endPoint?.latitude,
            end_longitude = endPoint?.longitude,
            weather_condition = weatherCondition,
            difficulty = difficulty,
            start_time = formatTime(hikeStartTime),
            end_time = formatTime(hikeEndTime),
            start_location_name = startLocationName.ifEmpty { "Start nespecificat" },
            end_location_name = endLocationName.ifEmpty { "Stop nespecificat" }
        )

        Log.d("FinishHike", "Trimitem hike: $hikeData")
        saveHikeToServer(hikeData)
    }


    private fun saveHikeToServer(hikeData: HikeRequest) {
        viewModelScope.launch {
            try {
                val response = apiService.saveHike(hikeData)
                if (response.isSuccessful) {
                    Log.d("HikeSave", "Hike saved successfully: ${response.body()?.message}")
                    stopNavigation()
                } else {
                    Log.e("HikeSave", "Error saving hike: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("HikeSave", "Network error: ${e.message}")
                // You can add offline saving logic here
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isNavigating) {
                delay(1000)
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

    private fun getCurrentDate(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return formatter.format(Date())
    }

    private fun formatTime(timestamp: Long): String {
        val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return formatter.format(Date(timestamp))
    }

    override fun onCleared() {
        super.onCleared()
        stopNavigation()
    }
}