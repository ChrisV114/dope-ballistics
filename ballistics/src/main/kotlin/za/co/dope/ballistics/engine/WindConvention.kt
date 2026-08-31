package za.co.dope.ballistics.engine

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

enum class BearingReference {
    TRUE,
    MAGNETIC,
}

enum class WindSpeedSelection {
    MINIMUM,
    AVERAGE,
    MAXIMUM,
    GUST,
}

enum class CrosswindEffect {
    LEFT,
    NONE,
    RIGHT,
}

data class WindObservation(
    val windFromDegrees: Double,
    val directionOfFireDegrees: Double,
    val bearingReference: BearingReference,
    val magneticDeclinationDegrees: Double? = null,
    val minimumSpeedMps: Double,
    val averageSpeedMps: Double,
    val maximumSpeedMps: Double,
    val gustSpeedMps: Double? = null,
    val selectedSpeed: WindSpeedSelection = WindSpeedSelection.AVERAGE,
)

data class WindComponents(
    val speedMps: Double,
    val headwindMps: Double,
    val crosswindMps: Double,
)

data class WindBracket(
    val minimum: WindComponents,
    val expected: WindComponents,
    val maximum: WindComponents,
    val gust: WindComponents?,
)

data class ResolvedWind(
    val windFromInputDegrees: Double,
    val directionOfFireInputDegrees: Double,
    val bearingReference: BearingReference,
    val windFromTrueDegrees: Double?,
    val directionOfFireTrueDegrees: Double?,
    val relativeWindFromDegrees: Double,
    val effect: CrosswindEffect,
    val selected: WindComponents,
    val bracket: WindBracket,
) {
    fun asEngineWind(): Wind =
        Wind(
            headwindMps = selected.headwindMps,
            crosswindMps = selected.crosswindMps,
        )
}

object WindConvention {
    /**
     * Bearings are clockwise from north and wind direction is where the wind comes from.
     * Relative angles are signed in [-180, 180). Positive engine crosswind moves the
     * projectile right; positive headwind travels from the target toward the shooter.
     */
    fun resolve(input: WindObservation): ResolvedWind {
        validate(input)
        val relative = normalizeSignedDegrees(input.windFromDegrees - input.directionOfFireDegrees)
        val minimum = components(input.minimumSpeedMps, relative)
        val expected = components(input.averageSpeedMps, relative)
        val maximum = components(input.maximumSpeedMps, relative)
        val gust = input.gustSpeedMps?.let { components(it, relative) }
        val selected =
            when (input.selectedSpeed) {
                WindSpeedSelection.MINIMUM -> minimum
                WindSpeedSelection.AVERAGE -> expected
                WindSpeedSelection.MAXIMUM -> maximum
                WindSpeedSelection.GUST -> requireNotNull(gust) { "Gust speed is required when gust is selected" }
            }
        val trueWind = toTrueDegrees(input.windFromDegrees, input.bearingReference, input.magneticDeclinationDegrees)
        val trueFire =
            toTrueDegrees(input.directionOfFireDegrees, input.bearingReference, input.magneticDeclinationDegrees)
        return ResolvedWind(
            windFromInputDegrees = input.windFromDegrees,
            directionOfFireInputDegrees = input.directionOfFireDegrees,
            bearingReference = input.bearingReference,
            windFromTrueDegrees = trueWind,
            directionOfFireTrueDegrees = trueFire,
            relativeWindFromDegrees = relative,
            effect = effect(selected.crosswindMps),
            selected = selected,
            bracket = WindBracket(minimum, expected, maximum, gust),
        )
    }

    fun normalizeDegrees(value: Double): Double {
        require(value.isFinite()) { "Bearing must be finite" }
        val normal = value % FULL_CIRCLE_DEGREES
        return if (normal < 0.0) normal + FULL_CIRCLE_DEGREES else normal
    }

    fun normalizeSignedDegrees(value: Double): Double {
        val normal = normalizeDegrees(value + HALF_CIRCLE_DEGREES) - HALF_CIRCLE_DEGREES
        return if (normal == -0.0) 0.0 else normal
    }

    private fun validate(input: WindObservation) {
        require(input.windFromDegrees in 0.0..<FULL_CIRCLE_DEGREES) { "Wind-from bearing must be 0 to 359.999 degrees" }
        require(input.directionOfFireDegrees in 0.0..<FULL_CIRCLE_DEGREES) {
            "Direction-of-fire bearing must be 0 to 359.999 degrees"
        }
        require(input.minimumSpeedMps.isFinite() && input.minimumSpeedMps >= 0.0) {
            "Minimum wind must be non-negative"
        }
        require(input.averageSpeedMps >= input.minimumSpeedMps) { "Average wind must not be below minimum" }
        require(input.maximumSpeedMps >= input.averageSpeedMps) { "Maximum wind must not be below average" }
        require(input.maximumSpeedMps.isFinite()) { "Maximum wind must be finite" }
        input.gustSpeedMps?.let {
            require(it.isFinite() && it >= input.maximumSpeedMps) { "Gust must not be below maximum wind" }
        }
        input.magneticDeclinationDegrees?.let { require(it.isFinite()) { "Magnetic declination must be finite" } }
    }

    private fun components(
        speedMps: Double,
        relativeDegrees: Double,
    ): WindComponents {
        val radians = relativeDegrees * PI / HALF_CIRCLE_DEGREES
        return WindComponents(
            speedMps = speedMps,
            headwindMps = speedMps * cos(radians),
            crosswindMps = -speedMps * sin(radians),
        )
    }

    private fun effect(crosswindMps: Double): CrosswindEffect =
        when {
            crosswindMps > EFFECT_EPSILON -> CrosswindEffect.RIGHT
            crosswindMps < -EFFECT_EPSILON -> CrosswindEffect.LEFT
            else -> CrosswindEffect.NONE
        }

    private fun toTrueDegrees(
        bearingDegrees: Double,
        reference: BearingReference,
        declinationDegrees: Double?,
    ): Double? =
        when (reference) {
            BearingReference.TRUE -> bearingDegrees
            BearingReference.MAGNETIC -> declinationDegrees?.let { normalizeDegrees(bearingDegrees + it) }
        }

    private const val FULL_CIRCLE_DEGREES = 360.0
    private const val HALF_CIRCLE_DEGREES = 180.0
    private const val EFFECT_EPSILON = 1e-9
}
