package za.co.dope.ballistics.data.environment

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
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
        val locationManager = manager ?: return Result.failure(IllegalStateException("Location service is unavailable"))
        val providers =
            listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER).filter {
                locationManager.isProviderEnabled(it)
            }
        if (providers.isEmpty()) return Result.failure(IllegalStateException("Turn on phone location and try again"))
        return runCatching {
            val fresh =
                withTimeoutOrNull(LOCATION_TIMEOUT_MILLIS) {
                    awaitSingleUpdate(locationManager, providers)
                }
            if (fresh != null) return@runCatching fresh.toReading(cachedFallback = false)
            val recentFallback =
                providers
                    .mapNotNull(locationManager::getLastKnownLocation)
                    .filter { System.currentTimeMillis() - it.time <= MAXIMUM_FALLBACK_AGE_MILLIS }
                    .maxByOrNull { it.time }
            requireNotNull(recentFallback) {
                "Location timed out. Move near a window, confirm phone Location is on, then retry."
            }.toReading(cachedFallback = true)
        }
    }

    private suspend fun awaitSingleUpdate(
        locationManager: LocationManager,
        providers: List<String>,
    ): Location? =
        suspendCancellableCoroutine { continuation ->
            val listener =
                object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        locationManager.removeUpdates(this)
                        if (continuation.isActive) continuation.resume(location)
                    }

                    @Deprecated("Compatibility callback")
                    override fun onStatusChanged(
                        provider: String?,
                        status: Int,
                        extras: Bundle?,
                    ) = Unit
                }
            try {
                providers.forEach { provider ->
                    locationManager.requestLocationUpdates(provider, 0L, 0f, listener, context.mainLooper)
                }
                continuation.invokeOnCancellation { locationManager.removeUpdates(listener) }
            } catch (error: SecurityException) {
                continuation.cancel(error)
            }
        }

    private fun Location.toReading(cachedFallback: Boolean): LocationReading =
        LocationReading(
            latitudeDegrees = latitude,
            longitudeDegrees = longitude,
            horizontalAccuracyMetres = accuracy.toDouble(),
            altitudeMetres = if (hasAltitude()) altitude else null,
            verticalAccuracyMetres = if (hasVerticalAccuracy()) verticalAccuracyMeters.toDouble() else null,
            approximate = accuracy >= 100f,
            capturedAtEpochMillis = time,
            cachedFallback = cachedFallback,
        )

    private companion object {
        const val LOCATION_TIMEOUT_MILLIS = 12_000L
        const val MAXIMUM_FALLBACK_AGE_MILLIS = 15 * 60 * 1000L
    }
}
