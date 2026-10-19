package com.aura.weather.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * OpenWeatherMap Current Weather DTO
 * Endpoint: /data/2.5/weather
 */
@Serializable
data class WeatherResponse(
    @SerialName("coord") val coord: CoordinatesDto,
    @SerialName("weather") val weather: List<WeatherConditionDto>,
    @SerialName("base") val base: String? = null,
    @SerialName("main") val main: MainWeatherDataDto,
    @SerialName("visibility") val visibility: Int = 10000,
    @SerialName("wind") val wind: WindDto,
    @SerialName("clouds") val clouds: CloudsDto? = null,
    @SerialName("dt") val dt: Long,
    @SerialName("sys") val sys: SysDto,
    @SerialName("timezone") val timezone: Int = 0,
    @SerialName("id") val id: Long = 0,
    @SerialName("name") val name: String,
    @SerialName("cod") val cod: Int = 200
)

@Serializable
data class CoordinatesDto(
    @SerialName("lon") val lon: Double,
    @SerialName("lat") val lat: Double
)

@Serializable
data class WeatherConditionDto(
    @SerialName("id") val id: Int,
    @SerialName("main") val main: String,
    @SerialName("description") val description: String,
    @SerialName("icon") val icon: String
)

@Serializable
data class MainWeatherDataDto(
    @SerialName("temp") val temp: Double,
    @SerialName("feels_like") val feelsLike: Double,
    @SerialName("temp_min") val tempMin: Double,
    @SerialName("temp_max") val tempMax: Double,
    @SerialName("pressure") val pressure: Int,
    @SerialName("humidity") val humidity: Int,
    @SerialName("sea_level") val seaLevel: Int? = null,
    @SerialName("grnd_level") val grndLevel: Int? = null
)

@Serializable
data class WindDto(
    @SerialName("speed") val speed: Double,
    @SerialName("deg") val deg: Int = 0,
    @SerialName("gust") val gust: Double? = null
)

@Serializable
data class CloudsDto(
    @SerialName("all") val all: Int = 0
)

@Serializable
data class SysDto(
    @SerialName("country") val country: String = "",
    @SerialName("sunrise") val sunrise: Long = 0,
    @SerialName("sunset") val sunset: Long = 0
)

/**
 * OpenWeatherMap 5-Day / 3-Hour Forecast DTO
 * Endpoint: /data/2.5/forecast
 */
@Serializable
data class ForecastResponse(
    @SerialName("cod") val cod: String,
    @SerialName("message") val message: Int = 0,
    @SerialName("cnt") val cnt: Int,
    @SerialName("list") val list: List<ForecastItemDto>,
    @SerialName("city") val city: CityInfoDto
)

@Serializable
data class ForecastItemDto(
    @SerialName("dt") val dt: Long,
    @SerialName("main") val main: MainWeatherDataDto,
    @SerialName("weather") val weather: List<WeatherConditionDto>,
    @SerialName("clouds") val clouds: CloudsDto? = null,
    @SerialName("wind") val wind: WindDto,
    @SerialName("visibility") val visibility: Int = 10000,
    @SerialName("pop") val pop: Double = 0.0, // Probability of precipitation (0.0 to 1.0)
    @SerialName("dt_txt") val dtTxt: String
)

@Serializable
data class CityInfoDto(
    @SerialName("id") val id: Long,
    @SerialName("name") val name: String,
    @SerialName("coord") val coord: CoordinatesDto,
    @SerialName("country") val country: String,
    @SerialName("timezone") val timezone: Int,
    @SerialName("sunrise") val sunrise: Long = 0,
    @SerialName("sunset") val sunset: Long = 0
)

/**
 * Clean UI Domain Models for Jetpack Compose
 */
data class WeatherData(
    val cityName: String,
    val country: String,
    val currentTempCelsius: Int,
    val feelsLikeCelsius: Int,
    val highTempCelsius: Int,
    val lowTempCelsius: Int,
    val condition: String,
    val conditionDescription: String,
    val iconCode: String,
    val humidityPercent: Int,
    val dewPointCelsius: Int,
    val windSpeedKmH: Int,
    val windDirectionDegrees: Int,
    val windGustsKmH: Int,
    val pressureHPa: Int,
    val visibilityKm: Int,
    val uvIndex: Int,
    val airQualityIndex: Int,
    val airQualityLabel: String,
    val narrativeInsight: String,
    val formattedTimestamp: String,
    val latitude: Double,
    val longitude: Double,
    val hourlyForecast: List<HourlyForecast>,
    val dailyForecast: List<DailyForecast>
)

data class HourlyForecast(
    val timeLabel: String,
    val hourOfDay: Int,
    val tempCelsius: Int,
    val conditionIcon: String,
    val conditionName: String,
    val precipitationChance: Int,
    val isNow: Boolean = false
)

data class DailyForecast(
    val dayName: String,
    val dateString: String,
    val minTempCelsius: Int,
    val maxTempCelsius: Int,
    val conditionName: String,
    val conditionIcon: String,
    val precipitationChance: Int
)

data class SavedCity(
    val id: String,
    val name: String,
    val country: String,
    val tempCelsius: Int,
    val highCelsius: Int,
    val lowCelsius: Int,
    val condition: String,
    val localTime: String,
    val isFavorite: Boolean = false,
    val advisoryNotice: String? = null,
    val themeGradient: CityTheme = CityTheme.CYAN,
    val badgeLabel: String,
    val latitude: Double,
    val longitude: Double
)

enum class CityTheme {
    CYAN, NIGHT, AMBER, CHARCOAL, FROST
}
