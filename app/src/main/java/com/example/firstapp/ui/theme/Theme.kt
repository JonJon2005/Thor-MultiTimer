package com.example.firstapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class AppThemeMode {
    DARK,
    LIGHT,
    OLED
}

enum class AppAccentColor {
    RED,
    BLUE,
    GREEN,
    PURPLE
}

private data class AccentPalette(
    val primary: Color,
    val secondary: Color,
    val tertiary: Color
)

private fun accentPalette(accentColor: AppAccentColor): AccentPalette = when (accentColor) {
    AppAccentColor.RED -> AccentPalette(
        primary = RedAccent,
        secondary = RedHighlight,
        tertiary = RedAccentDark
    )
    AppAccentColor.BLUE -> AccentPalette(
        primary = BlueAccent,
        secondary = BlueHighlight,
        tertiary = BlueAccentDark
    )
    AppAccentColor.GREEN -> AccentPalette(
        primary = GreenAccent,
        secondary = GreenHighlight,
        tertiary = GreenAccentDark
    )
    AppAccentColor.PURPLE -> AccentPalette(
        primary = PurpleAccent,
        secondary = PurpleHighlight,
        tertiary = PurpleAccentDark
    )
}

private fun darkScheme(
    palette: AccentPalette,
    background: Color,
    surface: Color,
    surfaceVariant: Color,
    outline: Color
) = darkColorScheme(
    primary = palette.primary,
    onPrimary = OnDark,
    secondary = palette.secondary,
    onSecondary = OnDark,
    tertiary = palette.tertiary,
    background = background,
    onBackground = OnDark,
    surface = surface,
    onSurface = OnDark,
    surfaceVariant = surfaceVariant,
    onSurfaceVariant = OnDarkMuted,
    outline = outline,
    error = palette.secondary,
    onError = OnDark
)

private fun lightScheme(
    palette: AccentPalette
) = lightColorScheme(
    primary = palette.primary,
    onPrimary = OnDark,
    secondary = palette.tertiary,
    onSecondary = OnDark,
    tertiary = palette.secondary,
    background = LightBackground,
    onBackground = OnLight,
    surface = LightSurface,
    onSurface = OnLight,
    surfaceVariant = LightSurfaceRaised,
    onSurfaceVariant = OnLightMuted,
    outline = LightOutline,
    error = palette.tertiary,
    onError = OnDark
)

@Composable
fun FirstAppTheme(
    themeMode: AppThemeMode = AppThemeMode.DARK,
    accentColor: AppAccentColor = AppAccentColor.RED,
    content: @Composable () -> Unit
) {
    val palette = accentPalette(accentColor)
    val colorScheme = when (themeMode) {
        AppThemeMode.DARK -> darkScheme(
            palette = palette,
            background = AppBackground,
            surface = AppSurface,
            surfaceVariant = AppSurfaceRaised,
            outline = AppOutline
        )
        AppThemeMode.LIGHT -> lightScheme(palette)
        AppThemeMode.OLED -> darkScheme(
            palette = palette,
            background = OledBackground,
            surface = OledSurface,
            surfaceVariant = OledSurfaceRaised,
            outline = OledOutline
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
