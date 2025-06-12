package com.example.pasipemunti.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.pasipemunti.auth.UserPreferencesManager

class ProfileViewModel(
    private val userPreferencesManager: UserPreferencesManager
) : ViewModel() {
    private val _userInfo = MutableStateFlow<UserInfo?>(null)
    val userInfo: StateFlow<UserInfo?> = _userInfo

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadUserInfo() {
        viewModelScope.launch {
            _isLoading.value = true

            val userData = userPreferencesManager.getUserData()
            if (userData != null) {
                _userInfo.value = UserInfo(
                    userId = userData.userId,
                    username = userData.username,
                    email = userData.email,
                    firstName = userData.firstName,
                    lastName = userData.lastName
                )
            }

            _isLoading.value = false
        }
    }

    fun logout() {
        viewModelScope.launch {
            // Șterge datele utilizatorului
            userPreferencesManager.clearUserData()

            // Resetează state-ul
            _userInfo.value = null

            // Aici poți adăuga logica de navigare către ecranul de login
            // sau poți emite un event pentru a informa UI-ul
        }
    }

    fun updateUserInfo(
        username: String? = null,
        email: String? = null,
        firstName: String? = null,
        lastName: String? = null
    ) {
        viewModelScope.launch {
            username?.let { userPreferencesManager.updateUsername(it) }
            email?.let { userPreferencesManager.updateEmail(it) }
            firstName?.let { userPreferencesManager.updateFirstName(it) }
            lastName?.let { userPreferencesManager.updateLastName(it) }

            // Reîncarcă datele pentru a actualiza UI-ul
            loadUserInfo()
        }
    }
}