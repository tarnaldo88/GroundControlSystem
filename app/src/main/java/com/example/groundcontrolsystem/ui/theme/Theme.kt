package com.example.groundcontrolsystem.ui.theme

import android.app.Activity
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
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val NightVisionColorScheme = darkColorScheme(
    primary = NightPrimary,
    secondary = NightSecondary,
    tertiary = NightTertiary,
    background = NightBackground,
    surface = NightSurface,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = NightOnBackground,
    onSurface = NightOnSurface,
    surfaceVariant = Color(0xFF1A0000),
    onSurfaceVariant = NightOnSurface
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

@Composable
fun GroundControlSystemTheme(
    darkTheme: Boolean = true,
    nightVision: Boolean = false, // New parameter for Night Vision
    dynamicColor: Boolean = false, // Disabled dynamic color for Night Vision consistency
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        nightVision -> NightVisionColorScheme
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
