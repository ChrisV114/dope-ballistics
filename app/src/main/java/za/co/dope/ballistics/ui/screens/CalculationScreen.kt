@file:Suppress("TooManyFunctions")

package za.co.dope.ballistics.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import za.co.dope.ballistics.data.ProfileRepository
import za.co.dope.ballistics.data.SessionRepository
import za.co.dope.ballistics.data.db.ActiveProfileSelectionEntity
import za.co.dope.ballistics.data.db.VerifiedDopeRecordEntity
import za.co.dope.ballistics.domain.BallisticsInputMapper
import za.co.dope.ballistics.engine.AngularUnit
import za.co.dope.ballistics.engine.StandardBallisticsEngine
import za.co.dope.ballistics.engine.TrajectoryResult
import za.co.dope.ballistics.engine.WindConvention
import za.co.dope.ballistics.engine.WindSpeedSelection
import za.co.dope.ballistics.ui.components.DopeCard
import za.co.dope.ballistics.ui.components.DopeSecondaryButton
import za.co.dope.ballistics.ui.components.DopeStatus
import za.co.dope.ballistics.ui.components.LabelValue
import za.co.dope.ballistics.ui.components.StatusChip
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.min

@Composable
@Suppress("LongMethod", "CyclomaticComplexMethod", "LongParameterList")
fun ResultsScreen(
    repository: ProfileRepository?,
    sessionRepository: SessionRepository?,
    windState: WindFormState,
    sessionDraft: SessionDraftState,
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
    val zeroProfiles by
        repository?.observeZeroProfiles()?.collectAsState(emptyList()) ?: remember {
            mutableStateOf(emptyList())
        }
    val verifiedDopeRecords by
        sessionRepository?.observeVerifiedDope()?.collectAsState(emptyList()) ?: remember {
            mutableStateOf(emptyList())
        }
    var setupLabels by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    val coroutineScope = rememberCoroutineScope()
    var distance by remember { mutableStateOf(if (previewMode) "800" else "100") }
    var result by remember { mutableStateOf<TrajectoryResult?>(null) }
    var profileLabel by remember { mutableStateOf<String?>(null) }
    var issues by remember { mutableStateOf<List<String>>(emptyList()) }
    var displayUnit by remember { mutableStateOf(AngularUnit.MIL) }
    var reticleHoldValid by remember { mutableStateOf(false) }
    var referenceEstimateLabel by remember { mutableStateOf<String?>(null) }
    var actualDial by remember { mutableStateOf(sessionDraft.actualDialValue) }
    LaunchedEffect(zeroProfiles, repository) {
        if (repository != null) {
            setupLabels =
                zeroProfiles.associate { zero ->
                    zero.id to (repository.zeroSetupLabel(zero.id) ?: "${zero.zeroDistanceMetres.toInt()} m setup")
                }
        }
    }

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
        val metres = parsePositiveDistance(distance)
        if (metres == null || metres <= 0.0) {
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
        referenceEstimateLabel =
            context.referenceAtmosphere.temperatureSource
                .takeIf { it.startsWith("ESTIMATED_") }
                ?.let { "Reference atmosphere estimated · environmental deviation is approximate" }
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
        CalculationAdjustments(distance, { distance = it }, windState)
        if (zeroProfiles.isNotEmpty()) {
            val selectedZero = zeroProfiles.firstOrNull { it.id == active?.zeroProfileId } ?: zeroProfiles.first()
            ChoiceRow(
                zeroProfiles,
                selectedZero,
                { selected ->
                    coroutineScope.launch {
                        repository?.activateZeroProfile(selected.id, System.currentTimeMillis())
                    }
                },
            ) { zero -> setupLabels[zero.id] ?: "${zero.zeroDistanceMetres.toInt()} m setup" }
        }
        profileLabel?.let {
            StatusChip(it, DopeStatus.READY)
            DopeSecondaryButton("Change rifle / load / scope", { onOpen("profiles") }, Modifier.fillMaxWidth())
        }
        referenceEstimateLabel?.let { StatusChip(it, DopeStatus.WARNING) }
        if (parsePositiveDistance(distance) == null) {
            StatusChip("Finish typing a positive distance", DopeStatus.WARNING)
        }

        val display = calculationDisplay(result, displayUnit) ?: previewDisplay(displayUnit).takeIf { previewMode }
        if (display != null) {
            CalculationResultGrid(display, reticleHoldValid || previewMode, windState)
            val previousDope =
                previousDopeForDistance(
                    records = verifiedDopeRecords,
                    activeZeroProfileId = active?.zeroProfileId,
                    distanceMetres = parsePositiveDistance(distance),
                ) ?: previewPreviousDope().takeIf { previewMode }
            PreviousDopeCard(previousDope, distance)
            DopeCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionHeading("Record actual setting")
                    Text(
                        "This records what you finally used. It never silently changes the calculator.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    CompactLiveField(
                        "Actual ${display.scopeUnit.name}",
                        actualDial,
                        { actualDial = sanitiseDecimalInput(it) },
                        Modifier.fillMaxWidth(),
                    )
                    DopeSecondaryButton(
                        "Continue to verified DOPE log",
                        {
                            sessionDraft.distanceMetres = distance
                            sessionDraft.actualDialValue = actualDial
                            onOpen("session")
                        },
                        Modifier.fillMaxWidth(),
                    )
                }
            }
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

@Composable
private fun PreviousDopeCard(
    record: VerifiedDopeRecordEntity?,
    distance: String,
) {
    DopeCard {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            SectionHeading("Previous DOPE")
            if (record == null) {
                Text(
                    "No verified manual record for $distance m with this setup.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            } else {
                LabelValue(
                    "Last used at ${number(record.distanceMetres)} m",
                    "${number(record.actualDialValue)} ${record.actualDialUnit}",
                )
                Text(
                    "Recorded ${previousDopeDate(record.createdAtEpochMillis)} · historical record only",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

internal fun previousDopeForDistance(
    records: List<VerifiedDopeRecordEntity>,
    activeZeroProfileId: String?,
    distanceMetres: Double?,
): VerifiedDopeRecordEntity? {
    if (activeZeroProfileId == null || distanceMetres == null) return null
    return records.firstOrNull { record ->
        record.zeroProfileId == activeZeroProfileId &&
            kotlin.math.abs(record.distanceMetres - distanceMetres) < 0.01 &&
            record.status == "VERIFIED"
    }
}

private fun previousDopeDate(epochMillis: Long): String {
    val formatter = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
    return formatter.format(Date(epochMillis))
}

private fun previewPreviousDope(): VerifiedDopeRecordEntity =
    VerifiedDopeRecordEntity(
        id = "preview-verified-dope",
        createdAtEpochMillis = 1_725_235_200_000,
        rifleId = "preview-rifle",
        rifleRevision = 1,
        ammunitionId = "preview-ammunition",
        ammunitionRevision = 1,
        scopeProfileId = "preview-scope",
        scopeProfileRevision = 1,
        zeroProfileId = "preview-zero",
        zeroProfileRevision = 1,
        profileSnapshotJson = "{}",
        distanceMetres = 800.0,
        distanceSource = "MANUAL",
        distanceUncertaintyMetres = 0.0,
        calculatedUnit = "MIL",
        calculatedRawValue = 8.0,
        calculatedDialValue = 8.0,
        calculatedClicks = 80,
        actualDialUnit = "MIL",
        actualDialValue = 8.0,
        numberOfShots = 3,
        conditionsJson = "{}",
        confidence = "MEDIUM",
        status = "VERIFIED",
        engineVersion = "preview",
        evidenceSha256 = "preview",
    )

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
    val windClicks: Int,
    val windScopeUnit: AngularUnit,
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
    WindWheel(
        windFromDegrees = windState.windFromDegrees.toDoubleOrNull() ?: 0.0,
        directionOfFireDegrees = windState.directionOfFireDegrees.toDoubleOrNull() ?: 0.0,
        locked = windState.locked,
        onWindFromChanged = { degrees ->
            windState.locked = false
            windState.windFromDegrees = number(WindConvention.normalizeDegrees(degrees))
        },
        height = 218.dp,
    )
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
        "Wind correction",
        windCorrectionLabel(display.wind, display.windClicks, unit, windState.averageSpeedMps),
        accent = MaterialTheme.colorScheme.secondary,
    )
}

@Composable
private fun CalculationAdjustments(
    distance: String,
    onDistanceChanged: (String) -> Unit,
    state: WindFormState,
) {
    DopeCard {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            SectionHeading("Live calculator inputs")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                CompactLiveField(
                    "Range",
                    distance,
                    { onDistanceChanged(sanitiseDecimalInput(it)) },
                    Modifier.weight(1f),
                )
                CompactLiveField(
                    "Fire°",
                    state.directionOfFireDegrees,
                    {
                        state.locked = false
                        state.directionOfFireDegrees = sanitiseDecimalInput(it)
                    },
                    Modifier.weight(1f),
                )
                CompactLiveField(
                    "Wind°",
                    state.windFromDegrees,
                    {
                        state.locked = false
                        state.windFromDegrees = sanitiseDecimalInput(it)
                    },
                    Modifier.weight(1f),
                )
                CompactLiveField(
                    "m/s",
                    state.averageSpeedMps,
                    { setAverageWindSpeed(state, it) },
                    Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun CompactLiveField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.height(58.dp),
        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
        textStyle = MaterialTheme.typography.titleMedium,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        shape = RoundedCornerShape(10.dp),
        colors =
            OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            ),
    )
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
        windClicks = solution.windageScope.clicks,
        windScopeUnit = solution.windageScope.unit,
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
        windClicks = 9,
        windScopeUnit = AngularUnit.MIL,
    )
}

internal fun windCorrectionLabel(
    windValue: Double,
    windClicks: Int,
    displayUnit: String,
    speedMps: String,
): String {
    val direction = if (windClicks < 0) "LEFT" else "RIGHT"
    val clickCount = kotlin.math.abs(windClicks)
    return "${number(kotlin.math.abs(windValue))} $displayUnit · $clickCount clicks $direction · $speedMps m/s"
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

internal fun parsePositiveDistance(value: String): Double? =
    value
        .trim()
        .replace(',', '.')
        .toDoubleOrNull()
        ?.takeIf { it > 0.0 }

internal fun sanitiseDecimalInput(value: String): String {
    val normalized = value.replace(',', '.')
    return buildString {
        normalized.forEachIndexed { index, character ->
            if (character.isAllowedDecimalCharacter(index, this)) {
                append(character)
            }
        }
    }
}

private fun Char.isAllowedDecimalCharacter(
    index: Int,
    current: CharSequence,
): Boolean = isDigit() || (this == '.' && '.' !in current) || (this == '-' && index == 0)

internal fun setAverageWindSpeed(
    state: WindFormState,
    value: String,
) {
    val cleaned = sanitiseDecimalInput(value)
    state.locked = false
    state.averageSpeedMps = cleaned
    state.selectedSpeed = WindSpeedSelection.AVERAGE
    cleaned.toDoubleOrNull()?.takeIf { it >= 0.0 }?.let { average ->
        state.minimumSpeedMps = number(min(state.minimumSpeedMps.toDoubleOrNull() ?: average, average))
        state.maximumSpeedMps = number(max(state.maximumSpeedMps.toDoubleOrNull() ?: average, average))
    }
}

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
