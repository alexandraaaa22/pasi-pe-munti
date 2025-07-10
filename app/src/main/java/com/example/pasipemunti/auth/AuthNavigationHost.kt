package com.example.pasipemunti.auth

import BottomMenu
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

// structura de navigare pentru partea de autentificare
@Composable
fun AuthNavigationHost() {
    val navController = rememberNavController() // cu navController, controlam si schimbam ecranele

    //rutele care pot fi urmate
    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") { SplashScreen(navController) }
        composable("auth_choice") { AuthChoiceScreen(navController) }
        composable("login") { LoginScreen(navController) }
        composable("register") { RegisterScreen(navController) }
        composable("main") { BottomMenu() }
    }
}
