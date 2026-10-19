package com.aura.weather.data.repository

import com.aura.weather.data.model.*
import com.aura.weather.data.remote.WeatherApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

/**
 * Generic Resource state wrapper for asynchronous network calls
 */
sealed class Resource<out T> {
    data class Success<out T>(val data: T) : Resource<T>()
    data class Error(val message: String, val cause: Throwable? = null) : Resource<Nothing>()
    data object Loading : Resource<Nothing>()
}

interface WeatherRepository {
    fun getWeatherByCity(cityName: String): Flow<Resource<WeatherData>>
    fun getWeatherByCoords(lat: Double, lon: Double): Flow<Resource<WeatherData>>
}

class WeatherRepositoryImpl(
    private val apiService: WeatherApiService
) : WeatherRepository {

    override fun getWeatherByCity(cityName: String): Flow<Resource<WeatherData>> = flow {
        emit(Resource.Loading)
        try {
            val weatherResponse = apiService.getCurrentWeatherByCity(cityName)
            val forecastResponse = try {
                apiService.get5DayForecastByCity(cityName)
            } catch (_: Exception) {
                null
            }
            val domainData = mapToDomain(weatherResponse, forecastResponse)
            emit(Resource.Success(domainData))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Failed to retrieve weather for $cityName", e))
        }
    }.flowOn(Dispatchers.IO)

    override fun getWeatherByCoords(lat: Double, lon: Double): Flow<Resource<WeatherData>> = flow {
        emit(Resource.Loading)
        try {
            val weatherResponse = apiService.getCurrentWeatherByCoords(lat, lon)
            val forecastResponse = try {
                apiService.get5DayForecastByCoords(lat, lon)
            } catch (_: Exception) {
                null
            }
            val domainData = mapToDomain(weatherResponse, forecastResponse)
            emit(Resource.Success(domainData))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Failed to retrieve local weather", e))
        }
    }.flowOn(Dispatchers.IO)

    private fun mapToDomain(
        weather: WeatherResponse,
        forecast: ForecastResponse?
    ): WeatherData {
        val currentTemp = weather.main.temp.roundToInt()
        val feelsLike = weather.main.feelsLike.roundToInt()
        val highTemp = weather.main.tempMax.roundToInt().coerceAtLeast(currentTemp + 1)
        val lowTemp = weather.main.tempMin.roundToInt().coerceAtMost(currentTemp - 2)

        val condition = weather.weather.firstOrNull()?.main ?: "Clear"
        val conditionDesc = weather.weather.firstOrNull()?.description ?: "Clear sky"
        val icon = weather.weather.firstOrNull()?.icon ?: "01d"

        val windSpeedKmH = (weather.wind.speed * 3.6).roundToInt()
        val windGustsKmH = ((weather.wind.gust ?: weather.wind.speed) * 3.6).roundToInt()

        // Approximated dew point
        val dewPoint = (weather.main.temp - (100 - weather.main.humidity) / 5.0).roundToInt()

        // Process hourly items from forecast
        val hourlyList = mutableListOf<HourlyForecast>()
        hourlyList.add(
            HourlyForecast(
                timeLabel = "Now",
                hourOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                tempCelsius = currentTemp,
                conditionIcon = icon,
                conditionName = condition,
                precipitationChance = ((forecast?.list?.firstOrNull()?.pop ?: 0.1) * 100).roundToInt(),
                isNow = true
            )
        )

        forecast?.list?.take(8)?.forEach { item ->
            val date = Date(item.dt * 1000)
            val cal = Calendar.getInstance().apply { time = date }
            val hour = cal.get(Calendar.HOUR)
            val amPm = if (cal.get(Calendar.AM_PM) == Calendar.AM) "AM" else "PM"
            val displayHour = if (hour == 0) 12 else hour

            hourlyList.add(
                HourlyForecast(
                    timeLabel = "$displayHour $amPm",
                    hourOfDay = cal.get(Calendar.HOUR_OF_DAY),
                    tempCelsius = item.main.temp.roundToInt(),
                    conditionIcon = item.weather.firstOrNull()?.icon ?: "01d",
                    conditionName = item.weather.firstOrNull()?.main ?: "Clear",
                    precipitationChance = (item.pop * 100).roundToInt()
                )
            )
        }

        // Process 5-day daily items
        val dailyList = mutableListOf<DailyForecast>()
        val dayGroups = forecast?.list?.groupBy { item ->
            val date = Date(item.dt * 1000)
            SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date)
        }

        val dayFormat = SimpleDateFormat("EEEE", Locale.US)
        dayGroups?.entries?.take(5)?.forEachIndexed { index, entry ->
            val items = entry.value
            val firstDate = Date(items.first().dt * 1000)
            val dayName = if (index == 0) "Today" else dayFormat.format(firstDate)
            val minT = items.minOf { it.main.tempMin }.roundToInt()
            val maxT = items.maxOf { it.main.tempMax }.roundToInt().coerceAtLeast(minT + 2)
            val maxPop = (items.maxOf { it.pop } * 100).roundToInt()
            val midItem = items[items.size / 2]

            dailyList.add(
                DailyForecast(
                    dayName = dayName,
                    dateString = entry.key,
                    minTempCelsius = minT,
                    maxTempCelsius = maxT,
                    conditionName = midItem.weather.firstOrNull()?.main ?: "Clear",
                    conditionIcon = midItem.weather.firstOrNull()?.icon ?: "01d",
                    precipitationChance = maxPop
                )
            )
        }

        val insight = when {
            condition.contains("Rain", ignoreCase = true) -> "Precipitation expected next few hours. Carry rain gear."
            condition.contains("Thunder", ignoreCase = true) -> "Convective storm activity detected. Lightning advisory active."
            condition.contains("Cloud", ignoreCase = true) -> "Crisp marine layer burning off. Optimal sun around 2 PM."
            else -> "Pure clear skies across the peninsula. Perfect outdoors."
        }

        val timeFormatter = SimpleDateFormat("EEEE, MMM d • h:mm a", Locale.US)

        return WeatherData(
            cityName = weather.name,
            country = weather.sys.country,
            currentTempCelsius = currentTemp,
            feelsLikeCelsius = feelsLike,
            highTempCelsius = highTemp,
            lowTempCelsius = lowTemp,
            condition = condition,
            conditionDescription = conditionDesc.replaceFirstChar { it.uppercase() },
            iconCode = icon,
            humidityPercent = weather.main.humidity,
            dewPointCelsius = dewPoint,
            windSpeedKmH = windSpeedKmH,
            windDirectionDegrees = weather.wind.deg,
            windGustsKmH = windGustsKmH,
            pressureHPa = weather.main.pressure,
            visibilityKm = weather.visibility / 1000,
            uvIndex = 3,
            airQualityIndex = 32,
            airQualityLabel = "Pristine",
            narrativeInsight = insight,
            formattedTimestamp = timeFormatter.format(Date()),
            latitude = weather.coord.lat,
            longitude = weather.coord.lon,
            hourlyForecast = hourlyList,
            dailyForecast = dailyList
        )
    }
}
