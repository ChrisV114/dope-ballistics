package za.co.dope.ballistics.data.environment

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import za.co.dope.ballistics.domain.DataSource
import za.co.dope.ballistics.domain.ReadingQuality
import za.co.dope.ballistics.domain.environment.SourcedReading
import za.co.dope.ballistics.domain.environment.WeatherReading
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class OpenMeteoWeatherProvider(
    private val enabled: Boolean,
    private val now: () -> Long = System::currentTimeMillis,
) : WeatherProvider {
    override val providerName = "Open-Meteo"

    override suspend fun currentWeather(
        latitudeDegrees: Double,
        longitudeDegrees: Double,
    ): Result<WeatherReading> =
        withContext(Dispatchers.IO) {
            runCatching {
                check(enabled) { "Internet weather is disabled" }
                require(latitudeDegrees in -90.0..90.0 && longitudeDegrees in -180.0..180.0)
                val current =
                    "temperature_2m,relative_humidity_2m,surface_pressure,pressure_msl," +
                        "wind_speed_10m,wind_direction_10m"
                val address =
                    "https://api.open-meteo.com/v1/forecast?latitude=${latitudeDegrees.encoded()}" +
                        "&longitude=${longitudeDegrees.encoded()}&current=$current&wind_speed_unit=ms&timezone=UTC"
                val connection = URL(address).openConnection() as HttpURLConnection
                try {
                    connection.connectTimeout = 10_000
                    connection.readTimeout = 10_000
                    connection.setRequestProperty("Accept", "application/json")
                    check(connection.responseCode in 200..299) {
                        "Weather request failed (${connection.responseCode})"
                    }
                    parse(
                        connection.inputStream.bufferedReader().use { it.readText() },
                        latitudeDegrees,
                        longitudeDegrees,
                    )
                } finally {
                    connection.disconnect()
                }
            }
        }

    internal fun parse(
        payload: String,
        requestedLatitude: Double,
        requestedLongitude: Double,
    ): WeatherReading {
        val root = Json.parseToJsonElement(payload).jsonObject
        val current = requireNotNull(root["current"]?.jsonObject) { "Weather response has no current conditions" }
        val captured = now()

        fun reading(
            name: String,
            transform: (Double) -> Double = { it },
        ) = SourcedReading(
            value =
                transform(
                    requireNotNull(current[name]?.jsonPrimitive?.double) {
                        "Weather response is missing $name"
                    },
                ),
            source = DataSource.WEATHER_SERVICE,
            quality = ReadingQuality.FAIR,
            capturedAtEpochMillis = captured,
        )
        return WeatherReading(
            temperatureKelvin = reading("temperature_2m") { it + 273.15 },
            stationPressurePascals = reading("surface_pressure") { it * 100.0 },
            meanSeaLevelPressurePascals = reading("pressure_msl") { it * 100.0 },
            relativeHumidityFraction = reading("relative_humidity_2m") { it / 100.0 },
            windSpeedMetresPerSecond = reading("wind_speed_10m"),
            windDirectionDegrees = reading("wind_direction_10m"),
            providerName = providerName,
            attribution = "Weather data by Open-Meteo.com",
            modelElevationMetres = root["elevation"]?.jsonPrimitive?.double,
            latitudeDegrees = root["latitude"]?.jsonPrimitive?.double ?: requestedLatitude,
            longitudeDegrees = root["longitude"]?.jsonPrimitive?.double ?: requestedLongitude,
            fetchedAtEpochMillis = captured,
        )
    }

    private fun Double.encoded(): String = URLEncoder.encode(toString(), Charsets.UTF_8.name())
}
