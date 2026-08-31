package za.co.dope.ballistics.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "rifles")
data class RifleEntity(
    @PrimaryKey val id: String,
    val profileName: String,
    val manufacturer: String,
    val model: String,
    val calibreLabel: String,
    val barrelLengthMetres: Double,
    val twistRateMetres: Double,
    val twistDirection: String,
    val internalReference: String? = null,
    val serialNumber: String? = null,
    val createdAtEpochMillis: Long,
    val modifiedAtEpochMillis: Long,
    val revision: Long = 1,
    val archived: Boolean = false,
    val favourite: Boolean = false,
    val notes: String? = null,
)

@Serializable
@Entity(
    tableName = "ammunition",
    foreignKeys = [
        ForeignKey(
            entity = RifleEntity::class,
            parentColumns = ["id"],
            childColumns = ["rifleId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [Index("rifleId")],
)
data class AmmunitionEntity(
    @PrimaryKey val id: String,
    val rifleId: String,
    val profileName: String,
    val manufacturer: String,
    val productLoadName: String,
    val bulletManufacturer: String,
    val bulletName: String,
    val bulletWeightKilograms: Double,
    val bulletDiameterMetres: Double? = null,
    val g1BallisticCoefficient: Double? = null,
    val g7BallisticCoefficient: Double? = null,
    val selectedDragModel: String,
    val muzzleVelocityMetresPerSecond: Double,
    val muzzleVelocityStandardDeviation: Double? = null,
    val velocityTemperatureCoefficient: Double? = null,
    val ammunitionReferenceTemperatureKelvin: Double? = null,
    val lotNumber: String? = null,
    val chronographDateEpochMillis: Long? = null,
    val chronographType: String? = null,
    val selectedChronographStringId: String? = null,
    val createdAtEpochMillis: Long,
    val modifiedAtEpochMillis: Long,
    val revision: Long = 1,
    val archived: Boolean = false,
    val favourite: Boolean = false,
    val notes: String? = null,
)

@Serializable
@Entity(
    tableName = "chronograph_strings",
    foreignKeys = [
        ForeignKey(
            entity = AmmunitionEntity::class,
            parentColumns = ["id"],
            childColumns = ["ammunitionId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [Index("ammunitionId")],
)
data class ChronographStringEntity(
    @PrimaryKey val id: String,
    val ammunitionId: String,
    val readingsMetresPerSecondJson: String,
    val averageMetresPerSecond: Double,
    val medianMetresPerSecond: Double,
    val minimumMetresPerSecond: Double,
    val maximumMetresPerSecond: Double,
    val extremeSpreadMetresPerSecond: Double,
    val sampleStandardDeviationMetresPerSecond: Double,
    val sampleCount: Int,
    val ammunitionTemperatureKelvin: Double? = null,
    val capturedAtEpochMillis: Long,
    val chronograph: String,
    val createdAtEpochMillis: Long,
    val modifiedAtEpochMillis: Long,
    val revision: Long = 1,
    val archived: Boolean = false,
    val favourite: Boolean = false,
    val notes: String? = null,
)

@Serializable
@Entity(tableName = "scope_families")
data class ScopeFamilyEntity(
    @PrimaryKey val id: String,
    val manufacturer: String,
    val model: String,
    val displayName: String,
    val minimumMagnification: Double,
    val maximumMagnification: Double,
    val objectiveDiameterMetres: Double,
    val tubeDiameterMetres: Double,
    val focalPlane: String,
    val illuminated: Boolean,
    val zeroStopAvailable: Boolean,
    val parallaxAdjustment: String,
    val turretCapType: String,
    val manufacturerSpecificationSourceNote: String,
    val builtInImmutable: Boolean = true,
)

@Serializable
@Entity(
    tableName = "scope_variants",
    foreignKeys = [
        ForeignKey(
            entity = ScopeFamilyEntity::class,
            parentColumns = ["id"],
            childColumns = ["familyId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [Index("familyId")],
)
data class ScopeVariantEntity(
    @PrimaryKey val id: String,
    val familyId: String,
    val displayName: String,
    val turretUnit: String,
    val nominalElevationClickRadians: Double,
    val nominalWindageClickRadians: Double,
    val reticleSystem: String,
    val reticleName: String,
    val reticleSubtensionMetadata: String? = null,
    val bdcMetadata: String? = null,
    val nominalElevationTravelRadians: Double? = null,
    val nominalWindageTravelRadians: Double? = null,
    val verificationStatus: String,
    val builtInImmutable: Boolean = true,
)

@Serializable
@Entity(
    tableName = "scope_profiles",
    foreignKeys = [
        ForeignKey(
            entity = ScopeFamilyEntity::class,
            parentColumns = ["id"],
            childColumns = ["familyId"],
            onDelete = ForeignKey.RESTRICT,
        ),
        ForeignKey(
            entity = ScopeVariantEntity::class,
            parentColumns = ["id"],
            childColumns = ["variantId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [Index("familyId"), Index("variantId")],
)
data class ScopeProfileEntity(
    @PrimaryKey val id: String,
    val familyId: String,
    val variantId: String,
    val profileName: String,
    val manufacturer: String,
    val model: String,
    val minimumMagnification: Double,
    val maximumMagnification: Double,
    val objectiveDiameterMetres: Double,
    val tubeDiameterMetres: Double,
    val focalPlane: String,
    val turretUnit: String,
    val elevationClickValueRadians: Double,
    val windageClickValueRadians: Double,
    val reticleSystem: String,
    val reticleName: String,
    val reticleSubtensionMetadata: String? = null,
    val bdcMetadata: String? = null,
    val elevationTravelRadians: Double? = null,
    val windageTravelRadians: Double? = null,
    val zeroStopAvailable: Boolean,
    val parallaxAdjustment: String,
    val illumination: Boolean,
    val turretCapType: String,
    val elevationDialDirection: String,
    val windageDialDirection: String,
    val sightHeightMetres: Double,
    val zeroDistanceMetres: Double,
    val zeroElevationOffsetRadians: Double = 0.0,
    val zeroWindageOffsetRadians: Double = 0.0,
    val manufacturerSpecificationSourceNote: String,
    val verificationStatus: String,
    val verificationDateEpochMillis: Long? = null,
    val bdcCalibrationVerified: Boolean = false,
    val createdAtEpochMillis: Long,
    val modifiedAtEpochMillis: Long,
    val revision: Long = 1,
    val archived: Boolean = false,
    val favourite: Boolean = false,
    val notes: String? = null,
)

@Serializable
@Entity(
    tableName = "scope_verifications",
    foreignKeys = [
        ForeignKey(
            entity = ScopeProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["scopeProfileId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("scopeProfileId")],
)
data class ScopeVerificationEntity(
    @PrimaryKey val id: String,
    val scopeProfileId: String,
    val physicalModelConfirmed: Boolean,
    val turretUnitConfirmed: Boolean,
    val clickValueConfirmed: Boolean,
    val reticleConfirmed: Boolean,
    val focalPlaneConfirmed: Boolean,
    val elevationDialDirectionConfirmed: Boolean,
    val windageDialDirectionConfirmed: Boolean,
    val zeroStopConfirmed: Boolean,
    val sightHeightConfirmed: Boolean,
    val zeroDistanceConfirmed: Boolean,
    val verifiedAtEpochMillis: Long? = null,
    val notes: String? = null,
)

@Serializable
@Entity(tableName = "reference_atmospheres")
data class ReferenceAtmosphereEntity(
    @PrimaryKey val id: String,
    val name: String,
    val temperatureKelvin: Double,
    val temperatureSource: String,
    val stationPressurePascals: Double,
    val pressureSource: String,
    val relativeHumidityFraction: Double? = null,
    val dewPointKelvin: Double? = null,
    val humiditySource: String,
    val altitudeMetres: Double,
    val altitudeSource: String,
    val capturedAtEpochMillis: Long,
    val latitudeDegrees: Double? = null,
    val longitudeDegrees: Double? = null,
    val locationIncludedInExports: Boolean = false,
    val densityAltitudeMetres: Double? = null,
    val createdAtEpochMillis: Long,
    val modifiedAtEpochMillis: Long,
    val revision: Long = 1,
    val archived: Boolean = false,
    val favourite: Boolean = false,
    val notes: String? = null,
)

@Entity(tableName = "environmental_snapshots")
data class EnvironmentalSnapshotEntity(
    @PrimaryKey val id: String,
    val name: String,
    val temperatureKelvin: Double,
    val temperatureSource: String,
    val temperatureQuality: String,
    val temperatureCapturedAtEpochMillis: Long,
    val stationPressurePascals: Double,
    val pressureSource: String,
    val pressureQuality: String,
    val pressureCapturedAtEpochMillis: Long,
    val relativeHumidityFraction: Double,
    val humiditySource: String,
    val humidityQuality: String,
    val humidityCapturedAtEpochMillis: Long,
    val altitudeMetres: Double,
    val altitudeSource: String,
    val altitudeQuality: String,
    val altitudeCapturedAtEpochMillis: Long,
    val latitudeDegrees: Double? = null,
    val longitudeDegrees: Double? = null,
    val horizontalAccuracyMetres: Double? = null,
    val verticalAccuracyMetres: Double? = null,
    val approximateLocation: Boolean = false,
    val locationIncludedInExports: Boolean = false,
    val magneticHeadingDegrees: Double? = null,
    val trueHeadingDegrees: Double? = null,
    val pitchDegrees: Double? = null,
    val rollDegrees: Double? = null,
    val orientationQuality: String? = null,
    val orientationStable: Boolean? = null,
    val pressureSampleSummaryJson: String? = null,
    val airDensityKilogramsPerCubicMetre: Double,
    val densityRatio: Double,
    val pressureAltitudeMetres: Double,
    val densityAltitudeMetres: Double,
    val dewPointKelvin: Double,
    val waterVapourPressurePascals: Double,
    val speedOfSoundMetresPerSecond: Double,
    val providerName: String? = null,
    val providerAttribution: String? = null,
    val capturedAtEpochMillis: Long,
    val notes: String? = null,
)

@Entity(tableName = "weather_cache", indices = [Index(value = ["fetchedAtEpochMillis"])])
data class WeatherCacheEntity(
    @PrimaryKey val coordinateKey: String,
    val latitudeDegrees: Double,
    val longitudeDegrees: Double,
    val temperatureKelvin: Double,
    val surfacePressurePascals: Double,
    val meanSeaLevelPressurePascals: Double?,
    val relativeHumidityFraction: Double,
    val windSpeedMetresPerSecond: Double?,
    val windDirectionDegrees: Double?,
    val providerName: String,
    val attribution: String,
    val modelElevationMetres: Double?,
    val fetchedAtEpochMillis: Long,
)

@Serializable
@Entity(tableName = "saved_ranges")
data class SavedRangeEntity(
    @PrimaryKey val id: String,
    val rangeName: String,
    val latitudeDegrees: Double? = null,
    val longitudeDegrees: Double? = null,
    val surveyedAltitudeMetres: Double? = null,
    val commonDistancesMetresJson: String,
    val magneticDeclinationRadians: Double? = null,
    val locationStoragePreference: String,
    val createdAtEpochMillis: Long,
    val modifiedAtEpochMillis: Long,
    val revision: Long = 1,
    val archived: Boolean = false,
    val favourite: Boolean = false,
    val notes: String? = null,
)

@Serializable
@Entity(
    tableName = "static_targets",
    foreignKeys = [
        ForeignKey(
            entity = SavedRangeEntity::class,
            parentColumns = ["id"],
            childColumns = ["savedRangeId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("savedRangeId")],
)
data class StaticTargetEntity(
    @PrimaryKey val id: String,
    val savedRangeId: String? = null,
    val name: String,
    val targetClass: String,
    val physicalWidthMetres: Double,
    val physicalHeightMetres: Double,
    val measuredDistanceMetres: Double? = null,
    val distanceSource: String? = null,
    val distanceQuality: String? = null,
    val distanceMeasuredAtEpochMillis: Long? = null,
    val distanceUncertaintyMetres: Double? = null,
    val distanceConfirmed: Boolean = false,
    val includeDistanceInDope: Boolean = false,
    val centreLatitudeDegrees: Double? = null,
    val centreLongitudeDegrees: Double? = null,
    val scoringZonesJson: String = "[]",
    val calibrationMarkerPositionsJson: String? = null,
    val referenceImageUri: String? = null,
    val expectedImpactDiameterMinimumMetres: Double? = null,
    val expectedImpactDiameterMaximumMetres: Double? = null,
    val createdAtEpochMillis: Long,
    val modifiedAtEpochMillis: Long,
    val revision: Long = 1,
    val archived: Boolean = false,
    val favourite: Boolean = false,
    val notes: String? = null,
)

@Serializable
@Entity(
    tableName = "zero_profiles",
    foreignKeys = [
        ForeignKey(entity = RifleEntity::class, parentColumns = ["id"], childColumns = ["rifleId"]),
        ForeignKey(entity = AmmunitionEntity::class, parentColumns = ["id"], childColumns = ["ammunitionId"]),
        ForeignKey(entity = ScopeProfileEntity::class, parentColumns = ["id"], childColumns = ["scopeProfileId"]),
        ForeignKey(
            entity = ReferenceAtmosphereEntity::class,
            parentColumns = ["id"],
            childColumns = ["referenceAtmosphereId"],
        ),
    ],
    indices = [Index("rifleId"), Index("ammunitionId"), Index("scopeProfileId"), Index("referenceAtmosphereId")],
)
data class ZeroProfileEntity(
    @PrimaryKey val id: String,
    val rifleId: String,
    val ammunitionId: String,
    val scopeProfileId: String,
    val zeroDistanceMetres: Double,
    val sightHeightAboveBoreMetres: Double,
    val zeroElevationOffsetRadians: Double,
    val zeroWindageOffsetRadians: Double,
    val zeroConfirmationDateEpochMillis: Long,
    val referenceAtmosphereId: String,
    val verified: Boolean,
    val dependencyFingerprint: String,
    val createdAtEpochMillis: Long,
    val modifiedAtEpochMillis: Long,
    val revision: Long = 1,
    val archived: Boolean = false,
    val favourite: Boolean = false,
    val notes: String? = null,
)
