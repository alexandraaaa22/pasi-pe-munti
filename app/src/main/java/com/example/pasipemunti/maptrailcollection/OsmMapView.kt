package com.example.pasipemunti.maptrailcollection

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import kotlinx.coroutines.launch

@Composable
fun OsmMapView(
    selectedMassif: MountainMassif?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var trails by remember { mutableStateOf<List<GpxTrail>>(emptyList()) }

    val trailManager = remember { GpxTrailManager(context) }

    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
        Configuration.getInstance().userAgentValue = context.packageName
    }

    LaunchedEffect(selectedMassif) {
        if (selectedMassif != null) {
            isLoading = true
            scope.launch {
                try {
                    val loadedTrails = trailManager.loadTrailsForZone(selectedMassif.name)
                    trails = loadedTrails

                    mapView?.let { map ->
                        trailManager.addTrailsToMap(map, loadedTrails)
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
                        trailManager.addTrailsToMap(this, trails)
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        ) { map ->
            if (trails.isNotEmpty()) {
                trailManager.addTrailsToMap(map, trails)
            }
        }

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

    DisposableEffect(Unit) {
        onDispose {
            mapView?.onDetach()
        }
    }
}