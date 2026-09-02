@file:Suppress("CyclomaticComplexMethod", "LongMethod", "LongParameterList", "MaxLineLength", "TooManyFunctions")

package za.co.dope.ballistics.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import android.provider.MediaStore
import android.widget.VideoView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.OutputOptions
import androidx.camera.video.PendingRecording
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.ar.core.ArCoreApk
import kotlinx.coroutines.delay
import za.co.dope.ballistics.data.ProfileRepository
import za.co.dope.ballistics.data.training.ArCapabilityReader
import za.co.dope.ballistics.data.training.ContinuousOrientationTracker
import za.co.dope.ballistics.data.training.TrainingRecordingStore
import za.co.dope.ballistics.domain.ReadingQuality
import za.co.dope.ballistics.domain.training.ArCapability
import za.co.dope.ballistics.domain.training.ArSupportState
import za.co.dope.ballistics.domain.training.OrientationMath
import za.co.dope.ballistics.domain.training.OrientationSample
import za.co.dope.ballistics.domain.training.TrainingRecording
import za.co.dope.ballistics.ui.components.DopeCard
import za.co.dope.ballistics.ui.components.DopeField
import za.co.dope.ballistics.ui.components.DopePrimaryButton
import za.co.dope.ballistics.ui.components.DopeSecondaryButton
import za.co.dope.ballistics.ui.components.DopeStatus
import za.co.dope.ballistics.ui.components.LabelValue
import za.co.dope.ballistics.ui.components.StatusChip
import za.co.dope.ballistics.ui.components.TopographicBackground
import za.co.dope.ballistics.ui.theme.DopeDesignTokens
import za.co.dope.ballistics.ui.theme.LocalDopeColors
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun TrainingVideoScreen(
    profileRepository: ProfileRepository? = null,
    onClose: () -> Unit,
    previewMode: Boolean = false,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val tracker = remember { ContinuousOrientationTracker(context) }
    val store = remember { TrainingRecordingStore(context) }
    var cameraPermission by remember { mutableStateOf(previewMode || context.hasPermission(Manifest.permission.CAMERA)) }
    var audioPermission by remember { mutableStateOf(context.hasPermission(Manifest.permission.RECORD_AUDIO)) }
    var audioRequested by remember { mutableStateOf(false) }
    var sessionName by
        remember {
            mutableStateOf(if (previewMode) "Practice orientation review" else "Training ${System.currentTimeMillis()}")
        }
    var sample by remember { mutableStateOf(if (previewMode) PreviewSample else null) }
    val recordedSamples = remember { mutableStateListOf<OrientationSample>() }
    var videoCapture by remember { mutableStateOf<VideoCapture<Recorder>?>(null) }
    var recording by remember { mutableStateOf<Recording?>(null) }
    var recordingStarted by remember { mutableLongStateOf(0L) }
    var pendingAudioStart by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var latest by remember { mutableStateOf(if (previewMode) null else store.latest()) }
    var recordings by remember { mutableStateOf(if (previewMode) emptyList() else store.all()) }
    var arCapability by remember { mutableStateOf(PreviewCapability.takeIf { previewMode } ?: CheckingCapability) }
    val environments by profileRepository?.observeEnvironmentalSnapshots()?.collectAsState(emptyList()) ?: remember {
        mutableStateOf(emptyList())
    }
    val environmentalSummary =
        environments.firstOrNull()?.let {
            "${(it.temperatureKelvin - 273.15).round(1)} °C · ${(it.stationPressurePascals / 100.0).round(0)} hPa · " +
                "${(it.relativeHumidityFraction * 100.0).round(0)}% RH · ${it.altitudeMetres.round(0)} m"
        } ?: "No saved environmental snapshot"

    val cameraLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted -> cameraPermission = granted }
    val audioLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            audioPermission = granted
            if (granted && pendingAudioStart) {
                pendingAudioStart = false
                startRecording(
                    context,
                    requireNotNull(videoCapture),
                    sessionName,
                    true,
                    environmentalSummary,
                    recordedSamples,
                    onStarted = { handle, started ->
                        recording = handle
                        recordingStarted = started
                    },
                    onFinished = { result ->
                        recording = null
                        result
                            .onSuccess { saved ->
                                store.save(saved)
                                latest = saved
                                recordings = store.all()
                                message = "Recording saved locally"
                            }.onFailure { message = it.message ?: "Recording failed" }
                    },
                )
            } else if (!granted) {
                pendingAudioStart = false
                message = "Microphone denied · turn audio off to record video only"
            }
        }

    DisposableEffect(Unit) {
        if (!previewMode) {
            tracker.start { reading ->
                sample = reading
                if (recording != null) {
                    recordedSamples += reading.copy(elapsedMillis = SystemClock.elapsedRealtime() - recordingStarted)
                }
            }
        }
        onDispose {
            recording?.stop()
            tracker.stop()
        }
    }
    LaunchedEffect(cameraPermission) {
        arCapability = if (previewMode) PreviewCapability else ArCapabilityReader(context).read()
    }

    TopographicBackground(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .verticalScroll(rememberScrollState())
                    .padding(DopeDesignTokens.Spacing.ScreenHorizontal),
            verticalArrangement = Arrangement.spacedBy(DopeDesignTokens.Spacing.Control),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "TRAINING · INFORMATIONAL",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                    Text("Orientation & video", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                DopeSecondaryButton("Close", onClose)
            }
            ArCapabilityPanel(
                capability = arCapability,
                onInstallAr = {
                    val activity = context.findActivity()
                    if (activity == null) {
                        message = "ARCore installation could not be opened"
                    } else {
                        runCatching { ArCoreApk.getInstance().requestInstall(activity, true) }
                            .onSuccess { status ->
                                message =
                                    if (status == ArCoreApk.InstallStatus.INSTALLED) {
                                        arCapability = ArCapabilityReader(context).read()
                                        "ARCore is installed"
                                    } else {
                                        "Complete the Google Play Services for AR installation, then return here"
                                    }
                            }.onFailure { message = it.message ?: "ARCore installation was declined or unavailable" }
                    }
                },
            )
            DopeField("Session name", sessionName, { sessionName = it })
            LabelValue("Environmental snapshot", environmentalSummary)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = audioRequested, onCheckedChange = { audioRequested = it })
                Text("Record microphone audio (optional)")
            }
            if (!cameraPermission) {
                DopeCard {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Camera permission is requested only here, when you choose to record.")
                        DopePrimaryButton("Allow camera", { cameraLauncher.launch(Manifest.permission.CAMERA) }, Modifier.fillMaxWidth())
                    }
                }
            } else {
                OrientationCameraPanel(
                    previewMode = previewMode,
                    sample = sample,
                    recording = recording != null,
                    onCaptureReady = { videoCapture = it },
                    lifecycleOwner = lifecycleOwner,
                )
                OrientationValues(sample, tracker.sourceLabel, tracker.available())
                DopePrimaryButton(
                    label = if (recording == null) "Start training recording" else "Stop and save recording",
                    onClick = {
                        if (recording != null) {
                            recording?.stop()
                        } else if (sessionName.isBlank()) {
                            message = "Enter a session name"
                        } else if (previewMode) {
                            message = "Recording is disabled in preview mode"
                        } else if (audioRequested && !audioPermission) {
                            pendingAudioStart = true
                            audioLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        } else {
                            startRecording(
                                context,
                                requireNotNull(videoCapture),
                                sessionName,
                                audioRequested,
                                environmentalSummary,
                                recordedSamples,
                                onStarted = { handle, started ->
                                    recording = handle
                                    recordingStarted = started
                                },
                                onFinished = { result ->
                                    recording = null
                                    result
                                        .onSuccess { saved ->
                                            store.save(saved)
                                            latest = saved
                                            recordings = store.all()
                                            message = "Recording saved locally"
                                        }.onFailure { message = it.message ?: "Recording failed" }
                                },
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = recording != null || videoCapture != null,
                )
            }
            message?.let { StatusChip(it, if (it.contains("saved")) DopeStatus.READY else DopeStatus.WARNING) }
            latest?.let { saved -> RecordingPlayback(saved, store) { message = it } }
            if (recordings.size > 1) {
                DopeCard {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Saved training sessions", style = MaterialTheme.typography.titleMedium)
                        recordings.take(8).forEach { saved ->
                            DopeSecondaryButton(
                                label = saved.sessionName,
                                onClick = { latest = saved },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }
            DopeCard {
                Text(
                    "The horizon, heading and angles are orientation references only. No corrected point of aim, " +
                        "impact marker, target tracking, lead indicator or live ballistic reticle is displayed.",
                    color = LocalDopeColors.current.textMuted,
                )
            }
        }
    }
}

@Composable
private fun OrientationCameraPanel(
    previewMode: Boolean,
    sample: OrientationSample?,
    recording: Boolean,
    onCaptureReady: (VideoCapture<Recorder>) -> Unit,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
) {
    val context = LocalContext.current
    Box(modifier = Modifier.fillMaxWidth().aspectRatio(9f / 16f).background(Color.Black)) {
        if (previewMode) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("CAMERA PREVIEW", color = Color.White) }
        } else {
            AndroidView(
                factory = { current ->
                    PreviewView(current).also { view ->
                        val providerFuture = ProcessCameraProvider.getInstance(current)
                        providerFuture.addListener(
                            {
                                val provider = providerFuture.get()
                                val preview = Preview.Builder().build().also { it.surfaceProvider = view.surfaceProvider }
                                val recorder =
                                    Recorder
                                        .Builder()
                                        .setQualitySelector(
                                            QualitySelector.from(
                                                Quality.FHD,
                                                androidx.camera.video.FallbackStrategy
                                                    .lowerQualityOrHigherThan(Quality.SD),
                                            ),
                                        ).build()
                                val capture = VideoCapture.withOutput(recorder)
                                provider.unbindAll()
                                provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, capture)
                                onCaptureReady(capture)
                            },
                            ContextCompat.getMainExecutor(current),
                        )
                    }
                },
                modifier = Modifier.fillMaxSize(),
            )
        }
        sample?.let { HorizonOverlay(it, recording) }
    }
}

@Composable
private fun HorizonOverlay(
    sample: OrientationSample,
    recording: Boolean,
) {
    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val angle = Math.toRadians(sample.rollDegrees)
            val halfLength = size.width * 0.34f
            val centre = Offset(size.width / 2f, (size.height / 2f + sample.pitchDegrees.toFloat() * 3f).coerceIn(0f, size.height))
            val delta = Offset((cos(angle) * halfLength).toFloat(), (sin(angle) * halfLength).toFloat())
            drawLine(Color.Cyan, centre - delta, centre + delta, strokeWidth = 4f)
        }
        Text(
            "${sample.capturedAtEpochMillis.timeLabel()} · ${sample.magneticHeadingDegrees.round(0)}° M · " +
                sample.accuracy.name.lowercase() + if (recording) " · REC" else "",
            color = if (recording) Color.Red else Color.White,
            modifier = Modifier.align(Alignment.TopCenter).background(Color.Black.copy(alpha = 0.55f)).padding(8.dp),
        )
        Text(
            "P ${sample.pitchDegrees.round(1)}°  R ${sample.rollDegrees.round(1)}°  ${if (sample.stable) "STABLE" else "MOVING"}",
            color = Color.White,
            modifier = Modifier.align(Alignment.BottomCenter).background(Color.Black.copy(alpha = 0.55f)).padding(8.dp),
        )
    }
}

@Composable
private fun OrientationValues(
    sample: OrientationSample?,
    source: String,
    available: Boolean,
) {
    DopeCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            LabelValue("Orientation source", if (available) source else "Unavailable")
            LabelValue("Heading", sample?.let { "${it.magneticHeadingDegrees.round(1)}° magnetic" } ?: "Waiting")
            LabelValue("Inclination", sample?.let { "${it.pitchDegrees.round(1)}°" } ?: "Waiting")
            LabelValue("Roll", sample?.let { "${it.rollDegrees.round(1)}°" } ?: "Waiting")
            StatusChip(
                label = if (sample?.stable == true) "Stable reading" else "Hold phone steady",
                status = if (sample?.stable == true) DopeStatus.READY else DopeStatus.WARNING,
            )
            if (sample?.accuracy == ReadingQuality.POOR || sample?.accuracy == ReadingQuality.UNAVAILABLE) {
                Text("Sensor confidence is low. Move away from metal and calibrate the compass before relying on heading.")
            }
        }
    }
}

@Composable
private fun ArCapabilityPanel(
    capability: ArCapability,
    onInstallAr: () -> Unit,
) {
    DopeCard {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("AR capability", style = MaterialTheme.typography.titleMedium)
            LabelValue("ARCore", capability.support.display())
            LabelValue("Optional Depth", capability.depthSupported?.let { if (it) "Supported" else "Unavailable" } ?: "Not checked")
            LabelValue("Depth confidence", "Capability only · no live depth sample")
            LabelValue("Best documented accuracy", "About 0.5–5 m · scene and motion dependent")
            Text(capability.detail, color = LocalDopeColors.current.textMuted)
            Text(
                "Depth is near-field context only and is never used as a long-range rangefinder.",
                color = LocalDopeColors.current.textMuted,
            )
            if (capability.support == ArSupportState.SUPPORTED_NOT_INSTALLED) {
                DopeSecondaryButton("Install / update ARCore", onInstallAr, Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun RecordingPlayback(
    recording: TrainingRecording,
    store: TrainingRecordingStore,
    onMessage: (String) -> Unit,
) {
    val context = LocalContext.current
    var videoView by remember { mutableStateOf<VideoView?>(null) }
    var position by remember { mutableLongStateOf(0L) }
    LaunchedEffect(videoView) {
        while (true) {
            position = videoView?.currentPosition?.toLong() ?: 0L
            delay(100)
        }
    }
    val synchronized = OrientationMath.nearestSample(recording.samples, position)
    DopeCard {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Latest recording", style = MaterialTheme.typography.titleMedium)
            Text(recording.sessionName)
            Text(recording.environmentalSummary, color = LocalDopeColors.current.textMuted)
            AndroidView(
                factory = { current ->
                    VideoView(current).also {
                        it.setVideoURI(recording.videoUri.toUri())
                        videoView = it
                    }
                },
                modifier = Modifier.fillMaxWidth().aspectRatio(9f / 16f).background(Color.Black),
            )
            synchronized?.let { HorizonPlaybackValues(it) }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DopeSecondaryButton(
                    "Play / pause",
                    { videoView?.let { if (it.isPlaying) it.pause() else it.start() } },
                    Modifier.weight(1f),
                )
                DopeSecondaryButton("Share video", { shareUri(context, recording.videoUri.toUri(), "video/mp4") }, Modifier.weight(1f))
            }
            DopeSecondaryButton(
                "Export sensor CSV",
                {
                    store
                        .exportSensorCsv(recording)
                        .onSuccess { uri ->
                            shareUri(context, uri, "text/csv")
                            onMessage("Sensor CSV saved and ready to share")
                        }.onFailure { onMessage(it.message ?: "CSV export failed") }
                },
                Modifier.fillMaxWidth(),
            )
            Text(
                "Playback overlays are synchronized from the local sensor stream. Burned-in overlay video export " +
                    "is pending codec validation; the original video and Sensor CSV are exportable now.",
                color = LocalDopeColors.current.textMuted,
            )
        }
    }
}

@Composable
private fun HorizonPlaybackValues(sample: OrientationSample) {
    Text(
        "${sample.elapsedMillis} ms · ${sample.magneticHeadingDegrees.round(1)}° M · " +
            "P ${sample.pitchDegrees.round(1)}° · R ${sample.rollDegrees.round(1)}° · " +
            if (sample.stable) "stable" else "moving",
    )
}

@SuppressLint("MissingPermission") // RECORD_AUDIO is checked immediately before withAudioEnabled and at both call sites.
private fun startRecording(
    context: Context,
    capture: VideoCapture<Recorder>,
    sessionName: String,
    includeAudio: Boolean,
    environmentalSummary: String,
    samples: MutableList<OrientationSample>,
    onStarted: (Recording, Long) -> Unit,
    onFinished: (Result<TrainingRecording>) -> Unit,
) {
    samples.clear()
    val id = UUID.randomUUID().toString()
    val startedAt = System.currentTimeMillis()
    val output = createVideoOutput(context, id)
    var pending: PendingRecording =
        when (val options = output.options) {
            is FileOutputOptions -> capture.output.prepareRecording(context, options)
            is MediaStoreOutputOptions -> capture.output.prepareRecording(context, options)
            else -> error("Unsupported video output")
        }
    if (includeAudio && context.hasPermission(Manifest.permission.RECORD_AUDIO)) {
        pending = pending.withAudioEnabled()
    }
    val active =
        pending.start(ContextCompat.getMainExecutor(context)) { event ->
            if (event is VideoRecordEvent.Finalize) {
                if (event.hasError()) {
                    onFinished(Result.failure(IllegalStateException("Video recording failed (${event.error})")))
                } else {
                    val uri = if (event.outputResults.outputUri != Uri.EMPTY) event.outputResults.outputUri else output.fallbackUri
                    onFinished(
                        Result.success(
                            TrainingRecording(
                                id = id,
                                sessionName = sessionName,
                                videoUri = uri.toString(),
                                sensorCsvUri = null,
                                startedAtEpochMillis = startedAt,
                                endedAtEpochMillis = System.currentTimeMillis(),
                                audioIncluded = includeAudio,
                                environmentalSummary = environmentalSummary,
                                samples = samples.toList(),
                            ),
                        ),
                    )
                }
            }
        }
    onStarted(active, SystemClock.elapsedRealtime())
}

private data class VideoOutput(
    val options: OutputOptions,
    val fallbackUri: Uri,
)

private fun createVideoOutput(
    context: Context,
    id: String,
): VideoOutput {
    val filename = "DOPE-$id.mp4"
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val values =
            ContentValues().apply {
                put(MediaStore.Video.Media.DISPLAY_NAME, filename)
                put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/DOPE")
            }
        VideoOutput(
            MediaStoreOutputOptions
                .Builder(context.contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                .setContentValues(values)
                .build(),
            Uri.EMPTY,
        )
    } else {
        val file = File(context.filesDir, "training-video/$filename").also { it.parentFile?.mkdirs() }
        VideoOutput(
            FileOutputOptions.Builder(file).build(),
            FileProvider.getUriForFile(context, "${context.packageName}.files", file),
        )
    }
}

private fun shareUri(
    context: Context,
    uri: Uri,
    mimeType: String,
) {
    context.startActivity(
        Intent.createChooser(
            Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            },
            "Share DOPE export",
        ),
    )
}

private fun Context.hasPermission(permission: String): Boolean =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

private tailrec fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }

private fun Double.round(decimals: Int): String = String.format(Locale.US, "%.${decimals}f", this)

private fun Long.timeLabel(): String = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date(this))

private fun ArSupportState.display(): String =
    when (this) {
        ArSupportState.SUPPORTED_INSTALLED -> "Supported · installed"
        ArSupportState.SUPPORTED_NOT_INSTALLED -> "Supported · install/update needed"
        ArSupportState.UNSUPPORTED -> "Unsupported · sensor fallback active"
        ArSupportState.CHECKING -> "Checking"
        ArSupportState.UNKNOWN -> "Unknown · sensor fallback active"
    }

private val CheckingCapability = ArCapability(ArSupportState.CHECKING, null, "Checking optional ARCore support")
private val PreviewCapability = ArCapability(ArSupportState.SUPPORTED_INSTALLED, true, "Preview capability")
private val PreviewSample =
    OrientationSample(0, 1_725_000_000_000, 312.0, -2.4, 1.2, ReadingQuality.GOOD, true)
