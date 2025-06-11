package com.example.pasipemunti.auth

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("/login")
    suspend fun loginUser(@Body loginRequest: LoginRequest): Response<UserResponse>

    @POST("/register")
    suspend fun registerUser(@Body registerRequest: RegisterRequest): Response<UserResponse>
}
