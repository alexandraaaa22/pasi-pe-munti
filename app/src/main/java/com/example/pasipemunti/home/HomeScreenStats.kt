package com.example.pasipemunti.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.Hiking
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.yml.charts.axis.AxisData
import co.yml.charts.common.model.Point
import co.yml.charts.ui.barchart.BarChart
import co.yml.charts.ui.barchart.models.*
import co.yml.charts.ui.linechart.LineChart
import co.yml.charts.ui.linechart.model.*
import com.example.pasipemunti.R
import androidx.lifecycle.viewmodel.compose.viewModel

object AppColors {
    val primaryGreen = Color(0xFF4CAF50) // A vibrant green
    val accentGreen = Color(0xFF8BC34A) // A lighter accent green
    val lightGreen = Color(0xFFC8E6C9) // Very light green for backgrounds
    val accentOrange = Color(0xFFFF9800) // A warm orange
    val skyBlue = Color(0xFF2196F3) // A clear blue
    val accentBlue = Color(0xFF03A9F4) // Another accent blue
    val textDark = Color(0xFF212121) // Dark text
    val textMedium = Color(0xFF757575) // Medium grey text
    val textLight = Color(0xFFBDBDBD) // Light grey text
    val backgroundLight = Color(0xFFF5F5F5) // Light background
    val surface = Color(0xFFFFFFFF) // White surface for cards
    val surfaceVariant = Color(0xFFEEEEEE) // Slightly darker surface for grids etc.
    val borderLight = Color(0xFFE0E0E0) // Light border
}

enum class TimeRange(val months: Int, val label: String) {
    SIX_MONTHS(6, "6 Months"),
    TWELVE_MONTHS(12, "12 Months")
}

enum class ChartType {
    BAR, LINE
}

@Composable
fun HikingStatsScreen(
    userId: String, // Adaugă userId ca parametru
    viewModel: HikingStatsViewModel = viewModel()
) {
    var selectedTimeRange by remember { mutableStateOf(TimeRange.SIX_MONTHS) }
    var selectedChartType by remember { mutableStateOf(ChartType.BAR) }

    // Observă datele din ViewModel
    val hikingData by viewModel.hikingData
    val months by viewModel.months
    val isLoading by viewModel.isLoading
    val error by viewModel.error

    val achievements by viewModel.achievements
    val isAchievementsLoading by viewModel.isAchievementsLoading
    val achievementsError by viewModel.achievementsError

    // Încarcă datele când se schimbă timpul sau userId
    LaunchedEffect(selectedTimeRange, userId) {
        viewModel.loadHikingData(userId, selectedTimeRange)
    }

    // Filter data based on selected time range
    val displayedData = hikingData.take(selectedTimeRange.months)
    val displayedMonths = months.take(selectedTimeRange.months)

    Box(modifier = Modifier.fillMaxSize()) {
        // Background image with overlay
        Image(
            painter = painterResource(id = R.drawable.home_screen_background),
            contentDescription = "Mountains background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Semi-transparent overlay for better readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x99FFFFFF))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text(
                text = "Rucsacul cu amintiri",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Kilometri parcurși pe lună",
                fontSize = 16.sp,
                color = Color(0xFF5E5E5E),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Chart time range selector
            TimeRangeSelector(
                selectedTimeRange = selectedTimeRange,
                onTimeRangeSelected = { selectedTimeRange = it }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Chart container with shadow and rounded corners
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 4.dp
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    when {
                        isLoading -> {
                            // Loading indicator
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = AppColors.primaryGreen
                                )
                            }
                        }
                        error != null -> {
                            // Error message
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Eroare",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Red
                                    )
                                    Text(
                                        text = error ?: "Eroare necunoscută",
                                        fontSize = 14.sp,
                                        color = AppColors.textLight,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = {
                                            viewModel.loadHikingData(userId, selectedTimeRange)
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = AppColors.primaryGreen
                                        )
                                    ) {
                                        Text("Reîncarcă")
                                    }
                                }
                            }
                        }
                        displayedData.isNotEmpty() -> {
                            // Chart
                            when (selectedChartType) {
                                ChartType.BAR -> HikingBarChart(
                                    hikingData = displayedData,
                                    months = displayedMonths
                                )
                                ChartType.LINE -> HikingLineChart(
                                    hikingData = displayedData,
                                    months = displayedMonths
                                )
                            }
                        }
                        else -> {
                            // No data message
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Nu există date disponibile",
                                    fontSize = 16.sp,
                                    color = AppColors.textLight
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Chart type selector
            ChartTypeSelector(
                selectedType = selectedChartType,
                onChartTypeSelected = { selectedChartType = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Stats summary card - doar dacă avem date
            if (displayedData.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF5F9F5)
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 2.dp
                    )
                ) {
                    EnhancedHikingStatsTable(
                        kmPerMonth = displayedData,
                        months = displayedMonths
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    AchievementsSection(
                        achievements = achievements,
                        isLoading = isAchievementsLoading,
                        error = achievementsError
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    WeatherSection()
                }
            }
        }
    }
}

@Composable
fun TimeRangeSelector(
    selectedTimeRange: TimeRange,
    onTimeRangeSelected: (TimeRange) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        TimeRange.values().forEach { timeRange ->
            Button(
                onClick = { onTimeRangeSelected(timeRange) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (timeRange == selectedTimeRange)
                        AppColors.primaryGreen else Color(0xFFE0E0E0)
                ),
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .height(36.dp),
                shape = RoundedCornerShape(18.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                Text(
                    text = timeRange.label,
                    color = if (timeRange == selectedTimeRange) Color.White else Color(0xFF616161),
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun ChartTypeSelector(
    selectedType: ChartType,
    onChartTypeSelected: (ChartType) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val types = listOf(
            ChartType.LINE to "Grafic linie",
            ChartType.BAR to "Grafic cu bare"
        )

        types.forEach { (type, label) ->
            Button(
                onClick = { onChartTypeSelected(type) },
                modifier = Modifier.padding(horizontal = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (type == selectedType) Color(0xFF2E7D32) else Color(0xFFE0E0E0),
                    contentColor = if (type == selectedType) Color.White else Color(0xFF616161)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(label, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun HikingBarChart(hikingData: List<Float>, months: List<String>) {
    val maxRange = 40 // Maximum value on Y-axis
    val barData = createBarChartData(hikingData)
    val yStepSize = 4 // Step size for Y-axis labels

    val xAxisData = AxisData.Builder()
        .axisStepSize(30.dp)
        .steps(hikingData.size - 1)
        .bottomPadding(30.dp)
        .axisLabelAngle(0f)
        .startDrawPadding(20.dp)
        .labelData { index -> months[index] }
        .axisLineColor(Color(0xFFAEAEAE))
        .axisLabelColor(Color(0xFF676767))
        .build()

    val yAxisData = AxisData.Builder()
        .steps(yStepSize)
        .labelAndAxisLinePadding(16.dp)
        .axisOffset(16.dp)
        .labelData { index -> (index * (maxRange / yStepSize)).toString() }
        .axisLineColor(Color(0xFFAEAEAE))
        .axisLabelColor(Color(0xFF676767))
        .build()

    val barChartData = BarChartData(
        chartData = barData,
        xAxisData = xAxisData,
        //yAxisData = yAxisData,
        barStyle = BarStyle(
            paddingBetweenBars = if (hikingData.size <= 6) 16.dp else 8.dp,
            barWidth = if (hikingData.size <= 6) 30.dp else 20.dp
        ),
        showYAxis = true,
        showXAxis = true,
        horizontalExtraSpace = 10.dp,
        backgroundColor = Color.Transparent
    )

    BarChart(
        modifier = Modifier.fillMaxSize(),
        barChartData = barChartData
    )
}

@Composable
fun HikingLineChart(hikingData: List<Float>, months: List<String>) {
    val maxRange = 40 // Maximum value on Y-axis
    val yStepSize = 4 // Step size for Y-axis labels

    // Convert hiking data to points
    val pointsData = hikingData.mapIndexed { index, value ->
        Point(index.toFloat(), value)
    }

    val xAxisData = AxisData.Builder()
        .axisStepSize(30.dp)
        .steps(hikingData.size - 1)
        .bottomPadding(30.dp)
        .axisLabelAngle(0f)
        .startDrawPadding(20.dp)
        .labelData { index -> months[index] }
        .axisLineColor(Color(0xFFAEAEAE))
        .axisLabelColor(Color(0xFF676767))
        .build()

    val yAxisData = AxisData.Builder()
        .steps(yStepSize)
        .labelAndAxisLinePadding(16.dp)
        .axisOffset(16.dp)
        .labelData { index -> (index * (maxRange / yStepSize)).toString() }
        .axisLineColor(Color(0xFFAEAEAE))
        .axisLabelColor(Color(0xFF676767))
        .build()

    // Define line style with hiking theme colors
    val lineStyle = LineStyle(
        color = Color(0xFF2E7D32),  // Dark green color
        width = 3f
    )

    // Define intersection points style
    val intersectionPoint = IntersectionPoint(
        color = Color(0xFF2E7D32),
        radius = 4.dp
    )

    // Define selection highlight style
    val selectionHighlightPoint = SelectionHighlightPoint(
        color = Color(0xFF66BB6A),  // Lighter green for highlight
        radius = 5.dp
    )

    // Shadow under the line
    val shadowUnderLine = ShadowUnderLine(
        alpha = 0.3f,
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF81C784),  // Light green
                Color.Transparent
            )
        )
    )

    val selectionHighlightPopUp = SelectionHighlightPopUp(
        backgroundColor = Color(0xFF2E7D32),
        backgroundAlpha = 0.9f,
        labelColor = Color.White,
        labelSize = 14.sp,
        popUpLabel = { x, y -> "${y.toInt()} km" }
    )

    val lineChartData = LineChartData(
        linePlotData = LinePlotData(
            lines = listOf(
                Line(
                    dataPoints = pointsData,
                    lineStyle = lineStyle,
                    intersectionPoint = intersectionPoint,
                    selectionHighlightPoint = selectionHighlightPoint,
                    shadowUnderLine = shadowUnderLine,
                    selectionHighlightPopUp = selectionHighlightPopUp
                )
            )
        ),
        xAxisData = xAxisData,
        yAxisData = yAxisData,
        gridLines = GridLines(
            color = Color(0x33AEAEAE),  // Light gray with transparency
            lineWidth = 1.dp,
            enableHorizontalLines = true,
            enableVerticalLines = false
        ),
        backgroundColor = Color.Transparent
    )

    LineChart(
        modifier = Modifier.fillMaxSize(),
        lineChartData = lineChartData
    )
}

fun createBarChartData(hikingData: List<Float>): List<BarData> {
    return hikingData.mapIndexed { index, value ->
        // Create a color gradient from light green to dark green based on value
        val normalizedValue = value / hikingData.maxOrNull()!! // 0.0 to 1.0
        val greenIntensity = (200 + (normalizedValue * 55)).toInt().coerceIn(200, 255)
        val barColor = Color(76, 175, greenIntensity)

        BarData(
            point = Point(index.toFloat(), value),
            color = barColor,
            label = "${value.toInt()} km"
        )
    }
}

@Composable
fun EnhancedHikingStatsTable(
    kmPerMonth: List<Float>,
    months: List<String>
) {
    // Calculate stats
    val totalKm = kmPerMonth.sum()
    val averageKm = kmPerMonth.average()
    val maxKm = kmPerMonth.maxOrNull() ?: 0f
    val mostActiveMonthIndex = kmPerMonth.indexOf(maxKm)
    val mostActiveMonth = if (mostActiveMonthIndex >= 0 && mostActiveMonthIndex < months.size) {
        months[mostActiveMonthIndex]
    } else {
        "N/A"
    }

    // Animation for the card
    val cardElevation by animateDpAsState(
        targetValue = 4.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "cardElevation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .shadow(
                elevation = cardElevation,
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
            Text(
                text = "Rezumatul drumețiilor",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = AppColors.primaryGreen,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                EnhancedStatItem(
                    value = "${"%.1f".format(averageKm)}",
                    unit = "km",
                    label = "Media lunară",
                    icon = Icons.Default.CalendarMonth,
                    iconTint = AppColors.primaryGreen
                )

                Divider(
                    modifier = Modifier
                        .height(50.dp)
                        .width(1.dp),
                    color = Color(0xFFE0E0E0)
                )

                EnhancedStatItem(
                    value = "${"%.0f".format(totalKm)}",
                    unit = "km",
                    label = "Distanța totală",
                    icon = Icons.Default.Hiking,
                    iconTint = AppColors.primaryGreen
                )

                Divider(
                    modifier = Modifier
                        .height(50.dp)
                        .width(1.dp),
                    color = Color(0xFFE0E0E0)
                )

                EnhancedStatItem(
                    value = mostActiveMonth,
                    subtitle = "${"%.0f".format(maxKm)} km",
                    label = "Cea mai activă lună",
                    icon = Icons.Default.EmojiEvents,
                    iconTint = AppColors.accentOrange
                )
            }
        }
    }
}


@Composable
fun EnhancedStatItem(
    value: String,
    label: String,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier,
    unit: String? = null,
    subtitle: String? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(horizontal = 4.dp)
    ) {
        // Icon with background
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            iconTint.copy(alpha = 0.2f),
                            iconTint.copy(alpha = 0.05f)
                        )
                    )
                )
                .padding(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Value with unit
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.textDark
            )

            if (unit != null) {
                Text(
                    text = unit,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.textLight,
                    modifier = Modifier.padding(start = 2.dp, bottom = 2.dp)
                )
            }
        }

        // Label
        Text(
            text = label,
            fontSize = 12.sp,
            color = AppColors.textLight
        )

        // Optional subtitle
        if (subtitle != null) {
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = AppColors.accentOrange,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

fun getAchievementIcon(iconName: String?): ImageVector {
    return when (iconName) {
        "distance" -> Icons.Default.Hiking
        "consecutive_days" -> Icons.Default.LocalFireDepartment
        "elevation" -> Icons.Default.Terrain
        "routes" -> Icons.Default.Explore
        "weather_sunny" -> Icons.Default.WbSunny
        "weather_cloudy" -> Icons.Default.Cloud
        "weather_rainy" -> Icons.Default.Grain
        else -> Icons.Default.EmojiEvents
    }
}

fun getAchievementColor(iconName: String?): Color {
    return when (iconName) {
        "distance" -> Color(0xFF7A5304)
        "consecutive_days" -> AppColors.accentOrange
        "elevation" -> AppColors.primaryGreen
        "routes" -> AppColors.skyBlue
        "weather_sunny" -> AppColors.accentOrange
        "weather_cloudy" -> AppColors.skyBlue
        "weather_rainy" -> AppColors.textLight
        else -> AppColors.primaryGreen
    }
}

// New Achievement Section
@Composable
fun AchievementsSection(
    achievements: List<Achievement>,
    isLoading: Boolean,
    error: String?
) {
    var expanded by remember { mutableStateOf(false) }

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
            // Header with expand/collapse button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Realizări",
                        tint = AppColors.accentOrange,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Realizări",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = AppColors.textDark
                    )

                    // Badge pentru realizările obținute
                    val earnedCount = achievements.count { it.earned }
                    if (earnedCount > 0) {
                        Badge(
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .background(
                                    AppColors.accentOrange,
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = earnedCount.toString(),
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = AppColors.textLight
                    )
                }
            }

            // Loading state
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = AppColors.primaryGreen,
                        strokeWidth = 2.dp
                    )
                }
            }

            // Error state
            else if (error != null) {
                Text(
                    text = error,
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Content
            else {
                AnimatedVisibility(
                    visible = expanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        achievements.forEach { achievement ->
                            AchievementItem(
                                title = achievement.name,
                                description = achievement.description,
                                progress = achievement.progress,
                                color = getAchievementColor(achievement.iconName),
                                icon = getAchievementIcon(achievement.iconName),
                                isEarned = achievement.earned
                            )
                        }
                    }
                }

                if (!expanded && achievements.isNotEmpty()) {
                    // Preview of achievements when collapsed
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        items(achievements.take(4)) { achievement ->
                            AchievementBadge(
                                progress = achievement.progress,
                                color = getAchievementColor(achievement.iconName),
                                icon = getAchievementIcon(achievement.iconName),
                                isEarned = achievement.earned
                            )
                        }

                        if (achievements.size > 4) {
                            item {
                                AchievementBadge(
                                    progress = 0f,
                                    color = AppColors.textLight,
                                    icon = Icons.Default.MoreHoriz,
                                    showMore = true
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AchievementItem(
    title: String,
    description: String,
    progress: Float,
    color: Color,
    icon: ImageVector,
    isEarned: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(
                if (isEarned) color.copy(alpha = 0.05f) else Color.Transparent,
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = if (isEarned) 12.dp else 0.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AchievementBadge(
            progress = progress,
            color = color,
            icon = icon,
            size = 50.dp,
            isEarned = isEarned
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    color = AppColors.textDark
                )

                if (isEarned) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Completed",
                        tint = color,
                        modifier = Modifier
                            .size(18.dp)
                            .padding(start = 4.dp)
                    )
                }
            }

            Text(
                text = description,
                fontSize = 14.sp,
                color = AppColors.textLight
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${(progress * 100).toInt()}%",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = if (isEarned) color else AppColors.textLight
            )

            if (isEarned) {
                Text(
                    text = "Obținută!",
                    fontSize = 12.sp,
                    color = color,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun AchievementBadge(
    progress: Float,
    color: Color,
    icon: ImageVector,
    size: Dp = 60.dp,
    isEarned: Boolean = false,
    showMore: Boolean = false
) {
    Box(contentAlignment = Alignment.Center) {
        // Progress indicator
        CircularProgressIndicator(
            progress = progress,
            modifier = Modifier.size(size),
            color = if (isEarned) color else color.copy(alpha = 0.6f),
            trackColor = color.copy(alpha = 0.2f),
            strokeWidth = 3.dp
        )

        // Center content
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(size - 8.dp)
                .clip(CircleShape)
                .background(
                    if (isEarned) {
                        Brush.radialGradient(
                            colors = listOf(
                                color.copy(alpha = 0.3f),
                                color.copy(alpha = 0.1f)
                            )
                        )
                    } else {
                        Brush.radialGradient(
                            colors = listOf(
                                color.copy(alpha = 0.15f),
                                color.copy(alpha = 0.05f)
                            )
                        )
                    }
                )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isEarned) color else color.copy(alpha = 0.6f),
                modifier = Modifier.size(size / 2.2f)
            )
        }

        // Earned indicator
        if (isEarned) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Earned",
                tint = color,
                modifier = Modifier
                    .size(16.dp)
                    .offset(x = (size / 3), y = -(size / 3))
                    .background(Color.White, CircleShape)
            )
        }
    }
}

// Weather Information Section
@Composable
fun WeatherSection() {
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                WeatherInsightItem(
                    title = "",
                    value = "în medie",
                    subtitle = "32.5 km",
                    icon = Icons.Default.WbSunny,
                    iconTint = AppColors.accentOrange
                )

                WeatherInsightItem(
                    title = "în medie",
                    value = "Înnorat",
                    subtitle = "21.3 km",
                    icon = Icons.Default.Cloud,
                    iconTint = AppColors.skyBlue
                )

                WeatherInsightItem(
                    title = "",
                    value = "în medie",
                    subtitle = "18.9 km",
                    icon = Icons.Default.Grain,
                    iconTint = AppColors.textLight
                )
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
    iconTint: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = iconTint,
            modifier = Modifier.size(28.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.textDark
        )

        Text(
            text = title,
            fontSize = 12.sp,
            color = AppColors.textLight
        )

        Text(
            text = subtitle,
            fontSize = 12.sp,
            color = iconTint,
            fontWeight = FontWeight.Medium
        )
    }
}

// Enhanced Hiking Chart with Animations
@Composable
fun EnhancedHikingLineChart(
    hikingData: List<Float>,
    months: List<String>,
    // Add more parameters as needed
) {
    // Existing chart implementation with added animations
    // This would replace your current HikingLineChart implementation

    // Chart animation state
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(hikingData) {
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(1000, easing = FastOutSlowInEasing)
        )
    }

    // Your existing chart implementation would go here
    // And would use animatedProgress.value to animate the chart elements

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .padding(16.dp)
    ) {
        // Placeholder for chart implementation
        Text(
            text = "Enhanced animated chart would go here",
            modifier = Modifier.align(Alignment.Center),
            color = AppColors.textLight
        )
    }
}

//@Preview(showBackground = true)
//@Composable
//fun HikingStatsScreenPreview() {
//    HikingStatsScreen()
//}

@Preview(showBackground = true)
@Composable
fun StatItemPreview() {
    EnhancedStatItem(
        value = "25",
        unit = "km",
        label = "Drumeția de astăzi",
        icon = Icons.Default.WbSunny,
        iconTint = AppColors.accentOrange
    )
}

@Preview(showBackground = true, widthDp = 100, heightDp = 100)
@Composable
fun AchievementBadgePreview() {
    AchievementBadge(
        progress = 0.75f,
        color = AppColors.primaryGreen,
        icon = Icons.Default.Hiking
    )
}