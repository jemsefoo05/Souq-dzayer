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
    primary = DarkPrimary,
    onPrimary = DzGreenDark,
    primaryContainer = DzGreenDark,
    onPrimaryContainer = DzGreenLight,
    secondary = DzGold,
    onSecondary = Color.Black,
    secondaryContainer = DzGoldOnContainer,
    onSecondaryContainer = DzGoldContainer,
    tertiary = DzRedUrgent,
    background = DarkSurface,
    surface = DarkSurfaceContainer,
    surfaceVariant = DarkSurfaceContainerHigh,
    onBackground = Color(0xFFE1E3DF),
    onSurface = Color(0xFFE1E3DF)
)

private val LightColorScheme = lightColorScheme(
    primary = DzGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = DzGreenLight,
    onPrimaryContainer = DzGreenOnContainer,
    secondary = DzGold,
    onSecondary = Color.White,
    secondaryContainer = DzGoldContainer,
    onSecondaryContainer = DzGoldOnContainer,
    tertiary = DzRedUrgent,
    background = SurfaceLight,
    surface = SurfaceContainerLowest,
    surfaceVariant = SurfaceContainer,
    onBackground = NeutralDark,
    onSurface = NeutralDark
)

@Composable
fun SouqDZTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent brand identity
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
    SouqDZTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}


