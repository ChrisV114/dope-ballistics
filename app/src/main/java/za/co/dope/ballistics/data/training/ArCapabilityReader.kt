@file:Suppress("MaxLineLength", "ReturnCount")

package za.co.dope.ballistics.data.training

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Config
import com.google.ar.core.Session
import za.co.dope.ballistics.domain.training.ArCapability
import za.co.dope.ballistics.domain.training.ArSupportState

class ArCapabilityReader(
    private val context: Context,
) {
    fun read(): ArCapability {
        val availability = ArCoreApk.getInstance().checkAvailability(context)
        if (availability.isTransient) {
            return ArCapability(ArSupportState.CHECKING, null, "ARCore capability check is still resolving")
        }
        if (availability.name.startsWith("UNKNOWN")) {
            return ArCapability(
                ArSupportState.UNKNOWN,
                null,
                "ARCore support could not be confirmed; sensor orientation and video remain available",
            )
        }
        if (!availability.isSupported) {
            return ArCapability(
                ArSupportState.UNSUPPORTED,
                false,
                "ARCore is unavailable; sensor orientation and video remain available",
            )
        }
        if (availability != ArCoreApk.Availability.SUPPORTED_INSTALLED) {
            return ArCapability(
                ArSupportState.SUPPORTED_NOT_INSTALLED,
                null,
                "Device supports ARCore; Google Play Services for AR is not installed or current",
            )
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            return ArCapability(
                ArSupportState.SUPPORTED_INSTALLED,
                null,
                "ARCore installed; allow camera to check optional Depth support",
            )
        }
        return runCatching {
            val session = Session(context)
            try {
                val depth = session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)
                ArCapability(
                    ArSupportState.SUPPORTED_INSTALLED,
                    depth,
                    if (depth) "ARCore and optional near-field Depth supported" else "ARCore supported; Depth unavailable",
                )
            } finally {
                session.close()
            }
        }.getOrElse {
            ArCapability(
                ArSupportState.SUPPORTED_INSTALLED,
                null,
                "ARCore installed; Depth check unavailable (${it.javaClass.simpleName})",
            )
        }
    }
}
