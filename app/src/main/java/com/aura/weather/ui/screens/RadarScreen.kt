package com.aura.weather.ui.screens

import android.annotation.SuppressLint
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.NearMe
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.delay

enum class RadarLayer(val label: String) {
    PRECIPITATION("Rain"),
    WIND_FLOW("Wind"),
    SATELLITE("Clouds"),
    TEMP("Temp")
}

@Composable
fun RadarScreen(
    cityName: String = "Gaza",
    targetLatitude: Double = 31.5212,
    targetLongitude: Double = 34.4421,
    onSearchCity: (String) -> Unit = {},
    onRecenterClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedLayer by remember { mutableStateOf(RadarLayer.PRECIPITATION) }
    var isPlaying by remember { mutableStateOf(false) }
    var playbackProgress by remember { mutableFloatStateOf(0.75f) }
    var speedIndex by remember { mutableIntStateOf(0) }
    val speeds = listOf("1.0x", "2.0x", "0.5x")

    // مستوى الزوم الديناميكي: كلما قل delta زاد التقريب على الشوارع
    var zoomLevelDelta by remember { mutableDoubleStateOf(0.015) }

    // تحديث الموقع تلقائياً بمجرد تمرير إحداثيات جديدة من البحث
    var currentLatitude by remember(targetLatitude) { mutableDoubleStateOf(targetLatitude) }
    var currentLongitude by remember(targetLongitude) { mutableDoubleStateOf(targetLongitude) }

    LaunchedEffect(isPlaying, speedIndex) {
        val delayTime = when (speedIndex) {
            0 -> 100L
            1 -> 50L
            else -> 180L
        }
        while (isPlaying) {
            delay(delayTime)
            playbackProgress += 0.015f
            if (playbackProgress > 1f) playbackProgress = 0f
        }
    }

    val deepBg = Color(0xFF070B14)
    val cardBg = Color(0xEB0E1424)
    val cyanAccent = Color(0xFF00E5FF)
    val borderStroke = Color(0x33FFFFFF)
    val textHigh = Color.White
    val textMuted = Color(0xFF8E9BB5)
    val amberHighlight = Color(0xFFFFB300)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(deepBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 58.dp) // تقليص الهامش السفلي لأقصى تمدد
        ) {
            // 1. الهيدر المدمج
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(cardBg)
                        .border(1.dp, borderStroke, CircleShape)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(cyanAccent)
                        )
                        Text(
                            text = "AURA DOPPLER • $cityName",
                            style = MaterialTheme.typography.titleSmall,
                            color = textHigh,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(cardBg)
                        .border(1.dp, borderStroke, CircleShape)
                        .clickable {
                            currentLatitude = targetLatitude
                            currentLongitude = targetLongitude
                            zoomLevelDelta = 0.015
                            onRecenterClick()
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.NearMe,
                            contentDescription = null,
                            tint = cyanAccent,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "Reset",
                            style = MaterialTheme.typography.labelSmall,
                            color = textHigh,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // فلاتر الطبقات (Rain, Wind, Clouds, Temp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 2.dp)
                    .clip(CircleShape)
                    .background(cardBg)
                    .border(1.dp, borderStroke, CircleShape)
                    .padding(2.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                RadarLayer.entries.forEach { layer ->
                    val isSelected = selectedLayer == layer
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(CircleShape)
                            .background(if (isSelected) cyanAccent else Color.Transparent)
                            .clickable { selectedLayer = layer }
                            .padding(vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = layer.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) deepBg else textMuted,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            // 2. كارد الخريطة الكبيرة مع أزرار الزووم الكبيرة والعائمة
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // تأخذ أكبر حيز ممكن من الشاشة
                    .padding(horizontal = 6.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFF0F131F))
                    .border(1.dp, borderStroke, RoundedCornerShape(22.dp))
            ) {
                InteractiveVectorMapView(
                    latitude = currentLatitude,
                    longitude = currentLongitude,
                    delta = zoomLevelDelta,
                    modifier = Modifier.fillMaxSize()
                )

                // بادج ديناميكي يعرض المدينة والإحداثيات الحقيقية
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                        .clip(CircleShape)
                        .background(cardBg)
                        .border(1.dp, borderStroke, CircleShape)
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "%.2f° N, %.2f° E • %s".format(currentLatitude, currentLongitude, cityName),
                        style = MaterialTheme.typography.labelSmall,
                        color = cyanAccent,
                        fontWeight = FontWeight.Bold
                    )
                }

                // أزرار تكبير وتصغير لمسية كبيرة وسهلة الاستخدام
                Column(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // زر التكبير (+)
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(cardBg)
                            .border(1.dp, borderStroke, CircleShape)
                            .clickable {
                                if (zoomLevelDelta > 0.003) zoomLevelDelta /= 1.8
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Zoom In",
                            tint = cyanAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // زر التصغير (-)
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(cardBg)
                            .border(1.dp, borderStroke, CircleShape)
                            .clickable {
                                if (zoomLevelDelta < 1.0) zoomLevelDelta *= 1.8
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Zoom Out",
                            tint = cyanAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // 3. شريط التحكم الزمني المضغوط والأنيق
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(cardBg)
                    .border(1.dp, borderStroke, RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "-2h", style = MaterialTheme.typography.labelSmall, color = textMuted)
                        Text(text = "-1h", style = MaterialTheme.typography.labelSmall, color = textMuted)
                        Text(text = "-30m", style = MaterialTheme.typography.labelSmall, color = textMuted)
                        Text(
                            text = "Now (Live)",
                            style = MaterialTheme.typography.labelMedium,
                            color = cyanAccent,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(text = "✨", fontSize = 12.sp)
                            Text(
                                text = "+30m AI",
                                style = MaterialTheme.typography.labelSmall,
                                color = amberHighlight,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Slider(
                        value = playbackProgress,
                        onValueChange = { playbackProgress = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(16.dp),
                        colors = SliderDefaults.colors(
                            thumbColor = cyanAccent,
                            activeTrackColor = cyanAccent,
                            inactiveTrackColor = Color(0xFF26324D)
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            IconButton(
                                onClick = { isPlaying = !isPlaying },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(cyanAccent)
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Play/Pause",
                                    tint = deepBg,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column {
                                val timeStr = when {
                                    playbackProgress < 0.25f -> "12:15 PM"
                                    playbackProgress < 0.50f -> "1:00 PM"
                                    playbackProgress < 0.70f -> "1:30 PM"
                                    playbackProgress < 0.85f -> "1:45 PM"
                                    else -> "2:15 PM"
                                }
                                Text(
                                    text = timeStr,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = textHigh,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "LIVE NEXRAD SCAN",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = cyanAccent,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.6.sp
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF1E283D))
                                    .clickable {
                                        speedIndex = (speedIndex + 1) % speeds.size
                                    }
                                    .padding(horizontal = 9.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = speeds[speedIndex],
                                    style = MaterialTheme.typography.labelMedium,
                                    color = textHigh,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF1E283D))
                                    .padding(horizontal = 9.dp, vertical = 5.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(text = "✨", fontSize = 12.sp)
                                    Text(
                                        text = "Forecast",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = textHigh,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * خريطة متجهة تفاعلية ديناميكية تستجيب للمدينة والزووم المحدد
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun InteractiveVectorMapView(
    latitude: Double,
    longitude: Double,
    delta: Double,
    modifier: Modifier = Modifier
) {
    val embedUrl = remember(latitude, longitude, delta) {
        "https://www.openstreetmap.org/export/embed.html?bbox=${longitude - delta}%2C${latitude - delta}%2C${longitude + delta}%2C${latitude + delta}&layer=mapnik&marker=$latitude%2C$longitude"
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { ctx ->
            WebView(ctx).apply {
                layoutParams = android.view.ViewGroup.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
                )
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.useWideViewPort = true
                settings.loadWithOverviewMode = true
                settings.cacheMode = WebSettings.LOAD_DEFAULT

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        view?.evaluateJavascript(
                            """
                            (function() {
                                document.documentElement.style.filter = 'brightness(0.7) invert(1) contrast(1.8) hue-rotate(200deg) saturate(0.3)';
                                document.body.style.backgroundColor = '#0F131F';
                            })();
                            """.trimIndent(),
                            null
                        )
                    }
                }
                setBackgroundColor(android.graphics.Color.parseColor("#0F131F"))
                loadUrl(embedUrl)
            }
        },
        update = { webView ->
            webView.loadUrl(embedUrl)
        }
    )
}