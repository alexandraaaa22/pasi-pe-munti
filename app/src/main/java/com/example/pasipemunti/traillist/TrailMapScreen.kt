package com.example.pasipemunti.traillist

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.pasipemunti.R
import com.example.pasipemunti.data.LocalDatabase
import com.example.pasipemunti.home.AppColors
import com.example.pasipemunti.navigation.MiniNavBar
import com.example.pasipemunti.navigation.NavigationPanel
import com.example.pasipemunti.searchhike.SearchHikeViewModel
import com.example.pasipemunti.ui.TrailListViewModel
import com.example.pasipemunti.ui.TrailListViewModelFactory
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import com.example.pasipemunti.auth.UserPreferencesManager


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrailMapScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val dao = remember { LocalDatabase.getDatabase(context).gpxTrailDao() }

    // Adaugă această linie pentru a configura User Agent-ul OsmDroid
    // Fă-o cât mai devreme posibil, de preferat într-o clasă Application
    // sau aici, înainte de a crea MapView
    DisposableEffect(Unit) {
        Configuration.getInstance().userAgentValue = "PasiPeMuntiApp/1.0 (contact@example.com)" // Pune numele aplicației și contactul tău
        onDispose { }
    }

    // Instanțiază UserPreferencesManager
    val userPreferencesManager = remember { UserPreferencesManager.getInstance(context) }

    val navBackStackEntry = remember { navController.getBackStackEntry("trailList") }
    val trailListViewModel: TrailListViewModel = viewModel(
        navBackStackEntry,
        factory = TrailListViewModelFactory(dao)
    )

    val searchHikeViewModel: SearchHikeViewModel = viewModel()

    LaunchedEffect(Unit) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        searchHikeViewModel.setFusedLocationClient(fusedLocationClient)

        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            searchHikeViewModel.locationPermissionGranted = true
            // Solicită locația inițială
            searchHikeViewModel.requestInitialLocation()
        }
    }

    val currentSelectedTrail = trailListViewModel.selectedTrail.value

    val isNavigating by remember { derivedStateOf { searchHikeViewModel.isNavigating } }
    val distanceTraveled by remember { derivedStateOf { searchHikeViewModel.distanceTraveled } }
    val distanceRemaining by remember { derivedStateOf { searchHikeViewModel.distanceRemaining } }
    val currentAltitude by remember { derivedStateOf { searchHikeViewModel.currentAltitude } }
    val elapsedTimeMillis by remember { derivedStateOf { searchHikeViewModel.elapsedTimeMillis } }

    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(currentSelectedTrail) {
        if (currentSelectedTrail != null) {
            if (searchHikeViewModel.routePoints != currentSelectedTrail.points) {
                searchHikeViewModel.routePoints = currentSelectedTrail.points
            }
        }
    }

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setBuiltInZoomControls(true)
            setMultiTouchControls(true)
            controller.setZoom(10.0)
        }
    }

    val currentLocation by remember { derivedStateOf { searchHikeViewModel.currentLocation } }
    val userMarker = remember { mutableStateOf<Marker?>(null) }

// Adaugă marker pentru utilizator când se schimbă locația
    LaunchedEffect(currentLocation) {
        if (currentLocation != null && isNavigating) {
            withContext(Dispatchers.Main) {
                try {
                    // Șterge marker-ul vechi
                    userMarker.value?.let { oldMarker ->
                        mapView.overlays.remove(oldMarker)
                    }

                    // Creează marker nou pentru utilizator
                    val newUserMarker = Marker(mapView).apply {
                        position = currentLocation
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        title = "Locația ta"

                        try {
                            val userDrawable = ResourcesCompat.getDrawable(
                                context.resources,
                                android.R.drawable.ic_menu_mylocation,
                                null
                            )
                            val userBitmap = (userDrawable as? BitmapDrawable)?.bitmap
                            if (userBitmap != null) {
                                icon = BitmapDrawable(
                                    context.resources,
                                    Bitmap.createScaledBitmap(userBitmap, 30, 30, true)
                                )
                            }
                        } catch (e: Exception) {
                            Log.e("TrailMapScreen", "Error loading user marker icon", e)
                        }
                    }

                    mapView.overlays.add(newUserMarker)
                    userMarker.value = newUserMarker

                    // Centrează harta pe utilizator
                    mapView.controller.animateTo(currentLocation)
                    mapView.invalidate()

                    Log.d("TrailMapScreen", "User location updated: ${currentLocation!!.latitude}, ${currentLocation!!.longitude}")
                } catch (e: Exception) {
                    Log.e("TrailMapScreen", "Error updating user location", e)
                }
            }
        }
    }


    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_DESTROY -> mapView.onDetach()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(currentSelectedTrail) {
        if (currentSelectedTrail != null && currentSelectedTrail.points.isNotEmpty()) {
            withContext(Dispatchers.IO) {
                try {
                    val pathOverlay = Polyline().apply {
                        setPoints(currentSelectedTrail.points)
                        color = Color.Blue.toArgb()
                        width = 8f
                    }

                    val (startIcon, endIcon) = try {
                        val startDrawable = ResourcesCompat.getDrawable(
                            context.resources,
                            R.drawable.boot_with_mnt,
                            null
                        )
                        val endDrawable = ResourcesCompat.getDrawable(
                            context.resources,
                            R.drawable.man_at_mnt_peak,
                            null
                        )
                        val startBitmap = (startDrawable as? BitmapDrawable)?.bitmap
                        val endBitmap = (endDrawable as? BitmapDrawable)?.bitmap

                        val scaledStartIcon = startBitmap?.let {
                            BitmapDrawable(context.resources, Bitmap.createScaledBitmap(it, 35, 35, true))
                        }
                        val scaledEndIcon = endBitmap?.let {
                            BitmapDrawable(context.resources, Bitmap.createScaledBitmap(it, 35, 35, true))
                        }
                        Pair(scaledStartIcon, scaledEndIcon)
                    } catch (e: Exception) {
                        Log.e("TrailMapScreen", "Error loading marker icons", e)
                        Pair(null, null)
                    }

                    val boundingBox = BoundingBox.fromGeoPoints(currentSelectedTrail.points)
                    val paddedBoundingBox = boundingBox.increaseByScale(1.3f)

                    withContext(Dispatchers.Main) {
                        try {
                            mapView.overlays.clear()
                            mapView.overlays.add(pathOverlay)

                            val startMarker = Marker(mapView).apply {
                                position = currentSelectedTrail.points.first()
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                icon = startIcon
                                title = "Start"
                            }
                            val endMarker = Marker(mapView).apply {
                                position = currentSelectedTrail.points.last()
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                icon = endIcon
                                title = "Sfârșit"
                            }
                            mapView.overlays.add(startMarker)
                            mapView.overlays.add(endMarker)

                            mapView.post {
                                mapView.zoomToBoundingBox(paddedBoundingBox, true, 100)
                                mapView.invalidate()
                            }
                        } catch (e: Exception) {
                            Log.e("TrailMapScreen", "Error updating map UI", e)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("TrailMapScreen", "Error processing trail data", e)
                }
            }
        } else {
            withContext(Dispatchers.Main) {
                mapView.overlays.clear()
                mapView.invalidate()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = currentSelectedTrail?.name ?: "Hartă Traseu", maxLines = 1)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, "backIcon")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.primaryGreen,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                AndroidView(
                    factory = { mapView },
                    modifier = Modifier.fillMaxSize()
                )

                val isNavPanelCollapsed = remember { mutableStateOf(false) }

                // Afișare NavigationPanel dacă se navighează
                if (isNavigating) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 16.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        if (isNavPanelCollapsed.value) {
                            MiniNavBar(onExpand = { isNavPanelCollapsed.value = false })
                        } else {
                            NavigationPanel(
                                distanceTraveled = distanceTraveled,
                                distanceRemaining = distanceRemaining,
                                currentAltitude = currentAltitude,
                                elapsedTimeMillis = elapsedTimeMillis,
                                onCollapse = { isNavPanelCollapsed.value = true },
                                onFinish = {
                                    val userId = userPreferencesManager.getUserData()?.userId ?: -1 // Preluăm userId-ul
                                    searchHikeViewModel.finishHike(
                                        userId = userId, // Transmitem userId-ul real
                                        difficulty = "moderate",
                                        weatherCondition = "sunny"
                                    )
                                    searchHikeViewModel.stopNavigation()
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }

                if (!isNavigating && currentSelectedTrail != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        Button(
                            onClick = {
                                searchHikeViewModel.startNavigation()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AppColors.primaryGreen)
                        ) {
                            Text(text = "Start Navigare", color = Color.White)
                        }
                    }
                }

                if (currentSelectedTrail == null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = AppColors.primaryGreen)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(text = "Se încarcă harta...", color = AppColors.textLight)
                        }
                    }
                }
            }
        }
    )
}