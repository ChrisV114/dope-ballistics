package za.co.dope.ballistics.domain

import kotlinx.serialization.Serializable
import java.util.UUID
import kotlin.math.sqrt

@Serializable
enum class DataSource {
    DEVICE_SENSOR,
    GPS,
    SAVED_RANGE,
    MANUAL,
    WEATHER_SERVICE,
    TERRAIN_SERVICE,
    BLUETOOTH_DEVICE,
    CAMERA_CALIBRATION,
    CAMERA_MEASUREMENT,
    ARCORE,
    CALCULATED,
    IMPORTED,
}

@Serializable
enum class ReadingQuality { EXCELLENT, GOOD, FAIR, POOR, UNAVAILABLE }

@Serializable
enum class AngularUnit { MIL, MOA }

@Serializable
enum class ReticleMeasurementSystem { MIL, MOA, BDC, HYBRID, UNKNOWN }

@Serializable
enum class FocalPlane { FIRST, SECOND, UNKNOWN }

@Serializable
enum class DialDirection { CLOCKWISE_UP, COUNTERCLOCKWISE_UP, UNKNOWN }

@Serializable
enum class VerificationStatus {
    FACTORY_TEMPLATE,
    REQUIRES_USER_VERIFICATION,
    USER_VERIFIED,
    MODIFIED_AFTER_VERIFICATION,
}

@Serializable
enum class DragModel { G1, G7 }

@Serializable
enum class TwistDirection { RIGHT, LEFT, UNKNOWN }

@Serializable
enum class StaticTargetClass {
    RECTANGULAR_PAPER,
    CIRCULAR_PAPER,
    GRID_PAPER,
    PRINTED_SILHOUETTE_RANGE_TARGET,
    PAINTED_STEEL,
    ELECTRONIC_RANGE_TARGET,
    CUSTOM_STATIC_RANGE_TARGET,
}

object ProfileIdentity {
    fun newId(): String = UUID.randomUUID().toString()
}

data class ChronographStatistics(
    val averageMetresPerSecond: Double,
    val medianMetresPerSecond: Double,
    val minimumMetresPerSecond: Double,
    val maximumMetresPerSecond: Double,
    val extremeSpreadMetresPerSecond: Double,
    val sampleStandardDeviationMetresPerSecond: Double,
    val sampleCount: Int,
)

object ChronographCalculator {
    fun calculate(readingsMetresPerSecond: List<Double>): ChronographStatistics {
        require(readingsMetresPerSecond.isNotEmpty()) { "At least one velocity reading is required" }
        require(readingsMetresPerSecond.all { it.isFinite() && it > 0.0 }) {
            "Velocity readings must be finite and positive"
        }
        val sorted = readingsMetresPerSecond.sorted()
        val average = sorted.average()
        val median =
            if (sorted.size % 2 == 0) {
                (sorted[sorted.size / 2 - 1] + sorted[sorted.size / 2]) / 2.0
            } else {
                sorted[sorted.size / 2]
            }
        val sampleDeviation =
            if (sorted.size < 2) {
                0.0
            } else {
                sqrt(sorted.sumOf { (it - average) * (it - average) } / (sorted.size - 1))
            }
        return ChronographStatistics(
            averageMetresPerSecond = average,
            medianMetresPerSecond = median,
            minimumMetresPerSecond = sorted.first(),
            maximumMetresPerSecond = sorted.last(),
            extremeSpreadMetresPerSecond = sorted.last() - sorted.first(),
            sampleStandardDeviationMetresPerSecond = sampleDeviation,
            sampleCount = sorted.size,
        )
    }
}

object ScopeVerificationRules {
    private val verificationCriticalFields =
        setOf(
            "turretUnit",
            "elevationClickValue",
            "windageClickValue",
            "reticleSystem",
            "focalPlane",
            "sightHeightMetres",
            "zeroDistanceMetres",
            "elevationDialDirection",
            "windageDialDirection",
            "variantId",
        )

    fun statusAfterEdit(
        current: VerificationStatus,
        changedFields: Set<String>,
    ): VerificationStatus =
        if (current == VerificationStatus.USER_VERIFIED && changedFields.any(verificationCriticalFields::contains)) {
            VerificationStatus.MODIFIED_AFTER_VERIFICATION
        } else {
            current
        }
}

object BdcRules {
    fun canDisplayCalibratedMark(
        reticleSystem: ReticleMeasurementSystem,
        calibrationVerified: Boolean,
    ): Boolean = reticleSystem != ReticleMeasurementSystem.BDC || calibrationVerified

    fun genericAngularHoldAllowed(system: ReticleMeasurementSystem): Boolean = system != ReticleMeasurementSystem.BDC
}

object TargetClassRules {
    private val forbiddenTerms = setOf("person", "people", "human", "animal", "vehicle", "car")

    fun validateName(name: String) {
        val normalised = name.lowercase()
        require(forbiddenTerms.none(normalised::contains)) {
            "Living subjects and vehicles are not supported target classes"
        }
    }
}
