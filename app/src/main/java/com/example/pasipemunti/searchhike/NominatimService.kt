package com.example.pasipemunti.searchhike

import retrofit2.http.GET
import retrofit2.http.Query

interface NominatimService {
    @GET("search")
    suspend fun search(
        @Query("q") query: String,
        @Query("format") format: String = "json"
    ): List<NominatimResult>
}

data class NominatimResult(
    val lat: String,
    val lon: String
)

