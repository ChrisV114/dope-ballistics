package za.co.dope.ballistics.data

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import za.co.dope.ballistics.data.db.AmmunitionEntity
import za.co.dope.ballistics.data.db.ChronographStringEntity
import za.co.dope.ballistics.data.db.DopeDatabase
import za.co.dope.ballistics.data.db.EnvironmentalSnapshotEntity
import za.co.dope.ballistics.data.db.ReferenceAtmosphereEntity
import za.co.dope.ballistics.data.db.RifleEntity
import za.co.dope.ballistics.data.db.SavedRangeEntity
import za.co.dope.ballistics.data.db.ScopeFamilyEntity
import za.co.dope.ballistics.data.db.ScopeProfileEntity
import za.co.dope.ballistics.data.db.ScopeTemplates
import za.co.dope.ballistics.data.db.ScopeVariantEntity
import za.co.dope.ballistics.data.db.ScopeVerificationEntity
import za.co.dope.ballistics.data.db.StaticTargetEntity
import za.co.dope.ballistics.data.db.WeatherCacheEntity
import za.co.dope.ballistics.data.db.ZeroProfileEntity
import za.co.dope.ballistics.domain.ChronographCalculator
import za.co.dope.ballistics.domain.ProfileIdentity
import za.co.dope.ballistics.domain.StaticTargetClass
import za.co.dope.ballistics.domain.TargetClassRules

data class ChronographEntry(
    val ammunitionId: String,
    val readingsMetresPerSecond: List<Double>,
    val chronograph: String,
    val capturedAtEpochMillis: Long,
    val readingsJson: String,
    val notes: String? = null,
)

data class CalculationProfileContext(
    val rifle: RifleEntity,
    val ammunition: AmmunitionEntity,
    val scope: ScopeProfileEntity,
    val zero: ZeroProfileEntity,
    val referenceAtmosphere: ReferenceAtmosphereEntity,
    val currentEnvironment: EnvironmentalSnapshotEntity,
)

@Suppress("TooManyFunctions")
class ProfileRepository(
    private val database: DopeDatabase,
) {
    private val dao = database.profileDao()

    fun observeRifles(): Flow<List<RifleEntity>> = dao.observeRifles()

    fun observeAmmunition(): Flow<List<AmmunitionEntity>> = dao.observeAmmunition()

    fun observeScopeFamilies(): Flow<List<ScopeFamilyEntity>> = dao.observeScopeFamilies()

    fun observeScopeProfiles(): Flow<List<ScopeProfileEntity>> = dao.observeScopeProfiles()

    fun observeReferenceAtmospheres(): Flow<List<ReferenceAtmosphereEntity>> = dao.observeReferenceAtmospheres()

    fun observeEnvironmentalSnapshots(): Flow<List<EnvironmentalSnapshotEntity>> = dao.observeEnvironmentalSnapshots()

    suspend fun saveEnvironmentalSnapshot(value: EnvironmentalSnapshotEntity) {
        require(value.temperatureKelvin in 180.0..340.0)
        require(value.stationPressurePascals in 30_000.0..110_000.0)
        require(value.relativeHumidityFraction in 0.0..1.0)
        dao.upsertEnvironmentalSnapshot(value)
    }

    suspend fun saveWeatherCache(value: WeatherCacheEntity) = dao.upsertWeatherCache(value)

    suspend fun weatherCache(coordinateKey: String): WeatherCacheEntity? = dao.weatherCache(coordinateKey)

    fun observeSavedRanges(): Flow<List<SavedRangeEntity>> = dao.observeSavedRanges()

    fun observeStaticTargets(rangeId: String?): Flow<List<StaticTargetEntity>> = dao.observeStaticTargets(rangeId)

    fun observeZeroProfiles(): Flow<List<ZeroProfileEntity>> = dao.observeZeroProfiles()

    suspend fun saveRifle(value: RifleEntity) {
        require(value.profileName.isNotBlank()) { "Profile name is required" }
        require(value.calibreLabel.isNotBlank()) { "Calibre/cartridge label is required" }
        require(value.barrelLengthMetres > 0.0) { "Barrel length must be positive" }
        require(value.twistRateMetres > 0.0) { "Twist rate must be positive" }
        dao.upsertRifle(value)
    }

    suspend fun saveAmmunition(value: AmmunitionEntity) {
        require(value.muzzleVelocityMetresPerSecond > 0.0) { "Muzzle velocity is required" }
        val selectedBc =
            when (value.selectedDragModel) {
                "G1" -> value.g1BallisticCoefficient
                "G7" -> value.g7BallisticCoefficient
                else -> null
            }
        require(selectedBc != null && selectedBc > 0.0) { "A valid selected ballistic coefficient is required" }
        require(value.bulletWeightKilograms > 0.0) { "Bullet weight is required" }
        dao.upsertAmmunition(value)
    }

    suspend fun saveChronographString(entry: ChronographEntry): ChronographStringEntity {
        val stats = ChronographCalculator.calculate(entry.readingsMetresPerSecond)
        val entity =
            ChronographStringEntity(
                id = ProfileIdentity.newId(),
                ammunitionId = entry.ammunitionId,
                readingsMetresPerSecondJson = entry.readingsJson,
                averageMetresPerSecond = stats.averageMetresPerSecond,
                medianMetresPerSecond = stats.medianMetresPerSecond,
                minimumMetresPerSecond = stats.minimumMetresPerSecond,
                maximumMetresPerSecond = stats.maximumMetresPerSecond,
                extremeSpreadMetresPerSecond = stats.extremeSpreadMetresPerSecond,
                sampleStandardDeviationMetresPerSecond = stats.sampleStandardDeviationMetresPerSecond,
                sampleCount = stats.sampleCount,
                capturedAtEpochMillis = entry.capturedAtEpochMillis,
                chronograph = entry.chronograph,
                createdAtEpochMillis = entry.capturedAtEpochMillis,
                modifiedAtEpochMillis = entry.capturedAtEpochMillis,
                notes = entry.notes,
            )
        dao.upsertChronographString(entity)
        return entity
    }

    suspend fun scopeVariants(familyId: String): List<ScopeVariantEntity> = dao.scopeVariants(familyId)

    suspend fun createScopeFromTemplate(
        variantId: String,
        profileName: String,
        nowEpochMillis: Long,
    ): ScopeProfileEntity {
        val variant = requireNotNull(dao.scopeVariant(variantId)) { "Unknown scope variant" }
        val family = requireNotNull(dao.scopeFamily(variant.familyId)) { "Unknown scope family" }
        val profile = ScopeTemplates.createUserProfile(family, variant, profileName, nowEpochMillis)
        dao.upsertScopeProfile(profile)
        return profile
    }

    suspend fun saveScopeProfile(value: ScopeProfileEntity) {
        require(value.profileName.isNotBlank()) { "Profile name is required" }
        require(value.elevationClickValueRadians > 0.0 && value.windageClickValueRadians > 0.0) {
            "Click values must be positive"
        }
        dao.upsertScopeProfile(value)
    }

    suspend fun saveScopeVerification(value: ScopeVerificationEntity) {
        dao.upsertScopeVerification(value)
    }

    suspend fun saveReferenceAtmosphere(value: ReferenceAtmosphereEntity) {
        require(value.temperatureKelvin > 0.0) { "Temperature must be above absolute zero" }
        require(value.stationPressurePascals > 0.0) { "Station pressure must be positive" }
        dao.upsertReferenceAtmosphere(value)
    }

    suspend fun saveRange(value: SavedRangeEntity) {
        require(value.rangeName.isNotBlank()) { "Range name is required" }
        dao.upsertSavedRange(value)
    }

    suspend fun saveStaticTarget(value: StaticTargetEntity) {
        TargetClassRules.validateName(value.name)
        requireNotNull(runCatching { StaticTargetClass.valueOf(value.targetClass) }.getOrNull()) {
            "Unsupported static target class"
        }
        require(value.physicalWidthMetres > 0.0 && value.physicalHeightMetres > 0.0) {
            "Target dimensions must be positive"
        }
        require(!value.includeDistanceInDope || value.distanceConfirmed) {
            "Only a confirmed target distance can populate DOPE settings"
        }
        dao.upsertStaticTarget(value)
    }

    suspend fun confirmedDopeTargets(): List<StaticTargetEntity> = dao.confirmedDopeTargets()

    suspend fun saveZeroProfile(value: ZeroProfileEntity) {
        require(value.zeroDistanceMetres > 0.0 && value.sightHeightAboveBoreMetres > 0.0) {
            "Zero distance and sight height must be positive"
        }
        dao.upsertZeroProfile(value)
    }

    suspend fun calculationContext(zeroProfileId: String? = null): CalculationProfileContext? {
        val zero =
            if (zeroProfileId == null) {
                dao.latestVerifiedZeroProfile()
            } else {
                dao.zeroProfile(zeroProfileId)
            }
        return zero?.let { loadCalculationContext(it) }
    }

    private suspend fun loadCalculationContext(zero: ZeroProfileEntity): CalculationProfileContext? {
        val rifle = dao.rifle(zero.rifleId)
        val ammunition = dao.ammunition(zero.ammunitionId)
        val scope = dao.scopeProfile(zero.scopeProfileId)
        val atmosphere = dao.referenceAtmosphere(zero.referenceAtmosphereId)
        val environment = dao.latestEnvironmentalSnapshot()
        if (listOf(rifle, ammunition, scope, atmosphere, environment).any { it == null }) {
            return null
        }
        return CalculationProfileContext(
            requireNotNull(rifle),
            requireNotNull(ammunition),
            requireNotNull(scope),
            zero,
            requireNotNull(atmosphere),
            requireNotNull(environment),
        )
    }

    suspend fun archiveRifle(
        id: String,
        nowEpochMillis: Long,
    ): Boolean = dao.archiveRifle(id, nowEpochMillis) == 1

    suspend fun deleteRifleIfUnreferenced(id: String): Boolean = dao.deleteRifleIfUnreferenced(id) == 1

    suspend fun exportV1(
        appVersion: String,
        createdAtEpochMillis: Long,
        includePreciseLocation: Boolean,
    ): String =
        database.withTransaction {
            ProfileExportCodec.encode(
                ProfileExportV1(
                    metadata =
                        ExportMetadataV1(
                            appVersion = appVersion,
                            createdAtEpochMillis = createdAtEpochMillis,
                            preciseLocationIncluded = includePreciseLocation,
                        ),
                    rifles = dao.exportRifles().map { it.copy(serialNumber = null) },
                    ammunition = dao.exportAmmunition(),
                    chronographStrings = dao.exportChronographStrings(),
                    scopeProfiles = dao.exportScopeProfiles(),
                    scopeVerifications = dao.exportScopeVerifications(),
                    referenceAtmospheres =
                        dao.exportReferenceAtmospheres().map { atmosphere ->
                            if (includePreciseLocation && atmosphere.locationIncludedInExports) {
                                atmosphere
                            } else {
                                atmosphere.copy(latitudeDegrees = null, longitudeDegrees = null)
                            }
                        },
                    savedRanges =
                        dao.exportSavedRanges().map { range ->
                            if (includePreciseLocation) {
                                range
                            } else {
                                range.copy(latitudeDegrees = null, longitudeDegrees = null)
                            }
                        },
                    staticTargets = dao.exportStaticTargets(),
                    zeroProfiles = dao.exportZeroProfiles(),
                ),
            )
        }

    suspend fun importV1(
        payload: String,
        duplicatePolicy: DuplicateImportPolicy,
    ): ProfileExportV1 =
        database.withTransaction {
            val decoded = ProfileExportCodec.decode(payload)
            val existing =
                ExistingProfileIds(
                    rifles = dao.exportRifles().mapTo(mutableSetOf()) { it.id },
                    ammunition = dao.exportAmmunition().mapTo(mutableSetOf()) { it.id },
                    chronographStrings = dao.exportChronographStrings().mapTo(mutableSetOf()) { it.id },
                    scopeProfiles = dao.exportScopeProfiles().mapTo(mutableSetOf()) { it.id },
                    scopeVerifications = dao.exportScopeVerifications().mapTo(mutableSetOf()) { it.id },
                    referenceAtmospheres = dao.exportReferenceAtmospheres().mapTo(mutableSetOf()) { it.id },
                    savedRanges = dao.exportSavedRanges().mapTo(mutableSetOf()) { it.id },
                    staticTargets = dao.exportStaticTargets().mapTo(mutableSetOf()) { it.id },
                    zeroProfiles = dao.exportZeroProfiles().mapTo(mutableSetOf()) { it.id },
                )
            val planned = ProfileImportPlanner.plan(decoded, existing, duplicatePolicy)
            planned.rifles.forEach { dao.upsertRifle(it) }
            planned.ammunition.forEach { dao.upsertAmmunition(it) }
            planned.chronographStrings.forEach { dao.upsertChronographString(it) }
            planned.scopeProfiles.forEach { dao.upsertScopeProfile(it) }
            planned.scopeVerifications.forEach { dao.upsertScopeVerification(it) }
            planned.referenceAtmospheres.forEach { dao.upsertReferenceAtmosphere(it) }
            planned.savedRanges.forEach { dao.upsertSavedRange(it) }
            planned.staticTargets.forEach { dao.upsertStaticTarget(it) }
            planned.zeroProfiles.forEach { dao.upsertZeroProfile(it) }
            planned
        }
}
