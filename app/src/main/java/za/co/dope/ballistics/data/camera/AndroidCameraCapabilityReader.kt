package za.co.dope.ballistics.data.camera

import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.os.Build

class AndroidCameraCapabilityReader(
    context: Context,
) {
    private val manager = context.getSystemService(CameraManager::class.java)

    fun readRearCameras(): List<CameraCapability> =
        manager.cameraIdList.mapNotNull { cameraId ->
            val characteristics = manager.getCameraCharacteristics(cameraId)
            val facing = characteristics[CameraCharacteristics.LENS_FACING]
            if (facing != CameraMetadata.LENS_FACING_BACK) return@mapNotNull null
            val physicalIds = characteristics.physicalCameraIds
            val active = characteristics[CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE]
            val physical = characteristics[CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE]
            val sizes =
                characteristics[CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP]
                    ?.getOutputSizes(ImageFormat.JPEG)
                    ?.sortedByDescending { it.width.toLong() * it.height }
                    ?.take(MAX_REPORTED_SIZES)
                    ?.map { "${it.width}×${it.height}" }
                    .orEmpty()
            val zoom =
                if (Build.VERSION.SDK_INT >= 30) {
                    characteristics[CameraCharacteristics.CONTROL_ZOOM_RATIO_RANGE]?.let { "${it.lower}–${it.upper}×" }
                } else {
                    null
                }
            CameraCapability(
                cameraId = cameraId,
                lensFacing = "Rear",
                focalLengthsMillimetres =
                    characteristics[CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS]?.toList().orEmpty(),
                sensorWidthMillimetres = physical?.width,
                sensorHeightMillimetres = physical?.height,
                activeWidthPixels = active?.width(),
                activeHeightPixels = active?.height(),
                jpegSizes = sizes,
                zoomRatioRange = zoom,
                opticalStabilisation =
                    characteristics[CameraCharacteristics.LENS_INFO_AVAILABLE_OPTICAL_STABILIZATION]
                        ?.any { it == CameraMetadata.LENS_OPTICAL_STABILIZATION_MODE_ON } == true,
                distortionMetadata = characteristics[CameraCharacteristics.LENS_DISTORTION] != null,
                physicalCameraIds = physicalIds,
            )
        }

    private companion object {
        const val MAX_REPORTED_SIZES = 8
    }
}
