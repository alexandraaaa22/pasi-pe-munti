package com.example.pasipemunti.home.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pasipemunti.home.models.AppColors
import com.example.pasipemunti.home.models.WeatherOption
import com.example.pasipemunti.home.models.WeatherStats
import com.example.pasipemunti.home.utils.getWeatherColor
import com.example.pasipemunti.home.utils.getWeatherIcon

// Weather Information Section
@Composable
fun WeatherSection(stats: List<WeatherStats>) {
    // Define weather options here or as a global constant
    val weatherOptions = listOf(
        WeatherOption("Însorit", "☀️", "sunny"),
        WeatherOption("Înnorat", "☁️", "cloudy"),
        WeatherOption("Ploios", "🌧️", "rainy"),
        WeatherOption("Zăpadă", "❄️", "snowy"),
        WeatherOption("Ceață", "🌫️", "foggy"),
        WeatherOption("Vânt", "💨", "windy")
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Cloud,
                    contentDescription = "Weather",
                    tint = AppColors.skyBlue,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Informații meteo",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = AppColors.textDark
                )
            }

            LazyRow( // Use LazyRow to display items horizontally
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp), // Space between items
                contentPadding = PaddingValues(horizontal = 0.dp) // No horizontal padding for the row itself
            ) {
                items(weatherOptions) { option -> // Iterate through the predefined list of options
                    // Find the actual stats for the current condition
                    val weatherStat = stats.find { it.condition.lowercase() == option.conditionKey.lowercase() }

                    // Display actual values if they exist, otherwise 0
                    val totalKm = weatherStat?.totalKm ?: 0f
                    val hikeCount = weatherStat?.hikeCount ?: 0

                    WeatherInsightItem(
                        title = option.label, // Use the label from WeatherOption for the title
                        value = "${"%.1f".format(totalKm)} km",
                        subtitle = "$hikeCount ${if (hikeCount == 1) "drumeție" else "drumeții"}",
                        icon = getWeatherIcon(option.conditionKey), // Use existing function for icon
                        iconTint = getWeatherColor(option.conditionKey), // Use existing function for color
                        modifier = Modifier.width(100.dp) // Adjust width of each item
                    )
                }
            }
        }
    }
}

@Composable
fun WeatherInsightItem(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(horizontal = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(28.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = title,
            fontSize = 14.sp, // Slightly smaller font for the label to fit
            fontWeight = FontWeight.Bold,
            color = AppColors.textDark,
            maxLines = 1 // Ensure it doesn't wrap
        )

        Text(
            text = value, // Total kilometers
            fontSize = 12.sp,
            color = AppColors.textLight
        )

        Text(
            text = subtitle, // Number of hikes
            fontSize = 12.sp,
            color = iconTint, // Use icon color for subtitle
            fontWeight = FontWeight.Medium
        )
    }
}