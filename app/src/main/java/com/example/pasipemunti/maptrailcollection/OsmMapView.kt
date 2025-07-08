package com.example.pasipemunti.maptrailcollection

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.pasipemunti.R
import com.example.pasipemunti.searchhike.SearchHikeViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import kotlinx.coroutines.launch
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

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

    val icon = BitmapFactory.decodeResource(context.resources, R.drawable.hiker)

    val trailManager = remember { GpxTrailManager(context) }

    // Configurare osmdroid
    LaunchedEffect(Unit) {
        checkAndRequestLocationPermission(context, viewModel)

        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
        Configuration.getInstance().userAgentValue = context.packageName
    }

    // Încarcă traseele când se schimbă zona
    LaunchedEffect(selectedMassif) {
        if (selectedMassif != null) {
            isLoading = true
            scope.launch {
                try {
                    val loadedTrails = trailManager.loadTrailsForZone(selectedMassif.name)
                    trails = loadedTrails

                    mapView?.let { map ->
                        trailManager.addTrailsToMap(map, trails, viewModel)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    isLoading = false
                }
            }
        }
    }

    Box(modifier = modifier) {
        // Componente AndroidView pentru MapView
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    setBuiltInZoomControls(true)

                    controller.setZoom(8.0)
                    controller.setCenter(GeoPoint(45.9432, 24.9668))

                    mapView = this

                    val myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(ctx), this)
                    val originalIcon = BitmapFactory.decodeResource(ctx.resources, R.drawable.hiker)
                    val scaledIcon = Bitmap.createScaledBitmap(originalIcon, 200, 200, true)
                    myLocationOverlay.setPersonIcon(scaledIcon)
                    myLocationOverlay.enableMyLocation()
                    myLocationOverlay.enableFollowLocation()
                    myLocationOverlay.runOnFirstFix {
                        Handler(Looper.getMainLooper()).post {
                            controller.animateTo(myLocationOverlay.myLocation)
                        }
                    }
                    this.overlays.add(myLocationOverlay)

                    if (trails.isNotEmpty()) {
                        trailManager.addTrailsToMap(this, trails, viewModel)
                    }
                }
            },
            update = { map ->
                if (trails.isNotEmpty()) {
                    trailManager.addTrailsToMap(map, trails, viewModel)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Indicator de încărcare
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.padding(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Se încarcă traseele...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        // Mesaj fallback dacă nu sunt trasee
        if (!isLoading && trails.isEmpty()) {
            Card(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Nu s-au găsit trasee",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Pentru ${selectedMassif?.name ?: "această zonă"}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }

    // Cleanup la închiderea Composable-ului
    DisposableEffect(Unit) {
        onDispose {
            mapView?.onDetach()
        }
    }
}