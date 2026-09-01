package za.co.dope.ballistics.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CalculationControlsTest {
    @Test
    fun windCorrectionNamesClickDirection() {
        assertEquals("0.6 MIL · 6 clicks LEFT · 4.5 m/s", windCorrectionLabel(-0.6, -6, "MIL", "4.5"))
        assertEquals("0.75 MOA · 3 clicks RIGHT · 2 m/s", windCorrectionLabel(0.75, 3, "MOA", "2"))
    }

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

    @Test
    fun blankDistanceIsAnEditableIntermediateValue() {
        assertNull(parsePositiveDistance(""))
        assertNull(parsePositiveDistance("0"))
        assertEquals(250.5, parsePositiveDistance("250,5")!!, 0.0)
    }

    @Test
    fun numericEditorKeepsOneDecimalSeparator() {
        assertEquals("250.5", sanitiseDecimalInput("250,5"))
        assertEquals("250.5", sanitiseDecimalInput("2a50..5m"))
        assertEquals("", sanitiseDecimalInput("m"))
    }

    @Test
    fun directAverageWindEntryKeepsTheBracketValid() {
        val state = WindFormState().apply { maximumSpeedMps = "4" }

        setAverageWindSpeed(state, "7,5")

        assertEquals("7.5", state.averageSpeedMps)
        assertEquals("7.5", state.maximumSpeedMps)
        state.observation()
    }
}
