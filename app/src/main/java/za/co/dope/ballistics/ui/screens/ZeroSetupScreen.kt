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
import za.co.dope.ballistics.data.db.RifleEntity
import za.co.dope.ballistics.data.db.ScopeProfileEntity
import za.co.dope.ballistics.data.db.ZeroProfileEntity
import za.co.dope.ballistics.domain.VerificationStatus
import za.co.dope.ballistics.ui.components.DopeCard
import za.co.dope.ballistics.ui.components.DopeField
import za.co.dope.ballistics.ui.components.DopeFieldConfig
import za.co.dope.ballistics.ui.components.DopePrimaryButton
import za.co.dope.ballistics.ui.components.DopeStatus
import za.co.dope.ballistics.ui.components.LabelValue
import za.co.dope.ballistics.ui.components.StatusChip

@Composable
@Suppress("LongMethod", "CyclomaticComplexMethod")
fun ZeroSetupScreen(repository: ProfileRepository?) {
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
    var rifleId by remember { mutableStateOf<String?>(null) }
    var ammunitionId by remember { mutableStateOf<String?>(null) }
    var scopeId by remember { mutableStateOf<String?>(null) }
    var zeroDistance by remember { mutableStateOf("") }
    var sightHeightMillimetres by remember { mutableStateOf("") }
    var referenceName by remember { mutableStateOf("Zero reference") }
    var temperatureCelsius by remember { mutableStateOf("") }
    var stationPressureHpa by remember { mutableStateOf("") }
    var humidityPercent by remember { mutableStateOf("") }
    var altitudeMetres by remember { mutableStateOf("") }
    var confirmed by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val availableAmmunition = ammunition.filter { it.rifleId == rifleId }
    val verifiedScopes = scopes.filter { it.verificationStatus == VerificationStatus.USER_VERIFIED.name }

    LaunchedEffect(rifles) {
        if (rifles.none { it.id == rifleId }) rifleId = rifles.firstOrNull()?.id
    }
    LaunchedEffect(rifleId, ammunition) {
        if (availableAmmunition.none { it.id == ammunitionId }) ammunitionId = availableAmmunition.firstOrNull()?.id
    }
    LaunchedEffect(verifiedScopes) {
        if (verifiedScopes.none { it.id == scopeId }) scopeId = verifiedScopes.firstOrNull()?.id
    }

    ScreenShell(title = "Active setup and zero", eyebrow = "EXPLICIT LINKS · USER CONFIRMED") {
        SetupChoices("Rifle", rifles, rifleId, { it.id }, { it.profileName }) { rifleId = it }
        SetupChoices(
            "Ammunition for selected rifle",
            availableAmmunition,
            ammunitionId,
            { it.id },
            { "${it.profileName} · ${it.selectedDragModel}" },
        ) { ammunitionId = it }
        SetupChoices(
            "Physically verified scope",
            verifiedScopes,
            scopeId,
            { it.id },
            { "${it.profileName} · ${it.turretUnit}" },
        ) { scopeId = it }
        if (scopes.isNotEmpty() && verifiedScopes.isEmpty()) {
            StatusChip("Verify a scope before creating a zero", DopeStatus.BLOCKED)
        }

        DopeCard {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeading("Confirmed rifle zero")
                DopeField(
                    "Zero distance",
                    zeroDistance,
                    { zeroDistance = it },
                    config = DopeFieldConfig(suffix = "m"),
                )
                DopeField(
                    "Sight height above bore",
                    sightHeightMillimetres,
                    { sightHeightMillimetres = it },
                    config = DopeFieldConfig(suffix = "mm"),
                )
                Text(
                    "Measure from bore centre to optic centre. This value is separate from target size.",
                    style = MaterialTheme.typography.bodySmall,
                )
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
                    "Use station pressure, not sea-level corrected pressure. Values remain editable " +
                        "and source-labelled manual.",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        androidx.compose.foundation.layout.Row {
            Checkbox(checked = confirmed, onCheckedChange = { confirmed = it })
            Text("I confirm this rifle, load, scope, zero and reference atmosphere", modifier = Modifier.fillMaxWidth())
        }
        message?.let { StatusChip(it, if (it == "Active setup saved") DopeStatus.READY else DopeStatus.BLOCKED) }
        DopePrimaryButton(
            "Save and make active",
            {
                val entry =
                    runCatching {
                        ZeroSetupEntry(
                            rifleId = requireNotNull(rifleId),
                            ammunitionId = requireNotNull(ammunitionId),
                            scopeProfileId = requireNotNull(scopeId),
                            zeroDistanceMetres = requireNotNull(zeroDistance.toDoubleOrNull()),
                            sightHeightAboveBoreMetres =
                                requireNotNull(sightHeightMillimetres.toDoubleOrNull()) / 1000.0,
                            referenceName = referenceName,
                            referenceTemperatureCelsius = requireNotNull(temperatureCelsius.toDoubleOrNull()),
                            referenceStationPressureHectopascals = requireNotNull(stationPressureHpa.toDoubleOrNull()),
                            referenceHumidityPercent = requireNotNull(humidityPercent.toDoubleOrNull()),
                            referenceAltitudeMetres = requireNotNull(altitudeMetres.toDoubleOrNull()),
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
