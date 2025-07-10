package com.example.pasipemunti.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class RegisterViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
            return RegisterViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// Metoda create(...) verifica daca se cere o instanta de RegisterViewModel si, daca da,
// o construieste manual folosind contextul oferit. Daca se cere alt tip de ViewModel,
// factory-ul arunca o exceptie pentru a preveni instantierea incorecta.