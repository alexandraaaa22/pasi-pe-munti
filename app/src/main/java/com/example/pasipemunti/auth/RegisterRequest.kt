package com.example.pasipemunti.auth

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val first_name: String,
    val last_name: String
)
