package za.co.dope.ballistics

import org.junit.Assert.assertEquals
import org.junit.Test

class FoundationTest {
    @Test
    fun packageIdentityMatchesMilestoneZeroDecision() {
        assertEquals("za.co.dope.ballistics", BuildConfig.APPLICATION_ID)
    }
}
