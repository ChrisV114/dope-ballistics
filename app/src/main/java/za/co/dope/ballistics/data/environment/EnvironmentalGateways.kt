package za.co.dope.ballistics.data.environment

import za.co.dope.ballistics.domain.environment.LocationReading
import za.co.dope.ballistics.domain.environment.OrientationReading
import za.co.dope.ballistics.domain.environment.PressureSampleSummary
import za.co.dope.ballistics.domain.environment.SensorCapability
import za.co.dope.ballistics.domain.environment.WeatherReading

interface SensorGateway {
    fun capabilities(): List<SensorCapability>

    suspend fun samplePressure(durationMillis: Long = 7_000L): Result<PressureSampleSummary>

    suspend fun sampleOrientation(durationMillis: Long = 2_000L): Result<OrientationReading>
}

interface LocationGateway {
    fun hasLocationPermission(): Boolean

    suspend fun currentLocation(): Result<LocationReading>
}

interface WeatherProvider {
    val providerName: String

    suspend fun currentWeather(
        latitudeDegrees: Double,
        longitudeDegrees: Double,
    ): Result<WeatherReading>
}

interface TerrainElevationProvider {
    suspend fun elevationMetres(
        latitudeDegrees: Double,
        longitudeDegrees: Double,
    ): Result<Double>
}

class DisabledTerrainElevationProvider : TerrainElevationProvider {
    override suspend fun elevationMetres(
        latitudeDegrees: Double,
        longitudeDegrees: Double,
    ): Result<Double> = Result.failure(UnsupportedOperationException("No terrain provider is configured"))
}

class ManualOnlyWeatherProvider : WeatherProvider {
    override val providerName = "Manual/offline"

    override suspend fun currentWeather(
        latitudeDegrees: Double,
        longitudeDegrees: Double,
    ): Result<WeatherReading> = Result.failure(IllegalStateException("Internet weather is disabled"))
}
