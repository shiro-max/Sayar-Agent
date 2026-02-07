package com.sayar.assistant.presentation.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Cooper-style Light Theme
private val LightColorScheme = lightColorScheme(
    primary = Lavender40,
    onPrimary = OnPrimaryLight,
    primaryContainer = Lavender90,
    onPrimaryContainer = Lavender10,
    secondary = Cream40,
    onSecondary = OnSecondaryLight,
    secondaryContainer = Cream90,
    onSecondaryContainer = Cream10,
    tertiary = Charcoal30,
    onTertiary = Color.White,
    tertiaryContainer = Charcoal90,
    onTertiaryContainer = Charcoal10,
    error = Red40,
    onError = Color.White,
    errorContainer = Red90,
    onErrorContainer = Red10,
    background = Background,
    onBackground = OnSurfaceLight,
    surface = Surface,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = Lavender50,
    outlineVariant = Lavender80
)

// Cooper-style Dark Theme
private val DarkColorScheme = darkColorScheme(
    primary = Lavender80,
    onPrimary = Lavender20,
    primaryContainer = Lavender30,
    onPrimaryContainer = Lavender90,
    secondary = Cream80,
    onSecondary = Cream20,
    secondaryContainer = Cream30,
    onSecondaryContainer = Cream90,
    tertiary = Charcoal80,
    onTertiary = Charcoal20,
    tertiaryContainer = Charcoal30,
    onTertiaryContainer = Charcoal90,
    error = Red80,
    onError = Red20,
    errorContainer = Red30,
    onErrorContainer = Red90,
    background = Charcoal10,
    onBackground = Charcoal90,
    surface = Charcoal10,
    onSurface = Charcoal90,
    surfaceVariant = Charcoal20,
    onSurfaceVariant = Charcoal80,
    outline = Lavender50,
    outlineVariant = Lavender30
)

@Composable
fun SayarAssistantTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disabled to use Cooper theme
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
