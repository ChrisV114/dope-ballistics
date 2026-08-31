package za.co.dope.ballistics.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class WindConventionTest {
    @Test
    fun windFromNorthWhileFiringEastMovesProjectileRight() {
        val result = WindConvention.resolve(observation(windFrom = 0.0, fire = 90.0))

        assertEquals(-90.0, result.relativeWindFromDegrees, TOLERANCE)
        assertEquals(CrosswindEffect.RIGHT, result.effect)
        assertEquals(10.0, result.selected.crosswindMps, TOLERANCE)
        assertEquals(0.0, result.selected.headwindMps, TOLERANCE)
    }

    @Test
    fun windFromSouthWhileFiringEastMovesProjectileLeft() {
        val result = WindConvention.resolve(observation(windFrom = 180.0, fire = 90.0))

        assertEquals(90.0, result.relativeWindFromDegrees, TOLERANCE)
        assertEquals(CrosswindEffect.LEFT, result.effect)
        assertEquals(-10.0, result.selected.crosswindMps, TOLERANCE)
    }

    @Test
    fun windFromDownrangeIsPositiveHeadwind() {
        val result = WindConvention.resolve(observation(windFrom = 90.0, fire = 90.0))

        assertEquals(10.0, result.selected.headwindMps, TOLERANCE)
        assertEquals(0.0, result.selected.crosswindMps, TOLERANCE)
    }

    @Test
    fun magneticBearingsResolveToTrueWithEastDeclination() {
        val result =
            WindConvention.resolve(
                observation(windFrom = 350.0, fire = 80.0).copy(
                    bearingReference = BearingReference.MAGNETIC,
                    magneticDeclinationDegrees = 12.0,
                ),
            )

        assertEquals(2.0, requireNotNull(result.windFromTrueDegrees), TOLERANCE)
        assertEquals(92.0, requireNotNull(result.directionOfFireTrueDegrees), TOLERANCE)
        assertEquals(-90.0, result.relativeWindFromDegrees, TOLERANCE)
    }

    @Test
    fun magneticRelativeWindWorksWithoutInventingTrueBearing() {
        val result =
            WindConvention.resolve(
                observation(windFrom = 270.0, fire = 0.0).copy(bearingReference = BearingReference.MAGNETIC),
            )

        assertNull(result.windFromTrueDegrees)
        assertNull(result.directionOfFireTrueDegrees)
        assertEquals(-90.0, result.relativeWindFromDegrees, TOLERANCE)
    }

    @Test
    fun selectedGustRequiresEnteredGust() {
        val invalid =
            observation(0.0, 0.0).copy(
                gustSpeedMps = null,
                selectedSpeed = WindSpeedSelection.GUST,
            )

        assertThrows(IllegalArgumentException::class.java) { WindConvention.resolve(invalid) }
    }

    private fun observation(
        windFrom: Double,
        fire: Double,
    ) = WindObservation(
        windFromDegrees = windFrom,
        directionOfFireDegrees = fire,
        bearingReference = BearingReference.TRUE,
        minimumSpeedMps = 5.0,
        averageSpeedMps = 10.0,
        maximumSpeedMps = 15.0,
        gustSpeedMps = 18.0,
    )

    private companion object {
        const val TOLERANCE = 1e-9
    }
}
