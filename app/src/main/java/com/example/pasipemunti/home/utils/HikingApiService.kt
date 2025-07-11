package com.example.pasipemunti.home.utils

import com.example.pasipemunti.home.models.Achievement
import com.example.pasipemunti.home.models.ApiResponse
import com.example.pasipemunti.home.models.HikeRequest
import com.example.pasipemunti.home.models.HikingStatsResponse
import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface HikingApiService {
    @GET("user/{userId}/hiking-stats")
    suspend fun getHikingStats(
        @Path("userId") userId: String,
        @Query("months") months: Int = 12
    ): Response<HikingStatsResponse>

    @GET("user/{userId}/achievements")
    suspend fun getUserAchievements(
        @Path("userId") userId: String
    ): Response<List<Achievement>>

    @GET("user/{userId}/weather-stats")
    suspend fun getWeatherStats(@Path("userId") userId: String): Response<WeatherStatsApiResponse>
}

interface ApiService {
    @POST("save-hike")
    suspend fun saveHike(@Body hikeData: HikeRequest): Response<ApiResponse>
}

data class WeatherStatsResponse(
    val condition: String,
    @SerializedName("total_km") // total_km din JSON -> totalKm
    val totalKm: Float,
    @SerializedName("hike_count")
    val hikeCount: Int
)

data class WeatherStatsApiResponse(
    val weather_stats: List<WeatherStatsResponse>
)
