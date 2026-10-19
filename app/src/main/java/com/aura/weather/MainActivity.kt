package com.aura.weather

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aura.weather.data.remote.RetrofitInstance
import com.aura.weather.data.repository.WeatherRepositoryImpl
import com.aura.weather.ui.navigation.AuraBottomNavigationBar
import com.aura.weather.ui.navigation.Screen
import com.aura.weather.ui.screens.HomeScreen
import com.aura.weather.ui.screens.RadarScreen
import com.aura.weather.ui.screens.SavedCitiesScreen
import com.aura.weather.ui.screens.SettingsScreen
import com.aura.weather.ui.theme.AuraWeatherTheme
import com.aura.weather.ui.viewmodel.WeatherViewModel

class MainActivity : ComponentActivity() {

    private val repository by lazy {
        WeatherRepositoryImpl(RetrofitInstance.api)
    }

    private val viewModel: WeatherViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return WeatherViewModel(repository) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AuraWeatherTheme {
                val navController = rememberNavController()
                val uiState by viewModel.uiState.collectAsState()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        AuraBottomNavigationBar(navController = navController)
                    }
                ) { paddingValues ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Home.route,
                        modifier = Modifier.padding(paddingValues)
                    ) {
                        composable(Screen.Home.route) {
                            HomeScreen(
                                uiState = uiState,
                                onSearchCity = { viewModel.searchCity(it) },
                                onGpsLocationClick = {
                                    // Default to SF GPS or FusedLocationProviderClient coordinates
                                    viewModel.fetchWeatherForCoordinates(37.7749, -122.4194)
                                },
                                onUnitToggle = { viewModel.toggleTemperatureUnit() },
                                onRetry = { viewModel.refresh() }
                            )
                        }

                        composable(Screen.Radar.route) {
                            val activeWeather = uiState.weatherData
                            RadarScreen(
                                cityName = activeWeather?.cityName ?: "Detected City",
                                targetLatitude = activeWeather?.latitude ?: 31.5000,
                                targetLongitude = activeWeather?.longitude ?: 34.4667,
                                onSearchCity = { viewModel.searchCity(it) },
                                onRecenterClick = {
                                    activeWeather?.let {
                                        viewModel.fetchWeatherForCoordinates(it.latitude, it.longitude)
                                    }
                                }
                            )
                        }

                        composable(Screen.SavedCities.route) {
                            SavedCitiesScreen(
                                savedCities = uiState.savedCities,
                                isCelsius = uiState.isCelsius,
                                isOrganizeMode = uiState.isOrganizeMode,
                                onCityClick = { city ->
                                    viewModel.loadDefaultCityWeather(city.name)
                                    navController.navigate(Screen.Home.route)
                                },
                                onDeleteCity = { cityId -> viewModel.removeCity(cityId) },
                                onToggleFavorite = { cityId -> viewModel.toggleFavorite(cityId) },
                                onToggleOrganize = { viewModel.toggleOrganizeMode() },
                                onAddCityClick = {
                                    // Trigger add dialog
                                }
                            )
                        }

                        composable(Screen.Settings.route) {
                            SettingsScreen(
                                isCelsius = uiState.isCelsius,
                                onToggleUnit = { viewModel.setTemperatureUnit(it) }
                            )
                        }
                    }
                }
            }
        }
    }
}
