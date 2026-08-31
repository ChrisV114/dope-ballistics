package za.co.dope.ballistics.ui.screens

import android.content.Intent
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.CompareArrows
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import za.co.dope.ballistics.BuildConfig
import za.co.dope.ballistics.data.ProfileRepository
import za.co.dope.ballistics.data.RangeCardExportFormat
import za.co.dope.ballistics.data.RangeCardFileExporter
import za.co.dope.ballistics.domain.AngularDisplayUnit
import za.co.dope.ballistics.domain.BallisticsInputMapper
import za.co.dope.ballistics.domain.ConfirmedDistance
import za.co.dope.ballistics.domain.DistanceDisplayUnit
import za.co.dope.ballistics.domain.EnvironmentSelection
import za.co.dope.ballistics.domain.RangeCardColumnSet
import za.co.dope.ballistics.domain.RangeCardDocument
import za.co.dope.ballistics.domain.RangeCardGenerator
import za.co.dope.ballistics.domain.RangeCardLayout
import za.co.dope.ballistics.domain.RangeCardMetadata
import za.co.dope.ballistics.domain.RangeCardRequest
import za.co.dope.ballistics.domain.RangeCardRow
import za.co.dope.ballistics.engine.StandardBallisticsEngine
import za.co.dope.ballistics.ui.components.DopeCard
import za.co.dope.ballistics.ui.components.DopeField
import za.co.dope.ballistics.ui.components.DopeFieldConfig
import za.co.dope.ballistics.ui.components.DopePrimaryButton
import za.co.dope.ballistics.ui.components.DopeSecondaryButton
import za.co.dope.ballistics.ui.components.DopeStatus
import za.co.dope.ballistics.ui.components.StatusChip
import java.util.Locale

@Composable
@Suppress("CyclomaticComplexMethod", "LongMethod")
fun RangeCardScreen(
    repository: ProfileRepository?,
    windState: WindFormState,
    onOpen: (String) -> Unit,
    previewMode: Boolean = false,
) {
    var startDistance by remember { mutableStateOf("100") }
    var endDistance by remember { mutableStateOf("1000") }
    var increment by remember { mutableStateOf("100") }
    var inclination by remember { mutableStateOf("0") }
    var distanceUnit by remember { mutableStateOf(DistanceDisplayUnit.METRES) }
    var environment by remember { mutableStateOf(EnvironmentSelection.CURRENT) }
    var angularUnit by remember { mutableStateOf(AngularDisplayUnit.MIL) }
    var columnSet by remember { mutableStateOf(RangeCardColumnSet.FIELD) }
    var layout by remember { mutableStateOf(RangeCardLayout.OUTDOOR) }
    var document by remember { mutableStateOf(if (previewMode) previewDocument() else null) }
    var message by remember { mutableStateOf<String?>(null) }
    var generating by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val exporter = remember(context) { RangeCardFileExporter(context) }
    ScreenShell(title = "Range card", eyebrow = "OFFLINE · VERIFIED INPUTS") {
        DopeCard {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("DISTANCE PLAN", style = MaterialTheme.typography.titleMedium)
                ChoiceRow(DistanceDisplayUnit.entries, distanceUnit, { distanceUnit = it }) { it.name }
                val distanceSuffix = if (distanceUnit == DistanceDisplayUnit.METRES) "m" else "yd"
                DopeField(
                    "Start",
                    startDistance,
                    { startDistance = it },
                    config = DopeFieldConfig(suffix = distanceSuffix),
                )
                DopeField("End", endDistance, { endDistance = it }, config = DopeFieldConfig(suffix = distanceSuffix))
                DopeField("Increment", increment, { increment = it }, config = DopeFieldConfig(suffix = distanceSuffix))
                DopeField("Inclination", inclination, { inclination = it }, config = DopeFieldConfig(suffix = "°"))
            }
        }
        Text("ENVIRONMENT", style = MaterialTheme.typography.labelLarge)
        ChoiceRow(EnvironmentSelection.entries, environment, { environment = it }) { it.name }
        Text("ANGULAR DISPLAY", style = MaterialTheme.typography.labelLarge)
        ChoiceRow(AngularDisplayUnit.entries, angularUnit, { angularUnit = it }) { it.name }
        Text("COLUMNS", style = MaterialTheme.typography.labelLarge)
        ChoiceRow(RangeCardColumnSet.entries, columnSet, { columnSet = it }) { it.name }
        Text("EXPORT LAYOUT", style = MaterialTheme.typography.labelLarge)
        ChoiceRow(RangeCardLayout.entries, layout, { layout = it }) { it.name.replace('_', ' ') }
        StatusChip(
            if (windState.locked) "Using locked manual wind" else "Wind remains editable",
            if (windState.locked) DopeStatus.READY else DopeStatus.WARNING,
        )
        DopePrimaryButton(
            if (generating) "Generating" else "Generate range card",
            {
                coroutineScope.launch {
                    generating = true
                    message = null
                    val result =
                        runCatching {
                            val profile =
                                requireNotNull(repository?.calculationContext()) {
                                    "A verified zero, linked profiles and current environment are required."
                                }
                            val end =
                                toMetres(
                                    requireNotNull(endDistance.toDoubleOrNull()) { "End distance is invalid." },
                                    distanceUnit,
                                )
                            val mapped =
                                BallisticsInputMapper.build(
                                    ammunition = profile.ammunition,
                                    scope = profile.scope,
                                    zero = profile.zero,
                                    reference = profile.referenceAtmosphere,
                                    current = profile.currentEnvironment,
                                    targetRangeMeters = end,
                                    inclinationDegrees = inclination.toDoubleOrNull() ?: 0.0,
                                )
                            val input = requireNotNull(mapped.input) { mapped.issues.joinToString(" ") }
                            val targetDistances =
                                repository.confirmedDopeTargets().mapNotNull { target ->
                                    target.measuredDistanceMetres?.let {
                                        ConfirmedDistance(
                                            it,
                                            target.distanceSource ?: "CONFIRMED_TARGET",
                                            target.distanceConfirmed,
                                        )
                                    }
                                }
                            RangeCardGenerator(StandardBallisticsEngine()).generate(
                                RangeCardRequest(
                                    trajectory = input,
                                    startDistanceMetres =
                                        toMetres(
                                            requireNotNull(startDistance.toDoubleOrNull()) {
                                                "Start distance is invalid."
                                            },
                                            distanceUnit,
                                        ),
                                    endDistanceMetres = end,
                                    incrementMetres =
                                        toMetres(
                                            requireNotNull(increment.toDoubleOrNull()) {
                                                "Increment is invalid."
                                            },
                                            distanceUnit,
                                        ),
                                    confirmedDistances = targetDistances,
                                    windObservation =
                                        requireNotNull(windState.observation()) { "Complete the manual wind values." },
                                    profileLabel =
                                        "${profile.rifle.profileName} · ${profile.ammunition.profileName} · " +
                                            profile.scope.profileName,
                                    appVersion = BuildConfig.VERSION_NAME,
                                    createdAtEpochMillis = System.currentTimeMillis(),
                                    distanceDisplayUnit = distanceUnit,
                                    environmentSelection = environment,
                                    angularDisplayUnit = angularUnit,
                                    columnSet = columnSet,
                                    layout = layout,
                                    reticleHoldValid =
                                        profile.scope.reticleSystem != "BDC" &&
                                            profile.scope.focalPlane == "FFP" &&
                                            profile.scope.reticleSystem == angularUnit.name,
                                ),
                            )
                        }
                    result.fold(
                        onSuccess = {
                            document = it
                            message = "${it.rows.size} rows generated offline"
                        },
                        onFailure = { message = it.message ?: "Range card generation failed" },
                    )
                    generating = false
                }
            },
            Modifier.fillMaxWidth(),
            enabled = !generating && (repository != null || previewMode),
        )
        message?.let {
            StatusChip(it, if (document == null) DopeStatus.BLOCKED else DopeStatus.READY)
        }
        document?.let { card ->
            RangeCardTable(card)
            ExportButtons(card, exporter)
            DopeSecondaryButton(
                "Comparison / what-if",
                { onOpen("comparison") },
                Modifier.fillMaxWidth(),
                Icons.AutoMirrored.Outlined.CompareArrows,
            )
        }
    }
}

@Composable
private fun RangeCardTable(document: RangeCardDocument) {
    DopeCard {
        Column(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(document.metadata.profileLabel, style = MaterialTheme.typography.titleMedium)
            RangeCardTableRow(columns = tableHeadings(document), heading = true)
            document.rows.forEach { row ->
                RangeCardTableRow(columns = tableValues(document, row))
            }
        }
    }
}

@Composable
private fun RangeCardTableRow(
    columns: List<String>,
    heading: Boolean = false,
) {
    val style = if (heading) MaterialTheme.typography.labelLarge else MaterialTheme.typography.bodyMedium
    Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
        columns.forEach { column ->
            Text(column, style = style, modifier = Modifier.width(118.dp))
        }
    }
}

@Composable
private fun ExportButtons(
    document: RangeCardDocument,
    exporter: RangeCardFileExporter,
) {
    val context = LocalContext.current
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        RangeCardExportFormat.entries.forEach { format ->
            DopeSecondaryButton(
                format.name,
                {
                    val file = exporter.export(document, format)
                    context.startActivity(
                        Intent.createChooser(exporter.shareIntent(file, format), "Share DOPE range card"),
                    )
                },
                Modifier.weight(1f),
                Icons.Outlined.FileDownload,
            )
        }
    }
}

private fun text(
    value: Double,
    suffix: String,
): String = String.format(Locale.ROOT, "%.2f %s", value, suffix)

private fun toMetres(
    value: Double,
    unit: DistanceDisplayUnit,
): Double = if (unit == DistanceDisplayUnit.METRES) value else value * 0.9144

private fun displayDistance(
    metres: Double,
    unit: String,
): String = if (unit == DistanceDisplayUnit.YARDS.name) text(metres / 0.9144, "yd") else text(metres, "m")

private fun tableHeadings(document: RangeCardDocument): List<String> =
    when (RangeCardColumnSet.valueOf(document.metadata.columnSet)) {
        RangeCardColumnSet.ESSENTIAL -> {
            listOf("Range", "Dial", "Clicks", "Wind")
        }

        RangeCardColumnSet.FIELD -> {
            listOf("Range", "Raw", "Dial", "Clicks", "Wind", "Wind bracket", "Velocity")
        }

        RangeCardColumnSet.FULL -> {
            listOf(
                "Range",
                "Raw",
                "Dial",
                "Clicks",
                "Reticle",
                "Env Δ",
                "Wind",
                "Wind bracket",
                "TOF",
                "Velocity",
                "Energy",
                "Mach",
                "Flight",
                "Uncertainty",
                "Warning",
            )
        }
    }

private fun tableValues(
    document: RangeCardDocument,
    row: RangeCardRow,
): List<String> {
    val essential =
        listOf(
            displayDistance(row.distanceMetres, document.metadata.distanceUnits),
            text(row.elevationDial, document.metadata.scopeUnits),
            row.elevationClicks.toString(),
            text(row.windSelected, document.metadata.units),
        )
    return when (RangeCardColumnSet.valueOf(document.metadata.columnSet)) {
        RangeCardColumnSet.ESSENTIAL -> {
            essential
        }

        RangeCardColumnSet.FIELD -> {
            listOf(
                essential[0],
                text(row.elevationRaw, document.metadata.units),
                essential[1],
                essential[2],
                essential[3],
                "${text(row.windMinimum, document.metadata.units)}…${text(row.windMaximum, document.metadata.units)}",
                text(row.remainingVelocityMps, "m/s"),
            )
        }

        RangeCardColumnSet.FULL -> {
            listOf(
                essential[0],
                text(row.elevationRaw, document.metadata.units),
                essential[1],
                essential[2],
                row.reticleHold?.let { text(it, document.metadata.units) } ?: "N/A",
                text(row.environmentalDeviation, document.metadata.units),
                essential[3],
                "${text(row.windMinimum, document.metadata.units)}…${text(row.windMaximum, document.metadata.units)}",
                text(row.timeOfFlightSeconds, "s"),
                text(row.remainingVelocityMps, "m/s"),
                text(row.remainingEnergyJoules, "J"),
                text(row.mach, "Mach"),
                row.flightState,
                text(row.elevationUncertainty, document.metadata.units),
                row.warningState,
            )
        }
    }
}

private fun previewDocument() =
    RangeCardDocument(
        metadata =
            RangeCardMetadata(
                appVersion = "preview",
                engineVersion = "dope-point-mass-1.0.0",
                createdAtEpochMillis = 0L,
                units = "MIL",
                scopeUnits = "MIL",
                distanceUnits = "METRES",
                environmentSelection = "CURRENT",
                inclinationDegrees = 0.0,
                windSummary = "312° TRUE; 4 m/s selected",
                columnSet = "FIELD",
                layout = "OUTDOOR",
                profileLabel = "Howa 6.5 Creedmoor · preview only",
                windConvention = "Wind-from",
            ),
        rows =
            listOf(100.0, 300.0, 500.0, 800.0, 1_000.0).mapIndexed { index, distance ->
                RangeCardRow(
                    distanceMetres = distance,
                    elevationRaw = listOf(0.0, 1.1, 3.5, 7.7, 11.5)[index],
                    elevationDial = listOf(0.0, 1.1, 3.5, 7.7, 11.5)[index],
                    elevationClicks = listOf(0, 11, 35, 77, 115)[index],
                    environmentalDeviation = 0.0,
                    windSelected = listOf(0.0, 0.3, 0.8, 1.6, 2.4)[index],
                    windMinimum = 0.0,
                    windMaximum = 0.0,
                    timeOfFlightSeconds = 0.0,
                    remainingVelocityMps = listOf(809.0, 650.0, 530.0, 390.0, 330.0)[index],
                    remainingEnergyJoules = 0.0,
                    mach = 0.0,
                    flightState = "PREVIEW",
                    elevationUncertainty = 0.0,
                    warningState = "PREVIEW",
                )
            },
        issues = listOf("Preview data only"),
    )
