import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.*
import com.example.pasipemunti.home.BarChartScreen
import com.example.pasipemunti.home.LineChartScreen
import com.example.pasipemunti.searchhike.SearchHikeScreen
import com.example.pasipemunti.ui.BottomMenuItem


@Composable
fun BottomMenu() {
    val navController = rememberNavController()
    var selectedItem by remember { mutableStateOf<BottomMenuItem>(BottomMenuItem.Home) }

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
            composable("home") { HomeScreen() }
            composable("searchHike") { SearchHikeScreen() }
            composable("trailList") { TrailListScreen() }
            composable("map") { MapScreen() }
            composable("profile") { ProfileScreen() }
        }
    }
}


@Composable
fun TrailListScreen() {
    Text("A list of available trails", Modifier.padding(16.dp))
}

@Composable
fun HomeScreen() {
   BarChartScreen()
}

@Composable
fun MapScreen() {
    Text("Screen with trails", Modifier.padding(16.dp))
}

@Composable
fun ProfileScreen() {
    Text("Profile screen", Modifier.padding(16.dp))
}

//@Preview
//@Composable
//fun BottomMenuPreview() {
//    BottomMenu()
//}