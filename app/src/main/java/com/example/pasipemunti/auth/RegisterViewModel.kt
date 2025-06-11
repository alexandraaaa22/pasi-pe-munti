package com.example.pasipemunti.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {
    private val _registerResult = MutableStateFlow<UserResponse?>(null)
    val registerResult: StateFlow<UserResponse?> = _registerResult

    fun register(
        username: String,
        email: String,
        password: String,
        firstName: String,
        lastName: String
    ) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.registerUser(
                    RegisterRequest(username, email, password, firstName, lastName)
                )
                if (response.isSuccessful) {
                    _registerResult.value = response.body()
                } else {
                    _registerResult.value = UserResponse(error = "Înregistrare eșuată", message = null, user_id = null, username = null, first_name = null, last_name = null)
                }
            } catch (e: Exception) {
                _registerResult.value = UserResponse(error = "Eroare rețea: ${e.localizedMessage}", message = null, user_id = null, username = null, first_name = null, last_name = null)
            }
        }
    }
}
