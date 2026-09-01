@file:Suppress("CyclomaticComplexMethod", "LongMethod", "MaxLineLength")

package za.co.dope.ballistics.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Sensors
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import za.co.dope.ballistics.BuildConfig
import za.co.dope.ballistics.data.ProfileRepository
import za.co.dope.ballistics.data.db.EnvironmentalSnapshotEntity
import za.co.dope.ballistics.data.db.WeatherCacheEntity
import za.co.dope.ballistics.data.environment.AndroidLocationGateway
import za.co.dope.ballistics.data.environment.AndroidSensorGateway
import za.co.dope.ballistics.data.environment.OpenMeteoWeatherProvider
import za.co.dope.ballistics.domain.DataSource
import za.co.dope.ballistics.domain.ProfileIdentity
import za.co.dope.ballistics.domain.ReadingQuality
import za.co.dope.ballistics.domain.environment.EnvironmentalMath
import za.co.dope.ballistics.domain.environment.LocationReading
import za.co.dope.ballistics.domain.environment.OrientationReading
import za.co.dope.ballistics.domain.environment.PressureSampleSummary
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
internal fun LiveEnvironmentScreen(
    onOpen: (String) -> Unit,
    repository: ProfileRepository?,
    previewMode: Boolean,
) {
    val context = LocalContext.current
    val sensors = remember(previewMode) { if (previewMode) null else AndroidSensorGateway(context.applicationContext) }
    val locationGateway = remember(previewMode) { if (previewMode) null else AndroidLocationGateway(context.applicationContext) }
    val weather = remember { OpenMeteoWeatherProvider(BuildConfig.OPEN_METEO_ENABLED) }
    val capabilities = remember(sensors) { sensors?.capabilities().orEmpty() }
    val scope = rememberCoroutineScope()
    var temperatureCelsius by remember { mutableStateOf("20.0") }
    var pressureHpa by remember { mutableStateOf("1013.25") }
    var humidityPercent by remember { mutableStateOf("50") }
    var altitudeMetres by remember { mutableStateOf("0") }
    var altitudeSource by remember { mutableStateOf(DataSource.MANUAL) }
    var pressureSource by remember { mutableStateOf(DataSource.MANUAL) }
    var pressureSummary by remember { mutableStateOf<PressureSampleSummary?>(null) }
    var location by remember { mutableStateOf<LocationReading?>(null) }
    var orientation by remember { mutableStateOf<OrientationReading?>(null) }
    var providerAttribution by remember { mutableStateOf<String?>(null) }
    var includeLocation by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("Manual values ready offline") }
    var busy by remember { mutableStateOf(false) }

    fun collectLocation() {
        scope.launch {
            busy = true
            requireNotNull(locationGateway).currentLocation().fold(
                onSuccess = {
                    location = it
                    it.altitudeMetres?.let { value ->
                        altitudeMetres = formatEnvironmentValue(value, 1)
                        altitudeSource = DataSource.GPS
                    }
                    status =
                        when {
                            it.cachedFallback -> "Recent cached location loaded; retry outdoors for a fresh fix"
                            it.approximate -> "Approximate location captured"
                            else -> "Precise location captured"
                        }
                },
                onFailure = { status = it.message ?: "Location unavailable" },
            )
            busy = false
        }
    }
    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.values.any { it }) collectLocation() else status = "Location permission denied; manual entry remains available"
        }

    ScreenShell(title = "Environment", eyebrow = "SOURCE · QUALITY · AGE") {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatusChip(if (busy) "Collecting" else "Ready", if (busy) DopeStatus.WARNING else DopeStatus.READY)
            StatusChip("Manual works offline", DopeStatus.INFO)
        }
        DopeCard {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Manual conditions", style = MaterialTheme.typography.titleMedium)
                DopeField("Temperature", temperatureCelsius, { temperatureCelsius = it }, config = DopeFieldConfig(suffix = "°C"))
                DopeField("Station pressure", pressureHpa, {
                    pressureHpa = it
                    pressureSource = DataSource.MANUAL
                }, config = DopeFieldConfig(suffix = "hPa"))
                DopeField("Relative humidity", humidityPercent, { humidityPercent = it }, config = DopeFieldConfig(suffix = "%"))
                DopeField(
                    "Altitude",
                    altitudeMetres,
                    {
                        altitudeMetres = it
                        altitudeSource = DataSource.MANUAL
                    },
                    config = DopeFieldConfig(suffix = "m"),
                )
                Text(
                    "Station/surface pressure only. Do not enter mean-sea-level or altimeter pressure.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        DopeCard {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Device capabilities", style = MaterialTheme.typography.titleMedium)
                capabilities.forEach { capability ->
                    LabelValue(capability.kind, if (capability.available) "Available · ${capability.name}" else "Not present")
                }
                Text(
                    "Battery and device thermal readings are never treated as ambient temperature.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        DopeSecondaryButton(
            "Sample barometer (7 seconds)",
            {
                scope.launch {
                    busy = true
                    requireNotNull(sensors).samplePressure().fold(
                        onSuccess = {
                            pressureSummary = it
                            pressureHpa = formatEnvironmentValue(it.stationPressurePascals / 100.0, 2)
                            pressureSource = DataSource.DEVICE_SENSOR
                            status =
                                if (it.stable) "Stable barometer sample accepted" else "Barometer sample is unstable; retry or use manual"
                        },
                        onFailure = { status = it.message ?: "Barometer unavailable" },
                    )
                    busy = false
                }
            },
            Modifier.fillMaxWidth(),
            Icons.Outlined.Sensors,
        )
        DopeSecondaryButton(
            "Capture location once",
            {
                if (locationGateway?.hasLocationPermission() == true) {
                    collectLocation()
                } else {
                    permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION))
                }
            },
            Modifier.fillMaxWidth(),
            Icons.Outlined.MyLocation,
        )
        DopeSecondaryButton(
            "Capture orientation",
            {
                scope.launch {
                    busy = true
                    requireNotNull(sensors).sampleOrientation().fold(
                        onSuccess = {
                            orientation = it
                            status =
                                if (it.stable) "Stable orientation captured" else "Device moved; orientation marked unstable"
                        },
                        onFailure = { status = it.message ?: "Orientation unavailable" },
                    )
                    busy = false
                }
            },
            Modifier.fillMaxWidth(),
            Icons.Outlined.Explore,
        )
        DopeSecondaryButton(
            "Fetch current weather",
            {
                val coordinates = location
                if (coordinates == null) {
                    status = "Capture a location before requesting weather"
                } else {
                    scope.launch {
                        busy = true
                        weather.currentWeather(coordinates.latitudeDegrees, coordinates.longitudeDegrees).fold(
                            onSuccess = {
                                temperatureCelsius = formatEnvironmentValue(it.temperatureKelvin.value - 273.15, 1)
                                pressureHpa = formatEnvironmentValue(it.stationPressurePascals.value / 100.0, 2)
                                humidityPercent = formatEnvironmentValue(it.relativeHumidityFraction.value * 100.0, 0)
                                pressureSource = DataSource.WEATHER_SERVICE
                                providerAttribution = it.attribution
                                if (coordinates.altitudeMetres == null && it.modelElevationMetres != null) {
                                    altitudeMetres = formatEnvironmentValue(it.modelElevationMetres, 1)
                                    altitudeSource = DataSource.WEATHER_SERVICE
                                }
                                repository?.saveWeatherCache(
                                    WeatherCacheEntity(
                                        coordinateKey = coordinates.cacheKey(),
                                        latitudeDegrees = it.latitudeDegrees,
                                        longitudeDegrees = it.longitudeDegrees,
                                        temperatureKelvin = it.temperatureKelvin.value,
                                        surfacePressurePascals = it.stationPressurePascals.value,
                                        meanSeaLevelPressurePascals = it.meanSeaLevelPressurePascals?.value,
                                        relativeHumidityFraction = it.relativeHumidityFraction.value,
                                        windSpeedMetresPerSecond = it.windSpeedMetresPerSecond?.value,
                                        windDirectionDegrees = it.windDirectionDegrees?.value,
                                        providerName = it.providerName,
                                        attribution = it.attribution,
                                        modelElevationMetres = it.modelElevationMetres,
                                        fetchedAtEpochMillis = it.fetchedAtEpochMillis,
                                    ),
                                )
                                status = "Weather estimate loaded; manual wind is never overwritten"
                            },
                            onFailure = { error ->
                                val cached = repository?.weatherCache(coordinates.cacheKey())
                                if (cached == null) {
                                    status = error.message ?: "Weather unavailable"
                                } else {
                                    temperatureCelsius = formatEnvironmentValue(cached.temperatureKelvin - 273.15, 1)
                                    pressureHpa = formatEnvironmentValue(cached.surfacePressurePascals / 100.0, 2)
                                    humidityPercent = formatEnvironmentValue(cached.relativeHumidityFraction * 100.0, 0)
                                    pressureSource = DataSource.WEATHER_SERVICE
                                    providerAttribution = cached.attribution
                                    if (coordinates.altitudeMetres == null && cached.modelElevationMetres != null) {
                                        altitudeMetres = formatEnvironmentValue(cached.modelElevationMetres, 1)
                                        altitudeSource = DataSource.WEATHER_SERVICE
                                    }
                                    val ageMinutes = (System.currentTimeMillis() - cached.fetchedAtEpochMillis) / 60_000L
                                    status = "Offline cached weather · $ageMinutes min old · check staleness"
                                }
                            },
                        )
                        busy = false
                    }
                }
            },
            Modifier.fillMaxWidth(),
            Icons.Outlined.Public,
        )
        DopeCard {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LabelValue("Status", status)
                LabelValue("Pressure source", pressureSource.name.replace('_', ' '))
                LabelValue("Altitude source", altitudeSource.name.replace('_', ' '))
                pressureSummary?.let {
                    LabelValue(
                        "Pressure quality",
                        if (it.stable) "Stable · ${it.retainedSampleCount} samples" else "Unstable · retry recommended",
                    )
                }
                location?.let {
                    LabelValue(
                        "Location",
                        if (it.approximate) {
                            "Approximate · ±${formatEnvironmentValue(
                                it.horizontalAccuracyMetres,
                                0,
                            )} m"
                        } else {
                            "Precise · ±${formatEnvironmentValue(it.horizontalAccuracyMetres, 0)} m"
                        },
                    )
                }
                orientation?.let {
                    LabelValue(
                        "Heading",
                        "${formatEnvironmentValue(it.magneticHeadingDegrees, 1)}° magnetic · " +
                            if (it.stable) "stable" else "unstable",
                    )
                }
                providerAttribution?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = includeLocation, onCheckedChange = { includeLocation = it })
                    Text("Include precise coordinates in this saved snapshot")
                }
            }
        }
        DopePrimaryButton(
            "Save atmospheric snapshot",
            {
                scope.launch {
                    val result =
                        runCatching {
                            val now = System.currentTimeMillis()
                            val temperature = temperatureCelsius.toDouble() + 273.15
                            val pressure = pressureHpa.toDouble() * 100.0
                            val humidity = humidityPercent.toDouble() / 100.0
                            val altitude = altitudeMetres.toDouble()
                            val calculated = EnvironmentalMath.calculate(temperature, pressure, humidity)
                            requireNotNull(repository) { "Database is unavailable in preview mode" }.saveEnvironmentalSnapshot(
                                EnvironmentalSnapshotEntity(
                                    id = ProfileIdentity.newId(),
                                    name = "Field conditions",
                                    temperatureKelvin = temperature,
                                    temperatureSource =
                                        if (providerAttribution ==
                                            null
                                        ) {
                                            DataSource.MANUAL.name
                                        } else {
                                            DataSource.WEATHER_SERVICE.name
                                        },
                                    temperatureQuality = ReadingQuality.GOOD.name,
                                    temperatureCapturedAtEpochMillis = now,
                                    stationPressurePascals = pressure,
                                    pressureSource = pressureSource.name,
                                    pressureQuality =
                                        if (pressureSummary?.stable ==
                                            true
                                        ) {
                                            ReadingQuality.GOOD.name
                                        } else {
                                            ReadingQuality.FAIR.name
                                        },
                                    pressureCapturedAtEpochMillis = now,
                                    relativeHumidityFraction = humidity,
                                    humiditySource =
                                        if (providerAttribution ==
                                            null
                                        ) {
                                            DataSource.MANUAL.name
                                        } else {
                                            DataSource.WEATHER_SERVICE.name
                                        },
                                    humidityQuality = ReadingQuality.FAIR.name,
                                    humidityCapturedAtEpochMillis = now,
                                    altitudeMetres = altitude,
                                    altitudeSource = altitudeSource.name,
                                    altitudeQuality = ReadingQuality.FAIR.name,
                                    altitudeCapturedAtEpochMillis = now,
                                    latitudeDegrees =
                                        location?.latitudeDegrees.takeIf {
                                            includeLocation
                                        },
                                    longitudeDegrees = location?.longitudeDegrees.takeIf { includeLocation },
                                    horizontalAccuracyMetres = location?.horizontalAccuracyMetres,
                                    verticalAccuracyMetres = location?.verticalAccuracyMetres,
                                    approximateLocation = location?.approximate ?: false,
                                    locationIncludedInExports = includeLocation,
                                    magneticHeadingDegrees = orientation?.magneticHeadingDegrees,
                                    trueHeadingDegrees = orientation?.trueHeadingDegrees,
                                    pitchDegrees = orientation?.pitchDegrees,
                                    rollDegrees = orientation?.rollDegrees,
                                    orientationQuality = orientation?.accuracy?.name,
                                    orientationStable = orientation?.stable,
                                    pressureSampleSummaryJson = pressureSummary?.toString(),
                                    airDensityKilogramsPerCubicMetre = calculated.airDensityKilogramsPerCubicMetre,
                                    densityRatio = calculated.densityRatio,
                                    pressureAltitudeMetres = calculated.pressureAltitudeMetres,
                                    densityAltitudeMetres = calculated.densityAltitudeMetres,
                                    dewPointKelvin = calculated.dewPointKelvin,
                                    waterVapourPressurePascals = calculated.waterVapourPressurePascals,
                                    speedOfSoundMetresPerSecond = calculated.speedOfSoundMetresPerSecond,
                                    providerName = providerAttribution?.let { weather.providerName },
                                    providerAttribution = providerAttribution,
                                    capturedAtEpochMillis = now,
                                ),
                            )
                        }
                    status = result.fold({ "Atmospheric snapshot saved" }, { it.message ?: "Could not save snapshot" })
                }
            },
            Modifier.fillMaxWidth(),
            Icons.Outlined.Save,
            enabled = !busy,
        )
        DopeSecondaryButton("Open wind", { onOpen("wind") }, Modifier.fillMaxWidth(), Icons.Outlined.Explore)
    }
}

internal fun formatEnvironmentValue(
    value: Double,
    decimals: Int,
): String = String.format(Locale.US, "%.${decimals}f", value)

private fun LocationReading.cacheKey(): String =
    "${formatEnvironmentValue(latitudeDegrees, 3)},${formatEnvironmentValue(longitudeDegrees, 3)}"
