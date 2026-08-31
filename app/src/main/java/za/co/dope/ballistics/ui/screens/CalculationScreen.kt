@file:Suppress("TooManyFunctions")

package za.co.dope.ballistics.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import za.co.dope.ballistics.data.ProfileRepository
import za.co.dope.ballistics.data.db.ActiveProfileSelectionEntity
import za.co.dope.ballistics.domain.BallisticsInputMapper
import za.co.dope.ballistics.engine.AngularUnit
import za.co.dope.ballistics.engine.StandardBallisticsEngine
import za.co.dope.ballistics.engine.TrajectoryResult
import za.co.dope.ballistics.engine.WindConvention
import za.co.dope.ballistics.ui.components.DopeCard
import za.co.dope.ballistics.ui.components.DopeField
import za.co.dope.ballistics.ui.components.DopeFieldConfig
import za.co.dope.ballistics.ui.components.DopeSecondaryButton
import za.co.dope.ballistics.ui.components.DopeStatus
import za.co.dope.ballistics.ui.components.LabelValue
import za.co.dope.ballistics.ui.components.StatusChip
import java.util.Locale
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.min

@Composable
@Suppress("LongMethod", "CyclomaticComplexMethod")
fun ResultsScreen(
    repository: ProfileRepository?,
    windState: WindFormState,
    onOpen: (String) -> Unit,
    previewMode: Boolean = false,
) {
    val active by
        repository?.observeActiveProfileSelection()?.collectAsState(null) ?: remember {
            mutableStateOf<ActiveProfileSelectionEntity?>(null)
        }
    val environments by
        repository?.observeEnvironmentalSnapshots()?.collectAsState(emptyList()) ?: remember {
            mutableStateOf(emptyList())
        }
    var distance by remember { mutableStateOf(if (previewMode) "800" else "100") }
    var confirmedDistances by remember { mutableStateOf<List<Double>>(emptyList()) }
    var result by remember { mutableStateOf<TrajectoryResult?>(null) }
    var profileLabel by remember { mutableStateOf<String?>(null) }
    var issues by remember { mutableStateOf<List<String>>(emptyList()) }
    var displayUnit by remember { mutableStateOf(AngularUnit.MIL) }
    var reticleHoldValid by remember { mutableStateOf(false) }

    LaunchedEffect(
        repository,
        active,
        environments,
        distance,
        windState.windFromDegrees,
        windState.directionOfFireDegrees,
        windState.minimumSpeedMps,
        windState.averageSpeedMps,
        windState.maximumSpeedMps,
        windState.gustSpeedMps,
        windState.reference,
        windState.selectedSpeed,
    ) {
        if (repository == null || previewMode) return@LaunchedEffect
        confirmedDistances =
            repository
                .confirmedDopeTargets()
                .mapNotNull { it.measuredDistanceMetres }
                .distinct()
                .sorted()
        val metres = distance.toDoubleOrNull()
        if (metres == null || metres <= 0.0) {
            result = null
            issues = listOf("Enter a positive target distance.")
            return@LaunchedEffect
        }
        val context = repository.calculationContext()
        if (context == null) {
            result = null
            issues = missingContextIssues(active, environments.isNotEmpty())
            return@LaunchedEffect
        }
        profileLabel = "${context.rifle.profileName} · ${context.ammunition.profileName} · ${context.scope.profileName}"
        reticleHoldValid = context.scope.reticleSystem != "BDC" && context.scope.focalPlane == "FIRST"
        val observation = windState.observation()
        if (observation == null) {
            result = null
            issues = listOf("Complete valid live wind values.")
            return@LaunchedEffect
        }
        val mapped =
            runCatching {
                val resolved = WindConvention.resolve(observation)
                BallisticsInputMapper.build(
                    ammunition = context.ammunition,
                    scope = context.scope,
                    zero = context.zero,
                    reference = context.referenceAtmosphere,
                    current = context.currentEnvironment,
                    targetRangeMeters = metres,
                    wind = resolved.asEngineWind(),
                )
            }.getOrElse {
                result = null
                issues = listOf(it.message ?: "Wind values are invalid.")
                return@LaunchedEffect
            }
        val input = mapped.input
        if (input == null) {
            result = null
            issues = mapped.issues
        } else {
            result = StandardBallisticsEngine().solve(input)
            issues = result?.issues.orEmpty()
        }
    }

    ReferencePanelShell(title = "Results") {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            AngularUnit.entries.forEach { unit ->
                FilterChip(
                    selected = displayUnit == unit,
                    onClick = { displayUnit = unit },
                    label = { Text(unit.name) },
                    modifier = Modifier.weight(1f),
                    colors =
                        FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                )
            }
        }
        ReferenceMetricTile("Distance", "${distance.ifBlank { "—" }} m")
        profileLabel?.let { StatusChip(it, DopeStatus.READY) }

        val display = calculationDisplay(result, displayUnit) ?: previewDisplay(displayUnit).takeIf { previewMode }
        if (display != null) {
            CalculationResultGrid(display, reticleHoldValid || previewMode, windState)
            CalculationAdjustments(distance, { distance = it }, confirmedDistances, windState, onOpen)
            result?.solution?.let { solution ->
                DopeCard {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionHeading("Confidence and trace")
                        LabelValue("Confidence", result?.confidence?.name.orEmpty())
                        LabelValue("Remaining velocity", "${number(solution.remainingVelocityMps)} m/s")
                        LabelValue("Engine", result?.trace?.engineVersion.orEmpty())
                        LabelValue(
                            "Drag model",
                            result
                                ?.trace
                                ?.dragModel
                                ?.name
                                .orEmpty(),
                        )
                        StatusChip(
                            if (solution.elevationScope.withinTravel) {
                                "Within entered scope travel"
                            } else {
                                "Outside entered scope travel"
                            },
                            if (solution.elevationScope.withinTravel) DopeStatus.READY else DopeStatus.WARNING,
                        )
                    }
                }
            }
        } else {
            DopeCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusChip("Cannot calculate yet", DopeStatus.BLOCKED)
                    issues.ifEmpty { listOf("Complete the active setup and current conditions.") }.forEach {
                        Text(it, style = MaterialTheme.typography.bodyMedium)
                    }
                    DopeSecondaryButton("Open active setup", { onOpen("zero_setup") }, Modifier.fillMaxWidth())
                    DopeSecondaryButton("Open current environment", { onOpen("environment") }, Modifier.fillMaxWidth())
                }
            }
        }
    }
}

private data class CalculationDisplay(
    val unit: AngularUnit,
    val referenceElevation: Double,
    val currentElevation: Double,
    val environmentalDeviation: Double,
    val totalElevation: Double,
    val elevationClicks: Int,
    val scopeUnit: AngularUnit,
    val reticleHold: Double,
    val wind: Double,
)

@Composable
private fun CalculationResultGrid(
    display: CalculationDisplay,
    reticleHoldValid: Boolean,
    windState: WindFormState,
) {
    val unit = display.unit.name
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        ReferenceMetricTile(
            "Reference Elevation",
            signed(display.referenceElevation, unit),
            Modifier.weight(1f),
            accent = Color(0xFF7BBE7B),
        )
        ReferenceMetricTile(
            "Current Elevation",
            "${number(display.currentElevation)} $unit",
            Modifier.weight(1f),
        )
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        ReferenceMetricTile(
            "Environmental Deviation",
            signed(display.environmentalDeviation, unit),
            Modifier.weight(1f),
            accent = Color(0xFF7BBE7B),
        )
        ReferenceMetricTile(
            "Total Elevation",
            "${number(display.totalElevation)} $unit",
            Modifier.weight(1f),
        )
    }
    ElevationDial(display.totalElevation, unit)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        ReferenceMetricTile(
            "Clicks (${display.scopeUnit.name})",
            "${display.elevationClicks} clicks",
            Modifier.weight(1f),
            accent = Color(0xFFB8C68A),
        )
        ReferenceMetricTile(
            "Reticle Hold",
            if (reticleHoldValid) "${number(display.reticleHold)} $unit" else "Not valid",
            Modifier.weight(1f),
        )
    }
    ReferenceMetricTile(
        "Wind (L to R)",
        "${number(display.wind)} $unit · ${windState.averageSpeedMps} m/s",
        accent = MaterialTheme.colorScheme.secondary,
    )
}

@Composable
@Suppress("LongMethod")
private fun ElevationDial(
    elevation: Double,
    unit: String,
) {
    val maximum = max(4.0, kotlin.math.ceil(kotlin.math.abs(elevation) / 4.0) * 4.0)
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    Canvas(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(190.dp)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.97f), RoundedCornerShape(12.dp))
                .padding(18.dp),
    ) {
        val radius = size.width * 0.38f
        val centre = Offset(size.width / 2f, size.height * 0.78f)
        val arcTopLeft = Offset(centre.x - radius, centre.y - radius)
        val arcSize =
            androidx.compose.ui.geometry
                .Size(radius * 2f, radius * 2f)
        drawArc(
            color = Color(0xFF6B7B8F),
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = arcTopLeft,
            size = arcSize,
            style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round),
        )
        repeat(17) { index ->
            val angle = (180.0 + index * 180.0 / 16.0) * PI / 180.0
            val outer =
                Offset(
                    centre.x + kotlin.math.cos(angle).toFloat() * radius,
                    centre.y + kotlin.math.sin(angle).toFloat() * radius,
                )
            val innerRadius = radius - if (index % 4 == 0) 16.dp.toPx() else 9.dp.toPx()
            val inner =
                Offset(
                    centre.x + kotlin.math.cos(angle).toFloat() * innerRadius,
                    centre.y + kotlin.math.sin(angle).toFloat() * innerRadius,
                )
            drawLine(Color(0xFFB5C1CF), inner, outer, 2.dp.toPx(), StrokeCap.Round)
        }
        val fraction = (kotlin.math.abs(elevation) / maximum).coerceIn(0.0, 1.0)
        val needleAngle = (180.0 + fraction * 180.0) * PI / 180.0
        val needleEnd =
            Offset(
                centre.x + kotlin.math.cos(needleAngle).toFloat() * radius * 0.78f,
                centre.y + kotlin.math.sin(needleAngle).toFloat() * radius * 0.78f,
            )
        drawLine(primary, centre, needleEnd, 5.dp.toPx(), StrokeCap.Round)
        drawCircle(secondary, 5.dp.toPx(), centre)
        drawContext.canvas.nativeCanvas.apply {
            val paint =
                android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                    color = android.graphics.Color.WHITE
                    textAlign = android.graphics.Paint.Align.CENTER
                }
            paint.textSize = 11.dp.toPx()
            drawText("ELEVATION", centre.x, centre.y - 48.dp.toPx(), paint)
            paint.textSize = 34.dp.toPx()
            drawText(number(elevation), centre.x, centre.y - 15.dp.toPx(), paint)
            paint.textSize = 13.dp.toPx()
            drawText(unit, centre.x, centre.y + 7.dp.toPx(), paint)
        }
    }
}

@Composable
private fun CalculationAdjustments(
    distance: String,
    onDistanceChanged: (String) -> Unit,
    confirmedDistances: List<Double>,
    state: WindFormState,
    onOpen: (String) -> Unit,
) {
    DopeCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionHeading("Live adjustments")
            DopeField("Distance", distance, onDistanceChanged, config = DopeFieldConfig(suffix = "m"))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DopeSecondaryButton("− 5 m", { onDistanceChanged(adjusted(distance, -5.0)) }, Modifier.weight(1f))
                DopeSecondaryButton("+ 5 m", { onDistanceChanged(adjusted(distance, 5.0)) }, Modifier.weight(1f))
            }
            confirmedDistances.take(6).forEach { metres ->
                DopeSecondaryButton(
                    "Use confirmed target · ${number(metres)} m",
                    { onDistanceChanged(number(metres)) },
                    Modifier.fillMaxWidth(),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DopeSecondaryButton("− 0.5 m/s", { adjustWindSpeed(state, -0.5) }, Modifier.weight(1f))
                DopeSecondaryButton("+ 0.5 m/s", { adjustWindSpeed(state, 0.5) }, Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DopeSecondaryButton("Wind − 5°", { adjustWindDirection(state, -5.0) }, Modifier.weight(1f))
                DopeSecondaryButton("Wind + 5°", { adjustWindDirection(state, 5.0) }, Modifier.weight(1f))
            }
            DopeSecondaryButton("Open full wind wheel", { onOpen("wind") }, Modifier.fillMaxWidth())
        }
    }
}

private fun calculationDisplay(
    result: TrajectoryResult?,
    unit: AngularUnit,
): CalculationDisplay? {
    val solution = result?.solution ?: return null
    return CalculationDisplay(
        unit = unit,
        referenceElevation = angularValue(solution.referenceElevationRadians, unit),
        currentElevation = angularValue(solution.currentElevationRadians, unit),
        environmentalDeviation = angularValue(solution.environmentalDeviationRadians, unit),
        totalElevation = angularValue(solution.currentElevationRadians, unit),
        elevationClicks = solution.elevationScope.clicks,
        scopeUnit = solution.elevationScope.unit,
        reticleHold = angularValue(solution.currentElevationRadians, unit),
        wind = angularValue(solution.windageRadians, unit),
    )
}

private fun previewDisplay(unit: AngularUnit): CalculationDisplay {
    val scale = if (unit == AngularUnit.MIL) 1.0 else 3.43774677
    return CalculationDisplay(
        unit = unit,
        referenceElevation = 2.42 * scale,
        currentElevation = 2.31 * scale,
        environmentalDeviation = -0.11 * scale,
        totalElevation = 2.31 * scale,
        elevationClicks = 23,
        scopeUnit = AngularUnit.MIL,
        reticleHold = 2.3 * scale,
        wind = 0.87 * scale,
    )
}

internal fun adjustWindSpeed(
    state: WindFormState,
    delta: Double,
) {
    val average = max(0.0, (state.averageSpeedMps.toDoubleOrNull() ?: 0.0) + delta)
    val minimum = min(state.minimumSpeedMps.toDoubleOrNull() ?: average, average)
    val maximum = max(state.maximumSpeedMps.toDoubleOrNull() ?: average, average)
    state.averageSpeedMps = number(average)
    state.minimumSpeedMps = number(minimum)
    state.maximumSpeedMps = number(maximum)
}

internal fun adjustWindDirection(
    state: WindFormState,
    delta: Double,
) {
    val current = state.windFromDegrees.toDoubleOrNull() ?: 0.0
    state.windFromDegrees = number(WindConvention.normalizeDegrees(current + delta))
}

private fun adjusted(
    value: String,
    delta: Double,
): String = number(max(1.0, (value.toDoubleOrNull() ?: 0.0) + delta))

private fun missingContextIssues(
    active: ActiveProfileSelectionEntity?,
    hasEnvironment: Boolean,
): List<String> =
    buildList {
        if (active == null) add("Create and select a verified active setup.")
        if (!hasEnvironment) add("Save current environmental conditions.")
        if (isEmpty()) add("The active setup has missing or unverified linked records.")
    }

private fun angularValue(
    radians: Double,
    unit: AngularUnit,
): Double =
    when (unit) {
        AngularUnit.MIL -> radians * 1_000.0
        AngularUnit.MOA -> radians * 180.0 / PI * 60.0
    }

private fun signed(
    value: Double,
    unit: String,
): String = "${if (value >= 0.0) "+" else ""}${number(value)} $unit"

private fun number(value: Double): String = String.format(Locale.US, "%.2f", value).trimEnd('0').trimEnd('.')
