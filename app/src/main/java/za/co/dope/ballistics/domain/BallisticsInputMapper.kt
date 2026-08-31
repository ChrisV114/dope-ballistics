package za.co.dope.ballistics.domain

import za.co.dope.ballistics.data.db.AmmunitionEntity
import za.co.dope.ballistics.data.db.EnvironmentalSnapshotEntity
import za.co.dope.ballistics.data.db.ReferenceAtmosphereEntity
import za.co.dope.ballistics.data.db.ScopeProfileEntity
import za.co.dope.ballistics.data.db.ZeroProfileEntity
import za.co.dope.ballistics.engine.Atmosphere
import za.co.dope.ballistics.engine.InputUncertainty
import za.co.dope.ballistics.engine.Projectile
import za.co.dope.ballistics.engine.ScopeAdjustment
import za.co.dope.ballistics.engine.ShotGeometry
import za.co.dope.ballistics.engine.TrajectoryInput
import za.co.dope.ballistics.engine.Wind
import za.co.dope.ballistics.engine.AngularUnit as EngineAngularUnit
import za.co.dope.ballistics.engine.DragModel as EngineDragModel

data class BallisticsInputBuildResult(
    val input: TrajectoryInput?,
    val issues: List<String>,
)

object BallisticsInputMapper {
    @Suppress("LongParameterList", "LongMethod", "CyclomaticComplexMethod")
    fun build(
        ammunition: AmmunitionEntity,
        scope: ScopeProfileEntity,
        zero: ZeroProfileEntity,
        reference: ReferenceAtmosphereEntity,
        current: EnvironmentalSnapshotEntity,
        targetRangeMeters: Double,
        inclinationDegrees: Double = 0.0,
        wind: Wind = Wind(),
        uncertainty: InputUncertainty = InputUncertainty(),
    ): BallisticsInputBuildResult {
        val issues =
            mutableListOf<String>().apply {
                if (!zero.verified) add("The selected zero profile must be verified.")
                if (scope.verificationStatus != VerificationStatus.USER_VERIFIED.name) {
                    add("The selected scope profile must be physically verified.")
                }
                if (zero.ammunitionId != ammunition.id) {
                    add("The zero profile does not belong to the selected ammunition.")
                }
                if (zero.scopeProfileId != scope.id) add("The zero profile does not belong to the selected scope.")
                if (zero.referenceAtmosphereId != reference.id) {
                    add("The zero profile does not belong to the selected reference atmosphere.")
                }
            }
        val dragModel = runCatching { EngineDragModel.valueOf(ammunition.selectedDragModel) }.getOrNull()
        if (dragModel == null) issues += "The ammunition drag model must be G1 or G7."
        val coefficient =
            when (dragModel) {
                EngineDragModel.G1 -> ammunition.g1BallisticCoefficient
                EngineDragModel.G7 -> ammunition.g7BallisticCoefficient
                null -> null
            }
        if (coefficient == null) issues += "The selected drag model has no manufacturer ballistic coefficient."
        val unit = runCatching { EngineAngularUnit.valueOf(scope.turretUnit) }.getOrNull()
        if (unit == null) issues += "The verified scope turret unit must be MIL or MOA."
        if (issues.isNotEmpty()) return BallisticsInputBuildResult(null, issues)

        val angularScale =
            when (unit!!) {
                EngineAngularUnit.MIL -> 1_000.0
                EngineAngularUnit.MOA -> 180.0 / Math.PI * 60.0
            }
        return BallisticsInputBuildResult(
            input =
                TrajectoryInput(
                    projectile =
                        Projectile(
                            massGrains = ammunition.bulletWeightKilograms / KILOGRAMS_PER_GRAIN,
                            ballisticCoefficient = coefficient!!,
                            dragModel = dragModel!!,
                            muzzleVelocityMps = ammunition.muzzleVelocityMetresPerSecond,
                            profileName = ammunition.bulletName,
                        ),
                    geometry =
                        ShotGeometry(
                            sightHeightMeters = zero.sightHeightAboveBoreMetres,
                            zeroRangeMeters = zero.zeroDistanceMetres,
                            targetRangeMeters = targetRangeMeters,
                            inclinationDegrees = inclinationDegrees,
                        ),
                    currentAtmosphere =
                        Atmosphere(
                            temperatureCelsius = current.temperatureKelvin - 273.15,
                            stationPressurePascal = current.stationPressurePascals,
                            relativeHumidityPercent = current.relativeHumidityFraction * 100.0,
                        ),
                    referenceAtmosphere =
                        Atmosphere(
                            temperatureCelsius = reference.temperatureKelvin - 273.15,
                            stationPressurePascal = reference.stationPressurePascals,
                            relativeHumidityPercent = (reference.relativeHumidityFraction ?: 0.0) * 100.0,
                        ),
                    wind = wind,
                    scope =
                        ScopeAdjustment(
                            unit = unit,
                            clickValue = scope.elevationClickValueRadians * angularScale,
                            maximumTravel = scope.elevationTravelRadians?.times(angularScale),
                            hasZeroStop = scope.zeroStopAvailable,
                        ),
                    uncertainty = uncertainty,
                ),
            issues = emptyList(),
        )
    }

    private const val KILOGRAMS_PER_GRAIN = 0.00006479891
}
