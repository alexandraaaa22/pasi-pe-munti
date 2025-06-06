package com.example.pasipemunti.traillist

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.pasipemunti.data.LocalDatabase // Import
import com.example.pasipemunti.data.GPXTrail // Import modelul de domeniu/UI
import com.example.pasipemunti.ui.TrailListViewModel // Import
import com.example.pasipemunti.ui.TrailListViewModelFactory // Import
import com.example.pasipemunti.home.HikingAppTheme // Asigură-te că importul e corect
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

// Enum pentru opțiunile de sortare
enum class SortOption(val displayName: String) {
    NAME("Denumire"),
    DISTANCE("Distanță"),
    ELEVATION("Altitudine"),
    ZONE("Zonă"),
    DURATION("Durată")
}

// Function to format duration in readable format (primește secunde)
fun formatDuration(durationSeconds: Long): String {
    val hours = TimeUnit.SECONDS.toHours(durationSeconds)
    val minutes = TimeUnit.SECONDS.toMinutes(durationSeconds) % 60
    return when {
        hours > 0 -> String.format("%dh %02dm", hours, minutes)
        else -> String.format("%dm", minutes)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrailListScreen(
    navController: NavController,
    viewModel: TrailListViewModel // Se așteaptă să fie pasat
) {
    // Colectează fluxul de date `allTrails` ca stare
    val allTrails by viewModel.allTrails.collectAsState(initial = emptyList())

    // State pentru sortare
    var currentSortOption by remember { mutableStateOf(SortOption.NAME) }
    var isAscending by remember { mutableStateOf(true) }
    var showSortMenu by remember { mutableStateOf(false) }

    // Sortează traseele în funcție de opțiunea selectată
    val sortedTrails = remember(allTrails, currentSortOption, isAscending) {
        val sorted = when (currentSortOption) {
            SortOption.NAME -> allTrails.sortedBy { it.name.lowercase() }
            SortOption.DISTANCE -> allTrails.sortedBy { it.distance }
            SortOption.ELEVATION -> allTrails.sortedBy { it.maxElevation }
            SortOption.ZONE -> allTrails.sortedBy { it.zone?.lowercase() ?: "z" } // Pune zonele null la final
            SortOption.DURATION -> allTrails.sortedBy { it.duration }
        }
        if (isAscending) sorted else sorted.reversed()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Traseele Tale",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = HikingAppTheme.primaryGreen
                    )
                },
                actions = {
                    // Buton pentru sortare
                    IconButton(
                        onClick = { showSortMenu = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sort,
                            contentDescription = "Sortare",
                            tint = HikingAppTheme.primaryGreen
                        )
                    }

                    // Dropdown menu pentru sortare
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        Text(
                            text = "Sortare după:",
                            fontWeight = FontWeight.Bold,
                            color = HikingAppTheme.textDark,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )

                        Divider(color = HikingAppTheme.primaryGreen.copy(alpha = 0.3f))

                        SortOption.values().forEach { option ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = option.displayName,
                                            color = if (currentSortOption == option)
                                                HikingAppTheme.primaryGreen else HikingAppTheme.textDark
                                        )
                                        if (currentSortOption == option) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Icon(
                                                imageVector = if (isAscending) Icons.Default.KeyboardArrowUp
                                                else Icons.Default.KeyboardArrowDown,
                                                contentDescription = if (isAscending) "Crescător" else "Descrescător",
                                                tint = HikingAppTheme.primaryGreen,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    if (currentSortOption == option) {
                                        // Dacă e aceeași opțiune, schimbă ordinea
                                        isAscending = !isAscending
                                    } else {
                                        // Opțiune nouă, începe cu crescător
                                        currentSortOption = option
                                        isAscending = true
                                    }
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = HikingAppTheme.primaryGreen
                ),
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        },
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Enhanced gradient background
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    HikingAppTheme.backgroundGreen.copy(alpha = 0.3f),
                                    Color.White,
                                    HikingAppTheme.backgroundGreen.copy(alpha = 0.1f)
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp)
                ) {
                    if (allTrails.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(
                                    color = HikingAppTheme.primaryGreen,
                                    strokeWidth = 3.dp,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Se încarcă traseele...",
                                    color = HikingAppTheme.textLight,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    } else {
                        // Trail count header cu info de sortare
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = HikingAppTheme.primaryGreen.copy(alpha = 0.1f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Hiking,
                                            contentDescription = "Trails",
                                            tint = HikingAppTheme.primaryGreen,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "${sortedTrails.size} trasee disponibile",
                                            color = HikingAppTheme.primaryGreen,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Info sortare
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Sort,
                                        contentDescription = "Sortare",
                                        tint = HikingAppTheme.primaryGreen.copy(alpha = 0.7f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Sortate după ${currentSortOption.displayName.lowercase()} " +
                                                "(${if (isAscending) "crescător" else "descrescător"})",
                                        color = HikingAppTheme.primaryGreen.copy(alpha = 0.8f),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(vertical = 16.dp)
                        ) {
                            items(sortedTrails, key = { it.id }) { trail ->
                                TrailCard(
                                    trail = trail,
                                    onClick = {
                                        viewModel.selectTrail(trail)
                                        navController.navigate("trailMap")
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun TrailCard(
    trail: GPXTrail,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = HikingAppTheme.primaryGreen.copy(alpha = 0.1f)
            )
            .animateContentSize(
                animationSpec = tween(
                    durationMillis = 300,
                    easing = LinearOutSlowInEasing
                )
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header with trail name and arrow
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = trail.name,
                    color = HikingAppTheme.textDark,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Vizualizează",
                    tint = HikingAppTheme.primaryGreen,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Enhanced stats section with better layout
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = HikingAppTheme.backgroundGreen.copy(alpha = 0.1f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TrailStatItem(
                        icon = Icons.Filled.Straighten,
                        value = String.format("%.1f", trail.distance),
                        unit = "km",
                        label = "Distanță"
                    )

                    Divider(
                        modifier = Modifier
                            .height(50.dp)
                            .width(1.dp),
                        color = HikingAppTheme.primaryGreen.copy(alpha = 0.3f)
                    )

                    TrailStatItem(
                        icon = Icons.Filled.TrendingUp,
                        value = String.format("%d", trail.elevationGain.toInt()),
                        unit = "m",
                        label = "Diferență de nivel"
                    )

                    Divider(
                        modifier = Modifier
                            .height(50.dp)
                            .width(1.dp),
                        color = HikingAppTheme.primaryGreen.copy(alpha = 0.3f)
                    )

                    TrailStatItem(
                        icon = Icons.Filled.Height,
                        value = String.format("%d", trail.maxElevation.toInt()),
                        unit = "m",
                        label = "Alt. Max."
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Additional info section
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Zone info
                if (trail.zone != null && trail.zone.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Zona",
                            tint = HikingAppTheme.primaryGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = trail.zone,
                            color = HikingAppTheme.textLight,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Duration info
                if (trail.duration > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Durata",
                            tint = HikingAppTheme.primaryGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Durata: ${formatDuration(trail.duration)}",
                            color = HikingAppTheme.textLight,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TrailStatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    unit: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = HikingAppTheme.primaryGreen,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.height(6.dp))

        Row(
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = value,
                fontWeight = FontWeight.Bold,
                color = HikingAppTheme.textDark,
                fontSize = 18.sp
            )
            Text(
                text = unit,
                color = HikingAppTheme.textLight,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 2.dp)
            )
        }

        Text(
            text = label,
            color = HikingAppTheme.textLight,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}