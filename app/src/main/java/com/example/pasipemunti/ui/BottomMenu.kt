import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.*
import com.example.pasipemunti.auth.UserPreferencesManager
import com.example.pasipemunti.data.LocalDatabase
import com.example.pasipemunti.home.HikingStatsScreen
import com.example.pasipemunti.maptrailcollection.MapTrailsScreen
import com.example.pasipemunti.profile.ProfileScreen
import com.example.pasipemunti.profile.ProfileViewModel
import com.example.pasipemunti.profile.ProfileViewModelFactory
import com.example.pasipemunti.searchhike.SearchHikeScreen
import com.example.pasipemunti.searchhike.SearchHikeViewModel
import com.example.pasipemunti.traillist.TrailListScreen
import com.example.pasipemunti.traillist.TrailMapScreen
import com.example.pasipemunti.ui.BottomMenuItem
import com.example.pasipemunti.ui.TrailListViewModel
import com.example.pasipemunti.ui.TrailListViewModelFactory


@Composable
fun BottomMenu() {
    val navController = rememberNavController()
    var selectedItem by remember { mutableStateOf<BottomMenuItem>(BottomMenuItem.Home) }

    // contextul pt bd
    val context = LocalContext.current
    val trailListViewModel: TrailListViewModel = viewModel(
        factory = TrailListViewModelFactory(LocalDatabase.getDatabase(context).gpxTrailDao())
    )

    val profileViewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModelFactory(UserPreferencesManager.getInstance(context))
    )

    // Moved userId inside BottomMenu composable so it can be accessed by HomeScreen
    val userId = UserPreferencesManager.getInstance(context).getUserData()?.userId

    val items = listOf(
        BottomMenuItem.TrailList,
        BottomMenuItem.Search,
        BottomMenuItem.Home,
        BottomMenuItem.Map,
        BottomMenuItem.Profile
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEach { item ->
                    val isSelected = item == selectedItem
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = if (isSelected) Color(0xff1a570a) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        },
                        label = {
                            Text(
                                text = item.label,
                                color = if (isSelected) Color(0xff1a570a) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        },
                        selected = isSelected,
                        onClick = {
                            selectedItem = item
                            when (item) {
                                is BottomMenuItem.Search -> navController.navigate("searchHike")
                                is BottomMenuItem.TrailList -> navController.navigate("trailList")
                                is BottomMenuItem.Home -> navController.navigate("home")
                                is BottomMenuItem.Map -> navController.navigate("map")
                                is BottomMenuItem.Profile -> navController.navigate("profile")
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color(0x1A47B36D)
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("home") {
                // Pass userId to HomeScreen
                HomeScreen(userId = userId.toString())
            }
            composable("searchHike") { SearchHikeScreen() }
            composable("trailList") {
                // Pasăm ViewModel-ul deja instanțiat
                com.example.pasipemunti.traillist.TrailListScreen(navController, trailListViewModel)
            }
            composable("trailList") {
                val context = LocalContext.current
                val dao = remember { LocalDatabase.getDatabase(context).gpxTrailDao() }
                val viewModel: TrailListViewModel = viewModel(factory = TrailListViewModelFactory(dao))
                TrailListScreen(navController = navController, viewModel = viewModel)
            }

            composable("trailMap") {
                TrailMapScreen(navController = navController)
            }
            composable("map") { MapScreen() }
            composable("profile") { ProfileScreen(profileViewModel) }
        }
    }
}

// Această funcție TrailListScreen de aici nu mai este necesară,
// deoarece o apelezi direct pe cea din pachetul `traillist`.
// Poți să o ștergi.

/* @Composable
fun TrailListScreen(
    navController: androidx.navigation.NavController,
    viewModel: TrailListViewModel = viewModel() // Aceasta va crea o nouă instanță dacă nu se specifică factory
) {
    com.example.pasipemunti.traillist.TrailListScreen(navController, viewModel)
} */


@Composable
fun HomeScreen(userId: String?) {
    // Handle the case where userId might be null
    userId?.let { id ->
        HikingStatsScreen(userId = id)
    } ?: run {
        // Show a loading or error state when userId is null
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Text("Loading user data...")
        }
    }
}

@Composable
fun MapScreen() {
    MapTrailsScreen()
}

//@Composable
//fun ProfileScreen() {
//    ProfileScreen()
//}

// @Preview
// @Composable
// fun BottomMenuPreview() {
//     BottomMenu()
// }