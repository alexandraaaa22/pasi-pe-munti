package com.example.pasipemunti.searchhike

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SearchHikeScreen(viewModel: SearchHikeViewModel = viewModel()) {
    val context = LocalContext.current
    val routePoints by remember { derivedStateOf { viewModel.routePoints } }
    var mapView: MapView? by remember { mutableStateOf(null) }

    LaunchedEffect(Unit) {
        Configuration.getInstance().load(
            context,
            context.getSharedPreferences("osm_prefs", Context.MODE_PRIVATE)
        )
    }

    Box(Modifier.fillMaxSize()) {
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
                    }
                    map.overlays.add(polyline)
                    map.controller.setCenter(routePoints.first())
                    map.invalidate()
                }
            },
            modifier = Modifier.fillMaxSize()
        )

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
                    label = { Text("End") },
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
                Text("Find", color = Color.White)
            }
        }
    }

}



