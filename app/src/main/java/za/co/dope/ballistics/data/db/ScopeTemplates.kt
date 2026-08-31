package za.co.dope.ballistics.data.db

import android.content.ContentValues
import androidx.sqlite.db.SupportSQLiteDatabase
import za.co.dope.ballistics.domain.AngularUnit
import za.co.dope.ballistics.domain.DialDirection
import za.co.dope.ballistics.domain.FocalPlane
import za.co.dope.ballistics.domain.ProfileIdentity
import za.co.dope.ballistics.domain.ReticleMeasurementSystem
import za.co.dope.ballistics.domain.VerificationStatus
import kotlin.math.PI

object ScopeTemplates {
    const val DNT_FAMILY_ID = "builtin-dnt-theone-7-35x56"
    const val DNT_MIL_VARIANT_ID = "builtin-dnt-theone-tor-mil"
    const val DNT_MOA_VARIANT_ID = "builtin-dnt-theone-tor-moa"
    const val ARKEN_FAMILY_ID = "builtin-arken-ep8-1-8x28"
    const val ARKEN_MIL_VARIANT_ID = "builtin-arken-ep8-klgrid"
    const val ARKEN_BDC_VARIANT_ID = "builtin-arken-ep8-klbox"

    private fun moaToRadians(moa: Double): Double = moa * PI / 180.0 / 60.0

    val families =
        listOf(
            ScopeFamilyEntity(
                id = DNT_FAMILY_ID,
                manufacturer = "DNT Optics",
                model = "TheOne 7–35×56",
                displayName = "DNT Optics TheOne 7–35×56 FFP",
                minimumMagnification = 7.0,
                maximumMagnification = 35.0,
                objectiveDiameterMetres = 0.056,
                tubeDiameterMetres = 0.034,
                focalPlane = FocalPlane.FIRST.name,
                illuminated = true,
                zeroStopAvailable = true,
                parallaxAdjustment = "Adjustable",
                turretCapType = "Exposed",
                manufacturerSpecificationSourceNote =
                    "Built-in editable starting template; regional and product revisions " +
                        "require physical confirmation.",
            ),
            ScopeFamilyEntity(
                id = ARKEN_FAMILY_ID,
                manufacturer = "Arken Optics",
                model = "EP-8 1–8×28",
                displayName = "Arken Optics EP-8 1–8×28 FFP",
                minimumMagnification = 1.0,
                maximumMagnification = 8.0,
                objectiveDiameterMetres = 0.028,
                tubeDiameterMetres = 0.034,
                focalPlane = FocalPlane.FIRST.name,
                illuminated = true,
                zeroStopAvailable = false,
                parallaxAdjustment = "Manufacturer specification requires confirmation",
                turretCapType = "Capped",
                manufacturerSpecificationSourceNote =
                    "Built-in editable starting template; physical variant and markings remain authoritative.",
            ),
        )

    val variants =
        listOf(
            ScopeVariantEntity(
                id = DNT_MIL_VARIANT_ID,
                familyId = DNT_FAMILY_ID,
                displayName = "DNT TheOne — MIL / TOR-MIL",
                turretUnit = AngularUnit.MIL.name,
                nominalElevationClickRadians = 0.0001,
                nominalWindageClickRadians = 0.0001,
                reticleSystem = ReticleMeasurementSystem.MIL.name,
                reticleName = "TOR-MIL",
                reticleSubtensionMetadata = "Fine reticle reference: 0.2 MIL",
                verificationStatus = VerificationStatus.REQUIRES_USER_VERIFICATION.name,
            ),
            ScopeVariantEntity(
                id = DNT_MOA_VARIANT_ID,
                familyId = DNT_FAMILY_ID,
                displayName = "DNT TheOne — MOA / TOR-MOA",
                turretUnit = AngularUnit.MOA.name,
                nominalElevationClickRadians = moaToRadians(0.25),
                nominalWindageClickRadians = moaToRadians(0.25),
                reticleSystem = ReticleMeasurementSystem.MOA.name,
                reticleName = "TOR-MOA",
                verificationStatus = VerificationStatus.REQUIRES_USER_VERIFICATION.name,
            ),
            ScopeVariantEntity(
                id = ARKEN_MIL_VARIANT_ID,
                familyId = ARKEN_FAMILY_ID,
                displayName = "Arken EP-8 — MIL / KLGRID",
                turretUnit = AngularUnit.MIL.name,
                nominalElevationClickRadians = 0.0001,
                nominalWindageClickRadians = 0.0001,
                reticleSystem = ReticleMeasurementSystem.MIL.name,
                reticleName = "KLGRID",
                reticleSubtensionMetadata = "Nominal adjustment range metadata: 30 MRAD",
                verificationStatus = VerificationStatus.REQUIRES_USER_VERIFICATION.name,
            ),
            ScopeVariantEntity(
                id = ARKEN_BDC_VARIANT_ID,
                familyId = ARKEN_FAMILY_ID,
                displayName = "Arken EP-8 — MOA / KLBOX",
                turretUnit = AngularUnit.MOA.name,
                nominalElevationClickRadians = moaToRadians(0.25),
                nominalWindageClickRadians = moaToRadians(0.25),
                reticleSystem = ReticleMeasurementSystem.BDC.name,
                reticleName = "KLBOX",
                bdcMetadata =
                    "BDC marks require an explicit verified calibration for cartridge, velocity, " +
                        "zero and reference environment.",
                nominalElevationTravelRadians = moaToRadians(110.0),
                verificationStatus = VerificationStatus.REQUIRES_USER_VERIFICATION.name,
            ),
        )

    fun createUserProfile(
        family: ScopeFamilyEntity,
        variant: ScopeVariantEntity,
        profileName: String,
        nowEpochMillis: Long,
    ): ScopeProfileEntity =
        ScopeProfileEntity(
            id = ProfileIdentity.newId(),
            familyId = family.id,
            variantId = variant.id,
            profileName = profileName,
            manufacturer = family.manufacturer,
            model = family.model,
            minimumMagnification = family.minimumMagnification,
            maximumMagnification = family.maximumMagnification,
            objectiveDiameterMetres = family.objectiveDiameterMetres,
            tubeDiameterMetres = family.tubeDiameterMetres,
            focalPlane = family.focalPlane,
            turretUnit = variant.turretUnit,
            elevationClickValueRadians = variant.nominalElevationClickRadians,
            windageClickValueRadians = variant.nominalWindageClickRadians,
            reticleSystem = variant.reticleSystem,
            reticleName = variant.reticleName,
            reticleSubtensionMetadata = variant.reticleSubtensionMetadata,
            bdcMetadata = variant.bdcMetadata,
            elevationTravelRadians = variant.nominalElevationTravelRadians,
            windageTravelRadians = variant.nominalWindageTravelRadians,
            zeroStopAvailable = family.zeroStopAvailable,
            parallaxAdjustment = family.parallaxAdjustment,
            illumination = family.illuminated,
            turretCapType = family.turretCapType,
            elevationDialDirection = DialDirection.UNKNOWN.name,
            windageDialDirection = DialDirection.UNKNOWN.name,
            sightHeightMetres = 0.0,
            zeroDistanceMetres = 0.0,
            manufacturerSpecificationSourceNote = family.manufacturerSpecificationSourceNote,
            verificationStatus = VerificationStatus.REQUIRES_USER_VERIFICATION.name,
            createdAtEpochMillis = nowEpochMillis,
            modifiedAtEpochMillis = nowEpochMillis,
        )

    fun insertBuiltIns(database: SupportSQLiteDatabase) {
        families.forEach { family ->
            database.insert("scope_families", 5, family.toValues())
        }
        variants.forEach { variant ->
            database.insert("scope_variants", 5, variant.toValues())
        }
    }

    private fun ScopeFamilyEntity.toValues() =
        ContentValues().apply {
            put("id", id)
            put("manufacturer", manufacturer)
            put("model", model)
            put("displayName", displayName)
            put("minimumMagnification", minimumMagnification)
            put("maximumMagnification", maximumMagnification)
            put("objectiveDiameterMetres", objectiveDiameterMetres)
            put("tubeDiameterMetres", tubeDiameterMetres)
            put("focalPlane", focalPlane)
            put("illuminated", illuminated)
            put("zeroStopAvailable", zeroStopAvailable)
            put("parallaxAdjustment", parallaxAdjustment)
            put("turretCapType", turretCapType)
            put("manufacturerSpecificationSourceNote", manufacturerSpecificationSourceNote)
            put("builtInImmutable", builtInImmutable)
        }

    private fun ScopeVariantEntity.toValues() =
        ContentValues().apply {
            put("id", id)
            put("familyId", familyId)
            put("displayName", displayName)
            put("turretUnit", turretUnit)
            put("nominalElevationClickRadians", nominalElevationClickRadians)
            put("nominalWindageClickRadians", nominalWindageClickRadians)
            put("reticleSystem", reticleSystem)
            put("reticleName", reticleName)
            put("reticleSubtensionMetadata", reticleSubtensionMetadata)
            put("bdcMetadata", bdcMetadata)
            put("nominalElevationTravelRadians", nominalElevationTravelRadians)
            put("nominalWindageTravelRadians", nominalWindageTravelRadians)
            put("verificationStatus", verificationStatus)
            put("builtInImmutable", builtInImmutable)
        }
}
