package za.co.dope.ballistics

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import za.co.dope.ballistics.data.environment.AndroidLocationGateway
import za.co.dope.ballistics.data.environment.AndroidSensorGateway
import za.co.dope.ballistics.domain.environment.EnvironmentalMath

@RunWith(AndroidJUnit4::class)
class EnvironmentalFallbackTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun manualEnvironmentCalculatesWithoutSensorsOrInternet() {
        val result = EnvironmentalMath.calculate(293.15, 90_000.0, 0.4)
        assertTrue(result.airDensityKilogramsPerCubicMetre > 0.0)
    }

    @Test
    fun optionalSensorDiagnosticsNeverRequireHardware() {
        val diagnostics = AndroidSensorGateway(context).capabilities()
        assertTrue(diagnostics.size == 7)
        assertTrue(diagnostics.all { it.kind.isNotBlank() })
    }

    @Test
    fun deniedLocationPermissionLeavesGatewayUnavailable() {
        val fineDenied =
            context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        val coarseDenied =
            context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        if (fineDenied && coarseDenied) assertFalse(AndroidLocationGateway(context).hasLocationPermission())
    }
}
