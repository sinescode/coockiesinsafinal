package com.turjaun.cookiesuploader.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.turjaun.cookiesuploader.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    var selectedTab by remember { mutableIntStateOf(0) }
    val viewModel: MainViewModel = viewModel()
    
    val tabTitles = listOf("Home", "Saved", "Settings")
    val tabIconsFilled = listOf(Icons.Filled.Home, Icons.Filled.Save, Icons.Filled.Settings)
    val tabIconsOutlined = listOf(Icons.Outlined.Home, Icons.Outlined.Save, Icons.Outlined.Settings)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tabTitles[selectedTab], color = AppColors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppColors.surface.copy(alpha = 0.95f)),
                modifier = Modifier.background(brush = Brush.verticalGradient(colors = listOf(AppColors.surface.copy(alpha = 0.98f), AppColors.surface.copy(alpha = 0.9f))))
            )
        },
        bottomBar = {
            NavigationBar(containerColor = AppColors.surface.copy(alpha = 0.98f), tonalElevation = 4.dp) {
                tabTitles.forEachIndexed { index, title ->
                    NavigationBarItem(
                        icon = { Icon(imageVector = if (selectedTab == index) tabIconsFilled[index] else tabIconsOutlined[index], contentDescription = title, modifier = Modifier.size(22.dp)) },
                        label = { Text(title, fontSize = 11.sp, fontWeight = if (selectedTab == index) FontWeight.Medium else FontWeight.Normal) },
                        selected = selectedTab == index,
                        onClick = { selectedTab = it },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AppColors.primary, unselectedIconColor = AppColors.textSecondary,
                            selectedTextColor = AppColors.primary, unselectedTextColor = AppColors.textSecondary,
                            indicatorColor = AppColors.primary.copy(alpha = 0.1f)
                        )
                    )
                }
            }
        },
        containerColor = AppColors.background
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (selectedTab) {
                0 -> HomeTab(viewModel)
                1 -> SavedTab(viewModel)
                2 -> SettingsTab(viewModel)
            }
        }
    }
}