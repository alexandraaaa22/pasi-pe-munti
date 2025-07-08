package com.example.pasipemunti.searchhike

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pasipemunti.R
import com.example.pasipemunti.auth.UserPreferencesManager
import com.example.pasipemunti.navigation.MiniNavBar
import com.example.pasipemunti.navigation.NavigationPanel
import org.osmdroid.views.overlay.Marker
import com.google.android.gms.location.LocationServices
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SearchHikeScreen(viewModel: SearchHikeViewModel = viewModel()) {
    val context = LocalContext.current
    val routePoints by remember { derivedStateOf { viewModel.routePoints } }
    var mapView: MapView? by remember { mutableStateOf(null) }
    val currentLocation by remember { derivedStateOf { viewModel.currentLocation } }
    val isNavigating by remember { derivedStateOf { viewModel.isNavigating } }

    // Observe navigation stats
    val distanceTraveled by remember { derivedStateOf { viewModel.distanceTraveled } }
    val distanceRemaining by remember { derivedStateOf { viewModel.distanceRemaining } }
    val currentAltitude by remember { derivedStateOf { viewModel.currentAltitude } }
    val elapsedTimeMillis by remember { derivedStateOf { viewModel.elapsedTimeMillis } }

    val userPrefs = remember { UserPreferencesManager.getInstance(context) }
    val userId = userPrefs.getUserData()?.userId

    var isNavPanelCollapsed by remember { mutableStateOf(false) }

    val locationPermissionsState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
    )

    LaunchedEffect(Unit) {
        Configuration.getInstance().load(
            context,
            context.getSharedPreferences("osm_prefs", Context.MODE_PRIVATE)
        )
        viewModel.setFusedLocationClient(LocationServices.getFusedLocationProviderClient(context))
    }

    LaunchedEffect(locationPermissionsState.allPermissionsGranted) {
        viewModel.locationPermissionGranted = locationPermissionsState.allPermissionsGranted
    }

    LaunchedEffect(Unit) {
        if (!locationPermissionsState.allPermissionsGranted) {
            locationPermissionsState.launchMultiplePermissionRequest()
        }
    }


    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = {
                MapView(it).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    controller.setZoom(13.0)
                    controller.setCenter(GeoPoint(45.9432, 24.9668))
                    setMultiTouchControls(true)
                    mapView = this
                }
            },
            update = { map ->
                map.overlays.clear()

                if (routePoints.isNotEmpty()) {
                    val polyline = Polyline().apply {
                        setPoints(routePoints)
                        color = 0xFF29780C.toInt()
                    }
                    map.overlays.add(polyline)

                    val startMarker = Marker(map).apply {
                        position = routePoints.first()
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        icon = ContextCompat.getDrawable(context, R.drawable.top)?.let { drawable ->
                            (drawable as BitmapDrawable).bitmap.let { bitmap ->
                                Bitmap.createScaledBitmap(bitmap, 30, 30, true)
                            }
                        }?.let { BitmapDrawable(context.resources, it) }
                        title = "Start"
                    }
                    val endMarker = Marker(map).apply {
                        position = routePoints.last()
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        icon = ContextCompat.getDrawable(context, R.drawable.top)?.let { drawable ->
                            (drawable as BitmapDrawable).bitmap.let { bitmap ->
                                Bitmap.createScaledBitmap(bitmap, 30, 30, true)
                            }
                        }?.let { BitmapDrawable(context.resources, it) }
                        title = "Finish"
                    }
                    map.overlays.add(startMarker)
                    map.overlays.add(endMarker)

                    if (!isNavigating) {
                        map.controller.setCenter(routePoints.first())
                        map.controller.setZoom(13.0)
                    }
                }

                currentLocation?.let { loc ->
                    val currentLocationMarker = Marker(map).apply {
                        position = loc
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        icon = ContextCompat.getDrawable(context, R.drawable.ic_my_location)?.let { drawable ->
                            (drawable as BitmapDrawable).bitmap.let { bitmap ->
                                Bitmap.createScaledBitmap(bitmap, 48, 48, true)
                            }
                        }?.let { BitmapDrawable(context.resources, it) }
                        title = "Locația ta"
                    }
                    map.overlays.add(currentLocationMarker)

                    if (isNavigating) {
                        map.controller.setCenter(loc)

                        // Zoom mai mare când navighezi, ajustat în funcție de panoul minimizat/extins
                        if (isNavPanelCollapsed) {
                            map.controller.setZoom(17.0) // Zoom și mai apropiat când bara e micșorată
                        } else {
                            map.controller.setZoom(16.0) // Zoom normal când e panoul mare
                        }
                    }
                }

                map.invalidate()
            },
            modifier = Modifier.fillMaxSize()
        )

        // Search Bar (Visible when not navigating)
        AnimatedVisibility  (
            visible = !isNavigating,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(
                        color = Color.White.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(12.dp)
            ) {
                Row(Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = viewModel.start,
                        onValueChange = { viewModel.start = it },
                        label = { Text("Start") },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 4.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xB3036e29),
                            unfocusedBorderColor = Color(0xB347B36D),
                            focusedLabelColor = Color(0xB3036e29),
                            unfocusedLabelColor = Color.DarkGray,
                            cursorColor = Color(0xB3036e29)
                        )
                    )
                    OutlinedTextField(
                        value = viewModel.end,
                        onValueChange = { viewModel.end = it },
                        label = { Text("Stop") },
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 4.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xB3036e29),
                            unfocusedBorderColor = Color(0xB347B36D),
                            focusedLabelColor = Color(0xB3036e29),
                            unfocusedLabelColor = Color.DarkGray,
                            cursorColor = Color(0xB3036e29)
                        )
                    )
                }
                Button(
                    onClick = {
                        viewModel.fetchRoute("5b3ce3597851110001cf62482d243a035da94fa886b41a12d9045b47")
                    },
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .align(Alignment.CenterHorizontally),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xB347B36D)
                    )
                ) {
                    Text("Găsește traseu", color = Color.White)
                }
            }
        }

        AnimatedVisibility(
            visible = isNavigating,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            if (!isNavPanelCollapsed) {
                // Panoul mare
                NavigationPanel(
                    distanceTraveled = distanceTraveled,
                    distanceRemaining = distanceRemaining,
                    currentAltitude = currentAltitude,
                    elapsedTimeMillis = elapsedTimeMillis,
                    onCollapse = { isNavPanelCollapsed = true },
                    onFinish = {
                        userId?.let {
                            viewModel.finishHike(userId = it)
                        }
                    }
                )
            } else {
                MiniNavBar(onExpand = { isNavPanelCollapsed = false })
            }
        }

        if (routePoints.isNotEmpty() && !isNavigating) { // Show Start button only if route is loaded and not navigating
            Button(
                onClick = {
                    if (locationPermissionsState.allPermissionsGranted) {
                        viewModel.startNavigation()
                    } else {
                        locationPermissionsState.launchMultiplePermissionRequest()
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd) // Position in bottom right
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2E7D32) // A green color
                )
            ) {
                Text("Începe traseul", color = Color.White)
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: String,
    color: Color,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(80.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 2.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = color.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// Helper function to format elapsed time
fun formatElapsedTime(millis: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(millis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}