package za.co.dope.ballistics.data.camera

import android.content.Context
import androidx.core.content.edit
import org.json.JSONArray
import org.json.JSONObject

class CameraCalibrationStore(
    context: Context,
) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun save(profile: CameraCalibrationProfile) {
        preferences.edit { putString(KEY_PROFILE, profile.toJson().toString()) }
    }

    fun load(): CameraCalibrationProfile? =
        preferences.getString(KEY_PROFILE, null)?.let { encoded ->
            runCatching { JSONObject(encoded).toProfile() }.getOrNull()
        }

    private fun CameraCalibrationProfile.toJson(): JSONObject =
        JSONObject()
            .put("manufacturer", configuration.manufacturer)
            .put("model", configuration.model)
            .put("cameraId", configuration.cameraId)
            .put("focalLengthMillimetres", configuration.focalLengthMillimetres.toDouble())
            .put("resolutionWidth", configuration.resolutionWidth)
            .put("resolutionHeight", configuration.resolutionHeight)
            .put("zoomRatio", configuration.zoomRatio.toDouble())
            .put("effectiveFocalLengthPixels", effectiveFocalLengthPixels)
            .put("meanAbsoluteErrorMetres", meanAbsoluteErrorMetres)
            .put("medianPercentageError", medianPercentageError)
            .put("percentile95ErrorMetres", percentile95ErrorMetres)
            .put("validFromMetres", validFromMetres)
            .put("validToMetres", validToMetres)
            .put("savedAtEpochMillis", savedAtEpochMillis)
            .put("appVersion", appVersion)
            .put(
                "samples",
                JSONArray().apply {
                    samples.forEach { sample ->
                        put(
                            JSONObject()
                                .put("knownSizeMillimetres", sample.knownSizeMillimetres)
                                .put("measuredDistanceMetres", sample.measuredDistanceMetres)
                                .put("pixelSpan", sample.pixelSpan),
                        )
                    }
                },
            )

    private fun JSONObject.toProfile(): CameraCalibrationProfile {
        val configuration =
            CameraConfiguration(
                manufacturer = getString("manufacturer"),
                model = getString("model"),
                cameraId = getString("cameraId"),
                focalLengthMillimetres = getDouble("focalLengthMillimetres").toFloat(),
                resolutionWidth = getInt("resolutionWidth"),
                resolutionHeight = getInt("resolutionHeight"),
                zoomRatio = getDouble("zoomRatio").toFloat(),
            )
        val encodedSamples = getJSONArray("samples")
        val samples =
            buildList {
                repeat(encodedSamples.length()) { index ->
                    val sample = encodedSamples.getJSONObject(index)
                    add(
                        CalibrationSample(
                            knownSizeMillimetres = sample.getDouble("knownSizeMillimetres"),
                            measuredDistanceMetres = sample.getDouble("measuredDistanceMetres"),
                            pixelSpan = sample.getDouble("pixelSpan"),
                        ),
                    )
                }
            }
        return CameraCalibrationProfile(
            configuration = configuration,
            samples = samples,
            effectiveFocalLengthPixels = getDouble("effectiveFocalLengthPixels"),
            meanAbsoluteErrorMetres = getDouble("meanAbsoluteErrorMetres"),
            medianPercentageError = getDouble("medianPercentageError"),
            percentile95ErrorMetres = getDouble("percentile95ErrorMetres"),
            validFromMetres = getDouble("validFromMetres"),
            validToMetres = getDouble("validToMetres"),
            savedAtEpochMillis = getLong("savedAtEpochMillis"),
            appVersion = getString("appVersion"),
        )
    }

    private companion object {
        const val PREFERENCES_NAME = "camera_calibration"
        const val KEY_PROFILE = "active_profile"
    }
}
