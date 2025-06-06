package com.example.pasipemunti

import BottomMenu
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.graphics.Color

@Composable
fun SetStatusBarColor() {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            // Schimbă mov-ul cu verde sau orice altă culoare dorești
            window.statusBarColor = Color(0xFF2E7D32).toArgb() // Verde
            // sau poți folosi:
            // window.statusBarColor = Color.White.toArgb() // Alb
            // window.statusBarColor = Color(0xFF1976D2).toArgb() // Albastru

            // Iconițele din status bar să fie albe pe fundal verde
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = false
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BottomMenu()
            SetStatusBarColor()
        }
    }
}