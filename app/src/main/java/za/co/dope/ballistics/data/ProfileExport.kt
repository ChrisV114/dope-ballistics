package za.co.dope.ballistics.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import za.co.dope.ballistics.data.db.AmmunitionEntity
import za.co.dope.ballistics.data.db.ChronographStringEntity
import za.co.dope.ballistics.data.db.ReferenceAtmosphereEntity
import za.co.dope.ballistics.data.db.RifleEntity
import za.co.dope.ballistics.data.db.SavedRangeEntity
import za.co.dope.ballistics.data.db.ScopeProfileEntity
import za.co.dope.ballistics.data.db.ScopeVerificationEntity
import za.co.dope.ballistics.data.db.StaticTargetEntity
import za.co.dope.ballistics.data.db.ZeroProfileEntity
import za.co.dope.ballistics.domain.ReticleMeasurementSystem
import za.co.dope.ballistics.domain.StaticTargetClass
import za.co.dope.ballistics.domain.TargetClassRules

@Serializable
data class ExportMetadataV1(
    val schemaVersion: Int = 1,
    val appVersion: String,
    val engineVersion: String = "not-implemented-milestone-2",
    val createdAtEpochMillis: Long,
    val internalUnits: String = "SI",
    val preciseLocationIncluded: Boolean,
)

@Serializable
data class ProfileExportV1(
    val metadata: ExportMetadataV1,
    val rifles: List<RifleEntity> = emptyList(),
    val ammunition: List<AmmunitionEntity> = emptyList(),
    val chronographStrings: List<ChronographStringEntity> = emptyList(),
    val scopeProfiles: List<ScopeProfileEntity> = emptyList(),
    val scopeVerifications: List<ScopeVerificationEntity> = emptyList(),
    val referenceAtmospheres: List<ReferenceAtmosphereEntity> = emptyList(),
    val savedRanges: List<SavedRangeEntity> = emptyList(),
    val staticTargets: List<StaticTargetEntity> = emptyList(),
    val zeroProfiles: List<ZeroProfileEntity> = emptyList(),
)

enum class DuplicateImportPolicy { DUPLICATE, MERGE_SAFE, REPLACE_CONFIRMED, CANCEL }

object ProfileExportCodec {
    private val json =
        Json {
            prettyPrint = true
            ignoreUnknownKeys = false
            explicitNulls = true
        }

    fun encode(export: ProfileExportV1): String {
        validate(export)
        return json.encodeToString(ProfileExportV1.serializer(), export)
    }

    fun decode(payload: String): ProfileExportV1 {
        require(payload.length <= 10_000_000) { "Import exceeds the 10 MB profile limit" }
        val decoded = json.decodeFromString(ProfileExportV1.serializer(), payload)
        validate(decoded)
        return decoded
    }

    fun validate(export: ProfileExportV1) {
        require(export.metadata.schemaVersion == 1) { "Unsupported profile schema version" }
        require(export.metadata.internalUnits == "SI") { "Profile import must use SI internal units" }
        validateRifles(export.rifles)
        validateAmmunition(export.ammunition)
        validateScopes(export.scopeProfiles)
        validateTargets(export.staticTargets)
    }

    private fun validateRifles(rifles: List<RifleEntity>) =
        rifles.forEach { rifle ->
            require(
                rifle.profileName.isNotBlank() &&
                    rifle.barrelLengthMetres > 0.0 &&
                    rifle.twistRateMetres > 0.0,
            )
        }

    private fun validateAmmunition(ammunitionProfiles: List<AmmunitionEntity>) =
        ammunitionProfiles.forEach { ammunition ->
            val selectedBc =
                when (ammunition.selectedDragModel) {
                    "G1" -> ammunition.g1BallisticCoefficient
                    "G7" -> ammunition.g7BallisticCoefficient
                    else -> null
                }
            require(
                selectedBc != null &&
                    selectedBc > 0.0 &&
                    ammunition.muzzleVelocityMetresPerSecond > 0.0,
            )
        }

    private fun validateScopes(scopes: List<ScopeProfileEntity>) =
        scopes.forEach { scope ->
            val reticle = ReticleMeasurementSystem.valueOf(scope.reticleSystem)
            val validBdc =
                reticle != ReticleMeasurementSystem.BDC ||
                    !scope.bdcCalibrationVerified ||
                    scope.bdcMetadata != null
            require(validBdc)
        }

    private fun validateTargets(targets: List<StaticTargetEntity>) =
        targets.forEach { target ->
            requireNotNull(runCatching { StaticTargetClass.valueOf(target.targetClass) }.getOrNull())
            TargetClassRules.validateName(target.name)
            require(target.physicalWidthMetres > 0.0 && target.physicalHeightMetres > 0.0)
            require(!target.includeDistanceInDope || target.distanceConfirmed)
        }
}
