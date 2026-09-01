package za.co.dope.ballistics.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import za.co.dope.ballistics.data.db.RifleEntity
import za.co.dope.ballistics.data.db.StaticTargetEntity
import za.co.dope.ballistics.domain.StaticTargetClass

class ProfileExportTest {
    @Test
    fun `schema v1 round trip retains SI values and metadata`() {
        val export =
            ProfileExportV1(
                metadata =
                    ExportMetadataV1(
                        appVersion = "0.2.0-m2",
                        createdAtEpochMillis = 1234L,
                        preciseLocationIncluded = false,
                    ),
                rifles =
                    listOf(
                        RifleEntity(
                            id = "rifle-1",
                            profileName = "Field rifle",
                            manufacturer = "Example",
                            model = "Model",
                            calibreLabel = ".308 Winchester",
                            barrelLengthMetres = 0.508,
                            twistRateMetres = 0.254,
                            twistDirection = "RIGHT",
                            defaultZeroDistanceMetres = 100.0,
                            sightHeightAboveBoreMetres = 0.05,
                            createdAtEpochMillis = 1L,
                            modifiedAtEpochMillis = 1L,
                        ),
                    ),
            )

        val decoded = ProfileExportCodec.decode(ProfileExportCodec.encode(export))

        assertEquals(1, decoded.metadata.schemaVersion)
        assertEquals("SI", decoded.metadata.internalUnits)
        assertEquals(0.508, decoded.rifles.single().barrelLengthMetres, 0.0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `unconfirmed measured distance cannot populate DOPE`() {
        ProfileExportCodec.validate(
            ProfileExportV1(
                metadata =
                    ExportMetadataV1(
                        appVersion = "test",
                        createdAtEpochMillis = 1L,
                        preciseLocationIncluded = false,
                    ),
                staticTargets =
                    listOf(
                        StaticTargetEntity(
                            id = "target-1",
                            name = "Gong 1",
                            targetClass = StaticTargetClass.PAINTED_STEEL.name,
                            physicalWidthMetres = 0.2,
                            physicalHeightMetres = 0.2,
                            measuredDistanceMetres = 150.0,
                            distanceConfirmed = false,
                            includeDistanceInDope = true,
                            createdAtEpochMillis = 1L,
                            modifiedAtEpochMillis = 1L,
                        ),
                    ),
            ),
        )
    }

    @Test
    fun `duplicate import remaps identifiers without overwriting`() {
        val rifle = sampleRifle()
        val export =
            ProfileExportV1(
                metadata =
                    ExportMetadataV1(
                        appVersion = "test",
                        createdAtEpochMillis = 1L,
                        preciseLocationIncluded = false,
                    ),
                rifles = listOf(rifle),
            )
        val existing = emptyExistingIds().copy(rifles = setOf(rifle.id))

        val planned = ProfileImportPlanner.plan(export, existing, DuplicateImportPolicy.DUPLICATE)

        assertNotEquals(rifle.id, planned.rifles.single().id)
    }

    private fun sampleRifle() =
        RifleEntity(
            id = "rifle-1",
            profileName = "Field rifle",
            manufacturer = "Example",
            model = "Model",
            calibreLabel = ".308 Winchester",
            barrelLengthMetres = 0.508,
            twistRateMetres = 0.254,
            twistDirection = "RIGHT",
            defaultZeroDistanceMetres = 100.0,
            sightHeightAboveBoreMetres = 0.05,
            createdAtEpochMillis = 1L,
            modifiedAtEpochMillis = 1L,
        )

    private fun emptyExistingIds() =
        ExistingProfileIds(
            rifles = emptySet(),
            ammunition = emptySet(),
            chronographStrings = emptySet(),
            scopeProfiles = emptySet(),
            scopeVerifications = emptySet(),
            referenceAtmospheres = emptySet(),
            savedRanges = emptySet(),
            staticTargets = emptySet(),
            zeroProfiles = emptySet(),
        )
}
