package com.lumatech.voltup

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.resources.Font
import voltup.shared.generated.resources.Res
import voltup.shared.generated.resources.*

val ElectricVoltGreen = Color(0xFF00E676)
val EnergyYellow = Color(0xFFFFD600)
val DarkBackground = Color(0xFF0E1116)
val DarkSurface = Color(0xFF181C23)
val DarkSurfaceVariant = Color(0xFF222731)
val DarkTextPrimary = Color(0xFFF1F5F9)
val DarkTextSecondary = Color(0xFF94A3B8)

val LightBackground = Color(0xFFF8FAFC)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFEDF2F7)
val LightPrimary = Color(0xFF00A35B)
val LightTextPrimary = Color(0xFF0F172A)
val LightTextSecondary = Color(0xFF64748B)

private val VoltUpDarkColorScheme = darkColorScheme(
    primary = ElectricVoltGreen,
    onPrimary = Color.Black,
    primaryContainer = DarkSurfaceVariant,
    onPrimaryContainer = ElectricVoltGreen,
    secondary = Color(0xFF38BDF8),
    onSecondary = Color.Black,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onBackground = DarkTextPrimary,
    onSurface = DarkTextPrimary,
    onSurfaceVariant = DarkTextSecondary,
    outline = Color(0xFF2A313C),
    tertiary = EnergyYellow
)

private val VoltUpLightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE8F5E9),
    onPrimaryContainer = LightPrimary,
    secondary = Color(0xFF0284C7),
    onSecondary = Color.White,
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    onBackground = LightTextPrimary,
    onSurface = LightTextPrimary,
    onSurfaceVariant = LightTextSecondary,
    outline = Color(0xFFE2E8F0),
    tertiary = EnergyYellow
)

@Composable
fun getVoltUpTypography(): Typography {
    val outfit = FontFamily(
        Font(Res.font.Outfit_Thin, FontWeight.Thin),
        Font(Res.font.Outfit_ExtraLight, FontWeight.ExtraLight),
        Font(Res.font.Outfit_Light, FontWeight.Light),
        Font(Res.font.Outfit_Regular, FontWeight.Normal),
        Font(Res.font.Outfit_Medium, FontWeight.Medium),
        Font(Res.font.Outfit_SemiBold, FontWeight.SemiBold),
        Font(Res.font.Outfit_Bold, FontWeight.Bold),
        Font(Res.font.Outfit_ExtraBold, FontWeight.ExtraBold),
        Font(Res.font.Outfit_Black, FontWeight.Black)
    )

    return Typography(
        headlineLarge = TextStyle(
            fontFamily = outfit,
            fontWeight = FontWeight.Bold,
            fontSize = 30.sp,
            letterSpacing = (-0.5).sp,
            lineHeight = 36.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = outfit,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            letterSpacing = (-0.3).sp,
            lineHeight = 30.sp
        ),
        headlineSmall = TextStyle(
            fontFamily = outfit,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            letterSpacing = (-0.2).sp,
            lineHeight = 26.sp
        ),
        titleLarge = TextStyle(
            fontFamily = outfit,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            letterSpacing = 0.sp,
            lineHeight = 24.sp
        ),
        titleMedium = TextStyle(
            fontFamily = outfit,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            letterSpacing = 0.sp,
            lineHeight = 22.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = outfit,
            fontWeight = FontWeight.Normal,
            fontSize = 15.sp,
            letterSpacing = 0.1.sp,
            lineHeight = 22.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = outfit,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            letterSpacing = 0.1.sp,
            lineHeight = 20.sp
        ),
        bodySmall = TextStyle(
            fontFamily = outfit,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            letterSpacing = 0.1.sp,
            lineHeight = 16.sp
        ),
        labelLarge = TextStyle(
            fontFamily = outfit,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            letterSpacing = 0.8.sp,
            lineHeight = 18.sp
        ),
        labelSmall = TextStyle(
            fontFamily = outfit,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            letterSpacing = 0.4.sp,
            lineHeight = 14.sp
        )
    )
}

@Composable
fun VoltUpTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val systemInDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        AppThemeMode.SYSTEM -> systemInDark
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
    }

    val colorScheme = if (isDark) VoltUpDarkColorScheme else VoltUpLightColorScheme
    val typography = getVoltUpTypography()

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}

@Preview
@Composable
fun VoltUpThemePreview() {
    VoltUpTheme(AppThemeMode.DARK) {
        Surface {
            Text("VoltUp Preview", style = MaterialTheme.typography.headlineMedium)
        }
    }
}
