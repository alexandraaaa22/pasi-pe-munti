package com.example.pasipemunti.home.view

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Hiking
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.yml.charts.axis.AxisData
import co.yml.charts.common.model.Point
import co.yml.charts.ui.barchart.BarChart
import co.yml.charts.ui.barchart.models.BarChartData
import co.yml.charts.ui.barchart.models.BarData
import co.yml.charts.ui.barchart.models.BarStyle
import co.yml.charts.ui.linechart.LineChart
import co.yml.charts.ui.linechart.model.*
import com.example.pasipemunti.home.models.AppColors
import com.example.pasipemunti.home.models.ChartType
import com.example.pasipemunti.home.models.TimeRange

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

    // Converteste valorile in puncte pe grafic
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

    // Defines line style with hiking theme colors
    val lineStyle = LineStyle(
        color = Color(0xFF2E7D32),  // Dark green color
        width = 3f
    )

    // Defines intersection points style
    val intersectionPoint = IntersectionPoint(
        color = Color(0xFF2E7D32),
        radius = 4.dp
    )

    // Defines selection highlight style
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
        val normalizedValue = if (hikingData.maxOrNull() != null && hikingData.maxOrNull() != 0f) {
            value / hikingData.maxOrNull()!!
        } else 0f
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

        Text(
            text = label,
            fontSize = 12.sp,
            color = AppColors.textLight
        )

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