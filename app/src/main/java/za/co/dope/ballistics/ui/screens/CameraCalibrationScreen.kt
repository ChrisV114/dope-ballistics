@file:Suppress(
    "ComplexCondition",
    "CyclomaticComplexMethod",
    "LongMethod",
    "LongParameterList",
    "MaxLineLength",
    "TooManyFunctions",
)

package za.co.dope.ballistics.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.CaptureResult
import android.hardware.camera2.TotalCaptureResult
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.camera2.interop.Camera2CameraInfo
import androidx.camera.camera2.interop.Camera2Interop
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import za.co.dope.ballistics.BuildConfig
import za.co.dope.ballistics.data.camera.AndroidCameraCapabilityReader
import za.co.dope.ballistics.data.camera.CalibrationSample
import za.co.dope.ballistics.data.camera.CameraCalibrationMath
import za.co.dope.ballistics.data.camera.CameraCalibrationProfile
import za.co.dope.ballistics.data.camera.CameraCalibrationStore
import za.co.dope.ballistics.data.camera.CameraCapability
import za.co.dope.ballistics.data.camera.CameraConfiguration
import za.co.dope.ballistics.data.camera.CameraFrameMetadata
import za.co.dope.ballistics.ui.components.DopeCard
import za.co.dope.ballistics.ui.components.DopeField
import za.co.dope.ballistics.ui.components.DopeFieldConfig
import za.co.dope.ballistics.ui.components.DopePrimaryButton
import za.co.dope.ballistics.ui.components.DopeSecondaryButton
import za.co.dope.ballistics.ui.components.DopeStatus
import za.co.dope.ballistics.ui.components.LabelValue
import za.co.dope.ballistics.ui.components.StatusChip
import java.io.File
import java.util.Locale
import kotlin.math.abs

@Composable
fun CameraCalibrationScreen(previewMode: Boolean = false) {
    val context = LocalContext.current
    val store = remember { CameraCalibrationStore(context) }
    var permissionGranted by
        remember {
            mutableStateOf(
                previewMode ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED,
            )
        }
    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            permissionGranted = granted
        }
    var capabilities by remember { mutableStateOf(if (previewMode) listOf(PreviewCapability) else emptyList()) }
    var capabilityError by remember { mutableStateOf<String?>(null) }
    var selectedCameraId by remember { mutableStateOf(if (previewMode) PreviewCapability.cameraId else null) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var frameMetadata by remember { mutableStateOf<CameraFrameMetadata?>(null) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var captureMessage by remember { mutableStateOf<String?>(null) }
    var knownSize by remember { mutableStateOf("") }
    var knownDistance by remember { mutableStateOf("") }
    val samples = remember { mutableStateListOf<CalibrationSample>() }
    var savedProfile by remember { mutableStateOf(store.load()) }
    var anchorStart by remember { mutableFloatStateOf(0.25f) }
    var anchorEnd by remember { mutableFloatStateOf(0.75f) }
    val checklist = remember { mutableStateListOf(false, false, false, false, false) }

    LaunchedEffect(permissionGranted) {
        if (permissionGranted && !previewMode) {
            runCatching { AndroidCameraCapabilityReader(context).readRearCameras() }
                .onSuccess {
                    capabilities = it
                    selectedCameraId = selectedCameraId ?: it.firstOrNull()?.cameraId
                }.onFailure { capabilityError = it.message ?: "Camera capability inspection failed" }
        }
    }

    ScreenShell(title = "Camera calibration", eyebrow = "STATIC TARGETS ONLY") {
        StatusChip(
            if (permissionGranted) "Camera permission granted" else "Camera permission required",
            if (permissionGranted) DopeStatus.READY else DopeStatus.WARNING,
        )
        if (!permissionGranted) {
            DopeCard {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Camera access is used only for an on-device still preview and calibration image.")
                    DopePrimaryButton(
                        "Allow camera",
                        { permissionLauncher.launch(Manifest.permission.CAMERA) },
                        Modifier.fillMaxWidth(),
                        Icons.Outlined.CameraAlt,
                    )
                }
            }
        } else {
            CapabilityPanel(
                capabilities,
                selectedCameraId,
                onSelect = { cameraId ->
                    if (cameraId != selectedCameraId) {
                        selectedCameraId = cameraId
                        frameMetadata = null
                        capturedBitmap = null
                        samples.clear()
                        captureMessage = "Camera changed · capture new calibration samples"
                    }
                },
            )
            capabilityError?.let { StatusChip(it, DopeStatus.BLOCKED) }
            selectedCameraId?.let { cameraId ->
                if (previewMode) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(280.dp).background(Color.Black),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("LIVE CAMERA PREVIEW", color = Color.White)
                    }
                } else {
                    LiveCameraPreview(
                        cameraId = cameraId,
                        onBound = { imageCapture = it },
                        onMetadata = { frameMetadata = it },
                    )
                }
                DopePrimaryButton(
                    "Capture calibration still",
                    {
                        captureStill(context.cacheDir, imageCapture, context) { result ->
                            result
                                .onSuccess { bitmap ->
                                    capturedBitmap = bitmap
                                    captureMessage = "Still captured · ${bitmap.width}×${bitmap.height}"
                                }.onFailure { captureMessage = it.message ?: "Capture failed" }
                        }
                    },
                    Modifier.fillMaxWidth(),
                    Icons.Outlined.CameraAlt,
                    enabled = imageCapture != null && !previewMode,
                )
            }
            captureMessage?.let { StatusChip(it, if (capturedBitmap != null) DopeStatus.READY else DopeStatus.BLOCKED) }
            frameMetadata?.let { metadata ->
                LabelValue(
                    "Captured focal length",
                    metadata.focalLengthMillimetres?.let { "$it mm" } ?: "Not exposed",
                )
                LabelValue("Captured zoom", "${metadata.zoomRatio}×")
                LabelValue("Crop region", metadata.cropRegion ?: "Not exposed")
            }
            capturedBitmap?.let { bitmap ->
                AnchorMeasurement(bitmap, anchorStart, anchorEnd) { start, end ->
                    anchorStart = start
                    anchorEnd = end
                }
                val pixelSpan = abs(anchorEnd - anchorStart) * bitmap.width
                LabelValue("Anchor span", "${pixelSpan.toDouble().round(1)} px")
                DopeField("Known object width", knownSize, { knownSize = it }, config = DopeFieldConfig("mm", numeric = true))
                DopeField(
                    "Tape-measured distance",
                    knownDistance,
                    { knownDistance = it },
                    config = DopeFieldConfig("m", numeric = true),
                )
                DopeSecondaryButton(
                    "Add calibration sample",
                    {
                        val size = knownSize.toDoubleOrNull()
                        val distance = knownDistance.toDoubleOrNull()
                        if (size != null && distance != null && size > 0 && distance > 0 && pixelSpan >= MIN_ANCHOR_PIXELS) {
                            samples += CalibrationSample(size, distance, pixelSpan.toDouble())
                            captureMessage = "Sample ${samples.size} added"
                        } else {
                            captureMessage = "Enter positive measurements and place anchors at least 20 px apart"
                        }
                    },
                    Modifier.fillMaxWidth(),
                )
            }
            CalibrationPanel(
                capabilities = capabilities,
                selectedCameraId = selectedCameraId,
                bitmap = capturedBitmap,
                samples = samples,
                savedProfile = savedProfile,
                frameMetadata = frameMetadata,
                onSave = { profile ->
                    store.save(profile)
                    savedProfile = profile
                    captureMessage = "Calibration saved locally"
                },
            )
        }
        SafetyAndAcceptancePanel(checklist)
    }
}

@Composable
private fun CapabilityPanel(
    capabilities: List<CameraCapability>,
    selectedCameraId: String?,
    onSelect: (String) -> Unit,
) {
    DopeCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionHeading("Physical lens capability")
            if (capabilities.isEmpty()) Text("No rear camera metadata available")
            capabilities.forEach { capability ->
                val selected = capability.cameraId == selectedCameraId
                DopeSecondaryButton(
                    if (selected) "Selected · ${capability.displayName}" else capability.displayName,
                    { onSelect(capability.cameraId) },
                    Modifier.fillMaxWidth(),
                )
                if (selected) {
                    LabelValue("Sensor", capability.sensorLabel())
                    LabelValue("Active array", capability.activeArrayLabel())
                    LabelValue("Zoom range", capability.zoomRatioRange ?: "Not exposed")
                    LabelValue(
                        "JPEG sizes",
                        capability.jpegSizes
                            .take(3)
                            .joinToString()
                            .ifBlank { "Not exposed" },
                    )
                    LabelValue("OIS", if (capability.opticalStabilisation) "Exposed" else "Not exposed")
                    LabelValue("Distortion", if (capability.distortionMetadata) "Metadata exposed" else "Not exposed")
                    LabelValue("Physical IDs", capability.physicalCameraIds.joinToString().ifBlank { "Logical ID only" })
                }
            }
        }
    }
}

@Composable
@androidx.annotation.OptIn(ExperimentalCamera2Interop::class)
private fun LiveCameraPreview(
    cameraId: String,
    onBound: (ImageCapture?) -> Unit,
    onMetadata: (CameraFrameMetadata) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context).apply { scaleType = PreviewView.ScaleType.FIT_CENTER } }
    DisposableEffect(cameraId, lifecycleOwner) {
        val providerFuture = ProcessCameraProvider.getInstance(context)
        val listener =
            Runnable {
                runCatching {
                    val provider = providerFuture.get()
                    val preview = Preview.Builder().build().also { it.surfaceProvider = previewView.surfaceProvider }
                    val captureBuilder =
                        ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                    Camera2Interop.Extender(captureBuilder).setSessionCaptureCallback(
                        object : CameraCaptureSession.CaptureCallback() {
                            override fun onCaptureCompleted(
                                session: CameraCaptureSession,
                                request: CaptureRequest,
                                result: TotalCaptureResult,
                            ) {
                                val zoomRatio =
                                    if (Build.VERSION.SDK_INT >= 30) {
                                        result[CaptureResult.CONTROL_ZOOM_RATIO] ?: 1f
                                    } else {
                                        1f
                                    }
                                val metadata =
                                    CameraFrameMetadata(
                                        focalLengthMillimetres = result[CaptureResult.LENS_FOCAL_LENGTH],
                                        zoomRatio = zoomRatio,
                                        cropRegion =
                                            result[CaptureResult.SCALER_CROP_REGION]?.let {
                                                "${it.left},${it.top}–${it.right},${it.bottom}"
                                            },
                                    )
                                ContextCompat.getMainExecutor(context).execute { onMetadata(metadata) }
                            }
                        },
                    )
                    val capture = captureBuilder.build()
                    val selector =
                        CameraSelector
                            .Builder()
                            .addCameraFilter { infos -> infos.filter { Camera2CameraInfo.from(it).cameraId == cameraId } }
                            .build()
                    provider.unbindAll()
                    val camera = provider.bindToLifecycle(lifecycleOwner, selector, preview, capture)
                    camera.cameraControl.setZoomRatio(1f)
                    onBound(capture)
                }.onFailure { onBound(null) }
            }
        providerFuture.addListener(listener, ContextCompat.getMainExecutor(context))
        onDispose {
            if (providerFuture.isDone) runCatching { providerFuture.get().unbindAll() }
            onBound(null)
        }
    }
    AndroidView(
        factory = { previewView },
        modifier = Modifier.fillMaxWidth().height(280.dp).background(Color.Black),
    )
}

@Composable
private fun AnchorMeasurement(
    bitmap: Bitmap,
    start: Float,
    end: Float,
    onChange: (Float, Float) -> Unit,
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .aspectRatio(bitmap.width.toFloat() / bitmap.height)
                .pointerInput(start, end) {
                    var movingStart = true
                    detectDragGestures(
                        onDragStart = { point ->
                            movingStart = abs(point.x / size.width - start) <= abs(point.x / size.width - end)
                        },
                    ) { change, _ ->
                        val position = (change.position.x / size.width).coerceIn(0f, 1f)
                        if (movingStart) onChange(position, end) else onChange(start, position)
                    }
                },
    ) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Captured calibration still",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.matchParentSize(),
        )
        Canvas(Modifier.matchParentSize()) {
            val y = size.height / 2f
            val first = start * size.width
            val second = end * size.width
            drawLine(Color.Cyan, Offset(first, y), Offset(second, y), strokeWidth = 5f)
            drawCircle(Color.Cyan, 18f, Offset(first, y), style = Stroke(5f))
            drawCircle(Color.Cyan, 18f, Offset(second, y), style = Stroke(5f))
        }
    }
    Text("Drag either cyan anchor to the known object's left and right edges.")
}

@Composable
private fun CalibrationPanel(
    capabilities: List<CameraCapability>,
    selectedCameraId: String?,
    bitmap: Bitmap?,
    samples: List<CalibrationSample>,
    savedProfile: CameraCalibrationProfile?,
    frameMetadata: CameraFrameMetadata?,
    onSave: (CameraCalibrationProfile) -> Unit,
) {
    val capability = capabilities.firstOrNull { it.cameraId == selectedCameraId }
    val focal = frameMetadata?.focalLengthMillimetres ?: capability?.focalLengthsMillimetres?.singleOrNull()
    val configuration =
        if (selectedCameraId != null && focal != null && bitmap != null) {
            CameraConfiguration(
                manufacturer = Build.MANUFACTURER,
                model = Build.MODEL,
                cameraId = selectedCameraId,
                focalLengthMillimetres = focal,
                resolutionWidth = bitmap.width,
                resolutionHeight = bitmap.height,
                zoomRatio = frameMetadata?.zoomRatio ?: 1f,
            )
        } else {
            null
        }
    DopeCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionHeading("Calibration fit")
            LabelValue("Samples", samples.size.toString())
            Text("Use at least two tape-measured distances. Three or more spanning the intended camera distance is preferred.")
            DopePrimaryButton(
                "Fit and save calibration",
                {
                    val validConfiguration = requireNotNull(configuration)
                    onSave(
                        CameraCalibrationMath.fit(
                            validConfiguration,
                            samples,
                            System.currentTimeMillis(),
                            BuildConfig.VERSION_NAME,
                        ),
                    )
                },
                Modifier.fillMaxWidth(),
                Icons.Outlined.CheckCircle,
                enabled = configuration != null && samples.size >= 2,
            )
            savedProfile?.let { profile ->
                val warning = configuration?.let(profile::warningFor)
                StatusChip(warning ?: "Saved calibration matches", if (warning == null) DopeStatus.READY else DopeStatus.WARNING)
                LabelValue("Effective focal length", "${profile.effectiveFocalLengthPixels.round(1)} px")
                LabelValue("Mean absolute error", "${profile.meanAbsoluteErrorMetres.round(2)} m")
                LabelValue("Median error", "${profile.medianPercentageError.round(1)} %")
                LabelValue("95th percentile error", "${profile.percentile95ErrorMetres.round(2)} m")
                LabelValue("Validated range", "${profile.validFromMetres.round(1)}–${profile.validToMetres.round(1)} m")
            }
        }
    }
}

@Composable
private fun SafetyAndAcceptancePanel(checklist: MutableList<Boolean>) {
    DopeCard {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            SectionHeading("Galaxy S25 physical acceptance")
            listOf(
                "Landscape stand is stable and camera controls remain clear",
                "Selected camera ID and focal length stay unchanged",
                "Captured resolution matches the saved calibration",
                "Zoom remains locked at 1.0× with no digital zoom",
                "Static paper or steel target edges are clearly visible",
            ).forEachIndexed { index, label ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = checklist[index], onCheckedChange = { checklist[index] = it })
                    Text(label, style = MaterialTheme.typography.bodyMedium)
                }
            }
            StatusChip(
                if (checklist.all { it }) "Physical checklist recorded" else "Physical S25 test pending",
                if (checklist.all { it }) DopeStatus.READY else DopeStatus.WARNING,
            )
        }
    }
    StatusChip("No people, animals, vehicles, hit detection or aiming overlay", DopeStatus.INFO)
    Text(
        "Anchor measurements are retained as calibration evidence only. This build does not calculate a target " +
            "distance or feed camera data into a ballistic solution.",
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(bottom = 8.dp),
    )
}

private fun captureStill(
    cacheDirectory: File,
    capture: ImageCapture?,
    context: android.content.Context,
    onResult: (Result<Bitmap>) -> Unit,
) {
    val validCapture = capture ?: return onResult(Result.failure(IllegalStateException("Camera is not ready")))
    val file = File(cacheDirectory, "camera-calibration-${System.currentTimeMillis()}.jpg")
    val output = ImageCapture.OutputFileOptions.Builder(file).build()
    validCapture.takePicture(
        output,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                onResult(
                    runCatching {
                        ImageDecoder.decodeBitmap(ImageDecoder.createSource(file)).copy(Bitmap.Config.ARGB_8888, false)
                    }.also { file.delete() },
                )
            }

            override fun onError(exception: ImageCaptureException) {
                onResult(Result.failure(exception))
            }
        },
    )
}

private fun CameraCapability.sensorLabel(): String =
    if (sensorWidthMillimetres != null && sensorHeightMillimetres != null) {
        "$sensorWidthMillimetres×$sensorHeightMillimetres mm"
    } else {
        "Not exposed"
    }

private fun CameraCapability.activeArrayLabel(): String =
    if (activeWidthPixels != null && activeHeightPixels != null) "$activeWidthPixels×$activeHeightPixels px" else "Not exposed"

private fun Double.round(decimals: Int): String = String.format(Locale.US, "%.${decimals}f", this)

private const val MIN_ANCHOR_PIXELS = 20f

private val PreviewCapability =
    CameraCapability(
        cameraId = "0",
        lensFacing = "Rear",
        focalLengthsMillimetres = listOf(5.4f),
        sensorWidthMillimetres = 7.6f,
        sensorHeightMillimetres = 5.7f,
        activeWidthPixels = 4000,
        activeHeightPixels = 3000,
        jpegSizes = listOf("4000×3000", "3840×2160"),
        zoomRatioRange = "1.0–10.0×",
        opticalStabilisation = true,
        distortionMetadata = true,
        physicalCameraIds = setOf("2", "3"),
    )
