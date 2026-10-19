package com.aura.weather.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryLight,
    onPrimary = OnPrimary,
    primaryContainer = ElectricCyan,
    onPrimaryContainer = DeepMidnightSapphire,
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SolarAmber,
    onSecondaryContainer = OnSecondary,
    tertiary = TwilightLavender,
    tertiaryContainer = TertiaryContainer,
    background = DeepMidnightSapphire,
    onBackground = TextHighEmphasis,
    surface = SurfaceBase,
    onSurface = TextHighEmphasis,
    surfaceVariant = SurfaceContainerHigh,
    onSurfaceVariant = TextVariant,
    outline = OutlineMuted,
    outlineVariant = OutlineVariant,
    error = WeatherError,
    errorContainer = WeatherErrorContainer
)

@Composable
fun AuraWeatherTheme(
    darkTheme: Boolean = true, // Aura Weather is natively optimized for OLED dark mode
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            window?.let {
                it.statusBarColor = DeepMidnightSapphire.toArgb()
                it.navigationBarColor = DeepMidnightSapphire.toArgb()
                WindowCompat.getInsetsController(it, view).apply {
                    isAppearanceLightStatusBars = false
                    isAppearanceLightNavigationBars = false
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
