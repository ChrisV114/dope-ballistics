package za.co.dope.ballistics.data.environment

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.suspendCancellableCoroutine
import za.co.dope.ballistics.domain.ReadingQuality
import za.co.dope.ballistics.domain.environment.OrientationReading
import za.co.dope.ballistics.domain.environment.PressureSampleSummary
import za.co.dope.ballistics.domain.environment.PressureStatistics
import za.co.dope.ballistics.domain.environment.SensorCapability
import kotlin.coroutines.resume
import kotlin.math.abs

class AndroidSensorGateway(
    context: Context,
) : SensorGateway {
    private val manager = context.getSystemService(SensorManager::class.java)

    override fun capabilities(): List<SensorCapability> =
        SUPPORTED_SENSORS.map { (kind, type) ->
            val sensor = manager?.getDefaultSensor(type)
            SensorCapability(
                kind = kind,
                available = sensor != null,
                name = sensor?.name,
                vendor = sensor?.vendor,
                version = sensor?.version,
                resolution = sensor?.resolution,
                maximumRange = sensor?.maximumRange,
                reportingMode = sensor?.reportingMode,
            )
        }

    override suspend fun samplePressure(durationMillis: Long): Result<PressureSampleSummary> {
        val sensor =
            manager?.getDefaultSensor(Sensor.TYPE_PRESSURE)
                ?: return Result.failure(UnsupportedOperationException("This device has no barometer"))
        return suspendCancellableCoroutine { continuation ->
            val samples = mutableListOf<Float>()
            val started = System.currentTimeMillis()
            lateinit var listener: SensorEventListener
            listener =
                object : SensorEventListener {
                    override fun onSensorChanged(event: SensorEvent) {
                        samples += event.values[0]
                        val elapsed = System.currentTimeMillis() - started
                        if (elapsed >= durationMillis && continuation.isActive) {
                            manager.unregisterListener(this)
                            continuation.resume(runCatching { PressureStatistics.summarise(samples, elapsed) })
                        }
                    }

                    override fun onAccuracyChanged(
                        sensor: Sensor?,
                        accuracy: Int,
                    ) = Unit
                }
            if (!manager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL)) {
                continuation.resume(Result.failure(IllegalStateException("Barometer could not be started")))
            }
            continuation.invokeOnCancellation { manager.unregisterListener(listener) }
        }
    }

    override suspend fun sampleOrientation(durationMillis: Long): Result<OrientationReading> {
        val sensor =
            manager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
                ?: return Result.failure(UnsupportedOperationException("This device has no rotation-vector sensor"))
        return suspendCancellableCoroutine { continuation ->
            val headings = mutableListOf<Double>()
            var lastPitch = 0.0
            var lastRoll = 0.0
            var accuracy = SensorManager.SENSOR_STATUS_UNRELIABLE
            val started = System.currentTimeMillis()
            lateinit var listener: SensorEventListener
            listener =
                object : SensorEventListener {
                    override fun onSensorChanged(event: SensorEvent) {
                        val rotation = FloatArray(9)
                        val orientation = FloatArray(3)
                        SensorManager.getRotationMatrixFromVector(rotation, event.values)
                        SensorManager.getOrientation(rotation, orientation)
                        headings += Math.toDegrees(orientation[0].toDouble()).let { if (it < 0) it + 360.0 else it }
                        lastPitch = Math.toDegrees(orientation[1].toDouble())
                        lastRoll = Math.toDegrees(orientation[2].toDouble())
                        val elapsed = System.currentTimeMillis() - started
                        if (elapsed >= durationMillis && continuation.isActive) {
                            manager.unregisterListener(this)
                            val range = (headings.maxOrNull() ?: 0.0) - (headings.minOrNull() ?: 0.0)
                            continuation.resume(
                                Result.success(
                                    OrientationReading(
                                        magneticHeadingDegrees = headings.average(),
                                        trueHeadingDegrees = null,
                                        pitchDegrees = lastPitch,
                                        rollDegrees = lastRoll,
                                        accuracy = accuracy.toQuality(),
                                        stable = abs(range) <= 3.0,
                                        capturedAtEpochMillis = System.currentTimeMillis(),
                                    ),
                                ),
                            )
                        }
                    }

                    override fun onAccuracyChanged(
                        sensor: Sensor?,
                        newAccuracy: Int,
                    ) {
                        accuracy = newAccuracy
                    }
                }
            if (!manager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME)) {
                continuation.resume(Result.failure(IllegalStateException("Orientation sensor could not be started")))
            }
            continuation.invokeOnCancellation { manager.unregisterListener(listener) }
        }
    }

    private fun Int.toQuality(): ReadingQuality =
        when (this) {
            SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> ReadingQuality.EXCELLENT
            SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> ReadingQuality.GOOD
            SensorManager.SENSOR_STATUS_ACCURACY_LOW -> ReadingQuality.POOR
            else -> ReadingQuality.UNAVAILABLE
        }

    private companion object {
        val SUPPORTED_SENSORS =
            listOf(
                "Pressure" to Sensor.TYPE_PRESSURE,
                "Ambient temperature" to Sensor.TYPE_AMBIENT_TEMPERATURE,
                "Relative humidity" to Sensor.TYPE_RELATIVE_HUMIDITY,
                "Rotation vector" to Sensor.TYPE_ROTATION_VECTOR,
                "Accelerometer" to Sensor.TYPE_ACCELEROMETER,
                "Magnetic field" to Sensor.TYPE_MAGNETIC_FIELD,
                "Gyroscope" to Sensor.TYPE_GYROSCOPE,
            )
    }
}
