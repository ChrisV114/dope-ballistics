package za.co.dope.ballistics.domain.environment

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import za.co.dope.ballistics.domain.DataSource
import za.co.dope.ballistics.domain.ReadingQuality

class EnvironmentalMathTest {
    @Test
    fun standardAtmosphereProducesExpectedPressureAndDensityAltitude() {
        val result = EnvironmentalMath.calculate(288.15, 101_325.0, 0.0)

        assertEquals(1.225, result.airDensityKilogramsPerCubicMetre, 0.001)
        assertEquals(0.0, result.pressureAltitudeMetres, 0.01)
        assertEquals(0.0, result.densityAltitudeMetres, 1.0)
    }

    @Test
    fun buckVapourPressureAndDewPointMatchReferenceCase() {
        val result = EnvironmentalMath.calculate(293.15, 101_325.0, 0.5)

        assertEquals(1_169.0, result.waterVapourPressurePascals, 2.0)
        assertEquals(282.41, result.dewPointKelvin, 0.1)
        assertTrue(result.speedOfSoundMetresPerSecond in 342.5..344.0)
    }

    @Test
    fun pressureStatisticsDiscardSettlingAndExposeInstability() {
        val stable = PressureStatistics.summarise(listOf(700f, 1200f, 900f, 900.1f, 899.9f, 900f), 6_000L)
        val unstable = PressureStatistics.summarise(listOf(700f, 1200f, 890f, 900f, 910f), 6_000L)

        assertEquals(90_000.0, stable.stationPressurePascals, 5.0)
        assertTrue(stable.stable)
        assertFalse(unstable.stable)
        assertEquals(2, stable.discardedSettlingSampleCount)
    }

    @Test
    fun sourcePriorityAndStalenessAreExplicit() {
        val now = 10_000L
        val weather = SourcedReading(100_000.0, DataSource.WEATHER_SERVICE, ReadingQuality.FAIR, 9_900L)
        val sensor = SourcedReading(99_900.0, DataSource.DEVICE_SENSOR, ReadingQuality.GOOD, 8_000L)

        assertEquals(sensor, EnvironmentalSourcePolicy.choosePressure(listOf(weather, sensor)))
        assertTrue(EnvironmentalSourcePolicy.isStale(sensor, now, 1_000L))
        assertFalse(EnvironmentalSourcePolicy.isStale(weather, now, 1_000L))
    }
}
