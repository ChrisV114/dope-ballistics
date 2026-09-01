package za.co.dope.ballistics.domain.environment

import za.co.dope.ballistics.domain.DataSource
import za.co.dope.ballistics.domain.ReadingQuality

data class SourcedReading(
    val value: Double,
    val source: DataSource,
    val quality: ReadingQuality,
    val capturedAtEpochMillis: Long,
    val uncertainty: Double? = null,
)

data class LocationReading(
    val latitudeDegrees: Double,
    val longitudeDegrees: Double,
    val horizontalAccuracyMetres: Double,
    val altitudeMetres: Double?,
    val verticalAccuracyMetres: Double?,
    val approximate: Boolean,
    val capturedAtEpochMillis: Long,
    val cachedFallback: Boolean = false,
)

data class OrientationReading(
    val magneticHeadingDegrees: Double,
    val trueHeadingDegrees: Double?,
    val pitchDegrees: Double,
    val rollDegrees: Double,
    val accuracy: ReadingQuality,
    val stable: Boolean,
    val capturedAtEpochMillis: Long,
)

data class PressureSampleSummary(
    val stationPressurePascals: Double,
    val medianPascals: Double,
    val minimumPascals: Double,
    val maximumPascals: Double,
    val standardDeviationPascals: Double,
    val retainedSampleCount: Int,
    val discardedSettlingSampleCount: Int,
    val intervalMillis: Long,
    val stable: Boolean,
)

data class AtmosphericResult(
    val airDensityKilogramsPerCubicMetre: Double,
    val densityRatio: Double,
    val pressureAltitudeMetres: Double,
    val densityAltitudeMetres: Double,
    val dewPointKelvin: Double,
    val waterVapourPressurePascals: Double,
    val speedOfSoundMetresPerSecond: Double,
)

data class WeatherReading(
    val temperatureKelvin: SourcedReading,
    val stationPressurePascals: SourcedReading,
    val meanSeaLevelPressurePascals: SourcedReading?,
    val relativeHumidityFraction: SourcedReading,
    val windSpeedMetresPerSecond: SourcedReading?,
    val windDirectionDegrees: SourcedReading?,
    val providerName: String,
    val attribution: String,
    val modelElevationMetres: Double?,
    val latitudeDegrees: Double,
    val longitudeDegrees: Double,
    val fetchedAtEpochMillis: Long,
)

data class SensorCapability(
    val kind: String,
    val available: Boolean,
    val name: String? = null,
    val vendor: String? = null,
    val version: Int? = null,
    val resolution: Float? = null,
    val maximumRange: Float? = null,
    val reportingMode: Int? = null,
)

object EnvironmentalSourcePolicy {
    private val pressurePriority =
        listOf(DataSource.DEVICE_SENSOR, DataSource.MANUAL, DataSource.WEATHER_SERVICE)
    private val ambientPriority =
        listOf(DataSource.DEVICE_SENSOR, DataSource.MANUAL, DataSource.WEATHER_SERVICE)
    private val altitudePriority =
        listOf(DataSource.SAVED_RANGE, DataSource.MANUAL, DataSource.GPS, DataSource.TERRAIN_SERVICE)

    fun choosePressure(readings: List<SourcedReading>): SourcedReading? = choose(readings, pressurePriority)

    fun chooseAmbient(readings: List<SourcedReading>): SourcedReading? = choose(readings, ambientPriority)

    fun chooseAltitude(readings: List<SourcedReading>): SourcedReading? = choose(readings, altitudePriority)

    fun isStale(
        reading: SourcedReading,
        nowEpochMillis: Long,
        maximumAgeMillis: Long,
    ): Boolean = nowEpochMillis - reading.capturedAtEpochMillis > maximumAgeMillis

    private fun choose(
        readings: List<SourcedReading>,
        priority: List<DataSource>,
    ): SourcedReading? =
        readings
            .filter { it.value.isFinite() && it.quality != ReadingQuality.UNAVAILABLE }
            .minWithOrNull(
                compareBy<SourcedReading> {
                    priority.indexOf(it.source).let { index ->
                        if (index <
                            0
                        ) {
                            Int.MAX_VALUE
                        } else {
                            index
                        }
                    }
                }.thenByDescending { it.capturedAtEpochMillis },
            )
}
