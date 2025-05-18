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
import kotlinx.coroutines.delay
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.io.File
import kotlin.math.pow

@Composable
fun TrailMapScreen(
    navController: NavController,
    viewModel: TrailListViewModel = viewModel()
) {
    val context = LocalContext.current
    val selectedTrail = viewModel.selectedTrail
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var showElevationProfile by remember { mutableStateOf(false) }
    var mapFullyInitialized by remember { mutableStateOf(false) }

    // Debug log to check if trail is selected
    LaunchedEffect(selectedTrail) {
        android.util.Log.d("TrailMapScreen", "Selected Trail: ${selectedTrail?.name ?: "null"}")
    }

    // Effect to ensure zoom happens after the map is ready
    LaunchedEffect(mapView, selectedTrail) {
        if (mapView != null && selectedTrail != null && !mapFullyInitialized) {
            // Short delay to ensure map is fully initialized
            delay(300)
            zoomToTrailBounds(mapView!!, selectedTrail)
            mapFullyInitialized = true

            // Add a log message for debugging
            android.util.Log.d("TrailMapScreen", "Map fully initialized and zoomed to trail")
        }
    }

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
                            createMapView(context).also {
                                mapView = it
                                setupMap(it, selectedTrail, context)
                            }
                        },
                        update = { view ->
                            // Store reference to mapView but don't re-initialize if already set up
                            if (mapView == null) {
                                mapView = view
                                setupMap(view, selectedTrail, context)
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Back button overlay
                    IconButton(
                        onClick = {
                            mapFullyInitialized = false
                            viewModel.clearSelectedTrail()
                            navController.popBackStack()
                        },
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

        // Limit initial map display size to improve loading performance
        minZoomLevel = 7.0 // Prevent zooming out too much
        maxZoomLevel = 19.0 // Allow good detail when zoomed in

        // Don't allow scrolling beyond the area we care about (will be adjusted in setupMap)
        setScrollableAreaLimitLatitude(
            MapView.getTileSystem().maxLatitude,
            MapView.getTileSystem().minLatitude,
            0
        )
    }
}

private fun setupMap(mapView: MapView, trail: GPXTrail, context: Context) {
    mapView.overlays.clear()

    // Only proceed if we have track points
    if (trail.points.isEmpty()) return

    // Calculate the bounds of this trail
    val boundingBox = BoundingBox.fromGeoPoints(trail.points)
    val paddedBoundingBox = boundingBox.increaseByScale(1.5f) // Slightly larger padding

    // Limit the map's scrollable area to just around the trail area
    // This constrains tile loading to the relevant area
    mapView.setScrollableAreaLimitLatitude(
        paddedBoundingBox.latNorth,
        paddedBoundingBox.latSouth,
        0
    )
    mapView.setScrollableAreaLimitLongitude(
        paddedBoundingBox.lonWest,
        paddedBoundingBox.lonEast,
        0
    )

    // Set minimum zoom level based on the trail's size
    // This prevents zooming out too far
    val diagonalDistance = calculateDistance(
        boundingBox.latNorth, boundingBox.lonWest,
        boundingBox.latSouth, boundingBox.lonEast
    )

    // Adjust minimum zoom level based on trail size
    val suggestedMinZoom = when {
        diagonalDistance > 100000 -> 8.0  // Very large trail (>100km diagonal)
        diagonalDistance > 50000 -> 9.0   // Large trail
        diagonalDistance > 20000 -> 10.0  // Medium trail
        diagonalDistance > 5000 -> 11.0   // Small trail
        else -> 12.0                      // Very small trail
    }
    mapView.minZoomLevel = suggestedMinZoom

    // Create path overlay
    val pathOverlay = Polyline().apply {
        outlinePaint.color = HikingAppTheme.primaryGreen.toArgb()
        outlinePaint.strokeWidth = 8f
        setPoints(trail.points)
    }

    // Add start and end markers
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

    mapView.invalidate()
}

// Separate function to handle zoom specifically
private fun zoomToTrailBounds(mapView: MapView, trail: GPXTrail) {
    if (trail.points.isNotEmpty()) {
        // Create a bounding box from the trail points
        val boundingBox = BoundingBox.fromGeoPoints(trail.points)

        // Add some padding around the trail
        val paddedBoundingBox = boundingBox.increaseByScale(1.2f)

        // Log the zoom operation
        android.util.Log.d("TrailMapScreen", "Zooming to trail bounds: $paddedBoundingBox")

        // Set appropriate zoom level based on the trail's size
        val diagonalDistance = calculateDistance(
            boundingBox.latNorth, boundingBox.lonWest,
            boundingBox.latSouth, boundingBox.lonEast
        )

        // Determine the best zoom level based on trail size
        val zoomLevel = when {
            diagonalDistance > 100000 -> 9.0  // Very large trail (>100km diagonal)
            diagonalDistance > 50000 -> 10.0  // Large trail
            diagonalDistance > 20000 -> 11.0  // Medium trail
            diagonalDistance > 5000 -> 13.0   // Small trail
            else -> 14.0                      // Very small trail
        }

        // Apply zoom level directly first for immediate display
        mapView.controller.setZoom(zoomLevel)

        // Then zoom to bounding box with animation
        mapView.zoomToBoundingBox(paddedBoundingBox, true, 500)

        // Force a redraw
        mapView.invalidate()
    }
}

private fun calculateDistance(
    lat1: Double, lon1: Double,
    lat2: Double, lon2: Double
): Double {
    val r = 6371000.0 // Earth radius in meters
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = Math.sin(dLat / 2).pow(2.0) +
            Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
            Math.sin(dLon / 2).pow(2.0)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    return r * c
}