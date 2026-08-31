package za.co.dope.ballistics.domain

import kotlinx.serialization.Serializable
import za.co.dope.ballistics.engine.BallisticsEngine
import za.co.dope.ballistics.engine.RangeCardInput
import za.co.dope.ballistics.engine.ResultConfidence
import za.co.dope.ballistics.engine.TrajectoryInput
import za.co.dope.ballistics.engine.Wind
import za.co.dope.ballistics.engine.WindComponents
import za.co.dope.ballistics.engine.WindConvention
import za.co.dope.ballistics.engine.WindObservation
import java.util.Locale
import kotlin.math.PI

data class ConfirmedDistance(
    val metres: Double,
    val source: String,
    val confirmed: Boolean,
)

enum class DistanceDisplayUnit {
    METRES,
    YARDS,
}

enum class EnvironmentSelection {
    CURRENT,
    REFERENCE,
}

enum class AngularDisplayUnit {
    MIL,
    MOA,
}

enum class RangeCardColumnSet {
    ESSENTIAL,
    FIELD,
    FULL,
}

enum class RangeCardLayout {
    OUTDOOR,
    HIGH_CONTRAST,
    RED_LIGHT,
}

data class RangeCardRequest(
    val trajectory: TrajectoryInput,
    val startDistanceMetres: Double,
    val endDistanceMetres: Double,
    val incrementMetres: Double,
    val confirmedDistances: List<ConfirmedDistance> = emptyList(),
    val windObservation: WindObservation,
    val profileLabel: String,
    val appVersion: String,
    val createdAtEpochMillis: Long,
    val distanceDisplayUnit: DistanceDisplayUnit = DistanceDisplayUnit.METRES,
    val environmentSelection: EnvironmentSelection = EnvironmentSelection.CURRENT,
    val angularDisplayUnit: AngularDisplayUnit = AngularDisplayUnit.MIL,
    val columnSet: RangeCardColumnSet = RangeCardColumnSet.FIELD,
    val layout: RangeCardLayout = RangeCardLayout.OUTDOOR,
    val reticleHoldValid: Boolean = false,
)

@Serializable
data class RangeCardMetadata(
    val schemaVersion: Int = 1,
    val appVersion: String,
    val engineVersion: String,
    val createdAtEpochMillis: Long,
    val units: String,
    val scopeUnits: String = units,
    val distanceUnits: String = DistanceDisplayUnit.METRES.name,
    val environmentSelection: String = EnvironmentSelection.CURRENT.name,
    val inclinationDegrees: Double = 0.0,
    val windSummary: String = "Not recorded",
    val columnSet: String = RangeCardColumnSet.FULL.name,
    val layout: String = RangeCardLayout.OUTDOOR.name,
    val preciseLocationIncluded: Boolean = false,
    val profileLabel: String,
    val windConvention: String,
)

@Serializable
data class RangeCardRow(
    val distanceMetres: Double,
    val elevationRaw: Double,
    val elevationDial: Double,
    val elevationClicks: Int,
    val reticleHold: Double? = null,
    val environmentalDeviation: Double,
    val windSelected: Double,
    val windMinimum: Double,
    val windMaximum: Double,
    val timeOfFlightSeconds: Double,
    val remainingVelocityMps: Double,
    val remainingEnergyJoules: Double,
    val mach: Double,
    val flightState: String,
    val elevationUncertainty: Double,
    val warningState: String,
)

@Serializable
data class RangeCardDocument(
    val metadata: RangeCardMetadata,
    val rows: List<RangeCardRow>,
    val issues: List<String>,
)

class RangeCardGenerator(
    private val engine: BallisticsEngine,
) {
    fun generate(request: RangeCardRequest): RangeCardDocument {
        val distances = buildDistances(request)
        val resolvedWind = WindConvention.resolve(request.windObservation)
        val trajectory = selectedTrajectory(request)
        val expected = solve(trajectory, distances, resolvedWind.asEngineWind())
        val minimum = solve(trajectory, distances, resolvedWind.bracket.minimum.toEngineWind())
        val maximum = solve(trajectory, distances, resolvedWind.bracket.maximum.toEngineWind())
        val issues = (expected.issues + minimum.issues + maximum.issues).distinct()
        val rows = buildRows(request, expected, minimum, maximum)
        return RangeCardDocument(
            metadata =
                RangeCardMetadata(
                    appVersion = request.appVersion,
                    engineVersion = expected.engineVersion,
                    createdAtEpochMillis = request.createdAtEpochMillis,
                    units = request.angularDisplayUnit.name,
                    scopeUnits = request.trajectory.scope.unit.name,
                    distanceUnits = request.distanceDisplayUnit.name,
                    environmentSelection = request.environmentSelection.name,
                    inclinationDegrees = request.trajectory.geometry.inclinationDegrees,
                    windSummary =
                        "${resolvedWind.windFromInputDegrees}° ${resolvedWind.bearingReference.name}; " +
                            "${resolvedWind.selected.speedMps} m/s selected",
                    columnSet = request.columnSet.name,
                    layout = request.layout.name,
                    profileLabel = request.profileLabel,
                    windConvention =
                        "Wind-from; bearings clockwise from north; positive crosswind moves projectile right",
                ),
            rows = rows,
            issues = issues,
        )
    }

    private fun solve(
        trajectory: TrajectoryInput,
        distances: List<Double>,
        wind: Wind,
    ) = engine.rangeCard(RangeCardInput(trajectory.copy(wind = wind), distances))

    private fun selectedTrajectory(request: RangeCardRequest): TrajectoryInput =
        when (request.environmentSelection) {
            EnvironmentSelection.CURRENT -> {
                request.trajectory
            }

            EnvironmentSelection.REFERENCE -> {
                request.trajectory.copy(currentAtmosphere = request.trajectory.referenceAtmosphere)
            }
        }

    private fun WindComponents.toEngineWind(): Wind = Wind(headwindMps = headwindMps, crosswindMps = crosswindMps)

    private fun buildRows(
        request: RangeCardRequest,
        expected: za.co.dope.ballistics.engine.RangeCardResult,
        minimum: za.co.dope.ballistics.engine.RangeCardResult,
        maximum: za.co.dope.ballistics.engine.RangeCardResult,
    ) = expected.rows.mapIndexedNotNull { index, expectedResult ->
        val expectedSolution = expectedResult.solution ?: return@mapIndexedNotNull null
        val minimumSolution = minimum.rows.getOrNull(index)?.solution ?: return@mapIndexedNotNull null
        val maximumSolution = maximum.rows.getOrNull(index)?.solution ?: return@mapIndexedNotNull null
        RangeCardRow(
            distanceMetres = expectedSolution.rangeMeters,
            elevationRaw = angularValue(expectedSolution.currentElevationRadians, request.angularDisplayUnit),
            elevationDial = expectedSolution.elevationScope.rounded,
            elevationClicks = expectedSolution.elevationScope.clicks,
            reticleHold = reticleHold(expectedSolution.currentElevationRadians, request),
            environmentalDeviation =
                angularValue(
                    expectedSolution.environmentalDeviationRadians,
                    request.angularDisplayUnit,
                ),
            windSelected = angularValue(expectedSolution.windageRadians, request.angularDisplayUnit),
            windMinimum = angularValue(minimumSolution.windageRadians, request.angularDisplayUnit),
            windMaximum = angularValue(maximumSolution.windageRadians, request.angularDisplayUnit),
            timeOfFlightSeconds = expectedSolution.timeOfFlightSeconds,
            remainingVelocityMps = expectedSolution.remainingVelocityMps,
            remainingEnergyJoules = expectedSolution.remainingEnergyJoules,
            mach = expectedSolution.mach,
            flightState = expectedSolution.flightState.name,
            elevationUncertainty =
                angularValue(
                    expectedSolution.elevationUncertainty.angularOneSigma,
                    request.angularDisplayUnit,
                ),
            warningState = warningState(expectedResult),
        )
    }

    private fun warningState(result: za.co.dope.ballistics.engine.TrajectoryResult): String =
        if (result.confidence == ResultConfidence.CONFIDENT && result.issues.isEmpty()) "CLEAR" else "WARNING"

    private fun buildDistances(request: RangeCardRequest): List<Double> {
        require(request.startDistanceMetres > 0.0) { "Start distance must be positive" }
        require(request.endDistanceMetres >= request.startDistanceMetres) { "End distance precedes start" }
        require(request.incrementMetres > 0.0) { "Increment must be positive" }
        require(request.confirmedDistances.all { it.confirmed }) {
            "Unconfirmed distances cannot populate a range card"
        }
        val span = request.endDistanceMetres - request.startDistanceMetres
        val count = (span / request.incrementMetres).toInt() + 1
        require(count in 1..MAXIMUM_ROWS) { "Range card must contain 1 to $MAXIMUM_ROWS regular rows" }
        val regular =
            List(count) { index -> request.startDistanceMetres + index * request.incrementMetres }
                .filter { it <= request.endDistanceMetres + DISTANCE_EPSILON }
        return (regular + request.confirmedDistances.map { it.metres })
            .onEach {
                require(it > 0.0 && it <= request.trajectory.maximumDistanceMeters) {
                    "Distance outside solver limits"
                }
            }.distinct()
            .sorted()
    }

    private fun angularValue(
        radians: Double,
        unit: AngularDisplayUnit,
    ): Double =
        when (unit) {
            AngularDisplayUnit.MIL -> radians * 1_000.0
            AngularDisplayUnit.MOA -> radians * 180.0 / PI * 60.0
        }

    private fun reticleHold(
        elevationRadians: Double,
        request: RangeCardRequest,
    ): Double? =
        if (request.reticleHoldValid && request.trajectory.scope.unit.name == request.angularDisplayUnit.name) {
            angularValue(elevationRadians, request.angularDisplayUnit)
        } else {
            null
        }

    private companion object {
        const val MAXIMUM_ROWS = 200
        const val DISTANCE_EPSILON = 1e-6
    }
}

object RangeCardCsvExporter {
    fun export(document: RangeCardDocument): String =
        buildString {
            appendLine("# DOPE range card schema,${document.metadata.schemaVersion}")
            appendLine("# App version,${csv(document.metadata.appVersion)}")
            appendLine("# Engine version,${csv(document.metadata.engineVersion)}")
            appendLine("# Created epoch millis,${document.metadata.createdAtEpochMillis}")
            appendLine("# Angular display units,${document.metadata.units}")
            appendLine("# Scope units,${document.metadata.scopeUnits}")
            appendLine("# Distance units,${document.metadata.distanceUnits}")
            appendLine("# Environment,${document.metadata.environmentSelection}")
            appendLine("# Inclination degrees,${number(document.metadata.inclinationDegrees)}")
            appendLine("# Wind,${csv(document.metadata.windSummary)}")
            appendLine("# Columns,${document.metadata.columnSet}")
            appendLine("# Layout,${document.metadata.layout}")
            appendLine("# Precise location included,${document.metadata.preciseLocationIncluded}")
            appendLine("# Profile,${csv(document.metadata.profileLabel)}")
            appendLine("# Wind convention,${csv(document.metadata.windConvention)}")
            appendLine(headers(document).joinToString(","))
            document.rows.forEach { row ->
                appendLine(values(document, row).joinToString(","))
            }
        }

    private fun headers(document: RangeCardDocument): List<String> {
        val distance =
            if (document.metadata.distanceUnits == DistanceDisplayUnit.YARDS.name) {
                "distance_yards"
            } else {
                "distance_m"
            }
        val essential = listOf(distance, "elevation_dial", "clicks", "wind_selected")
        return when (RangeCardColumnSet.valueOf(document.metadata.columnSet)) {
            RangeCardColumnSet.ESSENTIAL -> {
                essential
            }

            RangeCardColumnSet.FIELD -> {
                listOf(
                    distance,
                    "elevation_raw",
                    "elevation_dial",
                    "clicks",
                    "wind_selected",
                    "wind_minimum",
                    "wind_maximum",
                    "velocity_mps",
                )
            }

            RangeCardColumnSet.FULL -> {
                listOf(
                    distance,
                    "elevation_raw",
                    "elevation_dial",
                    "clicks",
                    "reticle_hold",
                    "environment_deviation",
                    "wind_selected",
                    "wind_minimum",
                    "wind_maximum",
                    "time_of_flight_s",
                    "velocity_mps",
                    "energy_j",
                    "mach",
                    "flight_state",
                    "uncertainty",
                    "warning_state",
                )
            }
        }
    }

    private fun values(
        document: RangeCardDocument,
        row: RangeCardRow,
    ): List<String> {
        val distance =
            if (document.metadata.distanceUnits == DistanceDisplayUnit.YARDS.name) {
                row.distanceMetres / 0.9144
            } else {
                row.distanceMetres
            }
        val essential =
            listOf(
                number(distance),
                number(row.elevationDial),
                row.elevationClicks.toString(),
                number(row.windSelected),
            )
        return when (RangeCardColumnSet.valueOf(document.metadata.columnSet)) {
            RangeCardColumnSet.ESSENTIAL -> {
                essential
            }

            RangeCardColumnSet.FIELD -> {
                listOf(
                    essential[0],
                    number(row.elevationRaw),
                    essential[1],
                    essential[2],
                    essential[3],
                    number(row.windMinimum),
                    number(row.windMaximum),
                    number(row.remainingVelocityMps),
                )
            }

            RangeCardColumnSet.FULL -> {
                listOf(
                    essential[0],
                    number(row.elevationRaw),
                    essential[1],
                    essential[2],
                    row.reticleHold?.let(::number).orEmpty(),
                    number(row.environmentalDeviation),
                    essential[3],
                    number(row.windMinimum),
                    number(row.windMaximum),
                    number(row.timeOfFlightSeconds),
                    number(row.remainingVelocityMps),
                    number(row.remainingEnergyJoules),
                    number(row.mach),
                    row.flightState,
                    number(row.elevationUncertainty),
                    row.warningState,
                )
            }
        }
    }

    private fun number(value: Double): String = String.format(Locale.ROOT, "%.6f", value)

    private fun csv(value: String): String = "\"${value.replace("\"", "\"\"")}\""
}
