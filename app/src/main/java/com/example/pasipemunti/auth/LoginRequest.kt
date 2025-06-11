package com.example.pasipemunti.auth

// Cererea de login
data class LoginRequest(
    val email: String,
    val password: String
)

// Răspunsul de la server după login
data class UserResponse(
    val message: String?,
    val user_id: Int?,
    val username: String?,
    val first_name: String?,
    val last_name: String?,
    val error: String? = null
)
