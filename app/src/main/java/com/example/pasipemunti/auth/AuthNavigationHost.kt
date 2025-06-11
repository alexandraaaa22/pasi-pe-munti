package com.example.pasipemunti.auth

import BottomMenu
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AuthNavigationHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "auth_choice") {
        composable("auth_choice") { AuthChoiceScreen(navController) }
        composable("login") { LoginScreen(navController) }
        composable("register") { RegisterScreen(navController) }
        composable("main") { BottomMenu() }
    }
}
