package com.example.pasipemunti.auth

// modelele pt cererile de login si register
data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val first_name: String,
    val last_name: String
)

// model pt raspunsul API
data class UserResponse(
    val message: String?,
    val user_id: Int?,
    val username: String?,
    val first_name: String?,
    val last_name: String?,
    val error: String? = null
)