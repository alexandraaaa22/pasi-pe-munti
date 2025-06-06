package com.example.pasipemunti.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomMenuItem(val label: String, val icon: ImageVector) {
    object TrailList : BottomMenuItem("Trasee", Icons.AutoMirrored.Filled.List)
    object Search : BottomMenuItem("Caută", Icons.Default.Search)
    object Home : BottomMenuItem("Acasă", Icons.Default.Home)
    object Map : BottomMenuItem("Hartă", Icons.Default.Map)
    object Profile : BottomMenuItem("Profil", Icons.Default.Person)
}