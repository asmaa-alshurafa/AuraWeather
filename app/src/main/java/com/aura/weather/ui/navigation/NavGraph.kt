package com.aura.weather.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.aura.weather.ui.theme.*

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Home : Screen("weather", "Weather", Icons.Default.Home)
    data object Radar : Screen("radar", "Live Radar", Icons.Default.LocationOn)
    data object SavedCities : Screen("saved_cities", "Saved Cities", Icons.Default.Place)
    data object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

@Composable
fun AuraBottomNavigationBar(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        Screen.Home,
        Screen.Radar,
        Screen.SavedCities,
        Screen.Settings
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        modifier = modifier
            .fillMaxWidth()
            .background(color = DeepMidnightSapphire.copy(alpha = 0.9f)),
        containerColor = DeepMidnightSapphire.copy(alpha = 0.92f),
        contentColor = TextHighEmphasis,
        tonalElevation = 8.dp
    ) {
        items.forEach { screen ->
            val isSelected = currentRoute == screen.route

            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = screen.title,
                        tint = if (isSelected) ElectricCyan else Color(0xFF8E99A8)
                    )
                },
                label = {
                    Text(
                        text = screen.title,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) ElectricCyan else Color(0xFF8E99A8)
                    )
                },
                selected = isSelected,
                onClick = {
                    if (currentRoute != screen.route) {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}