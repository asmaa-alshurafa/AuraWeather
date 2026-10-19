package com.aura.weather.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.weather.data.model.CityTheme
import com.aura.weather.data.model.SavedCity
import com.aura.weather.data.model.WeatherData
import com.aura.weather.data.repository.Resource
import com.aura.weather.data.repository.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * UI State for the Weather Application
 */
data class WeatherUiState(
    val isLoading: Boolean = false,
    val weatherData: WeatherData? = null,
    val errorMessage: String? = null,
    val isCelsius: Boolean = false, // Defaults to Fahrenheit per screenshots (°F default, toggleable to °C)
    val searchQuery: String = "",
    val savedCities: List<SavedCity> = emptyList(),
    val isOrganizeMode: Boolean = false,
    val isRefreshing: Boolean = false
)

enum class TemperatureUnit {
    CELSIUS, FAHRENHEIT
}

class WeatherViewModel(
    private val repository: WeatherRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeatherUiState(isLoading = true))
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    init {
        initDefaultSavedCities()
        loadDefaultCityWeather("San Francisco")
    }

    private fun initDefaultSavedCities() {
        val initialList = listOf(
            SavedCity(
                id = "sf",
                name = "San Francisco",
                country = "United States",
                tempCelsius = 20,
                highCelsius = 22,
                lowCelsius = 12,
                condition = "Partly Cloudy",
                localTime = "10:45 AM PDT",
                isFavorite = true,
                themeGradient = CityTheme.CYAN,
                badgeLabel = "AQI 28 (Pristine)",
                latitude = 37.7749,
                longitude = -122.4194
            ),
            SavedCity(
                id = "tokyo",
                name = "Tokyo",
                country = "Japan",
                tempCelsius = 19,
                highCelsius = 21,
                lowCelsius = 15,
                condition = "Heavy Neon Rain",
                localTime = "02:45 AM JST",
                advisoryNotice = "Wind Advisory · Coastal Gales up to 48kt",
                themeGradient = CityTheme.NIGHT,
                badgeLabel = "Precip 95%",
                latitude = 35.6762,
                longitude = 139.6503
            ),
            SavedCity(
                id = "ny",
                name = "New York",
                country = "United States",
                tempCelsius = 24,
                highCelsius = 27,
                lowCelsius = 18,
                condition = "Clear Zenith",
                localTime = "01:45 PM EDT",
                themeGradient = CityTheme.AMBER,
                badgeLabel = "UV 7 (Very High)",
                latitude = 40.7128,
                longitude = -74.0060
            ),
            SavedCity(
                id = "london",
                name = "London",
                country = "United Kingdom",
                tempCelsius = 14,
                highCelsius = 16,
                lowCelsius = 10,
                condition = "River Fog & Mist",
                localTime = "06:45 PM BST",
                themeGradient = CityTheme.CHARCOAL,
                badgeLabel = "Humidity 92%",
                latitude = 51.5074,
                longitude = -0.1278
            ),
            SavedCity(
                id = "reykjavik",
                name = "Reykjavik",
                country = "Iceland",
                tempCelsius = 4,
                highCelsius = 5,
                lowCelsius = 1,
                condition = "Brisk Flurries",
                localTime = "05:45 PM GMT",
                themeGradient = CityTheme.FROST,
                badgeLabel = "Aurora Prob. 78%",
                latitude = 64.1466,
                longitude = -21.9426
            )
        )
        _uiState.update { it.copy(savedCities = initialList) }
    }

    fun loadDefaultCityWeather(city: String) {
        viewModelScope.launch {
            repository.getWeatherByCity(city).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                    }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                weatherData = resource.data,
                                errorMessage = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = resource.message
                            )
                        }
                    }
                }
            }
        }
    }

    fun fetchWeatherForCoordinates(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            repository.getWeatherByCoords(latitude, longitude).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                    }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                weatherData = resource.data,
                                errorMessage = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = resource.message
                            )
                        }
                    }
                }
            }
        }
    }

    fun toggleTemperatureUnit() {
        _uiState.update { it.copy(isCelsius = !it.isCelsius) }
    }

    fun setTemperatureUnit(isCelsius: Boolean) {
        _uiState.update { it.copy(isCelsius = isCelsius) }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun searchCity(cityName: String) {
        if (cityName.isNotBlank()) {
            loadDefaultCityWeather(cityName.trim())
            _uiState.update { it.copy(searchQuery = "") }
        }
    }

    fun addCity(name: String) {
        viewModelScope.launch {
            try {
                val newCity = SavedCity(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    country = "Global",
                    tempCelsius = 21,
                    highCelsius = 24,
                    lowCelsius = 15,
                    condition = "Partly Cloudy",
                    localTime = "Just Added",
                    themeGradient = CityTheme.CYAN,
                    badgeLabel = "Live Telemetry",
                    latitude = 0.0,
                    longitude = 0.0
                )
                _uiState.update { it.copy(savedCities = listOf(newCity) + it.savedCities) }
            } catch (_: Exception) {}
        }
    }

    fun removeCity(cityId: String) {
        _uiState.update { current ->
            current.copy(savedCities = current.savedCities.filterNot { it.id == cityId })
        }
    }

    fun toggleOrganizeMode() {
        _uiState.update { it.copy(isOrganizeMode = !it.isOrganizeMode) }
    }

    fun toggleFavorite(cityId: String) {
        _uiState.update { current ->
            val updated = current.savedCities.map { city ->
                if (city.id == cityId) city.copy(isFavorite = !city.isFavorite) else city
            }
            current.copy(savedCities = updated)
        }
    }

    fun refresh() {
        val currentCity = _uiState.value.weatherData?.cityName ?: "San Francisco"
        loadDefaultCityWeather(currentCity)
    }

    /**
     * Converts a Celsius temperature integer to either Celsius or Fahrenheit based on current state
     */
    fun formatTemp(celsius: Int): String {
        return if (_uiState.value.isCelsius) {
            "$celsius°"
        } else {
            val f = ((celsius * 9) / 5) + 32
            "$f°"
        }
    }

    fun getDisplayTempValue(celsius: Int): Int {
        return if (_uiState.value.isCelsius) {
            celsius
        } else {
            ((celsius * 9) / 5) + 32
        }
    }
}
