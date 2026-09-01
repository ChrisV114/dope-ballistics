package za.co.dope.ballistics.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import za.co.dope.ballistics.data.ProfileRepository
import za.co.dope.ballistics.data.ZeroSetupEntry
import za.co.dope.ballistics.data.db.ActiveProfileSelectionEntity
import za.co.dope.ballistics.data.db.AmmunitionEntity
import za.co.dope.ballistics.data.db.EnvironmentalSnapshotEntity
import za.co.dope.ballistics.data.db.RifleEntity
import za.co.dope.ballistics.data.db.ScopeProfileEntity
import za.co.dope.ballistics.data.db.ZeroProfileEntity
import za.co.dope.ballistics.domain.VerificationStatus
import za.co.dope.ballistics.ui.components.DopeCard
import za.co.dope.ballistics.ui.components.DopeField
import za.co.dope.ballistics.ui.components.DopeFieldConfig
import za.co.dope.ballistics.ui.components.DopePrimaryButton
import za.co.dope.ballistics.ui.components.DopeSecondaryButton
import za.co.dope.ballistics.ui.components.DopeStatus
import za.co.dope.ballistics.ui.components.LabelValue
import za.co.dope.ballistics.ui.components.StatusChip
import java.util.Locale

@Composable
@Suppress("LongMethod", "CyclomaticComplexMethod")
fun ZeroSetupScreen(
    repository: ProfileRepository?,
    draftState: SetupDraftState,
) {
    val rifles by repository?.observeRifles()?.collectAsState(emptyList()) ?: remember { mutableStateOf(emptyList()) }
    val ammunition by
        repository?.observeAmmunition()?.collectAsState(emptyList()) ?: remember { mutableStateOf(emptyList()) }
    val scopes by
        repository?.observeScopeProfiles()?.collectAsState(emptyList()) ?: remember { mutableStateOf(emptyList()) }
    val zeroProfiles by
        repository?.observeZeroProfiles()?.collectAsState(emptyList()) ?: remember { mutableStateOf(emptyList()) }
    val active by
        repository?.observeActiveProfileSelection()?.collectAsState(null) ?: remember {
            mutableStateOf<ActiveProfileSelectionEntity?>(null)
        }
    val environments by
        repository?.observeEnvironmentalSnapshots()?.collectAsState(emptyList()) ?: remember {
            mutableStateOf(emptyList<EnvironmentalSnapshotEntity>())
        }
    var referenceName by remember { mutableStateOf("Zero reference") }
    var temperatureCelsius by remember { mutableStateOf("") }
    var stationPressureHpa by remember { mutableStateOf("") }
    var humidityPercent by remember { mutableStateOf("") }
    var altitudeMetres by remember { mutableStateOf("") }
    var confirmed by remember { mutableStateOf(false) }
    var referenceEstimated by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val availableAmmunition = ammunition.filter { it.rifleId == draftState.rifleId }
    val verifiedScopes = scopes.filter { it.verificationStatus == VerificationStatus.USER_VERIFIED.name }
    val selectedRifle = rifles.firstOrNull { it.id == draftState.rifleId }

    LaunchedEffect(rifles) {
        if (rifles.none { it.id == draftState.rifleId }) draftState.rifleId = rifles.firstOrNull()?.id
    }
    LaunchedEffect(draftState.rifleId, ammunition) {
        if (availableAmmunition.none { it.id == draftState.ammunitionId }) {
            draftState.ammunitionId = availableAmmunition.firstOrNull()?.id
        }
    }
    LaunchedEffect(verifiedScopes) {
        if (verifiedScopes.none { it.id == draftState.scopeId }) draftState.scopeId = verifiedScopes.firstOrNull()?.id
    }
    LaunchedEffect(active, zeroProfiles) {
        zeroProfiles.firstOrNull { it.id == active?.zeroProfileId }?.let { zero ->
            draftState.rifleId = zero.rifleId
            draftState.ammunitionId = zero.ammunitionId
            draftState.scopeId = zero.scopeProfileId
        }
    }

    ScreenShell(title = "Active setup and zero", eyebrow = "EXPLICIT LINKS · USER CONFIRMED") {
        SetupChoices("Rifle", rifles, draftState.rifleId, { it.id }, { it.profileName }) { draftState.rifleId = it }
        SetupChoices(
            "Ammunition for selected rifle",
            availableAmmunition,
            draftState.ammunitionId,
            { it.id },
            { "${it.profileName} · ${it.selectedDragModel}" },
        ) { draftState.ammunitionId = it }
        SetupChoices(
            "Physically verified scope",
            verifiedScopes,
            draftState.scopeId,
            { it.id },
            { "${it.profileName} · ${it.turretUnit}" },
        ) { draftState.scopeId = it }
        if (scopes.isNotEmpty() && verifiedScopes.isEmpty()) {
            StatusChip("Verify a scope before creating a zero", DopeStatus.BLOCKED)
        }

        DopeCard {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeading("Confirmed rifle zero")
                LabelValue(
                    "Zero distance",
                    selectedRifle?.defaultZeroDistanceMetres?.let { "${it.compact()} m" } ?: "Add to rifle profile",
                )
                LabelValue(
                    "Sight height above bore",
                    selectedRifle?.sightHeightAboveBoreMetres?.let { "${(it * 1000).compact()} mm" }
                        ?: "Add to rifle profile",
                )
                Text(
                    "These values come from the selected rifle profile and are copied into the saved zero record.",
                    style = MaterialTheme.typography.bodySmall,
                )
                val rifleGeometryMissing =
                    selectedRifle?.defaultZeroDistanceMetres == null ||
                        selectedRifle.sightHeightAboveBoreMetres == null
                if (rifleGeometryMissing) {
                    StatusChip("Complete zero and sight height on the rifle profile", DopeStatus.BLOCKED)
                }
            }
        }
        DopeCard {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeading("Atmosphere when zero was confirmed")
                DopeField("Reference name", referenceName, { referenceName = it })
                DopeField(
                    "Temperature",
                    temperatureCelsius,
                    { temperatureCelsius = it },
                    config = DopeFieldConfig(suffix = "°C"),
                )
                DopeField(
                    "Station pressure",
                    stationPressureHpa,
                    { stationPressureHpa = it },
                    config = DopeFieldConfig(suffix = "hPa"),
                )
                DopeField(
                    "Relative humidity",
                    humidityPercent,
                    { humidityPercent = it },
                    config = DopeFieldConfig(suffix = "%"),
                )
                DopeField(
                    "Altitude",
                    altitudeMetres,
                    { altitudeMetres = it },
                    config = DopeFieldConfig(suffix = "m"),
                )
                Text(
                    "If the original weather is unknown, use the latest saved current conditions as an explicit " +
                        "estimate. It remains labelled estimated in calculations and exports.",
                    style = MaterialTheme.typography.bodySmall,
                )
                DopeSecondaryButton(
                    "Use latest saved conditions as estimate",
                    {
                        environments.firstOrNull()?.let { environment ->
                            referenceName = "Estimated zero reference"
                            temperatureCelsius = (environment.temperatureKelvin - 273.15).compact()
                            stationPressureHpa = (environment.stationPressurePascals / 100.0).compact()
                            humidityPercent = (environment.relativeHumidityFraction * 100.0).compact()
                            altitudeMetres = environment.altitudeMetres.compact()
                            referenceEstimated = true
                            message = "Estimated reference loaded; review and confirm"
                        } ?: run { message = "Save current conditions on Environment first" }
                    },
                    Modifier.fillMaxWidth(),
                )
                if (referenceEstimated) {
                    StatusChip("Estimated from current conditions", DopeStatus.WARNING)
                }
            }
        }
        androidx.compose.foundation.layout.Row {
            Checkbox(checked = confirmed, onCheckedChange = { confirmed = it })
            Text(
                if (referenceEstimated) {
                    "I confirm the equipment and zero; the reference atmosphere is an estimate"
                } else {
                    "I confirm this rifle, load, scope, zero and reference atmosphere"
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        message?.let {
            val status =
                when {
                    it == "Active setup saved" -> DopeStatus.READY

                    it.startsWith("Estimated reference loaded") ||
                        it.startsWith("Save current conditions") -> DopeStatus.WARNING

                    else -> DopeStatus.BLOCKED
                }
            StatusChip(it, status)
        }
        DopePrimaryButton(
            "Save and make active",
            {
                val entry =
                    runCatching {
                        ZeroSetupEntry(
                            rifleId = requireNotNull(draftState.rifleId),
                            ammunitionId = requireNotNull(draftState.ammunitionId),
                            scopeProfileId = requireNotNull(draftState.scopeId),
                            zeroDistanceMetres =
                                requireNotNull(selectedRifle).let {
                                    requireNotNull(it.defaultZeroDistanceMetres)
                                },
                            sightHeightAboveBoreMetres = requireNotNull(selectedRifle.sightHeightAboveBoreMetres),
                            referenceName = referenceName,
                            referenceTemperatureCelsius = requireNotNull(temperatureCelsius.toDoubleOrNull()),
                            referenceStationPressureHectopascals = requireNotNull(stationPressureHpa.toDoubleOrNull()),
                            referenceHumidityPercent = requireNotNull(humidityPercent.toDoubleOrNull()),
                            referenceAltitudeMetres = requireNotNull(altitudeMetres.toDoubleOrNull()),
                            referenceSource = if (referenceEstimated) "ESTIMATED_FROM_CURRENT" else "MANUAL",
                            referenceNotes =
                                if (referenceEstimated) {
                                    "Owner reported historical zero atmosphere unknown; copied from the latest saved " +
                                        "current conditions as an explicit estimate."
                                } else {
                                    null
                                },
                            verified = confirmed,
                            nowEpochMillis = System.currentTimeMillis(),
                        )
                    }.getOrNull()
                if (repository == null || entry == null || !confirmed) {
                    message = "Complete every field and confirm the setup"
                } else {
                    coroutineScope.launch {
                        runCatching { repository.createAndActivateZero(entry) }
                            .onSuccess { message = "Active setup saved" }
                            .onFailure { message = it.message ?: "Setup could not be saved" }
                    }
                }
            },
            Modifier.fillMaxWidth(),
            enabled = repository != null,
        )

        if (zeroProfiles.isNotEmpty()) {
            DopeCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionHeading("Saved setups")
                    zeroProfiles.forEach { zero ->
                        SavedZeroChoice(zero, active?.zeroProfileId == zero.id, rifles, ammunition, scopes) {
                            coroutineScope.launch {
                                repository?.activateZeroProfile(zero.id, System.currentTimeMillis())
                                message = "Active setup saved"
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun Double.compact(): String = String.format(Locale.US, "%.2f", this).trimEnd('0').trimEnd('.')

@Composable
@Suppress("LongParameterList")
private fun SavedZeroChoice(
    zero: ZeroProfileEntity,
    active: Boolean,
    rifles: List<RifleEntity>,
    ammunition: List<AmmunitionEntity>,
    scopes: List<ScopeProfileEntity>,
    onSelect: () -> Unit,
) {
    val label =
        listOfNotNull(
            rifles.firstOrNull { it.id == zero.rifleId }?.profileName,
            ammunition.firstOrNull { it.id == zero.ammunitionId }?.profileName,
            scopes.firstOrNull { it.id == zero.scopeProfileId }?.profileName,
        ).joinToString(" · ")
    FilterChip(
        selected = active,
        onClick = onSelect,
        label = { Text("$label · ${zero.zeroDistanceMetres.toInt()} m") },
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
@Suppress("LongParameterList")
private fun <T> SetupChoices(
    heading: String,
    values: List<T>,
    selectedId: String?,
    id: (T) -> String,
    label: (T) -> String,
    onSelect: (String) -> Unit,
) {
    DopeCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionHeading(heading)
            if (values.isEmpty()) {
                LabelValue("Status", "Nothing available")
            } else {
                values.forEach { value ->
                    FilterChip(
                        selected = selectedId == id(value),
                        onClick = { onSelect(id(value)) },
                        label = { Text(label(value)) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}
