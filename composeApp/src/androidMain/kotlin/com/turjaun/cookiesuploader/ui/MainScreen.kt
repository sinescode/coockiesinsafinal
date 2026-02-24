package com.turjaun.cookiesuploader.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.turjaun.cookiesuploader.App
import com.turjaun.cookiesuploader.viewmodel.MainViewModel
import com.turjaun.cookiesuploader.ui.screens.HomeTab
import com.turjaun.cookiesuploader.ui.screens.SavedTab
import com.turjaun.cookiesuploader.ui.screens.SettingsTab

@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel { MainViewModel(App.container) }
) {
    var currentTabIndex by remember { mutableIntStateOf(0) }
    
    val serverStatus by viewModel.serverStatus.collectAsState()
    val isChecking by viewModel.isChecking.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Account Manager", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF111827)
                ),
                actions = {
                    // Server Status Indicator
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .background(
                                color = when (serverStatus) {
                                    "ON" -> Color.Green.copy(alpha = 0.2f)
                                    "OFF" -> Color.Red.copy(alpha = 0.2f)
                                    else -> Color.Gray.copy(alpha = 0.1f)
                                },
                                shape = MaterialTheme.shapes.small
                            )                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = serverStatus,
                            color = when (serverStatus) {
                                "ON" -> Color.Green
                                "OFF" -> Color.Red
                                else -> Color.Gray
                            },
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                        )
                    }
                    
                    // Refresh Button
                    if (isChecking) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(16.dp)
                                .size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                    } else {
                        IconButton(
                            onClick = { viewModel.checkServerStatus() },
                            content = { Icon(Icons.Default.Refresh, contentDescription = "Check Server", tint = Color.White) }
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF111827),
                contentColor = Color(0xFF94a3b8)
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = currentTabIndex == 0,
                    onClick = { currentTabIndex = 0 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF22c55e),
                        selectedTextColor = Color(0xFF22c55e)
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.SaveAlt, contentDescription = "Saved") },
                    label = { Text("Saved") },                    selected = currentTabIndex == 1,
                    onClick = { currentTabIndex = 1 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF22c55e),
                        selectedTextColor = Color(0xFF22c55e)
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    selected = currentTabIndex == 2,
                    onClick = { currentTabIndex = 2 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF22c55e),
                        selectedTextColor = Color(0xFF22c55e)
                    )
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (currentTabIndex) {
                0 -> HomeTab(viewModel)
                1 -> SavedTab(viewModel)
                2 -> SettingsTab(viewModel)
            }
        }
    }
}