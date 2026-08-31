package za.co.dope.ballistics.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
@Suppress("TooManyFunctions")
interface ProfileDao {
    @Upsert suspend fun upsertRifle(value: RifleEntity)

    @Query("SELECT * FROM rifles WHERE archived = 0 ORDER BY favourite DESC, profileName")
    fun observeRifles(): Flow<List<RifleEntity>>

    @Query("SELECT * FROM rifles WHERE id = :id")
    suspend fun rifle(id: String): RifleEntity?

    @Query(
        "UPDATE rifles SET archived = 1, modifiedAtEpochMillis = :modifiedAt, " +
            "revision = revision + 1 WHERE id = :id",
    )
    suspend fun archiveRifle(
        id: String,
        modifiedAt: Long,
    ): Int

    @Query(
        "DELETE FROM rifles WHERE id = :id " +
            "AND NOT EXISTS (SELECT 1 FROM ammunition WHERE rifleId = :id) " +
            "AND NOT EXISTS (SELECT 1 FROM zero_profiles WHERE rifleId = :id)",
    )
    suspend fun deleteRifleIfUnreferenced(id: String): Int

    @Upsert suspend fun upsertAmmunition(value: AmmunitionEntity)

    @Query("SELECT * FROM ammunition WHERE archived = 0 ORDER BY favourite DESC, profileName")
    fun observeAmmunition(): Flow<List<AmmunitionEntity>>

    @Query("SELECT * FROM ammunition WHERE id = :id")
    suspend fun ammunition(id: String): AmmunitionEntity?

    @Query(
        "UPDATE ammunition SET archived = 1, modifiedAtEpochMillis = :modifiedAt, " +
            "revision = revision + 1 WHERE id = :id",
    )
    suspend fun archiveAmmunition(
        id: String,
        modifiedAt: Long,
    ): Int

    @Upsert suspend fun upsertChronographString(value: ChronographStringEntity)

    @Query(
        "SELECT * FROM chronograph_strings WHERE ammunitionId = :ammunitionId AND archived = 0 " +
            "ORDER BY capturedAtEpochMillis DESC",
    )
    fun observeChronographStrings(ammunitionId: String): Flow<List<ChronographStringEntity>>

    @Upsert suspend fun upsertScopeFamily(value: ScopeFamilyEntity)

    @Upsert suspend fun upsertScopeVariant(value: ScopeVariantEntity)

    @Query("SELECT * FROM scope_families ORDER BY manufacturer, model")
    fun observeScopeFamilies(): Flow<List<ScopeFamilyEntity>>

    @Query("SELECT * FROM scope_families ORDER BY manufacturer, model")
    suspend fun scopeFamilies(): List<ScopeFamilyEntity>

    @Query("SELECT * FROM scope_variants WHERE familyId = :familyId ORDER BY displayName")
    suspend fun scopeVariants(familyId: String): List<ScopeVariantEntity>

    @Query("SELECT * FROM scope_variants WHERE id = :id")
    suspend fun scopeVariant(id: String): ScopeVariantEntity?

    @Query("SELECT * FROM scope_families WHERE id = :id")
    suspend fun scopeFamily(id: String): ScopeFamilyEntity?

    @Upsert suspend fun upsertScopeProfile(value: ScopeProfileEntity)

    @Query("SELECT * FROM scope_profiles WHERE archived = 0 ORDER BY favourite DESC, profileName")
    fun observeScopeProfiles(): Flow<List<ScopeProfileEntity>>

    @Query("SELECT * FROM scope_profiles WHERE id = :id")
    suspend fun scopeProfile(id: String): ScopeProfileEntity?

    @Query(
        "UPDATE scope_profiles SET archived = 1, modifiedAtEpochMillis = :modifiedAt, " +
            "revision = revision + 1 WHERE id = :id",
    )
    suspend fun archiveScopeProfile(
        id: String,
        modifiedAt: Long,
    ): Int

    @Upsert suspend fun upsertScopeVerification(value: ScopeVerificationEntity)

    @Query(
        "SELECT * FROM scope_verifications WHERE scopeProfileId = :scopeProfileId " +
            "ORDER BY verifiedAtEpochMillis DESC",
    )
    suspend fun scopeVerifications(scopeProfileId: String): List<ScopeVerificationEntity>

    @Upsert suspend fun upsertReferenceAtmosphere(value: ReferenceAtmosphereEntity)

    @Query("SELECT * FROM reference_atmospheres WHERE archived = 0 ORDER BY favourite DESC, name")
    fun observeReferenceAtmospheres(): Flow<List<ReferenceAtmosphereEntity>>

    @Query("SELECT * FROM reference_atmospheres WHERE id = :id")
    suspend fun referenceAtmosphere(id: String): ReferenceAtmosphereEntity?

    @Upsert suspend fun upsertSavedRange(value: SavedRangeEntity)

    @Query("SELECT * FROM saved_ranges WHERE archived = 0 ORDER BY favourite DESC, rangeName")
    fun observeSavedRanges(): Flow<List<SavedRangeEntity>>

    @Query("SELECT * FROM saved_ranges WHERE id = :id")
    suspend fun savedRange(id: String): SavedRangeEntity?

    @Upsert suspend fun upsertStaticTarget(value: StaticTargetEntity)

    @Query(
        "SELECT * FROM static_targets WHERE archived = 0 " +
            "AND (:savedRangeId IS NULL OR savedRangeId = :savedRangeId) ORDER BY name",
    )
    fun observeStaticTargets(savedRangeId: String?): Flow<List<StaticTargetEntity>>

    @Query("SELECT * FROM static_targets WHERE id = :id")
    suspend fun staticTarget(id: String): StaticTargetEntity?

    @Upsert suspend fun upsertZeroProfile(value: ZeroProfileEntity)

    @Query("SELECT * FROM zero_profiles WHERE archived = 0 ORDER BY favourite DESC, modifiedAtEpochMillis DESC")
    fun observeZeroProfiles(): Flow<List<ZeroProfileEntity>>

    @Query("SELECT * FROM zero_profiles WHERE id = :id")
    suspend fun zeroProfile(id: String): ZeroProfileEntity?

    @Query(
        "UPDATE zero_profiles SET verified = 0, modifiedAtEpochMillis = :modifiedAt, " +
            "revision = revision + 1 WHERE dependencyFingerprint != :fingerprint",
    )
    suspend fun invalidateZeroProfilesWithDifferentFingerprint(
        fingerprint: String,
        modifiedAt: Long,
    ): Int

    @Query("SELECT * FROM rifles")
    suspend fun exportRifles(): List<RifleEntity>

    @Query("SELECT * FROM ammunition")
    suspend fun exportAmmunition(): List<AmmunitionEntity>

    @Query("SELECT * FROM chronograph_strings")
    suspend fun exportChronographStrings(): List<ChronographStringEntity>

    @Query("SELECT * FROM scope_profiles")
    suspend fun exportScopeProfiles(): List<ScopeProfileEntity>

    @Query("SELECT * FROM scope_verifications")
    suspend fun exportScopeVerifications(): List<ScopeVerificationEntity>

    @Query("SELECT * FROM reference_atmospheres")
    suspend fun exportReferenceAtmospheres(): List<ReferenceAtmosphereEntity>

    @Query("SELECT * FROM saved_ranges")
    suspend fun exportSavedRanges(): List<SavedRangeEntity>

    @Query("SELECT * FROM static_targets")
    suspend fun exportStaticTargets(): List<StaticTargetEntity>

    @Query("SELECT * FROM zero_profiles")
    suspend fun exportZeroProfiles(): List<ZeroProfileEntity>
}
