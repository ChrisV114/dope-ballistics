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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import za.co.dope.ballistics.ui.components.DopeCard
import za.co.dope.ballistics.ui.components.DopeField
import za.co.dope.ballistics.ui.components.DopeFieldConfig
import za.co.dope.ballistics.ui.components.DopePrimaryButton
import za.co.dope.ballistics.ui.components.DopeSecondaryButton
import za.co.dope.ballistics.ui.components.DopeStatus
import za.co.dope.ballistics.ui.components.DopeWordmark
import za.co.dope.ballistics.ui.components.LabelValue
import za.co.dope.ballistics.ui.components.ResultPanel
import za.co.dope.ballistics.ui.components.StatusChip
import za.co.dope.ballistics.ui.components.TopographicBackground
import za.co.dope.ballistics.ui.theme.DopeDesignTokens
import za.co.dope.ballistics.ui.theme.LocalDopeColors

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
            enabled = false,
        )
        Text(
            "Complete and verify profiles before a confident result can be calculated.",
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
fun ProfilesScreen(onOpen: (String) -> Unit) {
    ScreenShell(title = "Profiles", eyebrow = "EQUIPMENT") {
        ProfileCard("Rifle", "No active rifle", "Add calibre, barrel and sight height", Icons.Outlined.Flag) {
            onOpen("rifle")
        }
        ProfileCard("Ammunition", "No active load", "Add projectile and verified velocity", Icons.Outlined.Tune) {
            onOpen("ammo")
        }
        ProfileCard("Scope", "Unverified", "Select family, variant and adjustment units", Icons.Outlined.Explore) {
            onOpen("scope")
        }
        StatusChip("Verification required", DopeStatus.WARNING)
    }
}

@Composable
fun RifleScreen() {
    var name by remember { mutableStateOf("Field rifle") }
    ScreenShell(title = "Rifle profile", eyebrow = "PROFILE SHELL") {
        DopeField("Profile name", name, { name = it })
        DopeField("Calibre", ".308 Winchester", {}, config = DopeFieldConfig(readOnly = true))
        DopeField(
            "Barrel length",
            "508",
            {},
            config = DopeFieldConfig(suffix = "mm", readOnly = true),
        )
        DopeField(
            "Sight height",
            "38",
            {},
            config = DopeFieldConfig(suffix = "mm", readOnly = true),
        )
        DopePrimaryButton("Save rifle", {}, Modifier.fillMaxWidth(), Icons.Outlined.CheckCircle)
    }
}

@Composable
fun AmmunitionScreen() {
    ScreenShell(title = "Ammunition", eyebrow = "PROFILE SHELL") {
        DopeField("Load name", "Training load", {}, config = DopeFieldConfig(readOnly = true))
        DopeField("Projectile", "168 gr match", {}, config = DopeFieldConfig(readOnly = true))
        DopeField(
            "Muzzle velocity",
            "—",
            {},
            config = DopeFieldConfig(suffix = "m/s", readOnly = true),
        )
        DopeCard {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionHeading("Chronograph status")
                StatusChip("Velocity required", DopeStatus.BLOCKED)
                Text("A confident trajectory remains blocked until a velocity source is recorded.")
            }
        }
        DopePrimaryButton("Add chronograph string", {}, Modifier.fillMaxWidth())
    }
}

@Composable
fun ScopeScreen(onOpen: (String) -> Unit) {
    ScreenShell(title = "Scope profile", eyebrow = "PROFILE SHELL") {
        DopeField("Family", "Select DNT or Arken", {}, config = DopeFieldConfig(readOnly = true))
        DopeField("Variant", "Not selected", {}, config = DopeFieldConfig(readOnly = true))
        DopeField("Adjustment", "MIL or MOA", {}, config = DopeFieldConfig(readOnly = true))
        StatusChip("Physical verification required", DopeStatus.WARNING)
        DopePrimaryButton("Review scope details", { onOpen("scope_detail") }, Modifier.fillMaxWidth())
    }
}

@Composable
fun ScopeDetailScreen() {
    ScreenShell(title = "Scope verification", eyebrow = "DETAIL SHELL") {
        DopeCard {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeading("Verify on the physical optic")
                LabelValue("Turret unit", "Unconfirmed")
                LabelValue("Click value", "Unconfirmed")
                LabelValue("Total travel", "Unconfirmed")
                LabelValue("Reticle", "Unconfirmed")
            }
        }
        StatusChip("Do not assume variant data", DopeStatus.BLOCKED)
        DopeSecondaryButton("Open verification checklist", {}, Modifier.fillMaxWidth())
        DopePrimaryButton("Mark verified", {}, Modifier.fillMaxWidth(), enabled = false)
    }
}

@Composable
fun EnvironmentScreen(onOpen: (String) -> Unit) {
    ScreenShell(title = "Environment", eyebrow = "SOURCE · AGE · VALUE") {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatusChip("Manual", DopeStatus.INFO)
            StatusChip("No live source", DopeStatus.WARNING)
        }
        DopeCard {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                LabelValue("Temperature", "20 °C · manual")
                LabelValue("Pressure", "1013.25 hPa · standard")
                LabelValue("Humidity", "50 % · manual")
                LabelValue("Altitude", "0 m · manual")
                LabelValue("Updated", "Design preview")
            }
        }
        DopePrimaryButton("Edit conditions", {}, Modifier.fillMaxWidth(), Icons.Outlined.WaterDrop)
        DopeSecondaryButton("Open wind", { onOpen("wind") }, Modifier.fillMaxWidth(), Icons.Outlined.Explore)
    }
}

@Composable
fun WindScreen() {
    ScreenShell(title = "Wind", eyebrow = "MANUAL INPUT SHELL") {
        ResultPanel("0.0", "m/s", "Wind speed", status = DopeStatus.INFO)
        DopeField("Direction", "12 o’clock", {}, config = DopeFieldConfig(readOnly = true))
        DopeField(
            "Reference",
            "True / magnetic not selected",
            {},
            config = DopeFieldConfig(readOnly = true),
        )
        DopeCard {
            Text(
                "Wind provenance and convention must be explicit. This shell does not calculate a hold.",
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
fun ResultsScreen() {
    ScreenShell(title = "Result", eyebrow = "BLOCKED PREVIEW") {
        ResultPanel("—", "MIL", "Elevation", status = DopeStatus.BLOCKED)
        ResultPanel("—", "MIL", "Wind", status = DopeStatus.BLOCKED)
        DopeCard {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                StatusChip("Cannot calculate", DopeStatus.BLOCKED)
                Text("Rifle, ammunition, scope verification and zero are incomplete.")
                LabelValue("Atmosphere", "No current snapshot")
                LabelValue("Engine", "Not implemented · Milestone 4")
            }
        }
        DopeSecondaryButton("Copy calculation details", {}, Modifier.fillMaxWidth())
    }
}

@Composable
fun RangeCardScreen() {
    ScreenShell(title = "Range card", eyebrow = "EXPORT SHELL") {
        DopeCard {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionHeading("Distance table")
                LabelValue("100 m", "—")
                LabelValue("200 m", "—")
                LabelValue("300 m", "—")
                LabelValue("400 m", "—")
            }
        }
        StatusChip("Awaiting valid calculation", DopeStatus.WARNING)
        DopePrimaryButton("Generate range card", {}, Modifier.fillMaxWidth(), enabled = false)
    }
}

@Composable
fun SessionScreen() {
    ScreenShell(title = "Sessions", eyebrow = "COMPLETED-STRING REVIEW") {
        DopeCard {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeading("No active session")
                Text("Start a deliberate range log with immutable setup and environment snapshots.")
                StatusChip("Analysis only after string ends", DopeStatus.READY)
            }
        }
        DopePrimaryButton("Start session", {}, Modifier.fillMaxWidth(), Icons.Outlined.PlayArrow)
        DopeSecondaryButton("Review completed session", {}, Modifier.fillMaxWidth(), Icons.Outlined.AddAPhoto)
    }
}

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
fun TargetRangeScreen(onOpen: (String) -> Unit) {
    var selected by remember { mutableStateOf(TargetPresets.first()) }
    var width by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    ScreenShell(title = "Target range", eyebrow = "STATIC TARGET · USER CONFIRMATION") {
        StatusChip("Camera ranging · Milestone 6", DopeStatus.INFO)
        TargetPresetSelector(selected = selected) { preset ->
            selected = preset
            width = preset.width
            height = preset.height
        }
        DopeField("Known width", width, { width = it }, config = DopeFieldConfig(suffix = "mm"))
        DopeField(
            "Known height / diameter",
            height,
            { height = it },
            config = DopeFieldConfig(suffix = "mm"),
        )
        DopeCard {
            Text(
                "Measure the physical target when possible. Presets are starting values, never silent " +
                    "assumptions; confirm the chosen dimensions before ranging.",
            )
        }
        DopePrimaryButton("Confirm target dimensions", {}, Modifier.fillMaxWidth(), Icons.Outlined.CheckCircle)
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
        UtilityRow("Range card shell", "Offline export layout") { onOpen("range_card") }
        UtilityRow("Camera calibration", "Physical-lens capability flow") { onOpen("camera_calibration") }
        UtilityRow("Target range", "Target presets or manual dimensions") { onOpen("target_range") }
        UtilityRow("Wind shell", "Explicit source and convention") { onOpen("wind") }
        DopeSecondaryButton("Cycle display mode", onThemeChange, Modifier.fillMaxWidth())
    }
}

@Composable
private fun ScreenShell(
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
private fun SectionHeading(text: String) {
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
