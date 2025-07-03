package com.example.pasipemunti.home

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
}

interface ApiService {
    @POST("save-hike")
    suspend fun saveHike(@Body hikeData: HikeRequest): Response<ApiResponse>
}