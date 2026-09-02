package za.co.dope.ballistics.data.training

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.core.content.edit
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import za.co.dope.ballistics.domain.training.OrientationMath
import za.co.dope.ballistics.domain.training.TrainingRecording
import java.io.File

class TrainingRecordingStore(
    private val context: Context,
) {
    private val preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    fun all(): List<TrainingRecording> =
        preferences.getString(RECORDINGS, null)?.let { encoded ->
            runCatching { json.decodeFromString<List<TrainingRecording>>(encoded) }.getOrNull()
        } ?: emptyList()

    fun latest(): TrainingRecording? = all().firstOrNull()

    fun save(recording: TrainingRecording) {
        val recordings = (listOf(recording) + all().filterNot { it.id == recording.id }).take(MAX_RECORDINGS)
        preferences.edit { putString(RECORDINGS, json.encodeToString(recordings)) }
    }

    fun exportSensorCsv(recording: TrainingRecording): Result<Uri> =
        runCatching {
            val filename = "DOPE-${recording.id}-sensors.csv"
            val uri =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val values =
                        ContentValues().apply {
                            put(MediaStore.Downloads.DISPLAY_NAME, filename)
                            put(MediaStore.Downloads.MIME_TYPE, "text/csv")
                            put(MediaStore.Downloads.RELATIVE_PATH, "Download/DOPE")
                        }
                    requireNotNull(context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values))
                } else {
                    val directory = File(context.filesDir, "exports").apply { mkdirs() }
                    val file = File(directory, filename)
                    FileProvider.getUriForFile(context, "${context.packageName}.files", file)
                }
            requireNotNull(context.contentResolver.openOutputStream(uri)).bufferedWriter().use {
                it.write(OrientationMath.toCsv(recording.samples))
            }
            uri
        }

    private companion object {
        const val PREFERENCES = "training_recordings"
        const val RECORDINGS = "recordings"
        const val MAX_RECORDINGS = 100
    }
}
