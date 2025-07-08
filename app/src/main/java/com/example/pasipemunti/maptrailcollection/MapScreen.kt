package com.example.pasipemunti.maptrailcollection

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.pasipemunti.R
import com.example.pasipemunti.navigation.MiniNavBar
import com.example.pasipemunti.navigation.NavigationPanel
import com.example.pasipemunti.searchhike.SearchHikeViewModel
import com.google.android.gms.location.LocationServices
// Adaugă importul pentru UserPreferencesManager
import com.example.pasipemunti.auth.UserPreferencesManager

@Composable
fun MapTrailsScreen(viewModel: SearchHikeViewModel = viewModel()) {
    var selectedMassif by remember { mutableStateOf<MountainMassif?>(null) }
    var showTrailsOnMap by remember { mutableStateOf(false) }
    var isNavigationExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    // Instanțiază UserPreferencesManager
    val userPreferencesManager = remember { UserPreferencesManager.getInstance(context) }

    // permission handling
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.locationPermissionGranted = isGranted
    }

    // Configurare FusedLocationClient
    LaunchedEffect(Unit) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        viewModel.setFusedLocationClient(fusedLocationClient)

        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) -> {
                viewModel.locationPermissionGranted = true
            }
            else -> {
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (!showTrailsOnMap) {
            // Ecranul principal cu grid-ul de carduri
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF87CEEB), // Sky blue
                                Color(0xFFF0F8FF) // Alice blue
                            )
                        )
                    )
            ) {
                // Header doar pe ecranul principal
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Explorează Munții României",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                // Grid cu carduri pentru masivele muntoase
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(mountainMassifs) { massif ->
                        MountainMassifCard(
                            massif = massif,
                            onClick = {
                                selectedMassif = massif
                                showTrailsOnMap = true
                            }
                        )
                    }
                }
            }
        } else {
            // Ecranul cu harta - fără header
            TrailMapView(
                selectedMassif = selectedMassif,
                onBackClick = {
                    showTrailsOnMap = false
                    selectedMassif = null
                },
                viewModel = viewModel
            )
        }

        // Navigation Panel pentru MapTrailsScreen
        if (viewModel.isNavigating) {
            if (isNavigationExpanded) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 16.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    NavigationPanel(
                        distanceTraveled = viewModel.distanceTraveled,
                        distanceRemaining = viewModel.distanceRemaining,
                        currentAltitude = viewModel.currentAltitude,
                        elapsedTimeMillis = viewModel.elapsedTimeMillis,
                        onCollapse = { isNavigationExpanded = false },
                        // *** AICI ESTE MODIFICAREA ***
                        onFinish = {
                            // Preluăm userId-ul de la utilizatorul logat
                            val userId = userPreferencesManager.getUserData()?.userId ?: -1
                            viewModel.finishHike(
                                userId = userId, // Transmitem userId-ul real
                                difficulty = "moderate",
                                weatherCondition = "sunny"
                            )
                            isNavigationExpanded = false
                        }
                    )
                }
            } else {
                // Mini navigation bar
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 16.dp),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    MiniNavBar(onExpand = { isNavigationExpanded = true })
                }
            }
        }
    }
}

@Composable
fun MountainMassifCard(
    massif: MountainMassif,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "card_scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .scale(scale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { onClick() }
                )
            },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box {
            // Imaginea de fundal
            AsyncImage(
                model = massif.imageRes,
                contentDescription = massif.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                error = painterResource(id = R.drawable.munte)
            )

            // Overlay gradient pentru text
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            ),
                            startY = 0f,
                            endY = Float.POSITIVE_INFINITY
                        )
                    )
            )

            // Conținutul textual
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Altitudinea în colțul din dreapta sus
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.9f)
                    ) {
                        Text(
                            text = "${massif.maxAltitude}m",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Numele masivului și numărul de trasee
                Column {
                    Text(
                        text = massif.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timeline,
                            contentDescription = "Trails",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${massif.trailCount} trasee",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}

// În MapTrailsScreen.kt - actualizează funcția TrailMapView
@Composable
fun TrailMapView(
    selectedMassif: MountainMassif?,
    onBackClick: () -> Unit,
    viewModel: SearchHikeViewModel
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        OsmMapView(
            selectedMassif = selectedMassif,
            viewModel = viewModel,
            modifier = Modifier.fillMaxSize()
        )

        // Buton de back suprapus pe hartă
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .background(
                    color = Color.White.copy(alpha = 0.9f),
                    shape = CircleShape
                )
                .size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color(0xFF2E7D32),
                modifier = Modifier.size(24.dp)
            )
        }

        // Opțional: Card cu informații despre masiv (suprapus în colțul din dreapta sus)
        selectedMassif?.let { massif ->
            Card(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.9f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = massif.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                    Text(
                        text = "${massif.trailCount} trasee",
                        fontSize = 12.sp,
                        color = Color(0xFF666666)
                    )
                }
            }
        }
    }
}

// Data class pentru masivele muntoase
data class MountainMassif(
    val id: String,
    val name: String,
    val imageRes: Int,
    val maxAltitude: Int,
    val trailCount: Int,
    val region: String
)

// Lista cu masivele muntoase din România
val mountainMassifs = listOf(
    MountainMassif(
        id = "fagaras",
        name = "Munții Făgăraș",
        imageRes = R.drawable.varful_moldoveanu,
        maxAltitude = 2545,
        trailCount = 25,
        region = "Carpații Meridionali"
    ),
    MountainMassif(
        id = "bucegi",
        name = "Munții Bucegi",
        imageRes = R.drawable.muntii_bucegi_sfinxul,
        maxAltitude = 2514,
        trailCount = 18,
        region = "Carpații Meridionali"
    ),
    MountainMassif(
        id = "piatra_craiului",
        name = "Munții Piatra Craiului",
        imageRes = R.drawable.muntii_piatra_craiului,
        maxAltitude = 2238,
        trailCount = 12,
        region = "Carpații Meridionali"
    ),
    MountainMassif(
        id = "ceahlau",
        name = "Munții Ceahlău",
        imageRes = R.drawable.ceahlau,
        maxAltitude = 1907,
        trailCount = 15,
        region = "Carpații Orientali"
    ),
    MountainMassif(
        id = "ciucas",
        name = "Munții Ciucaș",
        imageRes = R.drawable.ciucas,
        maxAltitude = 1954,
        trailCount = 14,
        region = "Carpații Orientali"
    ),
    MountainMassif(
        id = "apuseni",
        name = "Munții Apuseni",
        imageRes = R.drawable.apuseni,
        maxAltitude = 1848,
        trailCount = 22,
        region = "Carpații Occidentali"
    )
    //    MountainMassif(
//        id = "postaru",
//        name = "Munții Postăvaru",
//        imageRes = ,
//        maxAltitude = 1799,
//        trailCount = 8,
//        region = "Carpații Meridionali"
//    ),
//    MountainMassif(
//        id = "retezat",
//        name = "Munții Retezat",
//        imageUrl = "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=400&h=300&fit=crop",
//        maxAltitude = 2509,
//        trailCount = 20,
//        region = "Carpații Meridionali"
//    ),
)