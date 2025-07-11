package com.example.pasipemunti.home.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.pasipemunti.home.models.AppColors

fun getAchievementIcon(iconName: String?): ImageVector {
    return when (iconName) {
        "trail_master" -> Icons.Default.Hiking
        "fire_boots" -> Icons.Default.LocalFireDepartment
        "altitude_master" -> Icons.Default.Terrain
        "route_collector" -> Icons.Default.Explore
        "weather_sunny" -> Icons.Default.WbSunny
        "weather_cloudy" -> Icons.Default.Cloud
        "weather_rainy" -> Icons.Default.Grain
        "steep_climb" -> Icons.Default.FormatLineSpacing
        "ultra_hiker" -> Icons.Default.DirectionsWalk
        "weather_warrior" -> Icons.Default.Umbrella
        "mountain_marathon" -> Icons.Default.Timer
        "hardcore_hiker" -> Icons.Default.Whatshot
        "globe_hiker" -> Icons.Default.Public
        "sunrise_boots" -> Icons.Default.WbTwilight
        "trail_sprinter" -> Icons.Default.DirectionsRun
        "relax_hiker" -> Icons.Default.Spa
        "routine_boots" -> Icons.Default.Loop
        else -> Icons.Default.EmojiEvents
    }
}

fun getAchievementColor(iconName: String?): Color {
    return when (iconName) {
        "trail_master" -> Color(0xFF7A5304)
        "fire_boots" -> AppColors.accentOrange
        "altitude_master" -> AppColors.primaryGreen
        "route_collector" -> AppColors.skyBlue
        "weather_sunny" -> AppColors.accentOrange
        "weather_cloudy" -> AppColors.skyBlue
        "weather_rainy" -> AppColors.textLight
        "steep_climb" -> Color(0xFFBF360C)
        "ultra_hiker" -> Color(0xFF33691E)
        "weather_warrior" -> Color(0xFF0277BD)
        "mountain_marathon" -> Color(0xFF6A1B9A)
        "hardcore_hiker" -> Color(0xFFD32F2F)
        "globe_hiker" -> Color(0xFF00838F)
        "sunrise_boots" -> Color(0xFFFF8F00)
        "trail_sprinter" -> Color(0xFF1B5E20)
        "relax_hiker" -> Color(0xFF0288D1)
        "routine_boots" -> Color(0xFF455A64)
        else -> AppColors.primaryGreen
    }
}

fun getWeatherIcon(condition: String): ImageVector {
    return when (condition.lowercase()) {
        "sunny" -> Icons.Default.WbSunny
        "cloudy" -> Icons.Default.Cloud
        "rainy" -> Icons.Default.Grain
        "snowy" -> Icons.Default.AcUnit
        "stormy" -> Icons.Default.Thunderstorm
        "foggy" -> Icons.Default.Cloud
        "windy" -> Icons.Default.Air
        else -> Icons.Default.Cloud
    }
}

fun getWeatherColor(condition: String): Color {
    return when (condition.lowercase()) {
        "sunny" -> AppColors.accentOrange
        "cloudy" -> AppColors.skyBlue
        "rainy" -> AppColors.textLight
        "snowy" -> Color(0xFFADD8E6)
        "stormy" -> Color(0xFF5D4037)
        "foggy" -> Color(0xFFBDBDBD)
        "windy" -> Color(0xFF9E9E9E)
        else -> AppColors.textLight
    }
}

fun getWeatherDisplayName(condition: String): String {
    return when (condition.lowercase()) {
        "sunny" -> "Soare"
        "cloudy" -> "Înnorat"
        "rainy" -> "Ploaie"
        "snowy" -> "Ninsoare"
        "stormy" -> "Furtună"
        "foggy" -> "Ceață"
        "windy" -> "Vânt"
        else -> condition.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}