@file:Suppress("MaxLineLength")

package za.co.dope.ballistics.data.environment

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OpenMeteoWeatherProviderTest {
    @Test
    fun parserKeepsSurfaceAndMeanSeaLevelPressureSeparate() {
        val provider = OpenMeteoWeatherProvider(enabled = true, now = { 1234L })
        val result =
            provider.parse(
                """{"latitude":-26.2,"longitude":28.0,"elevation":1700.0,"current":{"temperature_2m":21.5,"relative_humidity_2m":44.0,"surface_pressure":832.4,"pressure_msl":1017.8,"wind_speed_10m":3.2,"wind_direction_10m":240.0}}""",
                -26.2,
                28.0,
            )

        assertEquals(83_240.0, result.stationPressurePascals.value, 0.01)
        assertEquals(101_780.0, requireNotNull(result.meanSeaLevelPressurePascals).value, 0.01)
        assertEquals(294.65, result.temperatureKelvin.value, 0.01)
        assertTrue(result.attribution.contains("Open-Meteo"))
    }
}
