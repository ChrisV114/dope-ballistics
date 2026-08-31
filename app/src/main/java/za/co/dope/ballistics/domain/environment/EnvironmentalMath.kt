package za.co.dope.ballistics.domain.environment

import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sqrt

object EnvironmentalMath {
    private const val DRY_AIR_GAS_CONSTANT = 287.05
    private const val WATER_VAPOUR_GAS_CONSTANT = 461.495
    private const val STANDARD_TEMPERATURE_KELVIN = 288.15
    private const val STANDARD_PRESSURE_PASCALS = 101_325.0
    private const val STANDARD_DENSITY = 1.225
    private const val LAPSE_RATE_KELVIN_PER_METRE = 0.0065
    private const val GRAVITY = 9.80665
    private const val HEAT_CAPACITY_RATIO = 1.4

    fun calculate(
        temperatureKelvin: Double,
        stationPressurePascals: Double,
        relativeHumidityFraction: Double,
    ): AtmosphericResult {
        require(temperatureKelvin in 180.0..340.0) { "Ambient temperature is outside the supported range" }
        require(stationPressurePascals in 30_000.0..110_000.0) { "Station pressure is outside the supported range" }
        require(relativeHumidityFraction in 0.0..1.0) { "Relative humidity must be between 0 and 1" }
        val temperatureCelsius = temperatureKelvin - 273.15
        val saturationHpa =
            6.1121 * exp((18.678 - temperatureCelsius / 234.5) * (temperatureCelsius / (257.14 + temperatureCelsius)))
        val vapourPressure = saturationHpa * 100.0 * relativeHumidityFraction
        val dryPressure = stationPressurePascals - vapourPressure
        val density =
            dryPressure / (DRY_AIR_GAS_CONSTANT * temperatureKelvin) +
                vapourPressure / (WATER_VAPOUR_GAS_CONSTANT * temperatureKelvin)
        return AtmosphericResult(
            airDensityKilogramsPerCubicMetre = density,
            densityRatio = density / STANDARD_DENSITY,
            pressureAltitudeMetres = pressureAltitudeMetres(stationPressurePascals),
            densityAltitudeMetres = densityAltitudeMetres(density),
            dewPointKelvin = dewPointKelvin(temperatureCelsius, relativeHumidityFraction),
            waterVapourPressurePascals = vapourPressure,
            speedOfSoundMetresPerSecond = sqrt(HEAT_CAPACITY_RATIO * DRY_AIR_GAS_CONSTANT * temperatureKelvin),
        )
    }

    fun pressureAltitudeMetres(stationPressurePascals: Double): Double {
        require(stationPressurePascals in 30_000.0..110_000.0)
        return 44_330.769 * (1.0 - (stationPressurePascals / STANDARD_PRESSURE_PASCALS).pow(0.190263))
    }

    fun dewPointKelvin(
        temperatureCelsius: Double,
        relativeHumidityFraction: Double,
    ): Double {
        if (relativeHumidityFraction == 0.0) return 0.0
        val alpha = ln(relativeHumidityFraction) + 17.625 * temperatureCelsius / (243.04 + temperatureCelsius)
        return 243.04 * alpha / (17.625 - alpha) + 273.15
    }

    fun densityAltitudeMetres(densityKilogramsPerCubicMetre: Double): Double {
        require(densityKilogramsPerCubicMetre in 0.2..1.6)
        var low = -1_000.0
        var high = 20_000.0
        repeat(80) {
            val midpoint = (low + high) / 2.0
            if (standardDensityAt(midpoint) > densityKilogramsPerCubicMetre) low = midpoint else high = midpoint
        }
        return (low + high) / 2.0
    }

    private fun standardDensityAt(altitudeMetres: Double): Double {
        val temperature = STANDARD_TEMPERATURE_KELVIN - LAPSE_RATE_KELVIN_PER_METRE * altitudeMetres
        val pressure =
            STANDARD_PRESSURE_PASCALS *
                (temperature / STANDARD_TEMPERATURE_KELVIN).pow(
                    GRAVITY / (DRY_AIR_GAS_CONSTANT * LAPSE_RATE_KELVIN_PER_METRE),
                )
        return pressure / (DRY_AIR_GAS_CONSTANT * temperature)
    }
}

object PressureStatistics {
    fun summarise(
        samplesHectopascals: List<Float>,
        intervalMillis: Long,
        settlingSamples: Int = 2,
    ): PressureSampleSummary {
        require(intervalMillis > 0L)
        val plausible =
            samplesHectopascals
                .drop(settlingSamples)
                .map(Float::toDouble)
                .filter { it in 300.0..1_100.0 }
                .sorted()
        require(plausible.size >= 3) { "At least three plausible pressure samples are required after settling" }
        val trim = if (plausible.size >= 10) plausible.size / 10 else 0
        val retained = plausible.drop(trim).dropLast(trim)
        val mean = retained.average()
        val median =
            if (retained.size % 2 ==
                0
            ) {
                (retained[retained.size / 2 - 1] + retained[retained.size / 2]) / 2.0
            } else {
                retained[retained.size / 2]
            }
        val deviation = sqrt(retained.sumOf { (it - mean) * (it - mean) } / (retained.size - 1).coerceAtLeast(1))
        return PressureSampleSummary(
            stationPressurePascals = mean * 100.0,
            medianPascals = median * 100.0,
            minimumPascals = retained.first() * 100.0,
            maximumPascals = retained.last() * 100.0,
            standardDeviationPascals = deviation * 100.0,
            retainedSampleCount = retained.size,
            discardedSettlingSampleCount = samplesHectopascals.size.coerceAtMost(settlingSamples),
            intervalMillis = intervalMillis,
            stable = deviation <= 0.35,
        )
    }
}
