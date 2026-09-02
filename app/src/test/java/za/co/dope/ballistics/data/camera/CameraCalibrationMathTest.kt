package za.co.dope.ballistics.data.camera

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class CameraCalibrationMathTest {
    private val configuration =
        CameraConfiguration(
            manufacturer = "Samsung",
            model = "Galaxy S25",
            cameraId = "0",
            focalLengthMillimetres = 5.4f,
            resolutionWidth = 4000,
            resolutionHeight = 3000,
            zoomRatio = 1f,
        )

    @Test
    fun fitReportsStableCalibrationAcrossMultipleDistances() {
        val profile =
            CameraCalibrationMath.fit(
                configuration,
                listOf(
                    CalibrationSample(500.0, 25.0, 80.0),
                    CalibrationSample(500.0, 50.0, 40.0),
                    CalibrationSample(500.0, 100.0, 20.0),
                ),
                savedAtEpochMillis = 1L,
                appVersion = "test",
            )

        assertEquals(4000.0, profile.effectiveFocalLengthPixels, 0.001)
        assertEquals(0.0, profile.meanAbsoluteErrorMetres, 0.001)
        assertEquals(25.0, profile.validFromMetres, 0.001)
        assertEquals(100.0, profile.validToMetres, 0.001)
        assertNull(profile.warningFor(configuration))
    }

    @Test
    fun changedLensResolutionAndZoomWarn() {
        val profile = profile()

        assertTrue(profile.warningFor(configuration.copy(cameraId = "2"))!!.contains("lens"))
        assertTrue(profile.warningFor(configuration.copy(resolutionWidth = 1920))!!.contains("resolution"))
        assertTrue(profile.warningFor(configuration.copy(zoomRatio = 2f))!!.contains("Zoom"))
    }

    @Test
    fun fitRejectsInsufficientOrInvalidSamples() {
        assertThrows(IllegalArgumentException::class.java) {
            CameraCalibrationMath.fit(
                configuration,
                listOf(CalibrationSample(500.0, 25.0, 80.0)),
                1L,
                "test",
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            CameraCalibrationMath.fit(
                configuration,
                listOf(CalibrationSample(0.0, 25.0, 80.0), CalibrationSample(500.0, 50.0, 40.0)),
                1L,
                "test",
            )
        }
    }

    private fun profile() =
        CameraCalibrationMath.fit(
            configuration,
            listOf(CalibrationSample(500.0, 25.0, 80.0), CalibrationSample(500.0, 50.0, 40.0)),
            1L,
            "test",
        )
}
