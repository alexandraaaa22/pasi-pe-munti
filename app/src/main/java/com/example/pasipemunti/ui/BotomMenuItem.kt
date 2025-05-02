package com.example.pasipemunti.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomMenuItem(val label: String, val icon: ImageVector) {
    object TrailList : BottomMenuItem("Trails", Icons.AutoMirrored.Filled.List)
    object Search : BottomMenuItem("Search", Icons.Default.Search)
    object Home : BottomMenuItem("Home", Icons.Default.Home)
    object Map : BottomMenuItem("Map", Icons.Default.Map)
    object Profile : BottomMenuItem("Profile", Icons.Default.Person)
}