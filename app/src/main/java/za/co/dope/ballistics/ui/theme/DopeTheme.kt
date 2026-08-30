package za.co.dope.ballistics.ui.theme

import android.graphics.Typeface
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val LocalDopeColors =
    staticCompositionLocalOf {
        DopeExtendedColors(
            surfaceStrong = DopeDesignTokens.Colors.SurfaceStrong,
            textMuted = DopeDesignTokens.Colors.TextMuted,
            lime = DopeDesignTokens.Colors.Lime,
            info = DopeDesignTokens.Colors.Info,
            topographicLine = DopeDesignTokens.Colors.TopographicLine,
        )
    }

private val Condensed = FontFamily(Typeface.create("sans-serif-condensed", Typeface.NORMAL))

private val DopeTypography =
    Typography(
        displayLarge =
            TextStyle(
                fontFamily = Condensed,
                fontWeight = FontWeight.Black,
                fontSize = 48.sp,
                lineHeight = 52.sp,
                letterSpacing = (-1).sp,
            ),
        headlineLarge =
            TextStyle(
                fontFamily = Condensed,
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp,
                lineHeight = 34.sp,
            ),
        headlineMedium =
            TextStyle(
                fontFamily = Condensed,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                lineHeight = 28.sp,
            ),
        titleLarge =
            TextStyle(
                fontFamily = Condensed,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                lineHeight = 24.sp,
            ),
        titleMedium =
            TextStyle(
                fontFamily = Condensed,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                lineHeight = 20.sp,
            ),
        bodyLarge = TextStyle(fontFamily = Condensed, fontSize = 16.sp, lineHeight = 22.sp),
        bodyMedium = TextStyle(fontFamily = Condensed, fontSize = 14.sp, lineHeight = 20.sp),
        labelLarge =
            TextStyle(
                fontFamily = Condensed,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                lineHeight = 18.sp,
                letterSpacing = 0.4.sp,
            ),
        labelMedium =
            TextStyle(
                fontFamily = Condensed,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.6.sp,
            ),
    )

private val StandardScheme =
    darkColorScheme(
        primary = DopeDesignTokens.Colors.Primary,
        onPrimary = DopeDesignTokens.Colors.TextPrimary,
        primaryContainer = DopeDesignTokens.Colors.SurfaceStrong,
        onPrimaryContainer = DopeDesignTokens.Colors.PrimaryBright,
        secondary = DopeDesignTokens.Colors.PrimaryBright,
        onSecondary = DopeDesignTokens.Colors.BlackBlue,
        tertiary = DopeDesignTokens.Colors.Lime,
        onTertiary = DopeDesignTokens.Colors.BlackBlue,
        background = DopeDesignTokens.Colors.BackgroundDeep,
        onBackground = DopeDesignTokens.Colors.TextPrimary,
        surface = DopeDesignTokens.Colors.SurfaceBase,
        onSurface = DopeDesignTokens.Colors.TextPrimary,
        surfaceVariant = DopeDesignTokens.Colors.SurfaceRaised,
        onSurfaceVariant = DopeDesignTokens.Colors.TextSecondary,
        outline = DopeDesignTokens.Colors.Border,
        error = DopeDesignTokens.Colors.Error,
        onError = DopeDesignTokens.Colors.TextPrimary,
    )

private val HighContrastScheme =
    StandardScheme.copy(
        background = Color.Black,
        surface = Color(0xFF101010),
        surfaceVariant = Color(0xFF202020),
        primary = DopeDesignTokens.Colors.PrimaryBright,
        onBackground = Color.White,
        onSurface = Color.White,
        outline = Color(0xFFB8C5D6),
    )

private val RedLightScheme =
    StandardScheme.copy(
        primary = DopeDesignTokens.Colors.RedLightPrimary,
        secondary = Color(0xFFFF6B62),
        tertiary = Color(0xFFFF8A80),
        background = DopeDesignTokens.Colors.RedLightBackground,
        onBackground = DopeDesignTokens.Colors.RedLightText,
        surface = DopeDesignTokens.Colors.RedLightSurface,
        onSurface = DopeDesignTokens.Colors.RedLightText,
        surfaceVariant = Color(0xFF2B0909),
        onSurfaceVariant = Color(0xFFFFB3AE),
        outline = Color(0xFF7A2525),
    )

private fun schemeFor(mode: DopeThemeMode): ColorScheme =
    when (mode) {
        DopeThemeMode.DARK -> StandardScheme
        DopeThemeMode.HIGH_CONTRAST -> HighContrastScheme
        DopeThemeMode.RED_LIGHT -> RedLightScheme
    }

@Composable
fun DopeTheme(
    mode: DopeThemeMode = DopeThemeMode.DARK,
    content: @Composable () -> Unit,
) {
    val extended =
        when (mode) {
            DopeThemeMode.DARK -> {
                DopeExtendedColors(
                    surfaceStrong = DopeDesignTokens.Colors.SurfaceStrong,
                    textMuted = DopeDesignTokens.Colors.TextMuted,
                    lime = DopeDesignTokens.Colors.Lime,
                    info = DopeDesignTokens.Colors.Info,
                    topographicLine = DopeDesignTokens.Colors.TopographicLine,
                )
            }

            DopeThemeMode.HIGH_CONTRAST -> {
                DopeExtendedColors(
                    surfaceStrong = Color(0xFF303030),
                    textMuted = Color(0xFFD7DEE8),
                    lime = Color(0xFFCCFF66),
                    info = Color(0xFF73DCFF),
                    topographicLine = Color(0xFF5B8FFF),
                )
            }

            DopeThemeMode.RED_LIGHT -> {
                DopeExtendedColors(
                    surfaceStrong = Color(0xFF3B0A0A),
                    textMuted = Color(0xFFDE8C86),
                    lime = Color(0xFFFF6B62),
                    info = Color(0xFFFF8A80),
                    topographicLine = Color(0xFF7A1616),
                )
            }
        }
    androidx.compose.runtime.CompositionLocalProvider(LocalDopeColors provides extended) {
        MaterialTheme(
            colorScheme = schemeFor(mode),
            typography = DopeTypography,
            content = content,
        )
    }
}
