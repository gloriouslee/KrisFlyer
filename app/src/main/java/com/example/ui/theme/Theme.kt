package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = SiaMidnightNavy,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCE4F5),
    onPrimaryContainer = SiaMidnightNavy,
    secondary = KrisGold,
    onSecondary = SiaMidnightNavy,
    secondaryContainer = Color(0xFFF6EAD0),
    onSecondaryContainer = Color(0xFF4F3809),
    tertiary = BatikTeal,
    onTertiary = Color.White,
    tertiaryContainer = BatikTealContainer,
    onTertiaryContainer = Color(0xFF063D3B),
    background = SilkIvory,
    onBackground = SiaMidnightNavy,
    surface = SilkCard,
    onSurface = SiaMidnightNavy,
    surfaceVariant = SilkWarmSurface,
    onSurfaceVariant = Color(0xFF535D70),
    outline = SilkOutline,
    outlineVariant = Color(0xFFEDE5D8),
    error = CrimsonAccent,
    onError = Color.White,
    errorContainer = CrimsonContainer,
    onErrorContainer = Color(0xFF60101B)
)

private val DarkColorScheme = darkColorScheme(
    primary = KrisChampagne,
    onPrimary = SiaMidnightNavy,
    primaryContainer = SiaDeepIndigo,
    onPrimaryContainer = KrisChampagne,
    secondary = KrisGold,
    onSecondary = ObsidianNight,
    secondaryContainer = Color(0xFF3E2F10),
    onSecondaryContainer = KrisChampagne,
    tertiary = Color(0xFF4CD3CD),
    onTertiary = Color(0xFF003735),
    tertiaryContainer = Color(0xFF0A4946),
    onTertiaryContainer = Color(0xFFADF3EF),
    background = ObsidianNight,
    onBackground = Color(0xFFF4F1EA),
    surface = ObsidianSurface,
    onSurface = Color(0xFFF4F1EA),
    surfaceVariant = ObsidianCard,
    onSurfaceVariant = Color(0xFFB5C0D4),
    outline = ObsidianOutline,
    outlineVariant = Color(0xFF1B2B4B),
    error = Color(0xFFF28B95),
    onError = Color(0xFF540D15),
    errorContainer = Color(0xFF751722),
    onErrorContainer = Color(0xFFFFD9DC)
)

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun KrisFlyerSimTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
