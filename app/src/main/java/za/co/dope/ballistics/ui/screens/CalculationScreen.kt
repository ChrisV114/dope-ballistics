package za.co.dope.ballistics.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
import za.co.dope.ballistics.ui.components.ResultPanel
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
) {
    val active by
        repository?.observeActiveProfileSelection()?.collectAsState(null) ?: remember {
            mutableStateOf<ActiveProfileSelectionEntity?>(null)
        }
    val environments by
        repository?.observeEnvironmentalSnapshots()?.collectAsState(emptyList()) ?: remember {
            mutableStateOf(emptyList())
        }
    var distance by remember { mutableStateOf("100") }
    var confirmedDistances by remember { mutableStateOf<List<Double>>(emptyList()) }
    var result by remember { mutableStateOf<TrajectoryResult?>(null) }
    var profileLabel by remember { mutableStateOf<String?>(null) }
    var issues by remember { mutableStateOf<List<String>>(emptyList()) }

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
        if (repository == null) return@LaunchedEffect
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

    ScreenShell(title = "Calculation", eyebrow = "ACTIVE SETUP · LIVE FIELD INPUTS") {
        profileLabel?.let { StatusChip(it, DopeStatus.READY) }
        DopeCard {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeading("Target distance")
                DopeField("Distance", distance, { distance = it }, config = DopeFieldConfig(suffix = "m"))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DopeSecondaryButton("− 5 m", { distance = adjusted(distance, -5.0) }, Modifier.weight(1f))
                    DopeSecondaryButton("+ 5 m", { distance = adjusted(distance, 5.0) }, Modifier.weight(1f))
                }
                confirmedDistances.take(6).forEach { metres ->
                    DopeSecondaryButton(
                        "Use confirmed target · ${number(metres)} m",
                        { distance = number(metres) },
                        Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        LiveWindCard(windState, onOpen)

        val solution = result?.solution
        if (solution != null) {
            ResultPanel(
                number(solution.elevationScope.rounded),
                solution.elevationScope.unit.name,
                "Elevation dial",
                status = DopeStatus.READY,
            )
            ResultPanel(
                number(solution.windageScope.rounded),
                solution.windageScope.unit.name,
                "Wind dial / hold",
                status = DopeStatus.READY,
            )
            DopeCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionHeading("Field result")
                    LabelValue("Elevation clicks", solution.elevationScope.clicks.toString())
                    LabelValue("Wind clicks", solution.windageScope.clicks.toString())
                    LabelValue(
                        "Environmental deviation",
                        angular(solution.environmentalDeviationRadians, solution.elevationScope.unit),
                    )
                    LabelValue(
                        "Reference elevation",
                        angular(solution.referenceElevationRadians, solution.elevationScope.unit),
                    )
                    LabelValue(
                        "Current elevation",
                        angular(solution.currentElevationRadians, solution.elevationScope.unit),
                    )
                    LabelValue("Remaining velocity", "${number(solution.remainingVelocityMps)} m/s")
                    LabelValue("Confidence", result?.confidence?.name.orEmpty())
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
            DopeCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionHeading("Calculation trace")
                    LabelValue("Engine", result?.trace?.engineVersion.orEmpty())
                    LabelValue(
                        "Drag model",
                        result
                            ?.trace
                            ?.dragModel
                            ?.name
                            .orEmpty(),
                    )
                    LabelValue("Integration", result?.trace?.integrationMethod.orEmpty())
                    LabelValue("Deterministic", result?.trace?.deterministic.toString())
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
private fun LiveWindCard(
    state: WindFormState,
    onOpen: (String) -> Unit,
) {
    val observation = state.observation()
    val resolved = observation?.let { runCatching { WindConvention.resolve(it) }.getOrNull() }
    DopeCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionHeading("Live wind")
            LabelValue("Average", "${state.averageSpeedMps} m/s")
            LabelValue("Wind from", "${state.windFromDegrees}°")
            resolved?.let {
                LabelValue("Relative", "${number(it.relativeWindFromDegrees)}° · ${it.effect.name}")
                LabelValue("Crosswind", "${number(it.selected.crosswindMps)} m/s")
                LabelValue("Headwind", "${number(it.selected.headwindMps)} m/s")
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

private fun angular(
    radians: Double,
    unit: AngularUnit,
): String {
    val value =
        when (unit) {
            AngularUnit.MIL -> radians * 1_000.0
            AngularUnit.MOA -> radians * 180.0 / PI * 60.0
        }
    return "${number(value)} ${unit.name}"
}

private fun number(value: Double): String = String.format(Locale.US, "%.2f", value).trimEnd('0').trimEnd('.')
