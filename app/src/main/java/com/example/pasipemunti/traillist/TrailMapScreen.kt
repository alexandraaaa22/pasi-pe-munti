package com.example.pasipemunti.traillist

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.example.pasipemunti.R
import com.example.pasipemunti.home.AppColors
import com.example.pasipemunti.ui.TrailListViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrailMapScreen(
    navController: NavController,
    viewModel: TrailListViewModel
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentSelectedTrail = viewModel.selectedTrail.value

    // Initialize map configuration once
    LaunchedEffect(Unit) {
        Configuration.getInstance().apply {
            load(context, context.getSharedPreferences("osmdroid", 0))
            userAgentValue = context.packageName
        }
    }

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setBuiltInZoomControls(true)
            setMultiTouchControls(true)
            // Set initial zoom and center
            controller.setZoom(10.0)
        }
    }

    // Handle lifecycle events
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

    // Handle trail selection and map updates
    LaunchedEffect(currentSelectedTrail) {
        if (currentSelectedTrail != null && currentSelectedTrail.points.isNotEmpty()) {

            // Do all heavy work in background
            withContext(Dispatchers.IO) {
                try {
                    // Prepare polyline
                    val pathOverlay = Polyline().apply {
                        setPoints(currentSelectedTrail.points)
                        color = Color.Blue.toArgb()
                        width = 8f
                    }

                    // Prepare resized icons for start and end
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

                    // Calculate bounding box
                    val boundingBox = BoundingBox.fromGeoPoints(currentSelectedTrail.points)
                    val paddedBoundingBox = boundingBox.increaseByScale(1.3f)

                    // Switch to main thread for UI updates
                    withContext(Dispatchers.Main) {
                        try {
                            // Clear existing overlays
                            mapView.overlays.clear()

                            // Add polyline
                            mapView.overlays.add(pathOverlay)

                            // Create and add start marker
                            val startMarker = Marker(mapView).apply {
                                position = currentSelectedTrail.points.first()
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                icon = startIcon
                                title = "Start"
                            }

                            // Create and add end marker
                            val endMarker = Marker(mapView).apply {
                                position = currentSelectedTrail.points.last()
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                icon = endIcon
                                title = "Sfârșit"
                            }

                            mapView.overlays.add(startMarker)
                            mapView.overlays.add(endMarker)

                            // Zoom to bounding box with animation
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
            // Clear map if no trail selected
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
                    Text(
                        text = currentSelectedTrail?.name ?: "Hartă Traseu",
                        maxLines = 1
                    )
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
                ) { view ->
                    // Additional setup if needed when view is created/updated
                }

                // Show loading indicator if no trail is selected
                if (currentSelectedTrail == null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                color = AppColors.primaryGreen
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Se încarcă harta...",
                                color = AppColors.textLight
                            )
                        }
                    }
                }
            }
        }
    )
}