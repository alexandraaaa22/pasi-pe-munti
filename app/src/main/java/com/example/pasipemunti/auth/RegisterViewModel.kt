package com.example.pasipemunti.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

//responsabil de logica de înregistrare a unui utilizator nou
class RegisterViewModel(
    private val context: Context
) : ViewModel() {

    // flow pentru a transmite rezultatul înregistrării către UI
    private val _registerResult = MutableStateFlow<UserResponse?>(null)
    val registerResult: StateFlow<UserResponse?> = _registerResult

    // trimite datele utilizatorului catre backend pentru inregistrare
    fun register(
        username: String,
        email: String,
        password: String,
        firstName: String,
        lastName: String
    ) {
        viewModelScope.launch {
            try {
                // apelul API de inregistrare
                val response = RetrofitInstance.api.registerUser(
                    RegisterRequest(username, email, password, firstName, lastName)
                )
                if (response.isSuccessful) {
                    val userResponse = response.body()
                    _registerResult.value = userResponse

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

    // salveaza datele utilizatorului in preferinte daca inregistrarea a fost cu succes
    private fun onRegisterSuccess(context: Context, userResponse: UserResponse, email: String) {
        val userPrefsManager = UserPreferencesManager.getInstance(context)
        userPrefsManager.saveUserData(userResponse)
        userPrefsManager.saveEmail(email) //  email-ul nu e in UserResponse
    }
}
