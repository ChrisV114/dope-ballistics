package za.co.dope.ballistics.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import za.co.dope.ballistics.data.db.VerifiedDopeRecordEntity

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

    @Test
    fun previousDopeUsesNewestVerifiedRecordAtExactDistanceForActiveSetup() {
        val records =
            listOf(
                verifiedDope("newest", "active-zero", 500.0, "VERIFIED", 3.7),
                verifiedDope("older", "active-zero", 500.0, "VERIFIED", 3.6),
                verifiedDope("other-distance", "active-zero", 501.0, "VERIFIED", 4.0),
                verifiedDope("other-setup", "other-zero", 500.0, "VERIFIED", 2.8),
            )

        val result = previousDopeForDistance(records, "active-zero", 500.0)

        assertEquals("newest", result?.id)
        assertEquals(3.7, result?.actualDialValue ?: 0.0, 0.0)
    }

    @Test
    fun previousDopeDoesNotPresentUnverifiedOrMissingSetupRecords() {
        val records = listOf(verifiedDope("calculated", "active-zero", 500.0, "CALCULATED", 3.7))

        assertNull(previousDopeForDistance(records, "active-zero", 500.0))
        assertNull(previousDopeForDistance(records, null, 500.0))
        assertNull(previousDopeForDistance(records, "active-zero", null))
    }

    private fun verifiedDope(
        id: String,
        zeroProfileId: String,
        distanceMetres: Double,
        status: String,
        actualDialValue: Double,
    ) = VerifiedDopeRecordEntity(
        id = id,
        createdAtEpochMillis = 1,
        rifleId = "rifle",
        rifleRevision = 1,
        ammunitionId = "ammunition",
        ammunitionRevision = 1,
        scopeProfileId = "scope",
        scopeProfileRevision = 1,
        zeroProfileId = zeroProfileId,
        zeroProfileRevision = 1,
        profileSnapshotJson = "{}",
        distanceMetres = distanceMetres,
        distanceSource = "MANUAL",
        distanceUncertaintyMetres = 0.0,
        calculatedUnit = "MIL",
        calculatedRawValue = 3.6,
        calculatedDialValue = 3.6,
        calculatedClicks = 36,
        actualDialUnit = "MIL",
        actualDialValue = actualDialValue,
        numberOfShots = 3,
        conditionsJson = "{}",
        confidence = "MEDIUM",
        status = status,
        engineVersion = "test",
        evidenceSha256 = "test",
    )
}
