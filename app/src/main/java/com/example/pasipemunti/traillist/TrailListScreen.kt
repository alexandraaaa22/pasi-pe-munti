package com.example.pasipemunti.traillist

import android.content.res.Configuration
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.pasipemunti.home.HikingAppTheme
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TrailListScreen(
    navController: NavController,
    viewModel: TrailListViewModel = viewModel()
) {
    val context = LocalContext.current
    val trails = viewModel.trails

    // Load trails when the screen is first displayed
    LaunchedEffect(Unit) {
        viewModel.loadTrails(context)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background with gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            HikingAppTheme.backgroundGreen,
                            Color.White
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Text(
                text = "Your Trails",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = HikingAppTheme.primaryGreen,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (trails.isEmpty()) {
                // Loading or empty state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = HikingAppTheme.primaryGreen)
                }
            } else {
                // Trail list
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(trails) { trail ->
                        TrailCard(
                            trail = trail,
                            onClick = {
                                viewModel.selectTrail(trail)
                                navController.navigate("trailMap")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TrailCard(
    trail: GPXTrail,
    onClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .animateContentSize(
                animationSpec = tween(
                    durationMillis = 300,
                    easing = LinearOutSlowInEasing
                )
            ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column {
            // Trail image with gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                // Use actual trail image or fallback
                Image(
                    painter = painterResource(id = trail.imageResId),
                    contentDescription = trail.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Gradient overlay for better text visibility
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0x99000000)
                                )
                            )
                        )
                )

                // Trail name at the bottom of the image
                Text(
                    text = trail.name,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                )
            }

            // Trail stats
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TrailStatItem(
                    icon = Icons.Default.Terrain,
                    value = String.format("%.1f km", trail.distance / 1000),
                    label = "Distance"
                )

                TrailStatItem(
                    icon = Icons.Default.TrendingUp,
                    value = String.format("%d m", trail.elevationGain.toInt()),
                    label = "Elevation"
                )

                TrailStatItem(
                    icon = Icons.Default.Timer,
                    value = formatDuration(trail.duration),
                    label = "Duration"
                )
            }

            // Expandable description
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Description",
                    fontWeight = FontWeight.Medium,
                    color = HikingAppTheme.textDark
                )

                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Show less" else "Show more",
                    tint = HikingAppTheme.primaryGreen
                )
            }

            if (expanded) {
                Text(
                    text = trail.description,
                    color = HikingAppTheme.textLight,
                    modifier = Modifier.padding(16.dp),
                    lineHeight = 20.sp
                )
            } else {
                Text(
                    text = trail.description,
                    color = HikingAppTheme.textLight,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 16.dp
                    ),
                    lineHeight = 20.sp
                )
            }

            // Additional trail info
            if (trail.date != null) {
                Divider(color = Color(0xFFEEEEEE))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Date",
                        tint = HikingAppTheme.textLight,
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = formatDate(trail.date),
                        color = HikingAppTheme.textLight,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun TrailStatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = HikingAppTheme.primaryGreen,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            color = HikingAppTheme.textDark,
            fontSize = 16.sp
        )

        Text(
            text = label,
            color = HikingAppTheme.textLight,
            fontSize = 12.sp
        )
    }
}

// Helper function to format duration
fun formatDuration(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60

    return if (hours > 0) {
        "$hours h ${minutes} min"
    } else {
        "$minutes min"
    }
}

// Helper function to format date
fun formatDate(date: Date): String {
    val format = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return format.format(date)
}

@Preview(name = "Light Mode")
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    name = "Dark Mode"
)
@Composable
fun TrailListScreenPreview() {
    TrailListScreen(navController = rememberNavController())
}