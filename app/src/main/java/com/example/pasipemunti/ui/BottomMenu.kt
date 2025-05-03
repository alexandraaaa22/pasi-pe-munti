import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.*
import com.example.pasipemunti.searchhike.SearchHikeScreen
import com.example.pasipemunti.ui.BottomMenuItem


@Composable
fun BottomMenu() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val items = listOf(
                    BottomMenuItem.TrailList,
                    BottomMenuItem.Search,
                    BottomMenuItem.Home,
                    BottomMenuItem.Map,
                    BottomMenuItem.Profile
                )
                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = false,
                        onClick = {
                            Log.d("Navigation", "Navigating to ${item.label}") // Log de debugging
                            when (item) {
                                is BottomMenuItem.Search -> navController.navigate("searchHike")
                                is BottomMenuItem.TrailList -> navController.navigate("trailList")
                                is BottomMenuItem.Home -> navController.navigate("home")
                                is BottomMenuItem.Map -> navController.navigate("map")
                                is BottomMenuItem.Profile -> navController.navigate("profile")
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "home", // Punctul de start
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
    Text("Home screen", Modifier.padding(16.dp))
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

@Preview(showBackground = true)
@Composable
fun SearchScreenPreview() {
    SearchHikeScreen()
}
