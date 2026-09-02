package za.co.dope.ballistics

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import za.co.dope.ballistics.data.training.ArCapabilityReader
import za.co.dope.ballistics.data.training.ContinuousOrientationTracker

@RunWith(AndroidJUnit4::class)
class TrainingFallbackTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun capabilityChecksNeverRequireArcoreOrSensors() {
        assertNotNull(ArCapabilityReader(context).read())
        val tracker = ContinuousOrientationTracker(context)
        assertNotNull(tracker.sourceLabel)
        tracker.stop()
    }

    @Test
    fun microphoneIsNotAutomaticallyGranted() {
        assertTrue(context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
    }
}
