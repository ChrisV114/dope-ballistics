package za.co.dope.ballistics.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Test

class CalculationControlsTest {
    @Test
    fun quickWindSpeedKeepsBracketOrdered() {
        val state =
            WindFormState().apply {
                minimumSpeedMps = "2"
                averageSpeedMps = "3"
                maximumSpeedMps = "4"
            }

        adjustWindSpeed(state, 2.0)

        assertEquals("5", state.averageSpeedMps)
        assertEquals("2", state.minimumSpeedMps)
        assertEquals("5", state.maximumSpeedMps)
        state.observation()
    }

    @Test
    fun quickWindDirectionWrapsThroughNorth() {
        val state = WindFormState().apply { windFromDegrees = "358" }

        adjustWindDirection(state, 5.0)

        assertEquals("3", state.windFromDegrees)
    }
}
