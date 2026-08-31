package za.co.dope.ballistics.domain

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import za.co.dope.ballistics.data.CalculationProfileContext
import za.co.dope.ballistics.data.ObservationConfidence
import za.co.dope.ballistics.data.VerifiedDataStatus
import za.co.dope.ballistics.data.db.AmmunitionEntity
import za.co.dope.ballistics.data.db.RifleEntity
import za.co.dope.ballistics.data.db.ScopeProfileEntity
import za.co.dope.ballistics.data.db.SessionSnapshotEntity
import za.co.dope.ballistics.data.db.VerifiedDopeRecordEntity
import za.co.dope.ballistics.data.db.ZeroProfileEntity
import za.co.dope.ballistics.engine.ResolvedWind
import za.co.dope.ballistics.engine.TrajectoryResult

@Serializable
private data class ProfileSnapshotV1(
    val rifle: RifleEntity,
    val ammunition: AmmunitionEntity,
    val scope: ScopeProfileEntity,
    val zero: ZeroProfileEntity,
)

@Serializable
private data class WindSnapshotV1(
    val windFromDegrees: Double,
    val directionOfFireDegrees: Double,
    val bearingReference: String,
    val windFromTrueDegrees: Double?,
    val directionOfFireTrueDegrees: Double?,
    val relativeWindFromDegrees: Double,
    val selectedSpeedMps: Double,
    val headwindMps: Double,
    val crosswindMps: Double,
    val effect: String,
    val minimumSpeedMps: Double,
    val averageSpeedMps: Double,
    val maximumSpeedMps: Double,
    val gustSpeedMps: Double?,
)

@Serializable
private data class CalculationResultSnapshotV1(
    val confidence: String,
    val issues: List<String>,
    val rangeMetres: Double?,
    val referenceElevationRadians: Double?,
    val currentElevationRadians: Double?,
    val environmentalDeviationRadians: Double?,
    val inclinationContributionRadians: Double?,
    val windageRadians: Double?,
    val timeOfFlightSeconds: Double?,
    val remainingVelocityMps: Double?,
    val remainingEnergyJoules: Double?,
    val mach: Double?,
    val flightState: String?,
)

@Serializable
private data class CalculationTraceSnapshotV1(
    val engineVersion: String,
    val dragReference: String,
    val dragModel: String,
    val integrationMethod: String,
    val integrationStepSeconds: Double,
    val zeroSolver: String,
    val zeroIterations: Int,
    val deterministic: Boolean,
    val notes: List<String>,
)

@Serializable
private data class ScopeRoundingSnapshotV1(
    val unit: String?,
    val rawElevation: Double?,
    val roundedElevation: Double?,
    val elevationClicks: Int?,
    val elevationResidual: Double?,
    val rawWindage: Double?,
    val roundedWindage: Double?,
    val windageClicks: Int?,
    val windageResidual: Double?,
)

object SessionSnapshotFactory {
    private val json = Json { encodeDefaults = true }

    @Suppress("LongParameterList")
    fun createSession(
        id: String,
        sessionName: String,
        startedAtEpochMillis: Long,
        completedAtEpochMillis: Long,
        profile: CalculationProfileContext,
        distanceMetres: Double,
        distanceSource: String,
        distanceUncertaintyMetres: Double,
        directionOfFireTrueDegrees: Double?,
        inclinationDegrees: Double,
        wind: ResolvedWind,
        result: TrajectoryResult,
        includePreciseLocation: Boolean,
        notes: String?,
    ): SessionSnapshotEntity {
        val location = locationSnapshot(profile, includePreciseLocation)
        val locationAllowed = location != null
        return SessionSnapshotEntity(
            id = id,
            sessionName = sessionName,
            startedAtEpochMillis = startedAtEpochMillis,
            completedAtEpochMillis = completedAtEpochMillis,
            preciseLocationIncluded = locationAllowed,
            locationSnapshotJson = location,
            rifleId = profile.rifle.id,
            rifleRevision = profile.rifle.revision,
            ammunitionId = profile.ammunition.id,
            ammunitionRevision = profile.ammunition.revision,
            scopeProfileId = profile.scope.id,
            scopeProfileRevision = profile.scope.revision,
            zeroProfileId = profile.zero.id,
            zeroProfileRevision = profile.zero.revision,
            profileSnapshotJson =
                json.encodeToString(
                    ProfileSnapshotV1(profile.rifle, profile.ammunition, profile.scope, profile.zero),
                ),
            referenceEnvironmentJson = json.encodeToString(profile.referenceAtmosphere),
            currentEnvironmentJson = json.encodeToString(profile.currentEnvironment),
            fieldSourcesJson =
                json.encodeToString(
                    mapOf(
                        "distance" to distanceSource,
                        "temperature" to profile.currentEnvironment.temperatureSource,
                        "pressure" to profile.currentEnvironment.pressureSource,
                        "humidity" to profile.currentEnvironment.humiditySource,
                        "altitude" to profile.currentEnvironment.altitudeSource,
                    ),
                ),
            distanceMetres = distanceMetres,
            distanceSource = distanceSource,
            distanceUncertaintyMetres = distanceUncertaintyMetres,
            directionOfFireTrueDegrees = directionOfFireTrueDegrees,
            inclinationDegrees = inclinationDegrees,
            windSnapshotJson = json.encodeToString(wind.snapshot()),
            calculationResultJson = json.encodeToString(result.snapshot()),
            calculationTraceJson = json.encodeToString(result.trace.snapshot()),
            engineVersion = result.trace.engineVersion,
            scopeRoundingJson = json.encodeToString(result.roundingSnapshot()),
            warningsJson = json.encodeToString(result.issues),
            notes = notes,
            contentSha256 = "",
        )
    }

    private fun locationSnapshot(
        profile: CalculationProfileContext,
        requested: Boolean,
    ): String? {
        val environment = profile.currentEnvironment
        val latitude = environment.latitudeDegrees
        val longitude = environment.longitudeDegrees
        val permitted = requested && environment.locationIncludedInExports
        val hasCoordinates = latitude != null && longitude != null
        return if (!permitted || !hasCoordinates) {
            null
        } else {
            buildJsonObject {
                put("latitudeDegrees", latitude)
                put("longitudeDegrees", longitude)
                put("approximate", environment.approximateLocation)
            }.toString()
        }
    }

    @Suppress("LongParameterList")
    fun createVerifiedDope(
        id: String,
        session: SessionSnapshotEntity,
        result: TrajectoryResult,
        actualDialValue: Double,
        actualDialClicks: Int?,
        observedVerticalMetres: Double?,
        observedHorizontalMetres: Double?,
        groupSizeMetres: Double?,
        numberOfShots: Int,
        conditionsJson: String,
        confidence: ObservationConfidence,
        status: VerifiedDataStatus,
        notes: String?,
        createdAtEpochMillis: Long,
    ): VerifiedDopeRecordEntity {
        val solution = requireNotNull(result.solution) { "A valid calculated result is required" }
        return VerifiedDopeRecordEntity(
            id = id,
            sessionSnapshotId = session.id,
            createdAtEpochMillis = createdAtEpochMillis,
            rifleId = session.rifleId,
            rifleRevision = session.rifleRevision,
            ammunitionId = session.ammunitionId,
            ammunitionRevision = session.ammunitionRevision,
            scopeProfileId = session.scopeProfileId,
            scopeProfileRevision = session.scopeProfileRevision,
            zeroProfileId = session.zeroProfileId,
            zeroProfileRevision = session.zeroProfileRevision,
            profileSnapshotJson = session.profileSnapshotJson,
            distanceMetres = session.distanceMetres,
            distanceSource = session.distanceSource,
            distanceUncertaintyMetres = session.distanceUncertaintyMetres,
            calculatedUnit = solution.elevationScope.unit.name,
            calculatedRawValue = solution.elevationScope.raw,
            calculatedDialValue = solution.elevationScope.rounded,
            calculatedClicks = solution.elevationScope.clicks,
            actualDialUnit = solution.elevationScope.unit.name,
            actualDialValue = actualDialValue,
            actualDialClicks = actualDialClicks,
            observedGroupCentreVerticalMetres = observedVerticalMetres,
            observedGroupCentreHorizontalMetres = observedHorizontalMetres,
            groupSizeMetres = groupSizeMetres,
            numberOfShots = numberOfShots,
            conditionsJson = conditionsJson,
            confidence = confidence.name,
            status = status.name,
            engineVersion = result.trace.engineVersion,
            notes = notes,
            evidenceSha256 = "",
        )
    }

    private fun ResolvedWind.snapshot() =
        WindSnapshotV1(
            windFromDegrees = windFromInputDegrees,
            directionOfFireDegrees = directionOfFireInputDegrees,
            bearingReference = bearingReference.name,
            windFromTrueDegrees = windFromTrueDegrees,
            directionOfFireTrueDegrees = directionOfFireTrueDegrees,
            relativeWindFromDegrees = relativeWindFromDegrees,
            selectedSpeedMps = selected.speedMps,
            headwindMps = selected.headwindMps,
            crosswindMps = selected.crosswindMps,
            effect = effect.name,
            minimumSpeedMps = bracket.minimum.speedMps,
            averageSpeedMps = bracket.expected.speedMps,
            maximumSpeedMps = bracket.maximum.speedMps,
            gustSpeedMps = bracket.gust?.speedMps,
        )

    private fun TrajectoryResult.snapshot() =
        CalculationResultSnapshotV1(
            confidence = confidence.name,
            issues = issues,
            rangeMetres = solution?.rangeMeters,
            referenceElevationRadians = solution?.referenceElevationRadians,
            currentElevationRadians = solution?.currentElevationRadians,
            environmentalDeviationRadians = solution?.environmentalDeviationRadians,
            inclinationContributionRadians = solution?.inclinationContributionRadians,
            windageRadians = solution?.windageRadians,
            timeOfFlightSeconds = solution?.timeOfFlightSeconds,
            remainingVelocityMps = solution?.remainingVelocityMps,
            remainingEnergyJoules = solution?.remainingEnergyJoules,
            mach = solution?.mach,
            flightState = solution?.flightState?.name,
        )

    private fun za.co.dope.ballistics.engine.CalculationTrace.snapshot() =
        CalculationTraceSnapshotV1(
            engineVersion = engineVersion,
            dragReference = dragReference,
            dragModel = dragModel.name,
            integrationMethod = integrationMethod,
            integrationStepSeconds = integrationStepSeconds,
            zeroSolver = zeroSolver,
            zeroIterations = zeroIterations,
            deterministic = deterministic,
            notes = notes,
        )

    private fun TrajectoryResult.roundingSnapshot() =
        ScopeRoundingSnapshotV1(
            unit = solution?.elevationScope?.unit?.name,
            rawElevation = solution?.elevationScope?.raw,
            roundedElevation = solution?.elevationScope?.rounded,
            elevationClicks = solution?.elevationScope?.clicks,
            elevationResidual = solution?.elevationScope?.residual,
            rawWindage = solution?.windageScope?.raw,
            roundedWindage = solution?.windageScope?.rounded,
            windageClicks = solution?.windageScope?.clicks,
            windageResidual = solution?.windageScope?.residual,
        )
}
