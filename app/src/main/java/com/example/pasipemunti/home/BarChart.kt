package com.example.pasipemunti.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.yml.charts.axis.AxisData
import co.yml.charts.axis.DataCategoryOptions
import co.yml.charts.common.model.Point
import co.yml.charts.common.utils.DataUtils
import co.yml.charts.ui.barchart.BarChart
import co.yml.charts.ui.barchart.models.BarChartData
import co.yml.charts.ui.barchart.models.BarChartType
import co.yml.charts.ui.barchart.models.BarData
import co.yml.charts.ui.barchart.models.BarStyle
import com.example.pasipemunti.R
import kotlin.random.Random

fun getBarChartData1(
    listSize: Int,
    maxRange: Int,
    barChartType: BarChartType,
    dataCategoryOptions: DataCategoryOptions
): List<BarData> {
    val list = arrayListOf<BarData>()

    // Valorile hardcodate pentru baruri
    val hardcodedValues = listOf(15f, 0f, 26f, 19f, 32f, 24f)

    for (index in 0 until listSize) {
        val point = when (barChartType) {
            BarChartType.VERTICAL -> {
                Point(
                    index.toFloat(),
                    hardcodedValues.getOrElse(index) { 0f }
                )
            }

            BarChartType.HORIZONTAL -> {
                Point(
                    hardcodedValues.getOrElse(index) { 0f },
                    index.toFloat()
                )
            }
        }

        val greenShade = Color(0, 255, Random.nextInt(100, 255))

        list.add(
            BarData(
                point = point,
                color = greenShade,
                dataCategoryOptions = dataCategoryOptions,
                label = "Bar$index",
            )
        )
    }
    return list
}


@Composable
fun BarChartScreen(){
    val maxRange = 100 //pana la ce valoare se duce pe axa y
    val barData = getBarChartData1(12, maxRange, BarChartType.VERTICAL, DataCategoryOptions(isDataCategoryInYAxis = true))
    //isDataCategoryInYAxis = true adica daca datele numerice sunt pe y
    val yStepSize = 10 //intervalul valorilor de pe axa y

    val hardcodedValues = listOf(15f, 0f, 26f, 19f, 32f, 24f)
    val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "June", "July", "Aug", "Sept", "Oct", "Nov", "Dec")

    val xAxisData = AxisData.Builder()
        .axisStepSize(30.dp)
        .steps(barData.size - 1)
        .bottomPadding(40.dp)
        .axisLabelAngle(20f)
        .startDrawPadding(20.dp)
        .labelData { index -> months[index] }
        .build()

    val yAxisData = AxisData.Builder()
        .steps(yStepSize)
        .labelAndAxisLinePadding(20.dp)
        .axisOffset(20.dp)
        .labelData { index -> (index * (maxRange / yStepSize)).toString() }
        .build()

    val barChartData = BarChartData(
        chartData = barData,
        xAxisData = xAxisData,
        yAxisData = yAxisData,
        barStyle = BarStyle(
            paddingBetweenBars = 20.dp,
            barWidth = 25.dp
        ),
        showYAxis = true,
        showXAxis = true,
        horizontalExtraSpace = 10.dp,
        backgroundColor = Color(0x33FFFFFF)
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 10.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.home_screen_background),
            contentDescription = "Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().alpha(0.6f)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "Distance hiked per month",
                color = Color.Black,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            BarChart(
                modifier = Modifier
                    .height(300.dp)
                    .fillMaxWidth(),
                barChartData = barChartData
            )
            StatsTable(kmPerMonth = hardcodedValues, months = months)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BarChartPreview(){
    BarChartScreen()
}

@Composable
fun StatsTable(kmPerMonth: List<Float>, months: List<String>) {
    val totalKm = kmPerMonth.sum()
    val averageKm = kmPerMonth.average()
    val mostActiveMonth = months[kmPerMonth.indexOf(kmPerMonth.maxOrNull() ?: 0f)]

    val data = listOf(
        listOf("${"%.2f".format(averageKm)} km", "${"%.0f".format(totalKm)} km", mostActiveMonth)
    )

    Column(modifier = Modifier.padding(16.dp)) {
        // Header
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Text(
                text = "Monthly avg distance",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "Total km",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "Most active month",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        data.forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                row.forEach { cell ->
                    Text(
                        text = cell,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}


//@Preview(showBackground = true)
//@Composable
//fun PreviewStatsTable() {
//    val kmPerMonth = listOf(300f, 320f, 290f, 310f, 350f, 340f, 360f, 380f, 370f, 330f, 340f, 310f)
//    val months = listOf("Ianuarie", "Februarie", "Martie", "Aprilie", "Mai", "Iunie", "Iulie", "August", "Septembrie", "Octombrie", "Noiembrie", "Decembrie")
//
//    StatsTable(kmPerMonth = kmPerMonth, months = months)
//}

