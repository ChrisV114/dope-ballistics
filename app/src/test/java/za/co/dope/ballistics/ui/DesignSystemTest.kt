package za.co.dope.ballistics.ui

import androidx.compose.ui.graphics.toArgb
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import za.co.dope.ballistics.ui.theme.DopeDesignTokens
import za.co.dope.ballistics.ui.theme.bottomNavigationHeightDp

class DesignSystemTest {
    @Test
    fun lockedColourTokensKeepApprovedArgbValues() {
        val expected =
            intArrayOf(
                0xFF0B1220.toInt(),
                0xFF08111E.toInt(),
                0xFF12182A.toInt(),
                0xFF1E293B.toInt(),
                0xFF2E3B4E.toInt(),
                0xFF334155.toInt(),
                0xFFF8FAFC.toInt(),
                0xFFCBD5E1.toInt(),
                0xFF94A3B8.toInt(),
                0xFF2563EB.toInt(),
                0xFF60A5FA.toInt(),
                0xFFA3E635.toInt(),
                0xFF22C55E.toInt(),
                0xFFF59E0B.toInt(),
                0xFFEF4444.toInt(),
                0xFF38BDF8.toInt(),
            )
        val actual =
            intArrayOf(
                DopeDesignTokens.Colors.BackgroundDeep.toArgb(),
                DopeDesignTokens.Colors.BlackBlue.toArgb(),
                DopeDesignTokens.Colors.SurfaceBase.toArgb(),
                DopeDesignTokens.Colors.SurfaceRaised.toArgb(),
                DopeDesignTokens.Colors.SurfaceStrong.toArgb(),
                DopeDesignTokens.Colors.Border.toArgb(),
                DopeDesignTokens.Colors.TextPrimary.toArgb(),
                DopeDesignTokens.Colors.TextSecondary.toArgb(),
                DopeDesignTokens.Colors.TextMuted.toArgb(),
                DopeDesignTokens.Colors.Primary.toArgb(),
                DopeDesignTokens.Colors.PrimaryBright.toArgb(),
                DopeDesignTokens.Colors.Lime.toArgb(),
                DopeDesignTokens.Colors.Success.toArgb(),
                DopeDesignTokens.Colors.Warning.toArgb(),
                DopeDesignTokens.Colors.Error.toArgb(),
                DopeDesignTokens.Colors.Info.toArgb(),
            )

        assertArrayEquals(expected, actual)
    }

    @Test
    fun bottomNavigationRemainsInLockedBandForCommonGestureInset() {
        assertEquals(72, bottomNavigationHeightDp(systemBottomInsetDp = 0))
        assertEquals(72, bottomNavigationHeightDp(systemBottomInsetDp = 16))
        assertEquals(80, bottomNavigationHeightDp(systemBottomInsetDp = 24))
    }

    @Test
    fun bottomNavigationExpandsForLargerPhoneControls() {
        val threeButtonHeight = bottomNavigationHeightDp(systemBottomInsetDp = 48)

        assertEquals(104, threeButtonHeight)
        assertTrue(threeButtonHeight >= 56 + 48)
    }
}
