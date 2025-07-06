package com.example.pasipemunti.maptrailcollection

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.pasipemunti.searchhike.SearchHikeViewModel
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import androidx.compose.animation.*
import com.example.pasipemunti.R
import com.example.pasipemunti.auth.UserPreferencesManager
import com.example.pasipemunti.navigation.MiniNavBar
import com.example.pasipemunti.navigation.NavigationPanel

fun checkAndRequestLocationPermission(context: Context, viewModel: SearchHikeViewModel) {
    val permission = android.Manifest.permission.ACCESS_FINE_LOCATION
    val granted = android.content.pm.PackageManager.PERMISSION_GRANTED
    val activity = context as? android.app.Activity ?: return

    if (androidx.core.content.ContextCompat.checkSelfPermission(context, permission) != granted) {
        androidx.core.app.ActivityCompat.requestPermissions(activity, arrayOf(permission), 1001)
    } else {
        viewModel.locationPermissionGranted = true
    }
}

@Composable
fun OsmMapView(
    selectedMassif: MountainMassif?,
    viewModel: SearchHikeViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var trails by remember { mutableStateOf<List<GpxTrail>>(emptyList()) }

    val trailManager = remember { GpxTrailManager(context) }

    val isNavigating by remember { derivedStateOf { viewModel.isNavigating } }
    val routePoints by remember { derivedStateOf { viewModel.routePoints } }
    val distanceTraveled by remember { derivedStateOf { viewModel.distanceTraveled } }
    val distanceRemaining by remember { derivedStateOf { viewModel.distanceRemaining } }
    val currentAltitude by remember { derivedStateOf { viewModel.currentAltitude } }
    val elapsedTimeMillis by remember { derivedStateOf { viewModel.elapsedTimeMillis } }

    val userPrefs = remember { UserPreferencesManager.getInstance(context) }
    val userId = userPrefs.getUserData()?.userId

    var isNavPanelCollapsed by remember { mutableStateOf(false) }

    var userLocationMarker by remember { mutableStateOf<Marker?>(null) }

    LaunchedEffect(Unit) {
        checkAndRequestLocationPermission(context, viewModel)
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
        Configuration.getInstance().userAgentValue = context.packageName
        viewModel.setFusedLocationClient(LocationServices.getFusedLocationProviderClient(context))
    }

    LaunchedEffect(selectedMassif) {
        if (selectedMassif != null) {
            isLoading = true
            scope.launch {
                try {
                    val loadedTrails = trailManager.loadTrailsForZone(selectedMassif.name)
                    trails = loadedTrails
                    mapView?.let { map -> trailManager.addTrailsToMap(map, trails, viewModel) }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    isLoading = false
                }
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    setBuiltInZoomControls(true)
                    controller.setZoom(8.0)
                    controller.setCenter(GeoPoint(45.9432, 24.9668))
                    mapView = this
                    if (trails.isNotEmpty()) {
                        trailManager.addTrailsToMap(this, trails, viewModel)
                    }
                }
            },
            update = { map ->
                if (trails.isNotEmpty()) {
                    trailManager.addTrailsToMap(map, trails, viewModel)
                }

                viewModel.currentLocation?.let { loc ->
                    if (userLocationMarker == null) {
                        userLocationMarker = Marker(map).apply {
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                            icon = ContextCompat.getDrawable(context, R.drawable.ic_my_location)?.let {
                                BitmapDrawable(context.resources, Bitmap.createScaledBitmap((it as BitmapDrawable).bitmap, 48, 48, true))
                            }
                            title = "Locația ta"
                            map.overlays.add(this)
                        }
                    }
                    userLocationMarker?.position = loc

                    if (isNavigating) {
                        map.controller.setCenter(loc)
                        map.controller.setZoom(if (isNavPanelCollapsed) 17.0 else 16.0)
                    }
                }
                map.invalidate()
            },
            modifier = Modifier.fillMaxSize()
        )

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        // Panoul de navigare
        AnimatedVisibility(
            visible = isNavigating,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            if (!isNavPanelCollapsed) {
                NavigationPanel(
                    distanceTraveled = distanceTraveled,
                    distanceRemaining = distanceRemaining,
                    currentAltitude = currentAltitude,
                    elapsedTimeMillis = elapsedTimeMillis,
                    onCollapse = { isNavPanelCollapsed = true },
                    onFinish = {
                        userId?.let { viewModel.finishHike(it) }
                    }
                )
            } else {
                MiniNavBar(onExpand = { isNavPanelCollapsed = false })
            }
        }

        // Buton start dacă avem rută dar nu navigăm
        if (routePoints.isNotEmpty() && !isNavigating) {
            Button(
                onClick = {
                    if (viewModel.locationPermissionGranted) {
                        viewModel.startNavigation()
                    } else {
                        checkAndRequestLocationPermission(context, viewModel)
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
            ) {
                Text("Începe traseul", color = Color.White)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mapView?.onDetach()
        }
    }
}


