package com.example.pasipemunti.home.models

import androidx.compose.ui.graphics.Color
import com.google.gson.annotations.SerializedName

object AppColors {
    val primaryGreen = Color(0xFF4CAF50)
    val accentGreen = Color(0xFF8BC34A)
    val accentOrange = Color(0xFFFF9800)
    val skyBlue = Color(0xFF2196F3)
    val accentBlue = Color(0xFF03A9F4)
    val textDark = Color(0xFF212121)
    val textMedium = Color(0xFF757575)
    val textLight = Color(0xFFBDBDBD)
}

enum class TimeRange(val months: Int, val label: String) {
    SIX_MONTHS(6, "6 Luni"),
    TWELVE_MONTHS(12, "12 Luni")
}

enum class ChartType {
    BAR, LINE
}

data class Achievement(
    val id: Int,
    val name: String,
    val description: String,
    @SerializedName("icon_name")
    val iconName: String?,
    val progress: Float,
    val earned: Boolean
)

data class WeatherStats(
    val condition: String,
    @SerializedName("total_km")
    val totalKm: Float,
    @SerializedName("hike_count") // hike_count din JSON -> hikeCount
    val hikeCount: Int
)

// Data class to define weather options for display
data class WeatherOption(
    val label: String, // de ex insorit
    val emoji: String,
    val conditionKey: String // Key matching 'condition' from API (e.g., "sunny")
)