@file:Suppress("TooManyFunctions")

package za.co.dope.ballistics.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.FilePresent
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import za.co.dope.ballistics.data.ProfileRepository
import za.co.dope.ballistics.data.db.AmmunitionEntity
import za.co.dope.ballistics.data.db.RifleEntity
import za.co.dope.ballistics.data.db.ScopeFamilyEntity
import za.co.dope.ballistics.data.db.ScopeProfileEntity
import za.co.dope.ballistics.data.db.ScopeVariantEntity
import za.co.dope.ballistics.data.db.ScopeVerificationEntity
import za.co.dope.ballistics.data.db.StaticTargetEntity
import za.co.dope.ballistics.domain.DataSource
import za.co.dope.ballistics.domain.DialDirection
import za.co.dope.ballistics.domain.DragModel
import za.co.dope.ballistics.domain.ProfileIdentity
import za.co.dope.ballistics.domain.ReadingQuality
import za.co.dope.ballistics.domain.StaticTargetClass
import za.co.dope.ballistics.domain.TwistDirection
import za.co.dope.ballistics.domain.VerificationStatus
import za.co.dope.ballistics.ui.components.DopeCard
import za.co.dope.ballistics.ui.components.DopeField
import za.co.dope.ballistics.ui.components.DopeFieldConfig
import za.co.dope.ballistics.ui.components.DopePrimaryButton
import za.co.dope.ballistics.ui.components.DopeSecondaryButton
import za.co.dope.ballistics.ui.components.DopeStatus
import za.co.dope.ballistics.ui.components.DopeWordmark
import za.co.dope.ballistics.ui.components.LabelValue
import za.co.dope.ballistics.ui.components.StatusChip
import za.co.dope.ballistics.ui.components.TopographicBackground
import za.co.dope.ballistics.ui.theme.DopeDesignTokens
import za.co.dope.ballistics.ui.theme.LocalDopeColors
import java.util.Locale
import kotlin.math.PI

@Composable
fun SplashScreen(onContinue: () -> Unit) {
    TopographicBackground(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 28.dp, vertical = 36.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            StatusChip(label = "Offline first", status = DopeStatus.READY)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                DopeWordmark(modifier = Modifier.fillMaxWidth())
                Text(
                    "FIELD DATA · MEASUREMENT · REVIEW",
                    modifier = Modifier.padding(top = 16.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = LocalDopeColors.current.textMuted,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Built for deliberate range work. Confirm every profile before relying on a result.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                DopePrimaryButton(
                    label = "Enter DOPE",
                    onClick = onContinue,
                    modifier = Modifier.fillMaxWidth(),
                    icon = Icons.Outlined.ChevronRight,
                )
            }
        }
    }
}

@Composable
fun DashboardScreen(onOpen: (String) -> Unit) {
    ScreenShell(title = "Field dashboard", eyebrow = "DESIGN PREVIEW") {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatusChip("Profiles incomplete", DopeStatus.WARNING)
            StatusChip("Offline", DopeStatus.READY)
        }
        DopeCard {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeading("Active setup")
                LabelValue("Rifle", "Add rifle")
                LabelValue("Ammunition", "Add ammunition")
                LabelValue("Scope", "Not verified", valueColor = DopeDesignTokens.Colors.Warning)
                LabelValue("Zero", "Not set")
            }
        }
        DopeCard {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeading("Current conditions")
                LabelValue("Environment", "Manual · no reading")
                LabelValue("Data age", "No source")
                LabelValue("Distance", "100 m")
                LabelValue("Wind", "0 m/s · manual")
            }
        }
        DopePrimaryButton(
            label = "Calculate",
            onClick = { onOpen("results") },
            modifier = Modifier.fillMaxWidth(),
            icon = Icons.Outlined.Calculate,
        )
        Text(
            "Calculation shows the exact missing setup or environmental input when it cannot run.",
            style = MaterialTheme.typography.bodyMedium,
            color = DopeDesignTokens.Colors.Warning,
        )
        ActionGrid(
            first = "Range card" to Icons.Outlined.FilePresent,
            second = "Measure target" to Icons.Outlined.Straighten,
            onFirst = { onOpen("range_card") },
            onSecond = { onOpen("target_range") },
        )
        ActionGrid(
            first = "Start session" to Icons.Outlined.PlayArrow,
            second = "Range analyst" to Icons.Outlined.AddAPhoto,
            onFirst = { onOpen("session") },
            onSecond = { onOpen("session") },
        )
    }
}

@Composable
fun ProfilesScreen(
    onOpen: (String) -> Unit,
    rifleCount: Int = 0,
    ammunitionCount: Int = 0,
    scopeCount: Int = 0,
    zeroCount: Int = 0,
) {
    ScreenShell(title = "Profiles", eyebrow = "EQUIPMENT") {
        ProfileCard(
            "Rifle",
            countStatus(rifleCount, "rifle"),
            "Add calibre, barrel length and twist data",
            Icons.Outlined.Flag,
        ) {
            onOpen("rifle")
        }
        ProfileCard(
            "Ammunition",
            countStatus(ammunitionCount, "load"),
            "Add projectile, BC and verified velocity",
            Icons.Outlined.Tune,
        ) {
            onOpen("ammo")
        }
        ProfileCard(
            "Scope",
            countStatus(scopeCount, "scope"),
            "Copy a built-in template, then verify the physical optic",
            Icons.Outlined.Explore,
        ) {
            onOpen("scope")
        }
        ProfileCard(
            "Active setup and zero",
            countStatus(zeroCount, "zero"),
            "Link a rifle, its ammunition, a verified scope and zero reference",
            Icons.Outlined.CheckCircle,
        ) {
            onOpen("zero_setup")
        }
        StatusChip(
            if (scopeCount == 0) "Verification required" else "Review physical verification",
            DopeStatus.WARNING,
        )
    }
}

@Composable
@Suppress("LongMethod")
fun RifleScreen(repository: ProfileRepository? = null) {
    val rifles by repository?.observeRifles()?.collectAsState(emptyList()) ?: remember { mutableStateOf(emptyList()) }
    var name by remember { mutableStateOf("") }
    var manufacturer by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var calibre by remember { mutableStateOf("") }
    var barrelMillimetres by remember { mutableStateOf("") }
    var twistMillimetres by remember { mutableStateOf("") }
    var saveMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    ScreenShell(title = "Rifle profile", eyebrow = "LOCAL DATABASE · SI STORAGE") {
        SavedRifleProfiles(rifles)
        SectionHeading("Add rifle")
        DopeField("Profile name", name, { name = it })
        DopeField("Manufacturer", manufacturer, { manufacturer = it })
        DopeField("Model", model, { model = it })
        DopeField("Calibre / cartridge", calibre, { calibre = it })
        DopeField(
            "Barrel length",
            barrelMillimetres,
            { barrelMillimetres = it },
            config = DopeFieldConfig(suffix = "mm"),
        )
        DopeField(
            "Twist rate",
            twistMillimetres,
            { twistMillimetres = it },
            config = DopeFieldConfig(suffix = "mm / turn"),
        )
        saveMessage?.let { StatusChip(it, if (it == "Rifle saved") DopeStatus.READY else DopeStatus.BLOCKED) }
        DopePrimaryButton(
            "Save rifle",
            {
                val barrel = barrelMillimetres.toDoubleOrNull()?.div(1000.0)
                val twist = twistMillimetres.toDoubleOrNull()?.div(1000.0)
                if (repository == null || barrel == null || twist == null) {
                    saveMessage = "Complete valid dimensions"
                } else {
                    val now = System.currentTimeMillis()
                    scope.launch {
                        runCatching {
                            repository.saveRifle(
                                RifleEntity(
                                    id = ProfileIdentity.newId(),
                                    profileName = name,
                                    manufacturer = manufacturer,
                                    model = model,
                                    calibreLabel = calibre,
                                    barrelLengthMetres = barrel,
                                    twistRateMetres = twist,
                                    twistDirection = TwistDirection.RIGHT.name,
                                    createdAtEpochMillis = now,
                                    modifiedAtEpochMillis = now,
                                ),
                            )
                        }.onSuccess { saveMessage = "Rifle saved" }
                            .onFailure { saveMessage = it.message ?: "Invalid rifle" }
                    }
                }
            },
            Modifier.fillMaxWidth(),
            Icons.Outlined.CheckCircle,
        )
    }
}

@Composable
@Suppress("LongMethod")
fun AmmunitionScreen(repository: ProfileRepository? = null) {
    val rifles by repository?.observeRifles()?.collectAsState(emptyList()) ?: remember { mutableStateOf(emptyList()) }
    val ammunition by
        repository?.observeAmmunition()?.collectAsState(emptyList()) ?: remember {
            mutableStateOf(emptyList())
        }
    var selectedRifleId by remember { mutableStateOf<String?>(null) }
    var profileName by remember { mutableStateOf("") }
    var bulletName by remember { mutableStateOf("") }
    var bulletWeightGrains by remember { mutableStateOf("") }
    var dragModel by remember { mutableStateOf(DragModel.G7) }
    var ballisticCoefficient by remember { mutableStateOf("") }
    var velocity by remember { mutableStateOf("") }
    var saveMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(rifles) {
        if (rifles.none { it.id == selectedRifleId }) selectedRifleId = rifles.firstOrNull()?.id
    }
    ScreenShell(title = "Ammunition", eyebrow = "LOCAL DATABASE · NO FABRICATED VALUES") {
        SavedAmmunitionProfiles(ammunition, rifles)
        SectionHeading("Add ammunition")
        SectionHeading("Linked rifle")
        if (rifles.isEmpty()) {
            StatusChip("Create a rifle first", DopeStatus.BLOCKED)
        } else {
            ChoiceRow(
                rifles,
                rifles.firstOrNull { it.id == selectedRifleId } ?: rifles.first(),
                { selectedRifleId = it.id },
            ) {
                it.profileName
            }
        }
        DopeField("Load profile name", profileName, { profileName = it })
        DopeField("Bullet name", bulletName, { bulletName = it })
        DopeField(
            "Bullet weight",
            bulletWeightGrains,
            { bulletWeightGrains = it },
            config = DopeFieldConfig(suffix = "gr"),
        )
        SectionHeading("Ballistic coefficient model")
        ChoiceRow(DragModel.entries, dragModel, { dragModel = it }) { it.name }
        DopeField("${dragModel.name} ballistic coefficient", ballisticCoefficient, { ballisticCoefficient = it })
        DopeField("Muzzle velocity", velocity, { velocity = it }, config = DopeFieldConfig(suffix = "m/s"))
        DopeCard {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionHeading("Chronograph status")
                StatusChip(
                    if (velocity.toDoubleOrNull() == null) "Velocity required" else "Manual velocity",
                    DopeStatus.WARNING,
                )
                Text("Add individual readings later to calculate average, median, spread and sample deviation.")
            }
        }
        saveMessage?.let { StatusChip(it, if (it == "Ammunition saved") DopeStatus.READY else DopeStatus.BLOCKED) }
        DopePrimaryButton(
            "Save ammunition",
            {
                val rifle = rifles.firstOrNull { it.id == selectedRifleId }
                val weight = bulletWeightGrains.toDoubleOrNull()?.times(0.00006479891)
                val bc = ballisticCoefficient.toDoubleOrNull()
                val speed = velocity.toDoubleOrNull()
                if (!validAmmunitionInput(repository, rifle, weight, bc, speed)) {
                    saveMessage = "Complete rifle, weight, BC and velocity"
                } else {
                    val validRepository = requireNotNull(repository)
                    val validRifle = requireNotNull(rifle)
                    val validWeight = requireNotNull(weight)
                    val validBc = requireNotNull(bc)
                    val validSpeed = requireNotNull(speed)
                    val now = System.currentTimeMillis()
                    scope.launch {
                        runCatching {
                            validRepository.saveAmmunition(
                                AmmunitionEntity(
                                    id = ProfileIdentity.newId(),
                                    rifleId = validRifle.id,
                                    profileName = profileName,
                                    manufacturer = "User entered",
                                    productLoadName = profileName,
                                    bulletManufacturer = "User entered",
                                    bulletName = bulletName,
                                    bulletWeightKilograms = validWeight,
                                    g1BallisticCoefficient = validBc.takeIf { dragModel == DragModel.G1 },
                                    g7BallisticCoefficient = validBc.takeIf { dragModel == DragModel.G7 },
                                    selectedDragModel = dragModel.name,
                                    muzzleVelocityMetresPerSecond = validSpeed,
                                    createdAtEpochMillis = now,
                                    modifiedAtEpochMillis = now,
                                ),
                            )
                        }.onSuccess { saveMessage = "Ammunition saved" }
                            .onFailure { saveMessage = it.message ?: "Invalid ammunition" }
                    }
                }
            },
            Modifier.fillMaxWidth(),
            Icons.Outlined.CheckCircle,
        )
    }
}

@Composable
private fun SavedRifleProfiles(rifles: List<RifleEntity>) {
    SectionHeading("Saved rifles")
    if (rifles.isEmpty()) {
        StatusChip("No saved rifles", DopeStatus.INFO)
        return
    }
    rifles.forEach { rifle ->
        DopeCard {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SectionHeading(rifle.profileName)
                LabelValue("Rifle", "${rifle.manufacturer} · ${rifle.model}")
                LabelValue("Cartridge", rifle.calibreLabel)
                LabelValue(
                    "Barrel / twist",
                    "${rifle.barrelLengthMetres.times(1000).toInt()} mm · " +
                        "1:${rifle.twistRateMetres.div(0.0254).toInt()}",
                )
            }
        }
    }
}

@Composable
private fun SavedAmmunitionProfiles(
    ammunition: List<AmmunitionEntity>,
    rifles: List<RifleEntity>,
) {
    SectionHeading("Saved loads")
    if (ammunition.isEmpty()) {
        StatusChip("No saved ammunition", DopeStatus.INFO)
        return
    }
    ammunition.forEach { load ->
        val linkedRifle = rifles.firstOrNull { it.id == load.rifleId }
        val coefficient = load.g7BallisticCoefficient ?: load.g1BallisticCoefficient
        DopeCard {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SectionHeading(load.profileName)
                LabelValue("Rifle", linkedRifle?.profileName ?: "Missing linked rifle")
                LabelValue("Bullet", load.bulletName)
                LabelValue(
                    "Drag / velocity",
                    "${load.selectedDragModel} ${coefficient ?: "—"} · " +
                        "${load.muzzleVelocityMetresPerSecond.toInt()} m/s",
                )
            }
        }
    }
}

@Composable
fun ScopeScreen(
    onOpen: (String) -> Unit,
    repository: ProfileRepository? = null,
) {
    val families by
        repository?.observeScopeFamilies()?.collectAsState(emptyList()) ?: remember {
            mutableStateOf(emptyList())
        }
    val profiles by
        repository?.observeScopeProfiles()?.collectAsState(emptyList()) ?: remember {
            mutableStateOf(emptyList())
        }
    var variants by remember { mutableStateOf<List<ScopeVariantEntity>>(emptyList()) }
    var message by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(families) {
        variants = families.flatMap { repository?.scopeVariants(it.id).orEmpty() }
    }
    ScreenShell(title = "Scope profile", eyebrow = "IMMUTABLE BUILT-INS · USER-OWNED COPIES") {
        families.forEach { family ->
            DopeCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionHeading(family.displayName)
                    variants.filter { it.familyId == family.id }.forEach { variant ->
                        LabelValue(variant.reticleName, "${variant.turretUnit} · ${variant.reticleSystem}")
                        DopeSecondaryButton(
                            "Create unverified ${variant.reticleName} profile",
                            {
                                scope.launch {
                                    runCatching {
                                        requireNotNull(repository).createScopeFromTemplate(
                                            variant.id,
                                            "${family.model} · ${variant.reticleName}",
                                            System.currentTimeMillis(),
                                        )
                                    }.onSuccess { message = "Unverified scope copy created" }
                                        .onFailure { message = it.message ?: "Could not create scope" }
                                }
                            },
                            Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
        if (families.isEmpty()) {
            StatusChip("Templates load from Room on first open", DopeStatus.INFO)
        }
        LabelValue("User scope profiles", profiles.size.toString())
        message?.let { StatusChip(it, DopeStatus.READY) }
        StatusChip("Physical verification required", DopeStatus.WARNING)
        DopePrimaryButton("Review scope details", { onOpen("scope_detail") }, Modifier.fillMaxWidth())
    }
}

@Composable
@Suppress("LongMethod")
fun ScopeDetailScreen(repository: ProfileRepository? = null) {
    val profiles by
        repository?.observeScopeProfiles()?.collectAsState(emptyList()) ?: remember {
            mutableStateOf(emptyList())
        }
    var selectedId by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(profiles) {
        if (profiles.none { it.id == selectedId }) selectedId = profiles.firstOrNull()?.id
    }
    val selected = profiles.firstOrNull { it.id == selectedId } ?: profiles.firstOrNull()
    var checks by remember(selected?.id) { mutableStateOf(List(10) { false }) }
    var elevationDirection by
        remember(selected?.id) {
            mutableStateOf(
                runCatching { DialDirection.valueOf(selected?.elevationDialDirection.orEmpty()) }
                    .getOrDefault(DialDirection.UNKNOWN),
            )
        }
    var windageDirection by
        remember(selected?.id) {
            mutableStateOf(
                runCatching { DialDirection.valueOf(selected?.windageDialDirection.orEmpty()) }
                    .getOrDefault(DialDirection.UNKNOWN),
            )
        }
    var message by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    ScreenShell(title = "Scope verification", eyebrow = "PHYSICAL OPTIC CHECKLIST") {
        DopeCard {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeading("Verify on the physical optic")
                if (profiles.isEmpty()) {
                    LabelValue("Profile", "Create a scope profile first")
                } else {
                    ChoiceRow(profiles, requireNotNull(selected), { selectedId = it.id }) { it.profileName }
                    LabelValue("Turret", "${selected.turretUnit} · ${selected.reticleName}")
                    LabelValue("Focal plane", selected.focalPlane)
                    LabelValue(
                        "Elevation click",
                        scopeClickLabel(selected.elevationClickValueRadians, selected.turretUnit),
                    )
                    LabelValue("Windage click", scopeClickLabel(selected.windageClickValueRadians, selected.turretUnit))
                    Text("ELEVATION DIAL DIRECTION", style = MaterialTheme.typography.labelLarge)
                    ChoiceRow(
                        DialDirection.entries.filterNot { it == DialDirection.UNKNOWN },
                        elevationDirection,
                        { elevationDirection = it },
                    ) { it.name.replace('_', ' ') }
                    Text("WINDAGE DIAL DIRECTION", style = MaterialTheme.typography.labelLarge)
                    ChoiceRow(
                        DialDirection.entries.filterNot { it == DialDirection.UNKNOWN },
                        windageDirection,
                        { windageDirection = it },
                    ) { it.name.replace('_', ' ') }
                }
                VerificationChecklistLabels.forEachIndexed { index, label ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = checks[index],
                            onCheckedChange = { checked ->
                                checks = checks.toMutableList().also { it[index] = checked }
                            },
                        )
                        Text(label, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
        StatusChip("Do not assume variant data", DopeStatus.BLOCKED)
        message?.let { StatusChip(it, DopeStatus.READY) }
        DopePrimaryButton(
            "Mark verified",
            {
                val profile = selected ?: return@DopePrimaryButton
                val now = System.currentTimeMillis()
                scope.launch {
                    val result =
                        runCatching {
                            requireNotNull(repository).saveScopeVerification(
                                ScopeVerificationEntity(
                                    id = ProfileIdentity.newId(),
                                    scopeProfileId = profile.id,
                                    physicalModelConfirmed = checks[0],
                                    turretUnitConfirmed = checks[1],
                                    clickValueConfirmed = checks[2],
                                    reticleConfirmed = checks[3],
                                    focalPlaneConfirmed = checks[4],
                                    elevationDialDirectionConfirmed = checks[5],
                                    windageDialDirectionConfirmed = checks[6],
                                    zeroStopConfirmed = checks[7],
                                    sightHeightConfirmed = checks[8],
                                    zeroDistanceConfirmed = checks[9],
                                    verifiedAtEpochMillis = now,
                                ),
                            )
                            repository.saveScopeProfile(
                                profile.copy(
                                    elevationDialDirection = elevationDirection.name,
                                    windageDialDirection = windageDirection.name,
                                    verificationStatus = VerificationStatus.USER_VERIFIED.name,
                                    verificationDateEpochMillis = now,
                                    modifiedAtEpochMillis = now,
                                    revision = profile.revision + 1,
                                ),
                            )
                        }
                    message = result.fold({ "Scope verified" }, { it.message ?: "Verification failed" })
                }
            },
            Modifier.fillMaxWidth(),
            Icons.Outlined.CheckCircle,
            enabled =
                selected != null && checks.all { it } &&
                    elevationDirection != DialDirection.UNKNOWN && windageDirection != DialDirection.UNKNOWN,
        )
    }
}

@Composable
fun EnvironmentScreen(
    onOpen: (String) -> Unit,
    repository: ProfileRepository? = null,
    previewMode: Boolean = false,
) = LiveEnvironmentScreen(onOpen, repository, previewMode)

@Composable
fun CameraCalibrationScreen() {
    ScreenShell(title = "Camera calibration", eyebrow = "STATIC TARGETS ONLY") {
        DopeCard {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeading("Selected lens")
                LabelValue("Camera", "Not inspected")
                LabelValue("Physical lens", "Not selected")
                LabelValue("Calibration", "Required")
            }
        }
        StatusChip("No digital-zoom assumption", DopeStatus.READY)
        DopePrimaryButton("Inspect camera capabilities", {}, Modifier.fillMaxWidth(), Icons.Outlined.CameraAlt)
        DopeSecondaryButton("Begin calibration", {}, Modifier.fillMaxWidth(), Icons.Outlined.Straighten)
    }
}

@Composable
@Suppress("LongMethod")
fun TargetRangeScreen(
    onOpen: (String) -> Unit,
    repository: ProfileRepository? = null,
) {
    var selected by remember { mutableStateOf(TargetPresets.first()) }
    var targetName by remember { mutableStateOf("") }
    var width by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var distance by remember { mutableStateOf("") }
    var saveMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    ScreenShell(title = "Target range", eyebrow = "STATIC TARGET · USER CONFIRMATION") {
        StatusChip("Camera ranging · Milestone 6", DopeStatus.INFO)
        TargetPresetSelector(selected = selected) { preset ->
            selected = preset
            width = preset.width
            height = preset.height
        }
        DopeField("Target name", targetName, { targetName = it })
        DopeField("Known width", width, { width = it }, config = DopeFieldConfig(suffix = "mm"))
        DopeField(
            "Known height / diameter",
            height,
            { height = it },
            config = DopeFieldConfig(suffix = "mm"),
        )
        DopeField("Confirmed distance", distance, { distance = it }, config = DopeFieldConfig(suffix = "m"))
        DopeCard {
            Text(
                "Measure the physical target when possible. Presets are starting values, never silent " +
                    "assumptions. A confirmed range is added to the target's DOPE distance list; " +
                    "unconfirmed camera measurements remain inactive.",
            )
        }
        saveMessage?.let {
            val status = if (it == "Target saved with DOPE distance") DopeStatus.READY else DopeStatus.BLOCKED
            StatusChip(it, status)
        }
        DopePrimaryButton(
            "Save target and DOPE distance",
            {
                val widthMetres = width.toDoubleOrNull()?.div(1000.0)
                val heightMetres = height.toDoubleOrNull()?.div(1000.0)
                val distanceMetres = distance.toDoubleOrNull()
                if (!validTargetInput(repository, widthMetres, heightMetres, distanceMetres)) {
                    saveMessage = "Complete confirmed dimensions and distance"
                } else {
                    val validRepository = requireNotNull(repository)
                    val validWidth = requireNotNull(widthMetres)
                    val validHeight = requireNotNull(heightMetres)
                    val validDistance = requireNotNull(distanceMetres)
                    val now = System.currentTimeMillis()
                    scope.launch {
                        runCatching {
                            validRepository.saveStaticTarget(
                                StaticTargetEntity(
                                    id = ProfileIdentity.newId(),
                                    name = targetName,
                                    targetClass = targetClassFor(selected),
                                    physicalWidthMetres = validWidth,
                                    physicalHeightMetres = validHeight,
                                    measuredDistanceMetres = validDistance,
                                    distanceSource = DataSource.MANUAL.name,
                                    distanceQuality = ReadingQuality.GOOD.name,
                                    distanceMeasuredAtEpochMillis = now,
                                    distanceConfirmed = true,
                                    includeDistanceInDope = true,
                                    createdAtEpochMillis = now,
                                    modifiedAtEpochMillis = now,
                                ),
                            )
                        }.onSuccess { saveMessage = "Target saved with DOPE distance" }
                            .onFailure { saveMessage = it.message ?: "Invalid target" }
                    }
                }
            },
            Modifier.fillMaxWidth(),
            Icons.Outlined.CheckCircle,
        )
        DopeSecondaryButton(
            "Open camera calibration",
            { onOpen("camera_calibration") },
            Modifier.fillMaxWidth(),
            Icons.Outlined.CameraAlt,
        )
    }
}

private data class TargetPreset(
    val label: String,
    val detail: String,
    val width: String = "",
    val height: String = "",
)

private val TargetPresets =
    listOf(
        TargetPreset("Manual measurement", "Enter the known width and height"),
        TargetPreset("IDPA cardboard target", "460.4 × 781.1 mm overall · 2026 rulebook", "460.4", "781.1"),
        TargetPreset("A4 paper", "210 × 297 mm · ISO 216", "210", "297"),
        TargetPreset("A3 paper", "297 × 420 mm · ISO 216", "297", "420"),
        TargetPreset("150 mm circular gong", "Common nominal size · confirm physical diameter", "150", "150"),
        TargetPreset("200 mm circular gong", "Common nominal size · confirm physical diameter", "200", "200"),
        TargetPreset("300 mm circular gong", "Common nominal size · confirm physical diameter", "300", "300"),
        TargetPreset("Custom gong", "Enter the measured width and height or diameter"),
    )

private val VerificationChecklistLabels =
    listOf(
        "Physical model confirmed",
        "Turret unit confirmed from markings",
        "Click value confirmed from markings or manual",
        "Reticle variant confirmed",
        "Focal plane confirmed",
        "Elevation dial direction confirmed",
        "Windage dial direction confirmed",
        "Zero stop confirmed or configured",
        "Sight height measured",
        "Zero distance confirmed",
    )

private fun scopeClickLabel(
    radians: Double,
    unit: String,
): String {
    val value = if (unit == "MIL") radians * 1_000.0 else radians * 180.0 / PI * 60.0
    return "${String.format(Locale.US, "%.3f", value).trimEnd('0').trimEnd('.')} $unit / click"
}

private fun countStatus(
    count: Int,
    singular: String,
): String =
    when (count) {
        0 -> "No saved $singular"
        1 -> "1 saved $singular"
        else -> "$count saved ${singular}s"
    }

private fun validAmmunitionInput(
    repository: ProfileRepository?,
    rifle: RifleEntity?,
    weightKilograms: Double?,
    ballisticCoefficient: Double?,
    velocityMetresPerSecond: Double?,
): Boolean =
    listOf(repository, rifle, weightKilograms, ballisticCoefficient, velocityMetresPerSecond).all {
        it != null
    }

private fun validTargetInput(
    repository: ProfileRepository?,
    widthMetres: Double?,
    heightMetres: Double?,
    distanceMetres: Double?,
): Boolean = listOf(repository, widthMetres, heightMetres, distanceMetres).all { it != null }

private fun targetClassFor(preset: TargetPreset): String =
    when {
        "gong" in preset.label.lowercase() -> StaticTargetClass.PAINTED_STEEL.name
        "IDPA" in preset.label -> StaticTargetClass.PRINTED_SILHOUETTE_RANGE_TARGET.name
        "paper" in preset.label.lowercase() -> StaticTargetClass.RECTANGULAR_PAPER.name
        else -> StaticTargetClass.CUSTOM_STATIC_RANGE_TARGET.name
    }

@Composable
private fun TargetPresetSelector(
    selected: TargetPreset,
    onSelect: (TargetPreset) -> Unit,
) {
    DopeCard {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionHeading("Known target size")
            TargetPresets.forEach { preset ->
                DopeSecondaryButton(
                    label = if (selected == preset) "Selected · " + preset.label else preset.label,
                    onClick = { onSelect(preset) },
                    modifier = Modifier.fillMaxWidth(),
                )
                if (selected == preset) {
                    Text(
                        preset.detail,
                        style = MaterialTheme.typography.bodyMedium,
                        color = LocalDopeColors.current.textMuted,
                    )
                }
            }
        }
    }
}

@Composable
fun MoreScreen(
    onOpen: (String) -> Unit,
    onThemeChange: () -> Unit,
) {
    ScreenShell(title = "More", eyebrow = "TOOLS & DISPLAY") {
        UtilityRow("Results shell", "Invalid inputs stay visibly blocked") { onOpen("results") }
        UtilityRow("Range cards", "Offline CSV, PDF and PNG") { onOpen("range_card") }
        UtilityRow("Camera calibration", "Physical-lens capability flow") { onOpen("camera_calibration") }
        UtilityRow("Target range", "Target presets or manual dimensions") { onOpen("target_range") }
        UtilityRow("Wind wheel", "True/magnetic wind-from convention") { onOpen("wind") }
        UtilityRow("Comparison", "Non-destructive what-if calculation") { onOpen("comparison") }
        DopeSecondaryButton("Cycle display mode", onThemeChange, Modifier.fillMaxWidth())
    }
}

@Composable
internal fun ScreenShell(
    title: String,
    eyebrow: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    TopographicBackground(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = DopeDesignTokens.Spacing.ScreenHorizontal, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(DopeDesignTokens.Spacing.Control),
        ) {
            DopeWordmark(modifier = Modifier.fillMaxWidth(0.42f))
            Text(eyebrow, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
            Text(
                title,
                modifier = Modifier.semantics { heading() },
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            content()
        }
    }
}

@Composable
internal fun SectionHeading(text: String) {
    Text(
        text.uppercase(),
        modifier = Modifier.semantics { heading() },
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.secondary,
    )
}

@Composable
private fun ActionGrid(
    first: Pair<String, ImageVector>,
    second: Pair<String, ImageVector>,
    onFirst: () -> Unit,
    onSecond: () -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        DopeSecondaryButton(first.first, onFirst, Modifier.weight(1f), first.second)
        DopeSecondaryButton(second.first, onSecond, Modifier.weight(1f), second.second)
    }
}

@Composable
private fun ProfileCard(
    title: String,
    status: String,
    detail: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    DopeCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Text(status, style = MaterialTheme.typography.titleMedium)
            Text(detail, style = MaterialTheme.typography.bodyMedium, color = LocalDopeColors.current.textMuted)
            DopeSecondaryButton("Open $title", onClick, Modifier.fillMaxWidth(), Icons.Outlined.ChevronRight)
        }
    }
}

@Composable
private fun UtilityRow(
    title: String,
    detail: String,
    onClick: () -> Unit,
) {
    DopeCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(detail, style = MaterialTheme.typography.bodyMedium, color = LocalDopeColors.current.textMuted)
            }
            DopeSecondaryButton("Open", onClick, icon = Icons.Outlined.ChevronRight)
        }
    }
}
