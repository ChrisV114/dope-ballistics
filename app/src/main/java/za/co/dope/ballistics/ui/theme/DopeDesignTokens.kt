package za.co.dope.ballistics.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object DopeDesignTokens {
    object Colors {
        val BackgroundDeep = Color(0xFF0B1220)
        val BlackBlue = Color(0xFF08111E)
        val SurfaceBase = Color(0xFF12182A)
        val SurfaceRaised = Color(0xFF1E293B)
        val SurfaceStrong = Color(0xFF2E3B4E)
        val Border = Color(0xFF334155)
        val TextPrimary = Color(0xFFF8FAFC)
        val TextSecondary = Color(0xFFCBD5E1)
        val TextMuted = Color(0xFF94A3B8)
        val Primary = Color(0xFF2563EB)
        val PrimaryBright = Color(0xFF60A5FA)
        val Lime = Color(0xFFA3E635)
        val Success = Color(0xFF22C55E)
        val Warning = Color(0xFFF59E0B)
        val Error = Color(0xFFEF4444)
        val Info = Color(0xFF38BDF8)
        val TopographicLine = Color(0xFF1D4ED8)
        val RedLightBackground = Color(0xFF090000)
        val RedLightSurface = Color(0xFF1A0505)
        val RedLightPrimary = Color(0xFFFF3B30)
        val RedLightText = Color(0xFFFFD5D2)
    }

    object Spacing {
        val Baseline = 4.dp
        val Chip = 8.dp
        val Control = 12.dp
        val ScreenHorizontal = 16.dp
        val Card = 16.dp
        val Section = 24.dp
    }

    object Sizing {
        val MinimumTouchTarget = 48.dp
        val PrimaryControlHeight = 52.dp
        val CardCorner = 12.dp
        val ControlCorner = 10.dp
        val Border = 1.dp
        val BottomNavigationMinimum = 72.dp
        val BottomNavigationContent = 56.dp
    }
}

enum class DopeThemeMode {
    DARK,
    HIGH_CONTRAST,
    RED_LIGHT,
}

/**
 * Keeps navigation content clear of the OS gesture bar and three-button controls.
 *
 * The locked 72–80 dp band is retained for the common 0–24 dp inset range. Larger system
 * navigation bars expand the container instead of allowing app actions to sit underneath them.
 */
fun bottomNavigationHeightDp(systemBottomInsetDp: Int): Int =
    maxOf(
        DopeDesignTokens.Sizing.BottomNavigationMinimum.value
            .toInt(),
        DopeDesignTokens.Sizing.BottomNavigationContent.value
            .toInt() + systemBottomInsetDp,
    )
