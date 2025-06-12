package com.example.pasipemunti.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.pasipemunti.auth.UserPreferencesManager

class ProfileViewModelFactory(
    private val userPreferencesManager: UserPreferencesManager
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(userPreferencesManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}