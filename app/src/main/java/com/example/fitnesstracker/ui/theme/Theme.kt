package com.example.fitnesstracker.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = PrimaryBlueDark,
    onPrimaryContainer = Color.White,
    secondary = AquaAccent,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF0F172A),
    onSecondaryContainer = Color(0xFFD6E5FF),
    tertiary = EmeraldAccent,
    background = Slate900,
    surface = Slate900,
    surfaceVariant = Color(0xFF1F2937),
    onSurface = Slate100,
    onSurfaceVariant = Slate200,
    outline = Color(0xFF334155)
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = PrimaryBlueDark,
    onPrimaryContainer = Color.White,
    secondary = AquaAccent,
    onSecondary = Slate900,
    secondaryContainer = Color(0xFFDDF6FF),
    onSecondaryContainer = Slate900,
    tertiary = EmeraldAccent,
    background = Slate100,
    surface = Slate100,
    surfaceVariant = Slate200,
    onSurface = Slate900,
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFF94A3B8)
)

@Composable
fun FitnessTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
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
