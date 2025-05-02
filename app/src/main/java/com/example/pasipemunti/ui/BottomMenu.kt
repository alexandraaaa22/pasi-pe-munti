package com.example.pasipemunti.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun BottomMenuScreen() {
    val selectedItem = remember { mutableStateOf<BottomMenuItem>(BottomMenuItem.Home) }

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
                        selected = selectedItem.value == item,
                        onClick = { selectedItem.value = item }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedItem.value) {
                is BottomMenuItem.TrailList -> TrailListScreen()
                is BottomMenuItem.Search -> SearchScreen()
                is BottomMenuItem.Home -> HomeScreen()
                is BottomMenuItem.Map -> MapScreen()
                is BottomMenuItem.Profile -> ProfileScreen()
            }
        }
    }
}

@Composable fun TrailListScreen() = Text("A list of available trails", Modifier.padding(16.dp))
@Composable fun SearchScreen() = Text("The screen for searching a trail", Modifier.padding(16.dp))
@Composable fun HomeScreen() = Text("Home screen", Modifier.padding(16.dp))
@Composable fun MapScreen() = Text("Map with possible trails", Modifier.padding(16.dp))
@Composable fun ProfileScreen() = Text("Profile screen", Modifier.padding(16.dp))

@Preview
@Composable
fun BottomMenuScreenPreview() {
    MyAppTheme {
        BottomMenuScreen()
    }
}

@Composable
fun MyAppTheme(content: @Composable () -> Unit) {
    androidx.compose.material.MaterialTheme {
        content()
    }
}
