package com.aura.weather.data.remote

import com.aura.weather.data.model.ForecastResponse
import com.aura.weather.data.model.WeatherResponse
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

/**
 * Retrofit2 API interface for OpenWeatherMap
 */
interface WeatherApiService {

    @GET("weather")
    suspend fun getCurrentWeatherByCity(
        @Query("q") cityName: String,
        @Query("units") units: String = "metric",
        @Query("appid") apiKey: String = API_KEY
    ): WeatherResponse

    @GET("weather")
    suspend fun getCurrentWeatherByCoords(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("units") units: String = "metric",
        @Query("appid") apiKey: String = API_KEY
    ): WeatherResponse

    @GET("forecast")
    suspend fun get5DayForecastByCity(
        @Query("q") cityName: String,
        @Query("units") units: String = "metric",
        @Query("appid") apiKey: String = API_KEY
    ): ForecastResponse

    @GET("forecast")
    suspend fun get5DayForecastByCoords(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("units") units: String = "metric",
        @Query("appid") apiKey: String = API_KEY
    ): ForecastResponse

    companion object {
        const val BASE_URL = "https://api.openweathermap.org/data/2.5/"
        const val API_KEY = "5b111e3737b3102908663fd62419fd0b"
    }
}

/**
 * Retrofit Client Singleton configured with Kotlinx Serialization & OkHttp logging
 */
object RetrofitInstance {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    val api: WeatherApiService by lazy {
        Retrofit.Builder()
            .baseUrl(WeatherApiService.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(WeatherApiService::class.java)
    }
}
