package com.example.pasipemunti.searchhike

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ORSService {
    @POST("v2/directions/foot-hiking/geojson")
    suspend fun getRoute(
        @Body body: ORSRequest,
        @Header("Authorization") apiKey: String = "5b3ce3597851110001cf62482d243a035da94fa886b41a12d9045b47"
    ): ORSResponse
}

data class ORSRequest(val coordinates: List<List<Double>>)
data class ORSResponse(val features: List<Feature>)
data class Feature(val geometry: Geometry)
data class Geometry(val coordinates: List<List<Double>>)
