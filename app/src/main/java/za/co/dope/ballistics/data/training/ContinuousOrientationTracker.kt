@file:Suppress("ReturnCount")

package za.co.dope.ballistics.data.training

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.SystemClock
import za.co.dope.ballistics.domain.ReadingQuality
import za.co.dope.ballistics.domain.training.OrientationMath
import za.co.dope.ballistics.domain.training.OrientationSample

class ContinuousOrientationTracker(
    context: Context,
) : SensorEventListener {
    private val manager = context.getSystemService(SensorManager::class.java)
    private val rotationVector = manager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val accelerometer = manager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magneticField = manager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    private var acceleration: FloatArray? = null
    private var magnetism: FloatArray? = null
    private var listener: ((OrientationSample) -> Unit)? = null
    private var startedElapsedRealtime = 0L
    private var accuracy = SensorManager.SENSOR_STATUS_UNRELIABLE
    private val recent = ArrayDeque<OrientationSample>()

    val sourceLabel: String
        get() = if (rotationVector != null) "Rotation vector" else "Accelerometer + magnetic field"

    fun available(): Boolean = rotationVector != null || (accelerometer != null && magneticField != null)

    fun start(onSample: (OrientationSample) -> Unit): Boolean {
        stop()
        listener = onSample
        startedElapsedRealtime = SystemClock.elapsedRealtime()
        val primary = rotationVector
        if (primary != null) return manager?.registerListener(this, primary, SensorManager.SENSOR_DELAY_GAME) == true
        val accelerationStarted =
            accelerometer?.let { manager?.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) } == true
        val magneticStarted =
            magneticField?.let { manager?.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) } == true
        if (!accelerationStarted || !magneticStarted) stop()
        return accelerationStarted && magneticStarted
    }

    fun stop() {
        manager?.unregisterListener(this)
        listener = null
        acceleration = null
        magnetism = null
        recent.clear()
    }

    override fun onSensorChanged(event: SensorEvent) {
        val rotation = FloatArray(9)
        when (event.sensor.type) {
            Sensor.TYPE_ROTATION_VECTOR -> {
                SensorManager.getRotationMatrixFromVector(rotation, event.values)
            }

            Sensor.TYPE_ACCELEROMETER -> {
                acceleration = event.values.copyOf()
                return emitFallback(rotation)
            }

            Sensor.TYPE_MAGNETIC_FIELD -> {
                magnetism = event.values.copyOf()
                return emitFallback(rotation)
            }

            else -> {
                return
            }
        }
        emit(rotation)
    }

    private fun emitFallback(rotation: FloatArray) {
        val gravity = acceleration ?: return
        val magnetic = magnetism ?: return
        if (SensorManager.getRotationMatrix(rotation, null, gravity, magnetic)) emit(rotation)
    }

    private fun emit(rotation: FloatArray) {
        val orientation = FloatArray(3)
        SensorManager.getOrientation(rotation, orientation)
        val provisional =
            OrientationSample(
                elapsedMillis = SystemClock.elapsedRealtime() - startedElapsedRealtime,
                capturedAtEpochMillis = System.currentTimeMillis(),
                magneticHeadingDegrees = OrientationMath.normaliseHeading(Math.toDegrees(orientation[0].toDouble())),
                pitchDegrees = Math.toDegrees(orientation[1].toDouble()),
                rollDegrees = Math.toDegrees(orientation[2].toDouble()),
                accuracy = accuracy.toQuality(),
                stable = false,
            )
        recent.addLast(provisional)
        while (recent.size > STABILITY_WINDOW) recent.removeFirst()
        listener?.invoke(
            provisional.copy(
                stable =
                    OrientationMath.isStable(
                        recent.map { it.magneticHeadingDegrees },
                        recent.map { it.pitchDegrees },
                        recent.map { it.rollDegrees },
                    ),
            ),
        )
    }

    override fun onAccuracyChanged(
        sensor: Sensor?,
        value: Int,
    ) {
        accuracy = value
    }

    private fun Int.toQuality(): ReadingQuality =
        when (this) {
            SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> ReadingQuality.EXCELLENT
            SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> ReadingQuality.GOOD
            SensorManager.SENSOR_STATUS_ACCURACY_LOW -> ReadingQuality.POOR
            else -> ReadingQuality.UNAVAILABLE
        }

    private companion object {
        const val STABILITY_WINDOW = 12
    }
}
