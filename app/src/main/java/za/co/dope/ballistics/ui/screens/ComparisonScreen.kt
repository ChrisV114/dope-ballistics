package za.co.dope.ballistics.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import za.co.dope.ballistics.data.ProfileRepository
import za.co.dope.ballistics.domain.BallisticsInputMapper
import za.co.dope.ballistics.domain.TrajectoryComparisonService
import za.co.dope.ballistics.engine.StandardBallisticsEngine
import za.co.dope.ballistics.engine.WindConvention
import za.co.dope.ballistics.ui.components.DopeCard
import za.co.dope.ballistics.ui.components.DopeField
import za.co.dope.ballistics.ui.components.DopeFieldConfig
import za.co.dope.ballistics.ui.components.DopePrimaryButton
import za.co.dope.ballistics.ui.components.DopeStatus
import za.co.dope.ballistics.ui.components.LabelValue
import za.co.dope.ballistics.ui.components.StatusChip

@Composable
@Suppress("CyclomaticComplexMethod", "LongMethod")
fun ComparisonScreen(
    repository: ProfileRepository?,
    windState: WindFormState,
) {
    var distance by remember { mutableStateOf("500") }
    var temperature by remember { mutableStateOf("25") }
    var pressure by remember { mutableStateOf("90000") }
    var humidity by remember { mutableStateOf("40") }
    var inclination by remember { mutableStateOf("0") }
    var elevationDelta by remember { mutableStateOf<Double?>(null) }
    var windDelta by remember { mutableStateOf<Double?>(null) }
    var velocityDelta by remember { mutableStateOf<Double?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    ScreenShell(title = "Comparison", eyebrow = "WHAT-IF · DOES NOT ALTER PROFILES") {
        DopeCard {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("ALTERNATIVE CONDITIONS", style = MaterialTheme.typography.titleMedium)
                DopeField("Distance", distance, { distance = it }, config = DopeFieldConfig(suffix = "m"))
                DopeField("Temperature", temperature, { temperature = it }, config = DopeFieldConfig(suffix = "°C"))
                DopeField("Station pressure", pressure, { pressure = it }, config = DopeFieldConfig(suffix = "Pa"))
                DopeField("Relative humidity", humidity, { humidity = it }, config = DopeFieldConfig(suffix = "%"))
                DopeField("Inclination", inclination, { inclination = it }, config = DopeFieldConfig(suffix = "°"))
            }
        }
        DopePrimaryButton(
            "Compare without saving",
            {
                coroutineScope.launch {
                    val compared =
                        runCatching {
                            val profiles =
                                requireNotNull(repository?.calculationContext()) {
                                    "Verified profile inputs are required."
                                }
                            val target = requireNotNull(distance.toDoubleOrNull()) { "Distance is invalid." }
                            val resolved =
                                WindConvention.resolve(
                                    requireNotNull(windState.observation()) { "Complete wind values." },
                                )
                            val baseline =
                                BallisticsInputMapper.build(
                                    profiles.ammunition,
                                    profiles.scope,
                                    profiles.zero,
                                    profiles.referenceAtmosphere,
                                    profiles.currentEnvironment,
                                    target,
                                    inclination.toDoubleOrNull() ?: 0.0,
                                    resolved.asEngineWind(),
                                )
                            val input = requireNotNull(baseline.input) { baseline.issues.joinToString(" ") }
                            val alternative =
                                input.copy(
                                    currentAtmosphere =
                                        input.currentAtmosphere.copy(
                                            temperatureCelsius = requireNotNull(temperature.toDoubleOrNull()),
                                            stationPressurePascal = requireNotNull(pressure.toDoubleOrNull()),
                                            relativeHumidityPercent = requireNotNull(humidity.toDoubleOrNull()),
                                        ),
                                )
                            TrajectoryComparisonService(StandardBallisticsEngine()).compare(input, alternative)
                        }
                    compared.fold(
                        onSuccess = {
                            elevationDelta = it.elevationDialDelta
                            windDelta = it.windageDialDelta
                            velocityDelta = it.remainingVelocityDeltaMps
                            message =
                                if (it.issues.isEmpty()) {
                                    "Comparison complete · profiles unchanged"
                                } else {
                                    it.issues.joinToString(" ")
                                }
                        },
                        onFailure = { message = it.message ?: "Comparison failed" },
                    )
                }
            },
            Modifier.fillMaxWidth(),
            enabled = repository != null,
        )
        if (elevationDelta != null) {
            DopeCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("ALTERNATIVE − BASELINE", style = MaterialTheme.typography.titleMedium)
                    LabelValue("Elevation dial delta", signed(requireNotNull(elevationDelta)))
                    LabelValue("Wind dial delta", signed(requireNotNull(windDelta)))
                    LabelValue("Remaining velocity delta", "${signed(requireNotNull(velocityDelta))} m/s")
                    StatusChip("No profile values changed", DopeStatus.READY)
                }
            }
        }
        message?.let { StatusChip(it, if (elevationDelta == null) DopeStatus.BLOCKED else DopeStatus.INFO) }
    }
}

private fun signed(value: Double): String = "%+.3f".format(java.util.Locale.ROOT, value)
