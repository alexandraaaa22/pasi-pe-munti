package com.example.pasipemunti.home.models

data class HikeData(
    val month: String,
    val distance: Float,
    val year: Int
)

data class HikingStatsResponse(
    val hikes: List<HikeData>,
    val totalDistance: Float,
    val averageDistance: Float
)

data class HikeRequest(
    val user_id: Int,
    val date: String,
    val distance_km: Float,
    val duration_minutes: Int,
    val elevation_gain: Int,
    val start_latitude: Double?,
    val start_longitude: Double?,
    val end_latitude: Double?,
    val end_longitude: Double?,
    val weather_condition: String,
    val difficulty: String,
    val start_time: String,
    val end_time: String,
    val start_location_name: String,
    val end_location_name: String
)

data class ApiResponse(
    val status: String,
    val message: String
)