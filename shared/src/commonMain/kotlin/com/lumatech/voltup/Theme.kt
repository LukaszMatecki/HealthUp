package com.lumatech.voltup

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// VoltUp Cyber-Eco Palette
val ElectricVoltGreen = Color(0xFF00E676)
val BrightLime = Color(0xFFAEEA00)
val DarkEnergyBackground = Color(0xFF0D1317)
val CardSurfaceDark = Color(0xFF162127)
val CardSurfaceVariant = Color(0xFF1E2D35)
val EnergyYellow = Color(0xFFFFD600)
val TextPrimary = Color(0xFFF1F5F9)
val TextSecondary = Color(0xFF94A3B8)
val AccentCyan = Color(0xFF00E5FF)

private val VoltUpDarkColorScheme = darkColorScheme(
    primary = ElectricVoltGreen,
    onPrimary = Color.Black,
    primaryContainer = CardSurfaceDark,
    onPrimaryContainer = ElectricVoltGreen,
    secondary = BrightLime,
    onSecondary = Color.Black,
    background = DarkEnergyBackground,
    surface = CardSurfaceDark,
    surfaceVariant = CardSurfaceVariant,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    tertiary = EnergyYellow
)

@Composable
fun VoltUpTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = VoltUpDarkColorScheme,
        content = content
    )
}
