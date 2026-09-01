package za.co.dope.ballistics.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        RifleEntity::class,
        AmmunitionEntity::class,
        ChronographStringEntity::class,
        ScopeFamilyEntity::class,
        ScopeVariantEntity::class,
        ScopeProfileEntity::class,
        ScopeVerificationEntity::class,
        ReferenceAtmosphereEntity::class,
        EnvironmentalSnapshotEntity::class,
        WeatherCacheEntity::class,
        SavedRangeEntity::class,
        StaticTargetEntity::class,
        ZeroProfileEntity::class,
        ActiveProfileSelectionEntity::class,
        SessionSnapshotEntity::class,
        VerifiedDopeRecordEntity::class,
    ],
    version = 7,
    exportSchema = true,
)
abstract class DopeDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao

    companion object {
        const val DATABASE_NAME = "dope.db"

        val MIGRATION_1_2 =
            object : Migration(1, 2) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    DopeSchemaV2.createStatements.forEach(db::execSQL)
                    ScopeTemplates.insertBuiltIns(db)
                }
            }

        val MIGRATION_2_3 =
            object : Migration(2, 3) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    DopeSchemaV3.createStatements.forEach(db::execSQL)
                }
            }

        val MIGRATION_3_4 =
            object : Migration(3, 4) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    DopeSchemaV4.createStatements.forEach(db::execSQL)
                }
            }

        val MIGRATION_4_5 =
            object : Migration(4, 5) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    DopeSchemaV5.createStatements.forEach(db::execSQL)
                }
            }

        val MIGRATION_5_6 =
            object : Migration(5, 6) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    StarterProfiles.insert(db)
                }
            }

        /** Repairs installations that reached schema 6 from an early review APK without starter rows. */
        val MIGRATION_6_7 =
            object : Migration(6, 7) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    StarterProfiles.insert(db)
                }
            }

        @Volatile private var instance: DopeDatabase? = null

        fun getInstance(context: Context): DopeDatabase =
            instance ?: synchronized(this) {
                instance
                    ?: Room
                        .databaseBuilder(context.applicationContext, DopeDatabase::class.java, DATABASE_NAME)
                        .addMigrations(
                            MIGRATION_1_2,
                            MIGRATION_2_3,
                            MIGRATION_3_4,
                            MIGRATION_4_5,
                            MIGRATION_5_6,
                            MIGRATION_6_7,
                        ).addCallback(
                            object : Callback() {
                                override fun onCreate(db: SupportSQLiteDatabase) {
                                    super.onCreate(db)
                                    StarterProfiles.insert(db)
                                }
                            },
                        ).build()
                        .also { instance = it }
            }
    }
}

internal object DopeSchemaV5 {
    val createStatements =
        listOf(
            """
            CREATE TABLE IF NOT EXISTS `active_profile_selection` (
                `id` TEXT NOT NULL, `zeroProfileId` TEXT NOT NULL,
                `updatedAtEpochMillis` INTEGER NOT NULL, PRIMARY KEY(`id`)
            )
            """.trimIndent(),
        )
}

internal object DopeSchemaV4 {
    val createStatements =
        listOf(
            """
            CREATE TABLE IF NOT EXISTS `session_snapshots` (
                `id` TEXT NOT NULL, `schemaVersion` INTEGER NOT NULL, `sessionName` TEXT NOT NULL,
                `startedAtEpochMillis` INTEGER NOT NULL, `completedAtEpochMillis` INTEGER NOT NULL,
                `savedRangeId` TEXT, `savedRangeName` TEXT, `preciseLocationIncluded` INTEGER NOT NULL,
                `locationSnapshotJson` TEXT, `rifleId` TEXT NOT NULL, `rifleRevision` INTEGER NOT NULL,
                `ammunitionId` TEXT NOT NULL, `ammunitionRevision` INTEGER NOT NULL,
                `scopeProfileId` TEXT NOT NULL, `scopeProfileRevision` INTEGER NOT NULL,
                `zeroProfileId` TEXT NOT NULL, `zeroProfileRevision` INTEGER NOT NULL,
                `profileSnapshotJson` TEXT NOT NULL, `referenceEnvironmentJson` TEXT NOT NULL,
                `currentEnvironmentJson` TEXT NOT NULL, `fieldSourcesJson` TEXT NOT NULL,
                `distanceMetres` REAL NOT NULL, `distanceSource` TEXT NOT NULL,
                `distanceUncertaintyMetres` REAL NOT NULL, `directionOfFireTrueDegrees` REAL,
                `inclinationDegrees` REAL NOT NULL, `windSnapshotJson` TEXT NOT NULL,
                `calculationResultJson` TEXT NOT NULL, `calculationTraceJson` TEXT NOT NULL,
                `engineVersion` TEXT NOT NULL, `scopeRoundingJson` TEXT NOT NULL,
                `warningsJson` TEXT NOT NULL, `notes` TEXT, `photoUrisJson` TEXT NOT NULL,
                `trainingVideoUrisJson` TEXT NOT NULL, `rangeAnalystStringsJson` TEXT NOT NULL,
                `contentSha256` TEXT NOT NULL, PRIMARY KEY(`id`)
            )
            """.trimIndent(),
            "CREATE INDEX IF NOT EXISTS `index_session_snapshots_completedAtEpochMillis` " +
                "ON `session_snapshots` (`completedAtEpochMillis`)",
            "CREATE INDEX IF NOT EXISTS `index_session_snapshots_rifleId` ON `session_snapshots` (`rifleId`)",
            "CREATE INDEX IF NOT EXISTS `index_session_snapshots_savedRangeId` ON `session_snapshots` (`savedRangeId`)",
            """
            CREATE TABLE IF NOT EXISTS `verified_dope_records` (
                `id` TEXT NOT NULL, `schemaVersion` INTEGER NOT NULL, `sessionSnapshotId` TEXT,
                `supersedesRecordId` TEXT, `createdAtEpochMillis` INTEGER NOT NULL,
                `rifleId` TEXT NOT NULL, `rifleRevision` INTEGER NOT NULL,
                `ammunitionId` TEXT NOT NULL, `ammunitionRevision` INTEGER NOT NULL,
                `scopeProfileId` TEXT NOT NULL, `scopeProfileRevision` INTEGER NOT NULL,
                `zeroProfileId` TEXT NOT NULL, `zeroProfileRevision` INTEGER NOT NULL,
                `profileSnapshotJson` TEXT NOT NULL, `distanceMetres` REAL NOT NULL,
                `distanceSource` TEXT NOT NULL, `distanceUncertaintyMetres` REAL NOT NULL,
                `calculatedUnit` TEXT NOT NULL, `calculatedRawValue` REAL NOT NULL,
                `calculatedDialValue` REAL NOT NULL, `calculatedClicks` INTEGER NOT NULL,
                `actualDialUnit` TEXT NOT NULL, `actualDialValue` REAL NOT NULL,
                `actualDialClicks` INTEGER, `observedGroupCentreVerticalMetres` REAL,
                `observedGroupCentreHorizontalMetres` REAL, `groupSizeMetres` REAL,
                `numberOfShots` INTEGER NOT NULL, `conditionsJson` TEXT NOT NULL,
                `confidence` TEXT NOT NULL, `status` TEXT NOT NULL, `engineVersion` TEXT NOT NULL,
                `notes` TEXT, `evidenceSha256` TEXT NOT NULL, PRIMARY KEY(`id`)
            )
            """.trimIndent(),
            "CREATE INDEX IF NOT EXISTS `index_verified_dope_records_createdAtEpochMillis` " +
                "ON `verified_dope_records` (`createdAtEpochMillis`)",
            "CREATE INDEX IF NOT EXISTS `index_verified_dope_records_rifleId` " +
                "ON `verified_dope_records` (`rifleId`)",
            "CREATE INDEX IF NOT EXISTS `index_verified_dope_records_sessionSnapshotId` " +
                "ON `verified_dope_records` (`sessionSnapshotId`)",
        )
}

internal object DopeSchemaV3 {
    val createStatements =
        listOf(
            """
            CREATE TABLE IF NOT EXISTS `environmental_snapshots` (
                `id` TEXT NOT NULL, `name` TEXT NOT NULL, `temperatureKelvin` REAL NOT NULL,
                `temperatureSource` TEXT NOT NULL, `temperatureQuality` TEXT NOT NULL,
                `temperatureCapturedAtEpochMillis` INTEGER NOT NULL,
                `stationPressurePascals` REAL NOT NULL, `pressureSource` TEXT NOT NULL,
                `pressureQuality` TEXT NOT NULL, `pressureCapturedAtEpochMillis` INTEGER NOT NULL,
                `relativeHumidityFraction` REAL NOT NULL, `humiditySource` TEXT NOT NULL,
                `humidityQuality` TEXT NOT NULL, `humidityCapturedAtEpochMillis` INTEGER NOT NULL,
                `altitudeMetres` REAL NOT NULL, `altitudeSource` TEXT NOT NULL,
                `altitudeQuality` TEXT NOT NULL, `altitudeCapturedAtEpochMillis` INTEGER NOT NULL,
                `latitudeDegrees` REAL, `longitudeDegrees` REAL, `horizontalAccuracyMetres` REAL,
                `verticalAccuracyMetres` REAL, `approximateLocation` INTEGER NOT NULL,
                `locationIncludedInExports` INTEGER NOT NULL, `magneticHeadingDegrees` REAL,
                `trueHeadingDegrees` REAL, `pitchDegrees` REAL, `rollDegrees` REAL,
                `orientationQuality` TEXT, `orientationStable` INTEGER,
                `pressureSampleSummaryJson` TEXT, `airDensityKilogramsPerCubicMetre` REAL NOT NULL,
                `densityRatio` REAL NOT NULL, `pressureAltitudeMetres` REAL NOT NULL,
                `densityAltitudeMetres` REAL NOT NULL, `dewPointKelvin` REAL NOT NULL,
                `waterVapourPressurePascals` REAL NOT NULL, `speedOfSoundMetresPerSecond` REAL NOT NULL,
                `providerName` TEXT, `providerAttribution` TEXT, `capturedAtEpochMillis` INTEGER NOT NULL,
                `notes` TEXT, PRIMARY KEY(`id`)
            )
            """.trimIndent(),
            """
            CREATE TABLE IF NOT EXISTS `weather_cache` (
                `coordinateKey` TEXT NOT NULL, `latitudeDegrees` REAL NOT NULL,
                `longitudeDegrees` REAL NOT NULL, `temperatureKelvin` REAL NOT NULL,
                `surfacePressurePascals` REAL NOT NULL, `meanSeaLevelPressurePascals` REAL,
                `relativeHumidityFraction` REAL NOT NULL, `windSpeedMetresPerSecond` REAL,
                `windDirectionDegrees` REAL, `providerName` TEXT NOT NULL, `attribution` TEXT NOT NULL,
                `modelElevationMetres` REAL, `fetchedAtEpochMillis` INTEGER NOT NULL,
                PRIMARY KEY(`coordinateKey`)
            )
            """.trimIndent(),
            "CREATE INDEX IF NOT EXISTS `index_weather_cache_fetchedAtEpochMillis` " +
                "ON `weather_cache` (`fetchedAtEpochMillis`)",
        )
}

internal object DopeSchemaV2 {
    val createStatements =
        listOf(
            """
            CREATE TABLE IF NOT EXISTS `rifles` (
                `id` TEXT NOT NULL, `profileName` TEXT NOT NULL, `manufacturer` TEXT NOT NULL,
                `model` TEXT NOT NULL, `calibreLabel` TEXT NOT NULL, `barrelLengthMetres` REAL NOT NULL,
                `twistRateMetres` REAL NOT NULL, `twistDirection` TEXT NOT NULL, `internalReference` TEXT,
                `serialNumber` TEXT, `createdAtEpochMillis` INTEGER NOT NULL,
                `modifiedAtEpochMillis` INTEGER NOT NULL, `revision` INTEGER NOT NULL,
                `archived` INTEGER NOT NULL, `favourite` INTEGER NOT NULL, `notes` TEXT,
                PRIMARY KEY(`id`)
            )
            """.trimIndent(),
            """
            CREATE TABLE IF NOT EXISTS `ammunition` (
                `id` TEXT NOT NULL, `rifleId` TEXT NOT NULL, `profileName` TEXT NOT NULL,
                `manufacturer` TEXT NOT NULL, `productLoadName` TEXT NOT NULL,
                `bulletManufacturer` TEXT NOT NULL, `bulletName` TEXT NOT NULL,
                `bulletWeightKilograms` REAL NOT NULL, `bulletDiameterMetres` REAL,
                `g1BallisticCoefficient` REAL, `g7BallisticCoefficient` REAL,
                `selectedDragModel` TEXT NOT NULL, `muzzleVelocityMetresPerSecond` REAL NOT NULL,
                `muzzleVelocityStandardDeviation` REAL, `velocityTemperatureCoefficient` REAL,
                `ammunitionReferenceTemperatureKelvin` REAL, `lotNumber` TEXT,
                `chronographDateEpochMillis` INTEGER, `chronographType` TEXT,
                `selectedChronographStringId` TEXT, `createdAtEpochMillis` INTEGER NOT NULL,
                `modifiedAtEpochMillis` INTEGER NOT NULL, `revision` INTEGER NOT NULL,
                `archived` INTEGER NOT NULL, `favourite` INTEGER NOT NULL, `notes` TEXT,
                PRIMARY KEY(`id`),
                FOREIGN KEY(`rifleId`) REFERENCES `rifles`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT
            )
            """.trimIndent(),
            """
            CREATE TABLE IF NOT EXISTS `chronograph_strings` (
                `id` TEXT NOT NULL, `ammunitionId` TEXT NOT NULL,
                `readingsMetresPerSecondJson` TEXT NOT NULL, `averageMetresPerSecond` REAL NOT NULL,
                `medianMetresPerSecond` REAL NOT NULL, `minimumMetresPerSecond` REAL NOT NULL,
                `maximumMetresPerSecond` REAL NOT NULL, `extremeSpreadMetresPerSecond` REAL NOT NULL,
                `sampleStandardDeviationMetresPerSecond` REAL NOT NULL, `sampleCount` INTEGER NOT NULL,
                `ammunitionTemperatureKelvin` REAL, `capturedAtEpochMillis` INTEGER NOT NULL,
                `chronograph` TEXT NOT NULL, `createdAtEpochMillis` INTEGER NOT NULL,
                `modifiedAtEpochMillis` INTEGER NOT NULL, `revision` INTEGER NOT NULL,
                `archived` INTEGER NOT NULL, `favourite` INTEGER NOT NULL, `notes` TEXT,
                PRIMARY KEY(`id`),
                FOREIGN KEY(`ammunitionId`) REFERENCES `ammunition`(`id`)
                    ON UPDATE NO ACTION ON DELETE RESTRICT
            )
            """.trimIndent(),
            """
            CREATE TABLE IF NOT EXISTS `scope_families` (
                `id` TEXT NOT NULL, `manufacturer` TEXT NOT NULL, `model` TEXT NOT NULL,
                `displayName` TEXT NOT NULL, `minimumMagnification` REAL NOT NULL,
                `maximumMagnification` REAL NOT NULL, `objectiveDiameterMetres` REAL NOT NULL,
                `tubeDiameterMetres` REAL NOT NULL, `focalPlane` TEXT NOT NULL,
                `illuminated` INTEGER NOT NULL, `zeroStopAvailable` INTEGER NOT NULL,
                `parallaxAdjustment` TEXT NOT NULL, `turretCapType` TEXT NOT NULL,
                `manufacturerSpecificationSourceNote` TEXT NOT NULL,
                `builtInImmutable` INTEGER NOT NULL, PRIMARY KEY(`id`)
            )
            """.trimIndent(),
            """
            CREATE TABLE IF NOT EXISTS `scope_variants` (
                `id` TEXT NOT NULL, `familyId` TEXT NOT NULL, `displayName` TEXT NOT NULL,
                `turretUnit` TEXT NOT NULL, `nominalElevationClickRadians` REAL NOT NULL,
                `nominalWindageClickRadians` REAL NOT NULL, `reticleSystem` TEXT NOT NULL,
                `reticleName` TEXT NOT NULL, `reticleSubtensionMetadata` TEXT, `bdcMetadata` TEXT,
                `nominalElevationTravelRadians` REAL, `nominalWindageTravelRadians` REAL,
                `verificationStatus` TEXT NOT NULL, `builtInImmutable` INTEGER NOT NULL,
                PRIMARY KEY(`id`),
                FOREIGN KEY(`familyId`) REFERENCES `scope_families`(`id`)
                    ON UPDATE NO ACTION ON DELETE RESTRICT
            )
            """.trimIndent(),
            """
            CREATE TABLE IF NOT EXISTS `scope_profiles` (
                `id` TEXT NOT NULL, `familyId` TEXT NOT NULL, `variantId` TEXT NOT NULL,
                `profileName` TEXT NOT NULL, `manufacturer` TEXT NOT NULL, `model` TEXT NOT NULL,
                `minimumMagnification` REAL NOT NULL, `maximumMagnification` REAL NOT NULL,
                `objectiveDiameterMetres` REAL NOT NULL, `tubeDiameterMetres` REAL NOT NULL,
                `focalPlane` TEXT NOT NULL, `turretUnit` TEXT NOT NULL,
                `elevationClickValueRadians` REAL NOT NULL, `windageClickValueRadians` REAL NOT NULL,
                `reticleSystem` TEXT NOT NULL, `reticleName` TEXT NOT NULL,
                `reticleSubtensionMetadata` TEXT, `bdcMetadata` TEXT, `elevationTravelRadians` REAL,
                `windageTravelRadians` REAL, `zeroStopAvailable` INTEGER NOT NULL,
                `parallaxAdjustment` TEXT NOT NULL, `illumination` INTEGER NOT NULL,
                `turretCapType` TEXT NOT NULL, `elevationDialDirection` TEXT NOT NULL,
                `windageDialDirection` TEXT NOT NULL, `sightHeightMetres` REAL NOT NULL,
                `zeroDistanceMetres` REAL NOT NULL, `zeroElevationOffsetRadians` REAL NOT NULL,
                `zeroWindageOffsetRadians` REAL NOT NULL,
                `manufacturerSpecificationSourceNote` TEXT NOT NULL,
                `verificationStatus` TEXT NOT NULL, `verificationDateEpochMillis` INTEGER,
                `bdcCalibrationVerified` INTEGER NOT NULL, `createdAtEpochMillis` INTEGER NOT NULL,
                `modifiedAtEpochMillis` INTEGER NOT NULL, `revision` INTEGER NOT NULL,
                `archived` INTEGER NOT NULL, `favourite` INTEGER NOT NULL, `notes` TEXT,
                PRIMARY KEY(`id`),
                FOREIGN KEY(`familyId`) REFERENCES `scope_families`(`id`)
                    ON UPDATE NO ACTION ON DELETE RESTRICT,
                FOREIGN KEY(`variantId`) REFERENCES `scope_variants`(`id`)
                    ON UPDATE NO ACTION ON DELETE RESTRICT
            )
            """.trimIndent(),
            """
            CREATE TABLE IF NOT EXISTS `scope_verifications` (
                `id` TEXT NOT NULL, `scopeProfileId` TEXT NOT NULL,
                `physicalModelConfirmed` INTEGER NOT NULL, `turretUnitConfirmed` INTEGER NOT NULL,
                `clickValueConfirmed` INTEGER NOT NULL, `reticleConfirmed` INTEGER NOT NULL,
                `focalPlaneConfirmed` INTEGER NOT NULL,
                `elevationDialDirectionConfirmed` INTEGER NOT NULL,
                `windageDialDirectionConfirmed` INTEGER NOT NULL,
                `zeroStopConfirmed` INTEGER NOT NULL, `sightHeightConfirmed` INTEGER NOT NULL,
                `zeroDistanceConfirmed` INTEGER NOT NULL, `verifiedAtEpochMillis` INTEGER,
                `notes` TEXT, PRIMARY KEY(`id`),
                FOREIGN KEY(`scopeProfileId`) REFERENCES `scope_profiles`(`id`)
                    ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent(),
            """
            CREATE TABLE IF NOT EXISTS `reference_atmospheres` (
                `id` TEXT NOT NULL, `name` TEXT NOT NULL, `temperatureKelvin` REAL NOT NULL,
                `temperatureSource` TEXT NOT NULL, `stationPressurePascals` REAL NOT NULL,
                `pressureSource` TEXT NOT NULL, `relativeHumidityFraction` REAL,
                `dewPointKelvin` REAL, `humiditySource` TEXT NOT NULL, `altitudeMetres` REAL NOT NULL,
                `altitudeSource` TEXT NOT NULL, `capturedAtEpochMillis` INTEGER NOT NULL,
                `latitudeDegrees` REAL, `longitudeDegrees` REAL,
                `locationIncludedInExports` INTEGER NOT NULL, `densityAltitudeMetres` REAL,
                `createdAtEpochMillis` INTEGER NOT NULL, `modifiedAtEpochMillis` INTEGER NOT NULL,
                `revision` INTEGER NOT NULL, `archived` INTEGER NOT NULL,
                `favourite` INTEGER NOT NULL, `notes` TEXT, PRIMARY KEY(`id`)
            )
            """.trimIndent(),
            """
            CREATE TABLE IF NOT EXISTS `saved_ranges` (
                `id` TEXT NOT NULL, `rangeName` TEXT NOT NULL, `latitudeDegrees` REAL,
                `longitudeDegrees` REAL, `surveyedAltitudeMetres` REAL,
                `commonDistancesMetresJson` TEXT NOT NULL, `magneticDeclinationRadians` REAL,
                `locationStoragePreference` TEXT NOT NULL, `createdAtEpochMillis` INTEGER NOT NULL,
                `modifiedAtEpochMillis` INTEGER NOT NULL, `revision` INTEGER NOT NULL,
                `archived` INTEGER NOT NULL, `favourite` INTEGER NOT NULL, `notes` TEXT,
                PRIMARY KEY(`id`)
            )
            """.trimIndent(),
            """
            CREATE TABLE IF NOT EXISTS `static_targets` (
                `id` TEXT NOT NULL, `savedRangeId` TEXT, `name` TEXT NOT NULL,
                `targetClass` TEXT NOT NULL, `physicalWidthMetres` REAL NOT NULL,
                `physicalHeightMetres` REAL NOT NULL, `measuredDistanceMetres` REAL,
                `distanceSource` TEXT, `distanceQuality` TEXT, `distanceMeasuredAtEpochMillis` INTEGER,
                `distanceUncertaintyMetres` REAL, `distanceConfirmed` INTEGER NOT NULL,
                `includeDistanceInDope` INTEGER NOT NULL, `centreLatitudeDegrees` REAL,
                `centreLongitudeDegrees` REAL, `scoringZonesJson` TEXT NOT NULL,
                `calibrationMarkerPositionsJson` TEXT, `referenceImageUri` TEXT,
                `expectedImpactDiameterMinimumMetres` REAL,
                `expectedImpactDiameterMaximumMetres` REAL, `createdAtEpochMillis` INTEGER NOT NULL,
                `modifiedAtEpochMillis` INTEGER NOT NULL, `revision` INTEGER NOT NULL,
                `archived` INTEGER NOT NULL, `favourite` INTEGER NOT NULL, `notes` TEXT,
                PRIMARY KEY(`id`),
                FOREIGN KEY(`savedRangeId`) REFERENCES `saved_ranges`(`id`)
                    ON UPDATE NO ACTION ON DELETE SET NULL
            )
            """.trimIndent(),
            """
            CREATE TABLE IF NOT EXISTS `zero_profiles` (
                `id` TEXT NOT NULL, `rifleId` TEXT NOT NULL, `ammunitionId` TEXT NOT NULL,
                `scopeProfileId` TEXT NOT NULL, `zeroDistanceMetres` REAL NOT NULL,
                `sightHeightAboveBoreMetres` REAL NOT NULL,
                `zeroElevationOffsetRadians` REAL NOT NULL, `zeroWindageOffsetRadians` REAL NOT NULL,
                `zeroConfirmationDateEpochMillis` INTEGER NOT NULL,
                `referenceAtmosphereId` TEXT NOT NULL, `verified` INTEGER NOT NULL,
                `dependencyFingerprint` TEXT NOT NULL, `createdAtEpochMillis` INTEGER NOT NULL,
                `modifiedAtEpochMillis` INTEGER NOT NULL, `revision` INTEGER NOT NULL,
                `archived` INTEGER NOT NULL, `favourite` INTEGER NOT NULL, `notes` TEXT,
                PRIMARY KEY(`id`),
                FOREIGN KEY(`rifleId`) REFERENCES `rifles`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION,
                FOREIGN KEY(`ammunitionId`) REFERENCES `ammunition`(`id`)
                    ON UPDATE NO ACTION ON DELETE NO ACTION,
                FOREIGN KEY(`scopeProfileId`) REFERENCES `scope_profiles`(`id`)
                    ON UPDATE NO ACTION ON DELETE NO ACTION,
                FOREIGN KEY(`referenceAtmosphereId`) REFERENCES `reference_atmospheres`(`id`)
                    ON UPDATE NO ACTION ON DELETE NO ACTION
            )
            """.trimIndent(),
            "CREATE INDEX IF NOT EXISTS `index_ammunition_rifleId` ON `ammunition` (`rifleId`)",
            """
            CREATE INDEX IF NOT EXISTS `index_chronograph_strings_ammunitionId`
            ON `chronograph_strings` (`ammunitionId`)
            """.trimIndent(),
            "CREATE INDEX IF NOT EXISTS `index_scope_variants_familyId` ON `scope_variants` (`familyId`)",
            "CREATE INDEX IF NOT EXISTS `index_scope_profiles_familyId` ON `scope_profiles` (`familyId`)",
            "CREATE INDEX IF NOT EXISTS `index_scope_profiles_variantId` ON `scope_profiles` (`variantId`)",
            """
            CREATE INDEX IF NOT EXISTS `index_scope_verifications_scopeProfileId`
            ON `scope_verifications` (`scopeProfileId`)
            """.trimIndent(),
            """
            CREATE INDEX IF NOT EXISTS `index_static_targets_savedRangeId`
            ON `static_targets` (`savedRangeId`)
            """.trimIndent(),
            "CREATE INDEX IF NOT EXISTS `index_zero_profiles_rifleId` ON `zero_profiles` (`rifleId`)",
            """
            CREATE INDEX IF NOT EXISTS `index_zero_profiles_ammunitionId`
            ON `zero_profiles` (`ammunitionId`)
            """.trimIndent(),
            """
            CREATE INDEX IF NOT EXISTS `index_zero_profiles_scopeProfileId`
            ON `zero_profiles` (`scopeProfileId`)
            """.trimIndent(),
            """
            CREATE INDEX IF NOT EXISTS `index_zero_profiles_referenceAtmosphereId`
            ON `zero_profiles` (`referenceAtmosphereId`)
            """.trimIndent(),
        )
}
