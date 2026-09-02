package za.co.dope.ballistics.domain.training

import java.util.Locale
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

object OrientationMath {
    fun normaliseHeading(degrees: Double): Double = ((degrees % 360.0) + 360.0) % 360.0

    fun circularMean(headings: List<Double>): Double {
        if (headings.isEmpty()) return 0.0
        val radians = headings.map(Math::toRadians)
        return normaliseHeading(Math.toDegrees(atan2(radians.sumOf(::sin), radians.sumOf(::cos))))
    }

    fun circularDelta(
        first: Double,
        second: Double,
    ): Double = abs(((normaliseHeading(first) - normaliseHeading(second) + 540.0) % 360.0) - 180.0)

    fun isStable(
        headings: List<Double>,
        pitches: List<Double>,
        rolls: List<Double>,
        toleranceDegrees: Double = 3.0,
    ): Boolean {
        if (headings.size < 4 || pitches.size != headings.size || rolls.size != headings.size) return false
        val headingMean = circularMean(headings)
        return headings.maxOf { circularDelta(it, headingMean) } <= toleranceDegrees &&
            spread(pitches) <= toleranceDegrees * 2.0 && spread(rolls) <= toleranceDegrees * 2.0
    }

    fun nearestSample(
        samples: List<OrientationSample>,
        elapsedMillis: Long,
    ): OrientationSample? = samples.minByOrNull { abs(it.elapsedMillis - elapsedMillis) }

    fun toCsv(samples: List<OrientationSample>): String =
        buildString {
            appendLine("elapsed_ms,timestamp_utc_ms,magnetic_heading_deg,pitch_deg,roll_deg,accuracy,stable")
            samples.forEach { sample ->
                appendLine(
                    listOf(
                        sample.elapsedMillis,
                        sample.capturedAtEpochMillis,
                        sample.magneticHeadingDegrees.csvNumber(),
                        sample.pitchDegrees.csvNumber(),
                        sample.rollDegrees.csvNumber(),
                        sample.accuracy.name,
                        sample.stable,
                    ).joinToString(","),
                )
            }
        }

    private fun spread(values: List<Double>): Double = (values.maxOrNull() ?: 0.0) - (values.minOrNull() ?: 0.0)

    private fun Double.csvNumber(): String = String.format(Locale.US, "%.3f", this)
}
