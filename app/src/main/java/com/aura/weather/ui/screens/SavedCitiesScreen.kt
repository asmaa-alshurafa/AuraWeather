package com.aura.weather.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aura.weather.data.model.CityTheme
import com.aura.weather.data.model.SavedCity
import com.aura.weather.ui.theme.*

@Composable
fun SavedCitiesScreen(
    savedCities: List<SavedCity> = emptyList(),
    isCelsius: Boolean = true,
    isOrganizeMode: Boolean = false,
    onCityClick: (SavedCity) -> Unit = {},
    onDeleteCity: (String) -> Unit = {},
    onToggleFavorite: (String) -> Unit = {},
    onToggleOrganize: () -> Unit = {},
    onAddCityClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var searchInput by remember { mutableStateOf("") }
    val filteredCities = remember(savedCities, searchInput) {
        if (searchInput.isBlank()) savedCities
        else savedCities.filter { it.name.contains(searchInput, ignoreCase = true) }
    }

    val deepBg = Color(0xFF0A0E1A)
    val cardBg = Color(0xFF141A29)
    val cyanColor = Color(0xFF00E5FF)
    val glassStroke = Color(0x33FFFFFF)
    val textHigh = Color.White
    val textMuted = Color(0xFF8E99A8)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(deepBg)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Search Bar & Add Button
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(CircleShape)
                            .background(cardBg.copy(alpha = 0.85f))
                            .border(1.dp, glassStroke, CircleShape)
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = "Search",
                                tint = cyanColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            androidx.compose.foundation.text.BasicTextField(
                                value = searchInput,
                                onValueChange = { searchInput = it },
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyMedium.copy(color = textHigh),
                                decorationBox = { inner ->
                                    if (searchInput.isEmpty()) {
                                        Text(
                                            text = "Search 200,000+ cities or airports...",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = textMuted.copy(alpha = 0.5f)
                                        )
                                    }
                                    inner()
                                }
                            )
                        }
                    }

                    Button(
                        onClick = onAddCityClick,
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = cyanColor),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = deepBg,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Add",
                            color = deepBg,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // 2. Thermal Horizon Spectrum Card
            item {
                ThermalHorizonSpectrumCard(cities = savedCities, isCelsius = isCelsius)
            }

            // 3. Monitored Realms Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Monitored Realms",
                            style = MaterialTheme.typography.headlineMedium,
                            color = textHigh
                        )
                        Text(
                            text = "(${savedCities.size})",
                            style = MaterialTheme.typography.bodySmall,
                            color = textMuted
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (isOrganizeMode) cyanColor.copy(alpha = 0.2f) else cardBg)
                            .border(1.dp, if (isOrganizeMode) cyanColor else glassStroke, CircleShape)
                            .clickable { onToggleOrganize() }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(
                                imageVector = Icons.Default.SwapVert,
                                contentDescription = null,
                                tint = if (isOrganizeMode) cyanColor else textMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (isOrganizeMode) "Done" else "Organize",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isOrganizeMode) cyanColor else textMuted
                            )
                        }
                    }
                }
            }

            // 4. Saved City Cards List
            items(filteredCities, key = { it.id }) { city ->
                SavedCityCard(
                    city = city,
                    isCelsius = isCelsius,
                    isOrganizeMode = isOrganizeMode,
                    onClick = { onCityClick(city) },
                    onDelete = { onDeleteCity(city.id) },
                    onToggleFavorite = { onToggleFavorite(city.id) }
                )
            }

            // 5. Telemetry Hub Status Card
            item {
                TelemetryHubCard()
            }
        }
    }
}

@Composable
fun ThermalHorizonSpectrumCard(
    cities: List<SavedCity>,
    isCelsius: Boolean
) {
    val cardBg = Color(0xFF141A29)
    val cyanColor = Color(0xFF00E5FF)
    val amberColor = Color(0xFFFFB300)
    val glassStroke = Color(0x33FFFFFF)
    val textHigh = Color.White
    val textMuted = Color(0xFF8E99A8)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(cardBg.copy(alpha = 0.88f))
            .border(1.dp, glassStroke, RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(imageVector = Icons.Default.Equalizer, contentDescription = null, tint = cyanColor, modifier = Modifier.size(16.dp))
                    Text(
                        text = "THERMAL HORIZON SPECTRUM",
                        style = MaterialTheme.typography.labelSmall,
                        color = textMuted
                    )
                }
                Text(
                    text = "${cities.size} Saved Hubs",
                    style = MaterialTheme.typography.labelSmall,
                    color = textMuted
                )
            }

            cities.take(5).forEach { city ->
                val minT = if (isCelsius) city.lowCelsius else (city.lowCelsius * 9 / 5 + 32)
                val maxT = if (isCelsius) city.highCelsius else (city.highCelsius * 9 / 5 + 32)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = city.name.take(8),
                        style = MaterialTheme.typography.labelMedium,
                        color = textMuted,
                        modifier = Modifier.width(64.dp)
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF26324D))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(0.6f)
                                .align(Alignment.Center)
                                .clip(CircleShape)
                                .background(
                                    Brush.horizontalGradient(
                                        when (city.themeGradient) {
                                            CityTheme.AMBER -> listOf(amberColor, Color(0xFFFF8F00))
                                            CityTheme.NIGHT -> listOf(Color(0xFF7C4DFF), cyanColor)
                                            CityTheme.FROST -> listOf(Color(0xFF80D8FF), Color(0xFF00B0FF))
                                            else -> listOf(cyanColor, amberColor)
                                        }
                                    )
                                )
                        )
                    }
                    Text(
                        text = "$minT° - $maxT°",
                        style = MaterialTheme.typography.labelSmall,
                        color = textHigh,
                        modifier = Modifier.width(60.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SavedCityCard(
    city: SavedCity,
    isCelsius: Boolean,
    isOrganizeMode: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val displayTemp = if (isCelsius) city.tempCelsius else (city.tempCelsius * 9 / 5 + 32)
    val displayHigh = if (isCelsius) city.highCelsius else (city.highCelsius * 9 / 5 + 32)
    val displayLow = if (isCelsius) city.lowCelsius else (city.lowCelsius * 9 / 5 + 32)

    val amberColor = Color(0xFFFFB300)
    val cyanColor = Color(0xFF00E5FF)
    val textHigh = Color.White
    val textMuted = Color(0xFF8E99A8)
    val glassStroke = Color(0x33FFFFFF)

    val backgroundBrush = when (city.themeGradient) {
        CityTheme.CYAN -> Brush.linearGradient(listOf(Color(0xFF122238), Color(0xFF0E172A), Color(0xFF0B1020)))
        CityTheme.NIGHT -> Brush.linearGradient(listOf(Color(0xFF171434), Color(0xFF10132B), Color(0xFF0B0C1E)))
        CityTheme.AMBER -> Brush.linearGradient(listOf(Color(0xFF2A2114), Color(0xFF1E1913), Color(0xFF0F1118)))
        CityTheme.CHARCOAL -> Brush.linearGradient(listOf(Color(0xFF152329), Color(0xFF101B22), Color(0xFF0C1219)))
        CityTheme.FROST -> Brush.linearGradient(listOf(Color(0xFF0E272A), Color(0xFF0A1B22), Color(0xFF081017)))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(backgroundBrush)
            .border(1.dp, glassStroke, RoundedCornerShape(28.dp))
            .clickable { onClick() }
            .padding(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (city.advisoryNotice != null) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(amberColor.copy(alpha = 0.2f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(imageVector = Icons.Default.Air, contentDescription = null, tint = amberColor, modifier = Modifier.size(14.dp))
                        Text(text = city.advisoryNotice, style = MaterialTheme.typography.labelSmall, color = amberColor, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = city.name,
                        style = MaterialTheme.typography.headlineLarge,
                        color = textHigh,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${city.country} • ${city.localTime}",
                        style = MaterialTheme.typography.bodySmall,
                        color = textMuted
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$displayTemp°",
                        style = MaterialTheme.typography.displayLarge,
                        color = when (city.themeGradient) {
                            CityTheme.AMBER -> amberColor
                            CityTheme.FROST -> Color(0xFF80D8FF)
                            else -> textHigh
                        },
                        fontWeight = FontWeight.ExtraLight
                    )
                    Text(
                        text = "H: $displayHigh° • L: $displayLow°",
                        style = MaterialTheme.typography.labelSmall,
                        color = textMuted
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0xFF0A0E1A).copy(alpha = 0.5f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(
                            imageVector = when {
                                city.condition.contains("Rain", ignoreCase = true) -> Icons.Default.WaterDrop
                                city.condition.contains("Sunny", ignoreCase = true) -> Icons.Default.WbSunny
                                city.condition.contains("Flurries", ignoreCase = true) -> Icons.Default.AcUnit
                                else -> Icons.Default.Cloud
                            },
                            contentDescription = null,
                            tint = cyanColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(text = city.condition, style = MaterialTheme.typography.bodySmall, color = textHigh)
                        Text(text = "• ${city.badgeLabel}", style = MaterialTheme.typography.bodySmall, color = cyanColor)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = onToggleFavorite, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = if (city.isFavorite) Icons.Default.Star else Icons.Outlined.StarOutline,
                            contentDescription = "Favorite",
                            tint = if (city.isFavorite) amberColor else textMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    AnimatedVisibility(visible = isOrganizeMode) {
                        IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete City",
                                tint = Color(0xFFFF5252),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TelemetryHubCard() {
    val cardBg = Color(0xFF141A29)
    val cyanColor = Color(0xFF00E5FF)
    val glassStroke = Color(0x33FFFFFF)
    val textHigh = Color.White
    val textMuted = Color(0xFF8E99A8)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(cardBg.copy(alpha = 0.85f))
            .border(1.dp, glassStroke, RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(imageVector = Icons.Default.CloudSync, contentDescription = null, tint = cyanColor, modifier = Modifier.size(18.dp))
                    Text(text = "Telemetry Hub", style = MaterialTheme.typography.titleMedium, color = textHigh)
                }
                Text(text = "ACTIVE SYNC", style = MaterialTheme.typography.labelSmall, color = cyanColor)
            }
            Text(
                text = "Pipeline: OpenWeatherMap One Call 3.0 • Direct TLS • 42ms Latency",
                style = MaterialTheme.typography.bodySmall,
                color = textMuted
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SavedCitiesScreenPreview() {
    AuraWeatherTheme {
        SavedCitiesScreen()
    }
}