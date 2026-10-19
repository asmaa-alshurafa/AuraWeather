package com.aura.weather.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aura.weather.data.model.DailyForecast
import com.aura.weather.data.model.HourlyForecast
import com.aura.weather.data.model.WeatherData
import com.aura.weather.ui.theme.*
import com.aura.weather.ui.viewmodel.WeatherUiState

@Composable
fun HomeScreen(
    uiState: WeatherUiState,
    onSearchCity: (String) -> Unit,
    onGpsLocationClick: () -> Unit,
    onUnitToggle: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepMidnightSapphire)
    ) {
        // Atmospheric Ambient Background Glows
        Box(
            modifier = Modifier
                .size(320.dp)
                .offset(x = (-80).dp, y = (-60).dp)
                .blur(100.dp)
                .background(ElectricCyan.copy(alpha = 0.12f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.TopEnd)
                .offset(x = 80.dp, y = 300.dp)
                .blur(120.dp)
                .background(SolarAmber.copy(alpha = 0.08f), CircleShape)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Top Search & Controls Row
            TopSearchBarRow(
                searchQuery = searchQuery,
                onQueryChange = { searchQuery = it },
                onSearch = {
                    onSearchCity(it)
                    searchQuery = ""
                },
                onGpsClick = onGpsLocationClick,
                isCelsius = uiState.isCelsius,
                onToggleUnit = onUnitToggle
            )

            // Loading / Error / Content state branching
            when {
                uiState.isLoading -> {
                    WeatherLoadingSkeleton()
                }
                uiState.errorMessage != null -> {
                    WeatherErrorBanner(
                        message = uiState.errorMessage,
                        onRetry = onRetry
                    )
                }
                uiState.weatherData != null -> {
                    val weather = uiState.weatherData

                    // 2. Hero Weather Card
                    HeroWeatherCard(
                        weather = weather,
                        isCelsius = uiState.isCelsius
                    )

                    // 3. Metrics 2x2 Bento Grid
                    Metrics2x2Grid(
                        weather = weather,
                        isCelsius = uiState.isCelsius
                    )

                    // 4. Hourly 24-Hour Telemetry Snap Carousel
                    HourlyForecastSection(
                        hourlyList = weather.hourlyForecast,
                        isCelsius = uiState.isCelsius
                    )

                    // 5. 5-Day Precision Outlook
                    FiveDayOutlookSection(
                        dailyList = weather.dailyForecast,
                        isCelsius = uiState.isCelsius
                    )

                    // 6. API Telemetry Sync Footer
                    TelemetrySyncFooter(
                        lastUpdated = weather.formattedTimestamp,
                        onSyncClick = onRetry
                    )
                }
            }
        }
    }
}

/**
 * Top Search Bar with Gps Locator and Segmented Unit Toggle Pill (°C / °F)
 */
@Composable
fun TopSearchBarRow(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onGpsClick: () -> Unit,
    isCelsius: Boolean,
    onToggleUnit: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Search Input Capsule
        Box(
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .clip(CircleShape)
                .background(SurfaceContainerHigh.copy(alpha = 0.85f))
                .border(1.dp, GlassStroke, CircleShape)
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "Search",
                    tint = ElectricCyan,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                androidx.compose.foundation.text.BasicTextField(
                    value = searchQuery,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextHighEmphasis),
                    modifier = Modifier.weight(1f),
                    decorationBox = { innerTextField ->
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Search global cities...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextVariant.copy(alpha = 0.6f)
                            )
                        }
                        innerTextField()
                    }
                )
                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { onSearch(searchQuery) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Go",
                            tint = ElectricCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Live GPS Button
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(SurfaceContainerHigh.copy(alpha = 0.85f))
                .border(1.dp, GlassStroke, CircleShape)
                .clickable { onGpsClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.MyLocation,
                contentDescription = "Current GPS Location",
                tint = ElectricCyan,
                modifier = Modifier.size(20.dp)
            )
            // Pulsing dot indicator
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-4).dp, y = 4.dp)
                    .size(8.dp)
                    .background(ElectricCyan, CircleShape)
            )
        }

        // Unit Switcher Pill (°C / °F)
        Row(
            modifier = Modifier
                .height(48.dp)
                .clip(CircleShape)
                .background(SurfaceContainerHigh.copy(alpha = 0.85f))
                .border(1.dp, GlassStroke, CircleShape)
                .padding(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isCelsius) ElectricCyan else Color.Transparent)
                    .clickable { if (!isCelsius) onToggleUnit() }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "°C",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isCelsius) DeepMidnightSapphire else TextVariant,
                    fontWeight = FontWeight.Bold
                )
            }
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (!isCelsius) ElectricCyan else Color.Transparent)
                    .clickable { if (isCelsius) onToggleUnit() }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "°F",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (!isCelsius) DeepMidnightSapphire else TextVariant,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Hero Weather Card with rounded-3xl corners, ambient internal gradient,
 * prominent temperature typography, and meteorological insight
 */
@Composable
fun HeroWeatherCard(
    weather: WeatherData,
    isCelsius: Boolean
) {
    val displayTemp = if (isCelsius) weather.currentTempCelsius else (weather.currentTempCelsius * 9 / 5 + 32)
    val displayFeels = if (isCelsius) weather.feelsLikeCelsius else (weather.feelsLikeCelsius * 9 / 5 + 32)
    val displayHigh = if (isCelsius) weather.highTempCelsius else (weather.highTempCelsius * 9 / 5 + 32)
    val displayLow = if (isCelsius) weather.lowTempCelsius else (weather.lowTempCelsius * 9 / 5 + 32)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(SurfaceContainerLow.copy(alpha = 0.92f))
            .border(1.dp, GlassStroke, RoundedCornerShape(32.dp))
            .padding(20.dp)
    ) {
        // Internal ambient bloom
        Box(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.TopEnd)
                .offset(x = 40.dp, y = (-40).dp)
                .blur(70.dp)
                .background(ElectricCyan.copy(alpha = 0.15f), CircleShape)
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // City name & live condition micro-pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = weather.cityName,
                            style = MaterialTheme.typography.headlineSmall,
                            color = TextHighEmphasis
                        )
                    }
                    Text(
                        text = weather.formattedTimestamp,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextVariant
                    )
                }

                // Live condition badge
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(ElectricCyan.copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(ElectricCyan, CircleShape)
                        )
                        Text(
                            text = weather.condition.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricCyan,
                            letterSpacing = 0.8.sp
                        )
                    }
                }
            }

            // Big Temperature Display and 3D Frosted Visual
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.Top) {
                        Text(
                            text = "$displayTemp",
                            style = MaterialTheme.typography.displayLarge,
                            color = TextHighEmphasis,
                            fontWeight = FontWeight.ExtraLight
                        )
                        Text(
                            text = "°",
                            fontSize = 32.sp,
                            color = ElectricCyan,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    Text(
                        text = "Feels like $displayFeels° • H: $displayHigh° L: $displayLow°",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextVariant
                    )
                }

                // Atmospheric Sun/Cloud 3D Cluster
                Box(
                    modifier = Modifier.size(110.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Solar orb with warm aura
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = (-8).dp, y = 4.dp)
                            .background(
                                Brush.radialGradient(
                                    listOf(SolarAmber, Secondary, SecondaryFixedDim)
                                ),
                                CircleShape
                            )
                            .blur(2.dp)
                    )
                    // Cloud overlay container
                    Icon(
                        imageVector = Icons.Default.Cloud,
                        contentDescription = "Cloud Condition",
                        tint = PrimaryLight.copy(alpha = 0.85f),
                        modifier = Modifier
                            .size(76.dp)
                            .align(Alignment.BottomCenter)
                    )
                }
            }

            // Narrative Meteorological Insight Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceContainer.copy(alpha = 0.6f))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Air,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = weather.narrativeInsight,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextHighEmphasis
                        )
                    }
                    Text(
                        text = "UV ${weather.uvIndex}",
                        style = MaterialTheme.typography.labelSmall,
                        color = ElectricCyan,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(SurfaceContainerHigh)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}

/**
 * 2x2 Bento Grid: Humidity, Wind, UV Index, Air Quality
 */
@Composable
fun Metrics2x2Grid(
    weather: WeatherData,
    isCelsius: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Humidity Card
            MetricTile(
                modifier = Modifier.weight(1f),
                title = "HUMIDITY",
                icon = Icons.Outlined.WaterDrop,
                badge = if (weather.humidityPercent > 70) "High" else "Optimal",
                primaryValue = "${weather.humidityPercent}%",
                subtitle = "Dew point ${if (isCelsius) "${weather.dewPointCelsius}°" else "${weather.dewPointCelsius * 9 / 5 + 32}°"}"
            )

            // 2. Wind Speed Card
            MetricTile(
                modifier = Modifier.weight(1f),
                title = "WIND",
                icon = Icons.Outlined.Air,
                badge = "NW Gusts",
                primaryValue = "${weather.windSpeedKmH} km/h",
                subtitle = "Gusts ${weather.windGustsKmH} km/h"
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 3. UV Index Card
            MetricTile(
                modifier = Modifier.weight(1f),
                title = "UV INDEX",
                icon = Icons.Outlined.WbSunny,
                badge = "Moderate",
                primaryValue = "${weather.uvIndex} of 11",
                subtitle = "Sunglasses advised",
                accentColor = SolarAmber
            )

            // 4. Air Quality Card
            MetricTile(
                modifier = Modifier.weight(1f),
                title = "AIR QUALITY",
                icon = Icons.Outlined.Eco,
                badge = "Good",
                primaryValue = "${weather.airQualityIndex} AQI",
                subtitle = "PM2.5: 8 µg/m³ • Fresh",
                accentColor = ElectricCyan
            )
        }
    }
}

@Composable
fun MetricTile(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector,
    badge: String,
    primaryValue: String,
    subtitle: String,
    accentColor: Color = ElectricCyan
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(SurfaceContainerLow.copy(alpha = 0.85f))
            .border(1.dp, GlassStroke, RoundedCornerShape(24.dp))
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium,
                        color = TextVariant
                    )
                }
                Text(
                    text = badge,
                    style = MaterialTheme.typography.labelSmall,
                    color = accentColor
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = primaryValue,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextHighEmphasis
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextVariant
            )
        }
    }
}

/**
 * Hourly 24-Hour Snap Carousel (LazyRow)
 */
@Composable
fun HourlyForecastSection(
    hourlyList: List<HourlyForecast>,
    isCelsius: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Schedule,
                    contentDescription = null,
                    tint = ElectricCyan,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "HOURLY FORECAST",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextVariant
                )
            }
            Text(
                text = "NEXT 24 HOURS",
                style = MaterialTheme.typography.labelSmall,
                color = ElectricCyan
            )
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(hourlyList) { item ->
                val displayTemp = if (isCelsius) item.tempCelsius else (item.tempCelsius * 9 / 5 + 32)

                Box(
                    modifier = Modifier
                        .width(68.dp)
                        .height(124.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (item.isNow) SurfaceContainerHigh.copy(alpha = 0.95f)
                            else SurfaceContainerLow.copy(alpha = 0.75f)
                        )
                        .border(
                            1.dp,
                            if (item.isNow) ElectricCyan.copy(alpha = 0.4f) else GlassStroke,
                            RoundedCornerShape(20.dp)
                        )
                        .padding(vertical = 10.dp, horizontal = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Text(
                            text = item.timeLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (item.isNow) ElectricCyan else TextVariant,
                            fontWeight = if (item.isNow) FontWeight.Bold else FontWeight.Normal
                        )

                        Icon(
                            imageVector = when {
                                item.conditionName.contains("Rain", ignoreCase = true) -> Icons.Default.WaterDrop
                                item.conditionName.contains("Cloud", ignoreCase = true) -> Icons.Default.Cloud
                                else -> Icons.Default.WbSunny
                            },
                            contentDescription = item.conditionName,
                            tint = if (item.conditionName.contains("Rain", ignoreCase = true)) ElectricCyan else SolarAmber,
                            modifier = Modifier.size(22.dp)
                        )

                        Text(
                            text = "$displayTemp°",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = TextHighEmphasis
                        )

                        Text(
                            text = "${item.precipitationChance}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (item.precipitationChance > 20) ElectricCyan else TextVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 5-Day Precision Outlook with horizontal min/max temperature gradient bars
 */
@Composable
fun FiveDayOutlookSection(
    dailyList: List<DailyForecast>,
    isCelsius: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(SurfaceContainerLow.copy(alpha = 0.88f))
            .border(1.dp, GlassStroke, RoundedCornerShape(28.dp))
            .padding(18.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "5-Day Outlook",
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextHighEmphasis
                    )
                }
                Text(
                    text = "Probability & Ranges",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextVariant
                )
            }

            dailyList.forEach { day ->
                val displayMin = if (isCelsius) day.minTempCelsius else (day.minTempCelsius * 9 / 5 + 32)
                val displayMax = if (isCelsius) day.maxTempCelsius else (day.maxTempCelsius * 9 / 5 + 32)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = day.dayName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = TextHighEmphasis,
                        modifier = Modifier.width(64.dp)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.width(72.dp)
                    ) {
                        Icon(
                            imageVector = when {
                                day.conditionName.contains("Rain", ignoreCase = true) -> Icons.Default.WaterDrop
                                day.conditionName.contains("Cloud", ignoreCase = true) -> Icons.Default.Cloud
                                else -> Icons.Default.WbSunny
                            },
                            contentDescription = null,
                            tint = if (day.precipitationChance > 40) ElectricCyan else SolarAmber,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "${day.precipitationChance}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (day.precipitationChance > 40) ElectricCyan else TextVariant
                        )
                    }

                    // Horizontal Temperature Range Gradient Bar
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "$displayMin°",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextVariant
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(CircleShape)
                                .background(SurfaceContainerHighest)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(0.65f)
                                    .align(Alignment.Center)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(ElectricCyan, SolarAmber)
                                        )
                                    )
                            )
                        }
                        Text(
                            text = "$displayMax°",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextHighEmphasis,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TelemetrySyncFooter(
    lastUpdated: String,
    onSyncClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(ElectricCyan, CircleShape)
            )
            Text(
                text = "OpenWeatherMap API • $lastUpdated",
                style = MaterialTheme.typography.labelSmall,
                color = TextVariant
            )
        }
        TextButton(
            onClick = onSyncClick,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Sync,
                contentDescription = null,
                tint = ElectricCyan,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Sync Now",
                style = MaterialTheme.typography.labelSmall,
                color = ElectricCyan
            )
        }
    }
}

@Composable
fun WeatherLoadingSkeleton() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(SurfaceContainerLow)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(96.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(SurfaceContainerLow)
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(96.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(SurfaceContainerLow)
            )
        }
    }
}

@Composable
fun WeatherErrorBanner(
    message: String,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = WeatherErrorContainer.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Weather Sync Interrupted",
                    style = MaterialTheme.typography.labelLarge,
                    color = WeatherError
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextVariant
                )
            }
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan)
            ) {
                Text(
                    text = "Retry",
                    color = DeepMidnightSapphire,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    AuraWeatherTheme {
        HomeScreen(
            uiState = WeatherUiState(
                isLoading = false,
                isCelsius = false
            ),
            onSearchCity = {},
            onGpsLocationClick = {},
            onUnitToggle = {},
            onRetry = {}
        )
    }
}
