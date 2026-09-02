package za.co.dope.ballistics.domain.training

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import za.co.dope.ballistics.domain.ReadingQuality

class OrientationMathTest {
    @Test
    fun circularMeanHandlesNorthWrap() {
        val mean = OrientationMath.circularMean(listOf(359.0, 0.0, 1.0))
        assertTrue(mean < 0.1 || mean > 359.9)
        assertEquals(2.0, OrientationMath.circularDelta(359.0, 1.0), 0.001)
    }

    @Test
    fun stabilityRejectsMotion() {
        assertTrue(
            OrientationMath.isStable(
                listOf(359.0, 0.0, 1.0, 0.5),
                listOf(1.0, 1.2, 0.8, 1.1),
                listOf(2.0, 2.1, 1.9, 2.0),
            ),
        )
        assertFalse(
            OrientationMath.isStable(
                listOf(10.0, 20.0, 30.0, 40.0),
                listOf(0.0, 0.0, 0.0, 0.0),
                listOf(0.0, 0.0, 0.0, 0.0),
            ),
        )
    }

    @Test
    fun playbackUsesNearestTimestampAndCsvHasNoLocation() {
        val first = sample(100, 5.0)
        val second = sample(300, 7.0)
        assertEquals(second, OrientationMath.nearestSample(listOf(first, second), 260))
        val csv = OrientationMath.toCsv(listOf(first))
        assertTrue(csv.contains("magnetic_heading_deg"))
        assertTrue(csv.contains("100,1000,5.000"))
        assertFalse(csv.contains("latitude", ignoreCase = true))
        assertFalse(csv.contains("longitude", ignoreCase = true))
    }

    private fun sample(
        elapsed: Long,
        heading: Double,
    ) = OrientationSample(elapsed, 1_000, heading, 1.0, 2.0, ReadingQuality.GOOD, true)
}
