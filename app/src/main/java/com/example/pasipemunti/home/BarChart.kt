package com.example.pasipemunti.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.yml.charts.axis.AxisData
import co.yml.charts.common.model.Point
import co.yml.charts.ui.barchart.BarChart
import co.yml.charts.ui.barchart.models.*
import co.yml.charts.ui.linechart.LineChart
import co.yml.charts.ui.linechart.model.*
import com.example.pasipemunti.R
import kotlin.random.Random

enum class TimeRange(val months: Int, val label: String) {
    SIX_MONTHS(6, "6 Months"),
    TWELVE_MONTHS(12, "12 Months")
}

enum class ChartType {
    BAR, LINE
}

@Composable
fun HikingStatsScreen() {
    var selectedTimeRange by remember { mutableStateOf(TimeRange.SIX_MONTHS) }
    var selectedChartType by remember { mutableStateOf(ChartType.BAR) }

    // Monthly hiking data (km)
    val hikingData = listOf(15f, 0f, 26f, 19f, 32f, 24f, 18f, 29f, 21f, 27f, 15f, 30f)
    val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

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

        // Main content - use ScrollableColumn to ensure visibility of all content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text(
                text = "Your Hiking Journey",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Distance hiked per month",
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
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Chart type selector
            ChartTypeSelector(
                selectedType = selectedChartType,
                onChartTypeSelected = { selectedChartType = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Stats summary card - ensure it's visible
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
                HikingStatsTable(
                    kmPerMonth = displayedData,
                    months = displayedMonths
                )
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
                        Color(0xFF2E7D32) else Color(0xFFE0E0E0)
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
            ChartType.BAR to "Bar Chart",
            ChartType.LINE to "Line Chart"
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
fun HikingStatsTable(kmPerMonth: List<Float>, months: List<String>) {
    // Make sure we have data to display
    if (kmPerMonth.isEmpty() || months.isEmpty()) {
        Text(
            text = "No hiking data available",
            modifier = Modifier.padding(16.dp),
            color = Color.Gray
        )
        return
    }

    val totalKm = kmPerMonth.sum()
    val averageKm = kmPerMonth.average()
    val maxKm = kmPerMonth.maxOrNull() ?: 0f
    val mostActiveMonthIndex = kmPerMonth.indexOf(maxKm)
    val mostActiveMonth = if (mostActiveMonthIndex >= 0 && mostActiveMonthIndex < months.size) {
        months[mostActiveMonthIndex]
    } else {
        "N/A"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Your Hiking Summary",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Color(0xFF2E7D32),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            StatItem(
                value = "${"%.1f".format(averageKm)} km",
                label = "Monthly Average",
                modifier = Modifier.weight(1f)
            )

            StatItem(
                value = "${"%.0f".format(totalKm)} km",
                label = "Total Distance",
                modifier = Modifier.weight(1f)
            )

            StatItem(
                value = mostActiveMonth,
                label = "Most Active Month",
                subtitle = "${"%.0f".format(maxKm)} km",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun StatItem(
    value: String,
    label: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(horizontal = 4.dp)
    ) {
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2E7D32)
        )

        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF5E5E5E)
        )

        if (subtitle != null) {
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = Color(0xFF757575),
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HikingStatsScreenPreview() {
    HikingStatsScreen()
}