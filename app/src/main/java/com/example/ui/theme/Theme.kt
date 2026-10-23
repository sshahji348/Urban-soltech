package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val UrbanSoltechDarkColorScheme = darkColorScheme(
    primary = SolarYellow,
    onPrimary = TextOnYellow,
    primaryContainer = SolarYellowDark,
    onPrimaryContainer = Color.White,
    secondary = SolarAmber,
    onSecondary = Color.Black,
    secondaryContainer = SlateSurfaceVariant,
    onSecondaryContainer = SolarYellowBright,
    tertiary = CyanAccent,
    onTertiary = Color.Black,
    background = SlateBackground,
    onBackground = TextPrimary,
    surface = SlateSurface,
    onSurface = TextPrimary,
    surfaceVariant = SlateSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = SlateBorder,
    outlineVariant = SlateBorderLight,
    error = EmergencyRed,
    onError = Color.White,
    errorContainer = EmergencyRedDark,
    onErrorContainer = Color.White
)

private val UrbanSoltechLightColorScheme = lightColorScheme(
    primary = Color(0xFFD97706),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFEF3C7),
    onPrimaryContainer = Color(0xFF78350F),
    secondary = Color(0xFF0F766E),
    onSecondary = Color.White,
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1),
    error = EmergencyRed,
    onError = Color.White
)

@Composable
fun UrbanSoltechTheme(
    darkTheme: Boolean = true, // Default to sleek industrial dark theme for solar tech aesthetic
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) UrbanSoltechDarkColorScheme else UrbanSoltechLightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
