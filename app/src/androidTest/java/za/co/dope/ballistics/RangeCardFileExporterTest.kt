package za.co.dope.ballistics

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import za.co.dope.ballistics.data.RangeCardExportFormat
import za.co.dope.ballistics.data.RangeCardFileExporter
import za.co.dope.ballistics.domain.RangeCardDocument
import za.co.dope.ballistics.domain.RangeCardMetadata
import za.co.dope.ballistics.domain.RangeCardRow

@RunWith(AndroidJUnit4::class)
class RangeCardFileExporterTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun csvPdfAndPngExportOfflineAndShareThroughContentUri() {
        val exporter = RangeCardFileExporter(context)
        val document = document()

        val csv = exporter.export(document, RangeCardExportFormat.CSV, "test-card")
        val pdf = exporter.export(document, RangeCardExportFormat.PDF, "test-card")
        val png = exporter.export(document, RangeCardExportFormat.PNG, "test-card")

        assertTrue(csv.readText().contains("# App version,\"test\""))
        assertEquals("%PDF", pdf.inputStream().use { String(it.readNBytes(4), Charsets.US_ASCII) })
        assertNotNull(BitmapFactory.decodeFile(png.absolutePath))
        val share = exporter.shareIntent(pdf, RangeCardExportFormat.PDF)
        assertEquals("content", streamUri(share)?.scheme)
    }

    private fun document() =
        RangeCardDocument(
            metadata =
                RangeCardMetadata(
                    appVersion = "test",
                    engineVersion = "test-engine",
                    createdAtEpochMillis = 1L,
                    units = "MIL",
                    profileLabel = "Offline fixture",
                    windConvention = "Wind-from",
                ),
            rows =
                listOf(
                    RangeCardRow(
                        distanceMetres = 100.0,
                        elevationRaw = 0.1,
                        elevationDial = 0.1,
                        elevationClicks = 1,
                        environmentalDeviation = 0.0,
                        windSelected = 0.2,
                        windMinimum = 0.1,
                        windMaximum = 0.3,
                        timeOfFlightSeconds = 0.12,
                        remainingVelocityMps = 700.0,
                        remainingEnergyJoules = 2_000.0,
                        mach = 2.0,
                        flightState = "SUPERSONIC",
                        elevationUncertainty = 0.05,
                        warningState = "CLEAR",
                    ),
                ),
            issues = emptyList(),
        )

    @Suppress("DEPRECATION")
    private fun streamUri(intent: android.content.Intent): Uri? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(android.content.Intent.EXTRA_STREAM, Uri::class.java)
        } else {
            intent.getParcelableExtra(android.content.Intent.EXTRA_STREAM)
        }
}
