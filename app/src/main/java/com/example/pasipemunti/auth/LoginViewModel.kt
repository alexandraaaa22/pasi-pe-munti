package com.example.pasipemunti.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val context: Context // ADAUGĂ context în constructor
) : ViewModel() {
    private val _loginResult = MutableStateFlow<UserResponse?>(null)
    val loginResult: StateFlow<UserResponse?> = _loginResult

    // Inițializează UserPreferencesManager
    private val userPreferencesManager = UserPreferencesManager.getInstance(context)

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.loginUser(LoginRequest(email, password))
                if (response.isSuccessful) {
                    val userResponse = response.body()
                    _loginResult.value = userResponse

                    // ADAUGĂ: Salvează datele utilizatorului după login reușit
                    userResponse?.let { user ->
                        if (user.error == null) {
                            userPreferencesManager.saveUserData(user)
                            userPreferencesManager.saveEmail(email) // Salvează email-ul separat
                        }
                    }
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
