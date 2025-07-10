package com.example.pasipemunti.auth

import android.content.Context
import android.content.SharedPreferences

class UserPreferencesManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "user_preferences"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_EMAIL = "email"
        private const val KEY_FIRST_NAME = "first_name"
        private const val KEY_LAST_NAME = "last_name"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"

        // Singleton pt o singura instanta
        @Volatile
        private var INSTANCE: UserPreferencesManager? = null

        fun getInstance(context: Context): UserPreferencesManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserPreferencesManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    // Salveaza datele utilizatorului dupa login/register
    fun saveUserData(userResponse: UserResponse) {
        with(sharedPreferences.edit()) {
            putInt(KEY_USER_ID, userResponse.user_id ?: -1)
            putString(KEY_USERNAME, userResponse.username ?: "")
            putString(KEY_FIRST_NAME, userResponse.first_name ?: "")
            putString(KEY_LAST_NAME, userResponse.last_name ?: "")
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }

    // email-ul pt ca nu vine in UserResponse
    fun saveEmail(email: String) {
        with(sharedPreferences.edit()) {
            putString(KEY_EMAIL, email)
            apply()
        }
    }

    // incarca datele utilizatorului
    fun getUserData(): UserData? {
        val isLoggedIn = sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
        if (!isLoggedIn) return null

        val userId = sharedPreferences.getInt(KEY_USER_ID, -1)
        val username = sharedPreferences.getString(KEY_USERNAME, "") ?: ""
        val email = sharedPreferences.getString(KEY_EMAIL, "") ?: ""
        val firstName = sharedPreferences.getString(KEY_FIRST_NAME, "") ?: ""
        val lastName = sharedPreferences.getString(KEY_LAST_NAME, "") ?: ""

        if (username.isEmpty()) return null

        return UserData(
            userId = if (userId != -1) userId else null,
            username = username,
            email = email,
            firstName = firstName,
            lastName = lastName
        )
    }

    // Șterge toate datele utilizatorului (logout)
    fun clearUserData() {
        with(sharedPreferences.edit()) {
            clear()
            apply()
        }
    }

    fun updateUserField(key: String, value: String) {
        with(sharedPreferences.edit()) {
            putString(key, value)
            apply()
        }
    }

    fun updateUsername(username: String) {
        updateUserField(KEY_USERNAME, username)
    }

    fun updateEmail(email: String) {
        updateUserField(KEY_EMAIL, email)
    }

    fun updateFirstName(firstName: String) {
        updateUserField(KEY_FIRST_NAME, firstName)
    }

    fun updateLastName(lastName: String) {
        updateUserField(KEY_LAST_NAME, lastName)
    }
}

// Data class pentru datele utilizatorului
data class UserData(
    val userId: Int?,
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String
)

