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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
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

val ElectricRed = Color(0xFFEF4444)
val DarkErrorContainer = Color(0xFF450A0A)
val DarkOnErrorContainer = Color(0xFFFCA5A5)

val LightError = Color(0xFFDC2626)
val LightErrorContainer = Color(0xFFFEE2E2)
val LightOnErrorContainer = Color(0xFF991B1B)

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
    tertiary = EnergyYellow,
    error = ElectricRed,
    onError = Color.White,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer
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
    tertiary = EnergyYellow,
    error = LightError,
    onError = Color.White,
    errorContainer = LightErrorContainer,
    onErrorContainer = LightOnErrorContainer
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

val LocalIsDarkTheme = androidx.compose.runtime.compositionLocalOf { false }

@Composable
fun isAppInDarkTheme(): Boolean = LocalIsDarkTheme.current

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

    androidx.compose.runtime.CompositionLocalProvider(
        LocalIsDarkTheme provides isDark
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            content = content
        )
    }
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

fun formatVoltUpAnnotated(
    text: String,
    defaultWeight: FontWeight = FontWeight.Normal,
    voltWeight: FontWeight = FontWeight.Bold,
    upWeight: FontWeight = FontWeight.Light
): androidx.compose.ui.text.AnnotatedString {
    return buildAnnotatedString {
        var currentIndex = 0
        while (currentIndex < text.length) {
            val voltUpIndex = text.indexOf("VoltUp", currentIndex)
            if (voltUpIndex == -1) {
                withStyle(SpanStyle(fontWeight = defaultWeight)) {
                    append(text.substring(currentIndex))
                }
                break
            } else {
                if (voltUpIndex > currentIndex) {
                    withStyle(SpanStyle(fontWeight = defaultWeight)) {
                        append(text.substring(currentIndex, voltUpIndex))
                    }
                }
                withStyle(SpanStyle(fontWeight = voltWeight)) {
                    append("Volt")
                }
                withStyle(SpanStyle(fontWeight = upWeight)) {
                    append("Up")
                }
                currentIndex = voltUpIndex + 6
            }
        }
    }
}
