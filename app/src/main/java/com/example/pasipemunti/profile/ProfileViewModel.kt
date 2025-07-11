package com.example.pasipemunti.profile

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
                    username = userData.username,
                    email = userData.email,
                    firstName = userData.firstName,
                    lastName = userData.lastName,
                    profilePictureUri = userData.profilePictureUri
                )
            }

            _isLoading.value = false
        }
    }

    fun updateProfilePicture(uri: String) {
        viewModelScope.launch {
            userPreferencesManager.updateProfilePicture(uri)

            loadUserInfo()
        }
    }
}