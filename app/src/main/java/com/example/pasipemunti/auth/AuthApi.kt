package com.example.pasipemunti.auth

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

// interfata Retrofit pt apelurile API legate de autentificare
interface AuthApi {

    //trimitem obiecte de tip LoginRequest (email si parola) sau RegisterRequest
    // si primim de la server un obiect UserResponse (datele user-ului)
    @POST("/login")
    suspend fun loginUser(@Body loginRequest: LoginRequest): Response<UserResponse>

    @POST("/register")
    suspend fun registerUser(@Body registerRequest: RegisterRequest): Response<UserResponse>
}
