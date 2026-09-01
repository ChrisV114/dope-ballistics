package za.co.dope.ballistics.data.db

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import za.co.dope.ballistics.domain.DialDirection
import za.co.dope.ballistics.domain.TwistDirection
import za.co.dope.ballistics.domain.VerificationStatus

/** Owner-requested editable profiles for private testing. Stable IDs keep seeding idempotent. */
object StarterProfiles {
    const val HOWA_RIFLE_ID = "starter-howa-65-creedmoor"
    const val HOWA_AMMUNITION_ID = "starter-lapua-139-scenar"
    const val HOWA_SCOPE_ID = "starter-dnt-theone-mil"
    const val M_AND_P_RIFLE_ID = "starter-sw-mp15-sport-iii"
    const val M_AND_P_AMMUNITION_ID = "starter-hornady-53-vmax"
    const val M_AND_P_SCOPE_ID = "starter-arken-ep8-klbox"

    fun rifles(nowEpochMillis: Long): List<RifleEntity> =
        listOf(
            RifleEntity(
                id = HOWA_RIFLE_ID,
                profileName = "Howa 6.5 Creedmoor — test",
                manufacturer = "Howa",
                model = "26-inch 6.5 Creedmoor (confirm model)",
                calibreLabel = "6.5 Creedmoor",
                barrelLengthMetres = 0.6604,
                twistRateMetres = 0.2032,
                twistDirection = TwistDirection.RIGHT.name,
                defaultZeroDistanceMetres = 100.0,
                sightHeightAboveBoreMetres = 0.06,
                internalReference = "OWNER_TEST_PROFILE",
                createdAtEpochMillis = nowEpochMillis,
                modifiedAtEpochMillis = nowEpochMillis,
                favourite = true,
                notes = "Owner-provided test data. Confirm the exact Howa model and all physical measurements.",
            ),
            RifleEntity(
                id = M_AND_P_RIFLE_ID,
                profileName = "S&W M&P15 Sport III .223 — test",
                manufacturer = "Smith & Wesson",
                model = "M&P15 Sport III",
                calibreLabel = ".223 Remington",
                barrelLengthMetres = 0.4064,
                twistRateMetres = 0.2032,
                twistDirection = TwistDirection.RIGHT.name,
                defaultZeroDistanceMetres = 50.0,
                sightHeightAboveBoreMetres = 0.06,
                internalReference = "OWNER_TEST_PROFILE",
                createdAtEpochMillis = nowEpochMillis,
                modifiedAtEpochMillis = nowEpochMillis,
                favourite = true,
                notes = "Owner-provided test data. Confirm the chamber marking and physical measurements.",
            ),
        )

    fun ammunition(nowEpochMillis: Long): List<AmmunitionEntity> =
        listOf(
            AmmunitionEntity(
                id = HOWA_AMMUNITION_ID,
                rifleId = HOWA_RIFLE_ID,
                profileName = "Lapua 139 gr Scenar — 809 m/s test",
                manufacturer = "Owner-entered load",
                productLoadName = "Lapua 139 gr Scenar test load",
                bulletManufacturer = "Lapua",
                bulletName = "139 gr Scenar GB458",
                bulletWeightKilograms = 139.0 * 0.00006479891,
                bulletDiameterMetres = 0.0067056,
                g7BallisticCoefficient = 0.290,
                selectedDragModel = "G7",
                muzzleVelocityMetresPerSecond = 809.0,
                createdAtEpochMillis = nowEpochMillis,
                modifiedAtEpochMillis = nowEpochMillis,
                favourite = true,
                notes =
                    "Testing value from the reviewed regression fixture. Confirm the exact bullet SKU, " +
                        "published G7 BC and chronograph average before field use.",
            ),
            AmmunitionEntity(
                id = M_AND_P_AMMUNITION_ID,
                rifleId = M_AND_P_RIFLE_ID,
                profileName = "Hornady 53 gr V-MAX — 920 m/s test",
                manufacturer = "Owner-entered load",
                productLoadName = "Hornady 53 gr V-MAX test load",
                bulletManufacturer = "Hornady",
                bulletName = "53 gr V-MAX",
                bulletWeightKilograms = 53.0 * 0.00006479891,
                bulletDiameterMetres = 0.0056896,
                g1BallisticCoefficient = 0.290,
                selectedDragModel = "G1",
                muzzleVelocityMetresPerSecond = 920.0,
                createdAtEpochMillis = nowEpochMillis,
                modifiedAtEpochMillis = nowEpochMillis,
                favourite = true,
                notes =
                    "Testing value from the reviewed regression fixture. Confirm the exact bullet SKU, " +
                        "published G1 BC and chronograph average before field use.",
            ),
        )

    fun scopes(nowEpochMillis: Long): List<ScopeProfileEntity> {
        val dntFamily = ScopeTemplates.families.single { it.id == ScopeTemplates.DNT_FAMILY_ID }
        val dntVariant = ScopeTemplates.variants.single { it.id == ScopeTemplates.DNT_MIL_VARIANT_ID }
        val arkenFamily = ScopeTemplates.families.single { it.id == ScopeTemplates.ARKEN_FAMILY_ID }
        val arkenVariant = ScopeTemplates.variants.single { it.id == ScopeTemplates.ARKEN_BDC_VARIANT_ID }
        return listOf(
            ScopeTemplates.createUserProfile(dntFamily, dntVariant, "DNT TheOne MIL — test", nowEpochMillis).copy(
                id = HOWA_SCOPE_ID,
                reticleName = "The One Reticle",
                elevationDialDirection = DialDirection.COUNTERCLOCKWISE_UP.name,
                windageDialDirection = DialDirection.COUNTERCLOCKWISE_UP.name,
                sightHeightMetres = 0.06,
                zeroDistanceMetres = 100.0,
                verificationStatus = VerificationStatus.REQUIRES_USER_VERIFICATION.name,
                favourite = true,
                notes = "Mounted on the Howa. Owner-provided starting values; complete physical verification.",
            ),
            ScopeTemplates
                .createUserProfile(arkenFamily, arkenVariant, "Arken EP-8 MOA / KLBOX — test", nowEpochMillis)
                .copy(
                    id = M_AND_P_SCOPE_ID,
                    elevationDialDirection = DialDirection.COUNTERCLOCKWISE_UP.name,
                    windageDialDirection = DialDirection.COUNTERCLOCKWISE_UP.name,
                    sightHeightMetres = 0.06,
                    zeroDistanceMetres = 50.0,
                    zeroStopAvailable = false,
                    verificationStatus = VerificationStatus.REQUIRES_USER_VERIFICATION.name,
                    bdcCalibrationVerified = false,
                    favourite = true,
                    notes =
                        "Mounted on the M&P15 Sport III. KLBOX is BDC and remains blocked from generic " +
                            "angular holds until physically calibrated and verified.",
                ),
        )
    }

    fun insert(
        database: SupportSQLiteDatabase,
        nowEpochMillis: Long = System.currentTimeMillis(),
    ) {
        ScopeTemplates.insertBuiltIns(database)
        val includeSetupDefaults = database.hasColumn("rifles", "defaultZeroDistanceMetres")
        val includeProfileMedia = database.hasColumn("rifles", "imageUri")
        rifles(nowEpochMillis).forEach {
            database.insert(
                "rifles",
                SQLiteDatabase.CONFLICT_IGNORE,
                it.toValues(includeSetupDefaults, includeProfileMedia),
            )
        }
        val includeLoadDetails = database.hasColumn("ammunition", "cartridgeOverallLengthMetres")
        ammunition(nowEpochMillis).forEach {
            database.insert("ammunition", SQLiteDatabase.CONFLICT_IGNORE, it.toValues(includeLoadDetails))
        }
        val includeScopeMedia = database.hasColumn("scope_profiles", "imageUri")
        scopes(nowEpochMillis).forEach {
            database.insert("scope_profiles", SQLiteDatabase.CONFLICT_IGNORE, it.toValues(includeScopeMedia))
        }
    }

    private fun RifleEntity.toValues(
        includeSetupDefaults: Boolean,
        includeProfileMedia: Boolean,
    ) = ContentValues().apply {
        put("id", id)
        put("profileName", profileName)
        put("manufacturer", manufacturer)
        put("model", model)
        put("calibreLabel", calibreLabel)
        put("barrelLengthMetres", barrelLengthMetres)
        put("twistRateMetres", twistRateMetres)
        put("twistDirection", twistDirection)
        if (includeSetupDefaults) {
            put("defaultZeroDistanceMetres", defaultZeroDistanceMetres)
            put("sightHeightAboveBoreMetres", sightHeightAboveBoreMetres)
        }
        if (includeProfileMedia) put("imageUri", imageUri)
        put("internalReference", internalReference)
        put("serialNumber", serialNumber)
        put("createdAtEpochMillis", createdAtEpochMillis)
        put("modifiedAtEpochMillis", modifiedAtEpochMillis)
        put("revision", revision)
        put("archived", archived)
        put("favourite", favourite)
        put("notes", notes)
    }

    private fun SupportSQLiteDatabase.hasColumn(
        table: String,
        column: String,
    ): Boolean =
        query("PRAGMA table_info(`$table`)").use { cursor ->
            val nameIndex = cursor.getColumnIndex("name")
            generateSequence { if (cursor.moveToNext()) cursor.getString(nameIndex) else null }.any { it == column }
        }

    private fun AmmunitionEntity.toValues(includeLoadDetails: Boolean) =
        ContentValues().apply {
            put("id", id)
            put("rifleId", rifleId)
            put("profileName", profileName)
            put("manufacturer", manufacturer)
            put("productLoadName", productLoadName)
            put("bulletManufacturer", bulletManufacturer)
            put("bulletName", bulletName)
            put("bulletWeightKilograms", bulletWeightKilograms)
            put("bulletDiameterMetres", bulletDiameterMetres)
            put("g1BallisticCoefficient", g1BallisticCoefficient)
            put("g7BallisticCoefficient", g7BallisticCoefficient)
            put("selectedDragModel", selectedDragModel)
            put("muzzleVelocityMetresPerSecond", muzzleVelocityMetresPerSecond)
            put("muzzleVelocityStandardDeviation", muzzleVelocityStandardDeviation)
            put("velocityTemperatureCoefficient", velocityTemperatureCoefficient)
            put("ammunitionReferenceTemperatureKelvin", ammunitionReferenceTemperatureKelvin)
            put("lotNumber", lotNumber)
            put("chronographDateEpochMillis", chronographDateEpochMillis)
            put("chronographType", chronographType)
            put("selectedChronographStringId", selectedChronographStringId)
            if (includeLoadDetails) {
                put("cartridgeOverallLengthMetres", cartridgeOverallLengthMetres)
                put("imageUri", imageUri)
            }
            put("createdAtEpochMillis", createdAtEpochMillis)
            put("modifiedAtEpochMillis", modifiedAtEpochMillis)
            put("revision", revision)
            put("archived", archived)
            put("favourite", favourite)
            put("notes", notes)
        }

    private fun ScopeProfileEntity.toValues(includeProfileMedia: Boolean) =
        ContentValues().apply {
            put("id", id)
            put("familyId", familyId)
            put("variantId", variantId)
            put("profileName", profileName)
            put("manufacturer", manufacturer)
            put("model", model)
            put("minimumMagnification", minimumMagnification)
            put("maximumMagnification", maximumMagnification)
            put("objectiveDiameterMetres", objectiveDiameterMetres)
            put("tubeDiameterMetres", tubeDiameterMetres)
            put("focalPlane", focalPlane)
            put("turretUnit", turretUnit)
            put("elevationClickValueRadians", elevationClickValueRadians)
            put("windageClickValueRadians", windageClickValueRadians)
            put("reticleSystem", reticleSystem)
            put("reticleName", reticleName)
            put("reticleSubtensionMetadata", reticleSubtensionMetadata)
            put("bdcMetadata", bdcMetadata)
            put("elevationTravelRadians", elevationTravelRadians)
            put("windageTravelRadians", windageTravelRadians)
            put("zeroStopAvailable", zeroStopAvailable)
            put("parallaxAdjustment", parallaxAdjustment)
            put("illumination", illumination)
            put("turretCapType", turretCapType)
            put("elevationDialDirection", elevationDialDirection)
            put("windageDialDirection", windageDialDirection)
            put("sightHeightMetres", sightHeightMetres)
            put("zeroDistanceMetres", zeroDistanceMetres)
            put("zeroElevationOffsetRadians", zeroElevationOffsetRadians)
            put("zeroWindageOffsetRadians", zeroWindageOffsetRadians)
            if (includeProfileMedia) put("imageUri", imageUri)
            put("manufacturerSpecificationSourceNote", manufacturerSpecificationSourceNote)
            put("verificationStatus", verificationStatus)
            put("verificationDateEpochMillis", verificationDateEpochMillis)
            put("bdcCalibrationVerified", bdcCalibrationVerified)
            put("createdAtEpochMillis", createdAtEpochMillis)
            put("modifiedAtEpochMillis", modifiedAtEpochMillis)
            put("revision", revision)
            put("archived", archived)
            put("favourite", favourite)
            put("notes", notes)
        }
}
