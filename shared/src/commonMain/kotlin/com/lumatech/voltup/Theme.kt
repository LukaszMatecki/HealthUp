package com.lumatech.voltup

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// VoltUp Cyber-Eco Palette (Default Dark)
val ElectricVoltGreen = Color(0xFF00E676)
val BrightLime = Color(0xFFAEEA00)
val DarkEnergyBackground = Color(0xFF0D1317)
val CardSurfaceDark = Color(0xFF162127)
val CardSurfaceVariant = Color(0xFF1E2D35)
val EnergyYellow = Color(0xFFFFD600)
val TextPrimary = Color(0xFFF1F5F9)
val TextSecondary = Color(0xFF94A3B8)
val AccentCyan = Color(0xFF00E5FF)
val ElectricOrange = Color(0xFFFF7043)

// Neon Cyan Palette
val CyanPrimary = Color(0xFF00E5FF)
val CyanBackground = Color(0xFF08121E)
val CyanSurface = Color(0xFF101F2E)
val CyanSurfaceVariant = Color(0xFF182B3E)

// Solar Amber Palette
val SolarPrimary = Color(0xFFFFB300)
val SolarSecondary = Color(0xFFFF6D00)
val SolarBackground = Color(0xFF14100C)
val SolarSurface = Color(0xFF211A14)
val SolarSurfaceVariant = Color(0xFF2E241C)

// AMOLED Black Palette
val AmoledBackground = Color(0xFF000000)
val AmoledSurface = Color(0xFF0C0C0C)
val AmoledSurfaceVariant = Color(0xFF161616)

// Eco Light Palette
val EcoLightPrimary = Color(0xFF008947)
val EcoLightSecondary = Color(0xFF00B0FF)
val EcoLightBackground = Color(0xFFF1F5F5)
val EcoLightSurface = Color(0xFFFFFFFF)
val EcoLightSurfaceVariant = Color(0xFFE2EBE8)
val EcoLightTextPrimary = Color(0xFF0F172A)
val EcoLightTextSecondary = Color(0xFF475569)

private val CyberDarkColorScheme = darkColorScheme(
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

private val NeonCyanColorScheme = darkColorScheme(
    primary = CyanPrimary,
    onPrimary = Color.Black,
    primaryContainer = CyanSurface,
    onPrimaryContainer = CyanPrimary,
    secondary = AccentCyan,
    onSecondary = Color.Black,
    background = CyanBackground,
    surface = CyanSurface,
    surfaceVariant = CyanSurfaceVariant,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    tertiary = EnergyYellow
)

private val SolarAmberColorScheme = darkColorScheme(
    primary = SolarPrimary,
    onPrimary = Color.Black,
    primaryContainer = SolarSurface,
    onPrimaryContainer = SolarPrimary,
    secondary = SolarSecondary,
    onSecondary = Color.Black,
    background = SolarBackground,
    surface = SolarSurface,
    surfaceVariant = SolarSurfaceVariant,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    tertiary = EnergyYellow
)

private val AmoledColorScheme = darkColorScheme(
    primary = ElectricVoltGreen,
    onPrimary = Color.Black,
    primaryContainer = AmoledSurface,
    onPrimaryContainer = ElectricVoltGreen,
    secondary = BrightLime,
    onSecondary = Color.Black,
    background = AmoledBackground,
    surface = AmoledSurface,
    surfaceVariant = AmoledSurfaceVariant,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    tertiary = EnergyYellow
)

private val EcoLightColorScheme = lightColorScheme(
    primary = EcoLightPrimary,
    onPrimary = Color.White,
    primaryContainer = EcoLightSurfaceVariant,
    onPrimaryContainer = EcoLightPrimary,
    secondary = EcoLightSecondary,
    onSecondary = Color.White,
    background = EcoLightBackground,
    surface = EcoLightSurface,
    surfaceVariant = EcoLightSurfaceVariant,
    onBackground = EcoLightTextPrimary,
    onSurface = EcoLightTextPrimary,
    onSurfaceVariant = EcoLightTextSecondary,
    tertiary = EnergyYellow
)

@Composable
fun VoltUpTheme(
    themeMode: ThemeMode = ThemeMode.CYBER_DARK,
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeMode) {
        ThemeMode.CYBER_DARK -> CyberDarkColorScheme
        ThemeMode.NEON_CYAN -> NeonCyanColorScheme
        ThemeMode.SOLAR_AMBER -> SolarAmberColorScheme
        ThemeMode.LIGHT_ECO -> EcoLightColorScheme
        ThemeMode.AMOLED_BLACK -> AmoledColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
