package com.example.pasipemunti.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val context: Context
) : ViewModel() {
    private val _registerResult = MutableStateFlow<UserResponse?>(null)
    val registerResult: StateFlow<UserResponse?> = _registerResult

    private val userPreferencesManager = UserPreferencesManager.getInstance(context)

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
                    val userResponse = response.body()
                    _registerResult.value = userResponse

                    // ADAUGĂ: Salvează datele utilizatorului după register reușit
                    userResponse?.let { user ->
                        if (user.error == null) {
                            onRegisterSuccess(context, user, email)
                        }
                    }
                } else {
                    _registerResult.value = UserResponse(
                        error = "Înregistrare eșuată",
                        message = null,
                        user_id = null,
                        username = null,
                        first_name = null,
                        last_name = null
                    )
                }
            } catch (e: Exception) {
                _registerResult.value = UserResponse(
                    error = "Eroare rețea: ${e.localizedMessage}",
                    message = null,
                    user_id = null,
                    username = null,
                    first_name = null,
                    last_name = null
                )
            }
        }
    }

    private fun onRegisterSuccess(context: Context, userResponse: UserResponse, email: String) {
        val userPrefsManager = UserPreferencesManager.getInstance(context)
        userPrefsManager.saveUserData(userResponse)
        userPrefsManager.saveEmail(email) // dacă email-ul nu vine în UserResponse
    }
}
