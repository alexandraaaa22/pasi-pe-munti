package com.example.pasipemunti.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

//logica de autentificare, comunicarea intre UI si backend
class LoginViewModel(
    private val context: Context // ADAUGĂ context în constructor
) : ViewModel() {

    //starea login-ului
    private val _loginResult = MutableStateFlow<UserResponse?>(null) //intern
    val loginResult: StateFlow<UserResponse?> = _loginResult //public (LoginScreen -> collectAsState())

    // salvam datele user-ului dupa autentificare
    private val userPreferencesManager = UserPreferencesManager.getInstance(context)

    // Functia face apel asincron catre server
    // Odata ce primeste raspunsul, actualizeaza starea interna a ViewModel-ului (loginResult)
    // cu datele utilizatorului daca autentificarea a fost ok sau cu un mesaj de eroare daca a esuat
    // Actualizarea starii permite UI-ului sa reactioneze automat, afisand ecranul principal in cazul unui login reusit sau un mesaj de eroare clar in caz contrar.
    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                // apelul API de autentificare
                val response = RetrofitInstance.api.loginUser(LoginRequest(email, password))
                if (response.isSuccessful) {
                    val userResponse = response.body()
                    _loginResult.value = userResponse

                    userResponse?.let { user ->
                        if (user.error == null) {
                            userPreferencesManager.saveUserData(user)
                            userPreferencesManager.saveEmail(email)
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
