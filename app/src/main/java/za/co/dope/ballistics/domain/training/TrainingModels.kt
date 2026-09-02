package za.co.dope.ballistics.domain.training

import kotlinx.serialization.Serializable
import za.co.dope.ballistics.domain.ReadingQuality

@Serializable
data class OrientationSample(
    val elapsedMillis: Long,
    val capturedAtEpochMillis: Long,
    val magneticHeadingDegrees: Double,
    val pitchDegrees: Double,
    val rollDegrees: Double,
    val accuracy: ReadingQuality,
    val stable: Boolean,
)

@Serializable
data class TrainingRecording(
    val id: String,
    val sessionName: String,
    val videoUri: String,
    val sensorCsvUri: String?,
    val startedAtEpochMillis: Long,
    val endedAtEpochMillis: Long,
    val audioIncluded: Boolean,
    val environmentalSummary: String = "Not captured",
    val samples: List<OrientationSample>,
)

enum class ArSupportState {
    SUPPORTED_INSTALLED,
    SUPPORTED_NOT_INSTALLED,
    UNSUPPORTED,
    CHECKING,
    UNKNOWN,
}

data class ArCapability(
    val support: ArSupportState,
    val depthSupported: Boolean?,
    val detail: String,
)
