package za.co.dope.ballistics.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import za.co.dope.ballistics.data.ObservationConfidence
import za.co.dope.ballistics.data.ProfileRepository
import za.co.dope.ballistics.data.SessionRepository
import za.co.dope.ballistics.data.VerifiedDataStatus
import za.co.dope.ballistics.domain.BallisticsInputMapper
import za.co.dope.ballistics.domain.ProfileIdentity
import za.co.dope.ballistics.domain.SessionSnapshotFactory
import za.co.dope.ballistics.engine.ResultConfidence
import za.co.dope.ballistics.engine.StandardBallisticsEngine
import za.co.dope.ballistics.engine.WindConvention
import za.co.dope.ballistics.ui.components.DopeCard
import za.co.dope.ballistics.ui.components.DopeField
import za.co.dope.ballistics.ui.components.DopeFieldConfig
import za.co.dope.ballistics.ui.components.DopePrimaryButton
import za.co.dope.ballistics.ui.components.DopeSecondaryButton
import za.co.dope.ballistics.ui.components.DopeStatus
import za.co.dope.ballistics.ui.components.LabelValue
import za.co.dope.ballistics.ui.components.StatusChip

@Composable
@Suppress("CyclomaticComplexMethod", "LongMethod")
fun SessionScreen(
    profileRepository: ProfileRepository?,
    sessionRepository: SessionRepository?,
    windState: WindFormState,
    sessionDraft: SessionDraftState,
    onOpen: (String) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var distance by remember { mutableStateOf(sessionDraft.distanceMetres) }
    var distanceUncertainty by remember { mutableStateOf("1") }
    var inclination by remember { mutableStateOf("0") }
    var actualDial by remember { mutableStateOf(sessionDraft.actualDialValue) }
    var shotCount by remember { mutableStateOf("3") }
    var verticalCentre by remember { mutableStateOf("") }
    var horizontalCentre by remember { mutableStateOf("") }
    var groupSize by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var includeLocation by remember { mutableStateOf(false) }
    var confidence by remember { mutableStateOf(ObservationConfidence.MEDIUM) }
    var status by remember { mutableStateOf(VerifiedDataStatus.VERIFIED) }
    var startedAt by remember { mutableStateOf(System.currentTimeMillis()) }
    var message by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val sessions by sessionRepository?.observeSessions()?.collectAsState(emptyList()) ?: remember {
        mutableStateOf(emptyList())
    }
    val records by sessionRepository?.observeVerifiedDope()?.collectAsState(emptyList()) ?: remember {
        mutableStateOf(emptyList())
    }
    ScreenShell(title = "Sessions", eyebrow = "IMMUTABLE · VERIFIED DOPE") {
        DopeCard {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("SESSION TYPE", style = MaterialTheme.typography.titleMedium)
                LabelValue("Free shoot", "Available now · log actual settings and groups")
                LabelValue("Match", "Planned · ordered targets and rifle changes")
                LabelValue("Shot timer / drills", "Planned · fixed owner-authored cue allowlist")
                StatusChip("This build saves Free shoot sessions only", DopeStatus.INFO)
            }
        }
        DopeCard {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("FREE SHOOT · COMPLETED RANGE EXERCISE", style = MaterialTheme.typography.titleMedium)
                DopeField("Session name", name, { name = it })
                DopeField("Confirmed distance", distance, { distance = it }, config = DopeFieldConfig(suffix = "m"))
                DopeField(
                    "Distance uncertainty",
                    distanceUncertainty,
                    { distanceUncertainty = it },
                    config = DopeFieldConfig(suffix = "m"),
                )
                DopeField("Inclination", inclination, { inclination = it }, config = DopeFieldConfig(suffix = "°"))
                DopeField(
                    "Actual dialled setting",
                    actualDial,
                    { actualDial = it },
                    config = DopeFieldConfig(suffix = "scope unit"),
                )
                DopeField("Number of shots", shotCount, { shotCount = it })
                DopeField(
                    "Group centre vertical",
                    verticalCentre,
                    { verticalCentre = it },
                    config = DopeFieldConfig(suffix = "m"),
                )
                DopeField(
                    "Group centre horizontal",
                    horizontalCentre,
                    { horizontalCentre = it },
                    config = DopeFieldConfig(suffix = "m"),
                )
                DopeField("Group size", groupSize, { groupSize = it }, config = DopeFieldConfig(suffix = "m"))
                DopeField("Notes", notes, { notes = it })
            }
        }
        Text("CONFIDENCE", style = MaterialTheme.typography.labelLarge)
        ChoiceRow(ObservationConfidence.entries, confidence, { confidence = it }) { it.name }
        Text("RECORD STATUS", style = MaterialTheme.typography.labelLarge)
        ChoiceRow(VerifiedDataStatus.entries, status, { status = it }) { it.name.replace('_', ' ') }
        FilterChip(
            selected = includeLocation,
            onClick = { includeLocation = !includeLocation },
            label = { Text("Include precise location when profile permits") },
        )
        StatusChip(
            if (windState.locked) "Locked wind will be snapshotted" else "Lock or confirm wind before saving",
            if (windState.locked) DopeStatus.READY else DopeStatus.WARNING,
        )
        DopePrimaryButton(
            "Save immutable session",
            {
                coroutineScope.launch {
                    val saved =
                        runCatching {
                            val profiles =
                                requireNotNull(profileRepository?.calculationContext()) {
                                    "Verified linked profiles and a current environment are required."
                                }
                            val resolvedWind =
                                WindConvention.resolve(
                                    requireNotNull(windState.observation()) { "Complete the wind values." },
                                )
                            val targetDistance = requireNotNull(distance.toDoubleOrNull()) { "Distance is invalid." }
                            val angle = inclination.toDoubleOrNull() ?: 0.0
                            val mapped =
                                BallisticsInputMapper.build(
                                    ammunition = profiles.ammunition,
                                    scope = profiles.scope,
                                    zero = profiles.zero,
                                    reference = profiles.referenceAtmosphere,
                                    current = profiles.currentEnvironment,
                                    targetRangeMeters = targetDistance,
                                    inclinationDegrees = angle,
                                    wind = resolvedWind.asEngineWind(),
                                )
                            val result =
                                StandardBallisticsEngine().solve(
                                    requireNotNull(mapped.input) { mapped.issues.joinToString(" ") },
                                )
                            require(result.confidence == ResultConfidence.CONFIDENT && result.solution != null) {
                                result.issues.joinToString(" ").ifBlank { "Calculation is blocked." }
                            }
                            val now = System.currentTimeMillis()
                            val session =
                                SessionSnapshotFactory.createSession(
                                    id = ProfileIdentity.newId(),
                                    sessionName = name.ifBlank { "Range session" },
                                    startedAtEpochMillis = startedAt,
                                    completedAtEpochMillis = now,
                                    profile = profiles,
                                    distanceMetres = targetDistance,
                                    distanceSource = "MANUAL_CONFIRMED",
                                    distanceUncertaintyMetres = distanceUncertainty.toDoubleOrNull() ?: 0.0,
                                    directionOfFireTrueDegrees = resolvedWind.directionOfFireTrueDegrees,
                                    inclinationDegrees = angle,
                                    wind = resolvedWind,
                                    result = result,
                                    includePreciseLocation = includeLocation,
                                    notes = notes.ifBlank { null },
                                )
                            val stored = requireNotNull(sessionRepository).appendSession(session)
                            actualDial.toDoubleOrNull()?.let { actual ->
                                sessionRepository.appendVerifiedDope(
                                    SessionSnapshotFactory.createVerifiedDope(
                                        id = ProfileIdentity.newId(),
                                        session = stored,
                                        result = result,
                                        actualDialValue = actual,
                                        actualDialClicks = null,
                                        observedVerticalMetres = verticalCentre.toDoubleOrNull(),
                                        observedHorizontalMetres = horizontalCentre.toDoubleOrNull(),
                                        groupSizeMetres = groupSize.toDoubleOrNull(),
                                        numberOfShots =
                                            requireNotNull(shotCount.toIntOrNull()) {
                                                "Shot count is invalid."
                                            },
                                        conditionsJson = stored.currentEnvironmentJson,
                                        confidence = confidence,
                                        status = status,
                                        notes = notes.ifBlank { null },
                                        createdAtEpochMillis = now,
                                    ),
                                )
                            }
                            startedAt = now
                            stored
                        }
                    message =
                        saved.fold({ "Session saved with immutable hash ${it.contentSha256.take(8)}…" }, {
                            it.message
                                ?: "Session save failed"
                        })
                }
            },
            Modifier.fillMaxWidth(),
            Icons.Outlined.Add,
            enabled = profileRepository != null && sessionRepository != null,
        )
        message?.let { StatusChip(it, if (it.startsWith("Session saved")) DopeStatus.READY else DopeStatus.BLOCKED) }
        DopeSecondaryButton("Open comparison / what-if", { onOpen("comparison") }, Modifier.fillMaxWidth())
        DopeCard {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    androidx.compose.material3.Icon(Icons.Outlined.History, contentDescription = null)
                    Text("DOPE LOG", style = MaterialTheme.typography.titleMedium)
                }
                LabelValue("Immutable sessions", sessions.size.toString())
                LabelValue("Verified observations", records.size.toString())
                sessions.take(5).forEach { session ->
                    LabelValue(session.sessionName, "${session.distanceMetres.toInt()} m · ${session.engineVersion}")
                }
                if (sessions.isEmpty()) Text("No completed sessions yet.")
            }
        }
    }
}

@Composable
internal fun <T> ChoiceRow(
    values: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    label: (T) -> String,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
        values.forEach { value ->
            FilterChip(
                selected = selected == value,
                onClick = { onSelect(value) },
                label = { Text(label(value)) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}
