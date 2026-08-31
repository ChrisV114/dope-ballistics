package za.co.dope.ballistics.data.environment

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import kotlinx.coroutines.suspendCancellableCoroutine
import za.co.dope.ballistics.domain.environment.LocationReading
import kotlin.coroutines.resume

class AndroidLocationGateway(
    private val context: Context,
) : LocationGateway {
    private val manager = context.getSystemService(LocationManager::class.java)

    override fun hasLocationPermission(): Boolean =
        context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    @Suppress("ReturnCount", "DEPRECATION")
    override suspend fun currentLocation(): Result<LocationReading> {
        if (!hasLocationPermission()) return Result.failure(SecurityException("Location permission was not granted"))
        val provider =
            when {
                manager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true -> LocationManager.GPS_PROVIDER
                manager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true -> LocationManager.NETWORK_PROVIDER
                else -> return Result.failure(IllegalStateException("No location provider is enabled"))
            }
        return suspendCancellableCoroutine { continuation ->
            val listener =
                object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        manager.removeUpdates(this)
                        if (continuation.isActive) continuation.resume(Result.success(location.toReading()))
                    }

                    @Deprecated("Compatibility callback")
                    override fun onStatusChanged(
                        provider: String?,
                        status: Int,
                        extras: Bundle?,
                    ) = Unit
                }
            try {
                manager.requestSingleUpdate(provider, listener, context.mainLooper)
                continuation.invokeOnCancellation { manager.removeUpdates(listener) }
            } catch (error: SecurityException) {
                continuation.resume(Result.failure(error))
            }
        }
    }

    private fun Location.toReading(): LocationReading =
        LocationReading(
            latitudeDegrees = latitude,
            longitudeDegrees = longitude,
            horizontalAccuracyMetres = accuracy.toDouble(),
            altitudeMetres = if (hasAltitude()) altitude else null,
            verticalAccuracyMetres = if (hasVerticalAccuracy()) verticalAccuracyMeters.toDouble() else null,
            approximate = accuracy >= 100f,
            capturedAtEpochMillis = time,
        )
}
