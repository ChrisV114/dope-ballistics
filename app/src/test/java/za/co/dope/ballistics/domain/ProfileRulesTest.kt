package za.co.dope.ballistics.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class ProfileRulesTest {
    @Test
    fun `chronograph statistics use sample standard deviation`() {
        val result = ChronographCalculator.calculate(listOf(800.0, 802.0, 798.0))

        assertEquals(800.0, result.averageMetresPerSecond, 0.0)
        assertEquals(800.0, result.medianMetresPerSecond, 0.0)
        assertEquals(4.0, result.extremeSpreadMetresPerSecond, 0.0)
        assertTrue(abs(result.sampleStandardDeviationMetresPerSecond - 2.0) < 1e-12)
        assertEquals(3, result.sampleCount)
    }

    @Test
    fun `critical scope edit invalidates verified status`() {
        val status =
            ScopeVerificationRules.statusAfterEdit(
                VerificationStatus.USER_VERIFIED,
                setOf("elevationClickValue"),
            )

        assertEquals(VerificationStatus.MODIFIED_AFTER_VERIFICATION, status)
        assertEquals(
            VerificationStatus.USER_VERIFIED,
            ScopeVerificationRules.statusAfterEdit(VerificationStatus.USER_VERIFIED, setOf("notes")),
        )
    }

    @Test
    fun `KLBOX BDC never becomes a generic angular hold`() {
        assertFalse(BdcRules.genericAngularHoldAllowed(ReticleMeasurementSystem.BDC))
        assertFalse(BdcRules.canDisplayCalibratedMark(ReticleMeasurementSystem.BDC, calibrationVerified = false))
        assertTrue(BdcRules.canDisplayCalibratedMark(ReticleMeasurementSystem.BDC, calibrationVerified = true))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `living target wording is rejected`() {
        TargetClassRules.validateName("animal silhouette")
    }
}
