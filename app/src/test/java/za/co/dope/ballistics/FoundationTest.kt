package za.co.dope.ballistics

import org.junit.Assert.assertEquals
import org.junit.Test

class FoundationTest {
    @Test
    fun applicationIdMatchesOwnerDecision() {
        assertEquals("za.co.bdstudio.dope", BuildConfig.APPLICATION_ID)
    }
}
