package com.aura.weather.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aura.weather.ui.theme.*

@Composable
fun SettingsScreen(
    isCelsius: Boolean = true,
    onToggleUnit: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
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
            // Header
            item {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineLarge,
                    color = textHigh,
                    fontWeight = FontWeight.Bold
                )
            }

            // Unit Settings Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(cardBg.copy(alpha = 0.85f))
                        .border(1.dp, glassStroke, RoundedCornerShape(24.dp))
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Temperature Unit",
                                style = MaterialTheme.typography.titleMedium,
                                color = textHigh
                            )
                            Text(
                                text = "Choose Celsius or Fahrenheit",
                                style = MaterialTheme.typography.bodySmall,
                                color = textMuted
                            )
                        }

                        // Toggle Buttons
                        Row(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0xFF0A0E1A))
                                .border(1.dp, glassStroke, CircleShape)
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (isCelsius) cyanColor else Color.Transparent)
                                    .clickable { onToggleUnit(true) }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "°C",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isCelsius) deepBg else textHigh,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (!isCelsius) cyanColor else Color.Transparent)
                                    .clickable { onToggleUnit(false) }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "°F",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (!isCelsius) deepBg else textHigh,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // App Details Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(cardBg.copy(alpha = 0.85f))
                        .border(1.dp, glassStroke, RoundedCornerShape(24.dp))
                        .padding(18.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Aura Weather Edition",
                            style = MaterialTheme.typography.titleMedium,
                            color = textHigh,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Version 1.0.0 • Clean Architecture with Jetpack Compose",
                            style = MaterialTheme.typography.bodySmall,
                            color = textMuted
                        )
                    }
                }
            }
        }
    }
}