package com.turjaun.cookiesuploader.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
import androidx.compose.material3.MaterialTheme
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
import com.turjaun.cookiesuploader.ui.theme.ModernColors

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
                title = {
                    Text(
                        tabTitles[selectedTab],
                        color = ModernColors.textPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ModernColors.surface.copy(alpha = 0.9f)
                ),
                modifier = Modifier.background(
                    brush = Brush.verticalGradient(
                        colors = listOf(ModernColors.surface.copy(alpha = 0.95f), ModernColors.surface.copy(alpha = 0.85f))
                    )
                )
            )
        },
        bottomBar = {
            ModernBottomNavigation(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                titles = tabTitles,
                iconsFilled = tabIconsFilled,
                iconsOutlined = tabIconsOutlined
            )
        },
        containerColor = ModernColors.background
    ) { padding ->
        // Smooth tab transitions
        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = {
                if (targetState > initialState) {
                    slideInHorizontally(animationSpec = tween(300)) { it } + fadeIn(animationSpec = tween(300))
                } else {
                    slideInHorizontally(animationSpec = tween(300)) { -it } + fadeIn(animationSpec = tween(300))
                } + slideOutHorizontally(animationSpec = tween(300)) { if (targetState > initialState) -it else it } + fadeOut(animationSpec = tween(300))
            },
            label = "TabTransition"
        ) { tab ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                when (tab) {
                    0 -> HomeTab(viewModel)
                    1 -> SavedTab(viewModel)
                    2 -> SettingsTab(viewModel)
                }
            }
        }
    }
}

@Composable
fun ModernBottomNavigation(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    titles: List<String>,
    iconsFilled: List<androidx.compose.ui.graphics.vector.ImageVector>,
    iconsOutlined: List<androidx.compose.ui.graphics.vector.ImageVector>
) {
    NavigationBar(
        containerColor = ModernColors.surface.copy(alpha = 0.95f),
        tonalElevation = 8.dp,
        modifier = Modifier.background(
            brush = Brush.verticalGradient(
                colors = listOf(ModernColors.surface, ModernColors.surfaceVariant)
            )
        )
    ) {
        titles.forEachIndexed { index, title ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = if (selectedTab == index) iconsFilled[index] else iconsOutlined[index],
                        contentDescription = title,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        title,
                        fontSize = 12.sp,
                        fontWeight = if (selectedTab == index) FontWeight.Medium else FontWeight.Normal
                    )
                },
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = ModernColors.primary,
                    unselectedIconColor = ModernColors.textSecondary,
                    selectedTextColor = ModernColors.primary,
                    unselectedTextColor = ModernColors.textSecondary,
                    indicatorColor = ModernColors.primary.copy(alpha = 0.12f)
                )
            )
        }
    }
}