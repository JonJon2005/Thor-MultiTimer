package com.example.firstapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val AppColorScheme = darkColorScheme(
    primary = RedAccent,
    onPrimary = OnDark,
    secondary = RedHighlight,
    onSecondary = OnDark,
    tertiary = RedAccentDark,
    background = AppBackground,
    onBackground = OnDark,
    surface = AppSurface,
    onSurface = OnDark,
    surfaceVariant = AppSurfaceRaised,
    onSurfaceVariant = OnDarkMuted,
    outline = AppOutline,
    error = RedHighlight,
    onError = OnDark
)

@Composable
fun FirstAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = Typography,
        content = content
    )
}
