package com.example.pasipemunti.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    private val _loginResult = MutableStateFlow<UserResponse?>(null)
    val loginResult: StateFlow<UserResponse?> = _loginResult

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.loginUser(LoginRequest(email, password))
                if (response.isSuccessful) {
                    _loginResult.value = response.body()
                } else {
                    _loginResult.value = UserResponse(
                        message = "",
                        user_id = null,
                        username = null,
                        first_name = null,
                        last_name = null,
                        error = "Eroare la autentificare"
                    )
                }
            } catch (e: Exception) {
                _loginResult.value = UserResponse(
                    message = "",
                    user_id = null,
                    username = null,
                    first_name = null,
                    last_name = null,
                    error = "Eroare rețea: ${e.localizedMessage}"
                )
            }
        }
    }
}
