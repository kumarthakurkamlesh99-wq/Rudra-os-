package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = AccentElectricBlue,
    onPrimary = DarkNavyBg,
    primaryContainer = Color(0xFF0369A1),
    onPrimaryContainer = Color(0xFFE0F2FE),
    secondary = AccentCyan,
    onSecondary = DarkNavyBg,
    secondaryContainer = Color(0xFF0E7490),
    onSecondaryContainer = Color(0xFFCFFAFE),
    tertiary = ScoreGreen,
    onTertiary = DarkNavyBg,
    background = DarkNavyBg,
    onBackground = SoftTextPrimaryDark,
    surface = DarkNavySurface,
    onSurface = SoftTextPrimaryDark,
    surfaceVariant = DarkNavySurfaceVariant,
    onSurfaceVariant = SoftTextSecondaryDark,
    outline = DarkNavyBorder,
    error = ScoreRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = Color(0xFF0369A1),
    secondary = LightSecondaryBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F2FE),
    onSecondaryContainer = Color(0xFF0369A1),
    tertiary = ScoreGreen,
    onTertiary = Color.White,
    background = LightWhiteBg,
    onBackground = SoftTextPrimaryLight,
    surface = LightWhiteSurface,
    onSurface = SoftTextPrimaryLight,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = SoftTextSecondaryLight,
    outline = LightBorder,
    error = ScoreRed,
    onError = Color.White
)

@Composable
fun RudraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    RudraTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}

