package za.co.dope.ballistics.data.camera

import kotlin.math.abs

data class CameraCapability(
    val cameraId: String,
    val lensFacing: String,
    val focalLengthsMillimetres: List<Float>,
    val sensorWidthMillimetres: Float?,
    val sensorHeightMillimetres: Float?,
    val activeWidthPixels: Int?,
    val activeHeightPixels: Int?,
    val jpegSizes: List<String>,
    val zoomRatioRange: String?,
    val opticalStabilisation: Boolean,
    val distortionMetadata: Boolean,
    val physicalCameraIds: Set<String>,
) {
    val displayName: String
        get() = "Camera $cameraId · ${focalLengthsMillimetres.joinToString { "$it mm" }}"
}

data class CameraConfiguration(
    val manufacturer: String,
    val model: String,
    val cameraId: String,
    val focalLengthMillimetres: Float,
    val resolutionWidth: Int,
    val resolutionHeight: Int,
    val zoomRatio: Float,
) {
    val fingerprint: String
        get() =
            listOf(
                manufacturer,
                model,
                cameraId,
                focalLengthMillimetres,
                resolutionWidth,
                resolutionHeight,
                zoomRatio,
            ).joinToString("|")
}

data class CameraFrameMetadata(
    val focalLengthMillimetres: Float?,
    val zoomRatio: Float,
    val cropRegion: String?,
)

data class CalibrationSample(
    val knownSizeMillimetres: Double,
    val measuredDistanceMetres: Double,
    val pixelSpan: Double,
) {
    val effectiveFocalLengthPixels: Double
        get() = pixelSpan * measuredDistanceMetres / (knownSizeMillimetres / 1000.0)
}

data class CameraCalibrationProfile(
    val configuration: CameraConfiguration,
    val samples: List<CalibrationSample>,
    val effectiveFocalLengthPixels: Double,
    val meanAbsoluteErrorMetres: Double,
    val medianPercentageError: Double,
    val percentile95ErrorMetres: Double,
    val validFromMetres: Double,
    val validToMetres: Double,
    val savedAtEpochMillis: Long,
    val appVersion: String,
) {
    fun warningFor(current: CameraConfiguration): String? =
        when {
            current.cameraId != configuration.cameraId -> "Wrong camera lens for saved calibration"

            current.resolutionWidth != configuration.resolutionWidth ||
                current.resolutionHeight != configuration.resolutionHeight -> "Wrong resolution for saved calibration"

            current.zoomRatio != configuration.zoomRatio -> "Zoom changed; calibration is not valid"

            current.fingerprint != configuration.fingerprint -> "Camera configuration changed"

            else -> null
        }
}

object CameraCalibrationMath {
    fun fit(
        configuration: CameraConfiguration,
        samples: List<CalibrationSample>,
        savedAtEpochMillis: Long,
        appVersion: String,
    ): CameraCalibrationProfile {
        require(samples.size >= 2) { "Capture at least two calibration samples" }
        require(samples.all { it.knownSizeMillimetres > 0 && it.measuredDistanceMetres > 0 && it.pixelSpan > 0 }) {
            "Calibration values must be positive"
        }
        val focalPixels = samples.map(CalibrationSample::effectiveFocalLengthPixels).average()
        val errors =
            samples.map { sample ->
                val predicted = focalPixels * (sample.knownSizeMillimetres / 1000.0) / sample.pixelSpan
                abs(predicted - sample.measuredDistanceMetres)
            }
        val percentageErrors =
            errors.zip(samples).map { (error, sample) -> error / sample.measuredDistanceMetres * 100.0 }.sorted()
        return CameraCalibrationProfile(
            configuration = configuration,
            samples = samples,
            effectiveFocalLengthPixels = focalPixels,
            meanAbsoluteErrorMetres = errors.average(),
            medianPercentageError = percentile(percentageErrors, 0.5),
            percentile95ErrorMetres = percentile(errors.sorted(), 0.95),
            validFromMetres = samples.minOf(CalibrationSample::measuredDistanceMetres),
            validToMetres = samples.maxOf(CalibrationSample::measuredDistanceMetres),
            savedAtEpochMillis = savedAtEpochMillis,
            appVersion = appVersion,
        )
    }

    private fun percentile(
        sorted: List<Double>,
        fraction: Double,
    ): Double {
        if (sorted.size == 1) return sorted.first()
        val position = (sorted.lastIndex * fraction).coerceIn(0.0, sorted.lastIndex.toDouble())
        val lower = position.toInt()
        val upper = (lower + 1).coerceAtMost(sorted.lastIndex)
        return sorted[lower] + (sorted[upper] - sorted[lower]) * (position - lower)
    }
}
