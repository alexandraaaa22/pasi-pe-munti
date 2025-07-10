package com.example.pasipemunti.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class LoginViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// ViewModelFactory -> creeaza LoginViewModel ce are nevoie de Context in constructor
// Avem nevoie de contextul pentru a initializa UserPreferencesManager
// Fara acest factory, sistemul nu ar sti cum sa furnizeze Contextul necesar ViewModel-ului
