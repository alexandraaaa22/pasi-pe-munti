package com.example.pasipemunti.traillist

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.pasipemunti.home.HikingAppTheme
import com.example.pasipemunti.traillist.TrailListViewModel
import com.example.pasipemunti.traillist.formatDuration
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.infowindow.InfoWindow
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow
import org.osmdroid.views.overlay.milestones.MilestoneBitmapDisplayer
import org.osmdroid.views.overlay.milestones.MilestoneManager
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TrailMapScreen(
    navController: NavController,
    viewModel: TrailListViewModel = viewModel()
) {
    val context = LocalContext.current
    val selectedTrail = viewModel.selectedTrail
    var mapInitialized by remember { mutableStateOf(false) }
    var showElevationProfile by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (selectedTrail == null) {
            // No trail selected, show error message
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Error",
                    tint = HikingAppTheme.primaryGreen,
                    modifier = Modifier.size(48.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "No trail selected",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { navController.popBackStack() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HikingAppTheme.primaryGreen
                    )
                ) {
                    Text("Go to Trail List")
                }
            }
        } else {
            // Main content with map
            Column(modifier = Modifier.fillMaxSize()) {
                // Map takes most of the screen
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    // OSMDroid Map View
                    AndroidView(
                        factory = { context ->
                            createMapView(context).apply {
                                // Initialize the map only once
                                if (!mapInitialized) {
                                    setupMap(this, selectedTrail, context)
                                    mapInitialized = true
                                }
                            }
                        },
                        update = { mapView ->
                            // Update map when selected trail changes
                            if (!mapInitialized || mapView.overlays.isEmpty()) {
                                setupMap(mapView, selectedTrail, context)
                                mapInitialized = true
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Back button overlay
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .padding(16.dp)
                            .size(48.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(HikingAppTheme.primaryGreen.copy(alpha = 0.8f))
                            .align(Alignment.TopStart)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = androidx.compose.ui.graphics.Color.White
                        )
                    }

                    // Toggle elevation profile button
                    IconButton(
                        onClick = { showElevationProfile = !showElevationProfile },
                        modifier = Modifier
                            .padding(16.dp)
                            .size(48.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(HikingAppTheme.primaryGreen.copy(alpha = 0.8f))
                            .align(Alignment.TopEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Terrain,
                            contentDescription = "Toggle Elevation Profile",
                            tint = androidx.compose.ui.graphics.Color.White
                        )
                    }
                }

                // Trail info card at the bottom
                TrailInfoCard(trail = selectedTrail)

                // Elevation profile card if toggled
                if (showElevationProfile) {
                    ElevationProfileCard(trail = selectedTrail)
                }
            }
        }
    }
}

@Composable
fun TrailInfoCard(trail: GPXTrail) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = androidx.compose.ui.graphics.Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = trail.name,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = HikingAppTheme.textDark
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TrailStatBox(
                    icon = Icons.Default.Straighten,
                    value = String.format("%.1f km", trail.distance / 1000),
                    label = "Distance"
                )

                TrailStatBox(
                    icon = Icons.Default.TrendingUp,
                    value = "${trail.elevationGain.toInt()} m",
                    label = "Elevation Gain"
                )

                TrailStatBox(
                    icon = Icons.Default.Timer,
                    value = formatDuration(trail.duration),
                    label = "Duration"
                )
            }

            if (trail.date != null) {
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Date",
                        tint = HikingAppTheme.textLight,
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

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
fun TrailStatBox(
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

@Composable
fun ElevationProfileCard(trail: GPXTrail) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = androidx.compose.ui.graphics.Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Elevation Profile",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = HikingAppTheme.textDark
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Simple elevation stats since we can't draw a chart directly here
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Max Elevation",
                        fontSize = 12.sp,
                        color = HikingAppTheme.textLight
                    )
                    Text(
                        text = "${trail.maxElevation.toInt()} m",
                        fontWeight = FontWeight.Bold,
                        color = HikingAppTheme.primaryGreen
                    )
                }

                Column {
                    Text(
                        text = "Elevation Gain",
                        fontSize = 12.sp,
                        color = HikingAppTheme.textLight
                    )
                    Text(
                        text = "${trail.elevationGain.toInt()} m",
                        fontWeight = FontWeight.Bold,
                        color = HikingAppTheme.primaryGreen
                    )
                }

                Column {
                    Text(
                        text = "Trail Length",
                        fontSize = 12.sp,
                        color = HikingAppTheme.textLight
                    )
                    Text(
                        text = String.format("%.1f km", trail.distance / 1000),
                        fontWeight = FontWeight.Bold,
                        color = HikingAppTheme.primaryGreen
                    )
                }
            }
        }
    }
}

private fun createMapView(context: Context): MapView {
    // Configure OSMDroid
    Configuration.getInstance().apply {
        userAgentValue = context.packageName
        osmdroidTileCache = File(context.cacheDir, "osmdroid")
    }

    return MapView(context).apply {
        setTileSource(TileSourceFactory.MAPNIK)
        setMultiTouchControls(true)
    }
}

private fun setupMap(mapView: MapView, trail: GPXTrail, context: Context) {
    mapView.overlays.clear()

    // Create path overlay
    val pathOverlay = Polyline().apply {
        outlinePaint.color = HikingAppTheme.primaryGreen.toArgb()
        outlinePaint.strokeWidth = 8f
        setPoints(trail.points)
    }

    // Add start and end markers
    if (trail.points.isNotEmpty()) {
        val startPoint = trail.points.first()
        val endPoint = trail.points.last()

        // Start marker
        val startMarker = Marker(mapView).apply {
            position = startPoint
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            icon = context.getDrawable(android.R.drawable.ic_menu_mylocation)
            title = "Start: ${trail.name}"
            snippet = "Elevation: ${startPoint.altitude.toInt()} m"
        }

        // End marker
        val endMarker = Marker(mapView).apply {
            position = endPoint
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            icon = context.getDrawable(android.R.drawable.ic_menu_myplaces)
            title = "End: ${trail.name}"
            snippet = "Elevation: ${endPoint.altitude.toInt()} m"
        }

        // Add overlays to map
        mapView.overlays.add(pathOverlay)
        mapView.overlays.add(startMarker)
        mapView.overlays.add(endMarker)

        // Zoom to trail bounds
        val boundingBox = BoundingBox.fromGeoPoints(trail.points)
        mapView.zoomToBoundingBox(boundingBox.increaseByScale(1.2f), true)
    }

    mapView.invalidate()
}

