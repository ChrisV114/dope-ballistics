package za.co.dope.ballistics.data

import za.co.dope.ballistics.domain.ProfileIdentity

data class ExistingProfileIds(
    val rifles: Set<String>,
    val ammunition: Set<String>,
    val chronographStrings: Set<String>,
    val scopeProfiles: Set<String>,
    val scopeVerifications: Set<String>,
    val referenceAtmospheres: Set<String>,
    val savedRanges: Set<String>,
    val staticTargets: Set<String>,
    val zeroProfiles: Set<String>,
) {
    fun conflictsWith(export: ProfileExportV1): Boolean =
        export.rifles.any { it.id in rifles } ||
            export.ammunition.any { it.id in ammunition } ||
            export.chronographStrings.any { it.id in chronographStrings } ||
            export.scopeProfiles.any { it.id in scopeProfiles } ||
            export.scopeVerifications.any { it.id in scopeVerifications } ||
            export.referenceAtmospheres.any { it.id in referenceAtmospheres } ||
            export.savedRanges.any { it.id in savedRanges } ||
            export.staticTargets.any { it.id in staticTargets } ||
            export.zeroProfiles.any { it.id in zeroProfiles }
}

object ProfileImportPlanner {
    fun plan(
        export: ProfileExportV1,
        existing: ExistingProfileIds,
        policy: DuplicateImportPolicy,
    ): ProfileExportV1 =
        when (policy) {
            DuplicateImportPolicy.CANCEL -> {
                require(!existing.conflictsWith(export)) { "Import cancelled because duplicate IDs exist" }
                export
            }

            DuplicateImportPolicy.MERGE_SAFE -> {
                safeMerge(export, existing)
            }

            DuplicateImportPolicy.REPLACE_CONFIRMED -> {
                export
            }

            DuplicateImportPolicy.DUPLICATE -> {
                duplicate(export)
            }
        }

    private fun safeMerge(
        export: ProfileExportV1,
        existing: ExistingProfileIds,
    ): ProfileExportV1 =
        export.copy(
            rifles = export.rifles.filterNot { it.id in existing.rifles },
            ammunition = export.ammunition.filterNot { it.id in existing.ammunition },
            chronographStrings = export.chronographStrings.filterNot { it.id in existing.chronographStrings },
            scopeProfiles = export.scopeProfiles.filterNot { it.id in existing.scopeProfiles },
            scopeVerifications = export.scopeVerifications.filterNot { it.id in existing.scopeVerifications },
            referenceAtmospheres =
                export.referenceAtmospheres.filterNot { it.id in existing.referenceAtmospheres },
            savedRanges = export.savedRanges.filterNot { it.id in existing.savedRanges },
            staticTargets = export.staticTargets.filterNot { it.id in existing.staticTargets },
            zeroProfiles = export.zeroProfiles.filterNot { it.id in existing.zeroProfiles },
        )

    private fun duplicate(export: ProfileExportV1): ProfileExportV1 {
        val rifleIds = export.rifles.associate { it.id to ProfileIdentity.newId() }
        val ammunitionIds = export.ammunition.associate { it.id to ProfileIdentity.newId() }
        val chronographIds = export.chronographStrings.associate { it.id to ProfileIdentity.newId() }
        val scopeIds = export.scopeProfiles.associate { it.id to ProfileIdentity.newId() }
        val verificationIds = export.scopeVerifications.associate { it.id to ProfileIdentity.newId() }
        val atmosphereIds = export.referenceAtmospheres.associate { it.id to ProfileIdentity.newId() }
        val rangeIds = export.savedRanges.associate { it.id to ProfileIdentity.newId() }
        val targetIds = export.staticTargets.associate { it.id to ProfileIdentity.newId() }
        val zeroIds = export.zeroProfiles.associate { it.id to ProfileIdentity.newId() }
        return export.copy(
            rifles = export.rifles.map { it.copy(id = rifleIds.getValue(it.id)) },
            ammunition =
                export.ammunition.map {
                    it.copy(id = ammunitionIds.getValue(it.id), rifleId = rifleIds[it.rifleId] ?: it.rifleId)
                },
            chronographStrings =
                export.chronographStrings.map {
                    it.copy(
                        id = chronographIds.getValue(it.id),
                        ammunitionId = ammunitionIds[it.ammunitionId] ?: it.ammunitionId,
                    )
                },
            scopeProfiles = export.scopeProfiles.map { it.copy(id = scopeIds.getValue(it.id)) },
            scopeVerifications =
                export.scopeVerifications.map {
                    it.copy(
                        id = verificationIds.getValue(it.id),
                        scopeProfileId = scopeIds[it.scopeProfileId] ?: it.scopeProfileId,
                    )
                },
            referenceAtmospheres =
                export.referenceAtmospheres.map { it.copy(id = atmosphereIds.getValue(it.id)) },
            savedRanges = export.savedRanges.map { it.copy(id = rangeIds.getValue(it.id)) },
            staticTargets =
                export.staticTargets.map {
                    it.copy(
                        id = targetIds.getValue(it.id),
                        savedRangeId = it.savedRangeId?.let { rangeId -> rangeIds[rangeId] ?: rangeId },
                    )
                },
            zeroProfiles =
                export.zeroProfiles.map {
                    it.copy(
                        id = zeroIds.getValue(it.id),
                        rifleId = rifleIds[it.rifleId] ?: it.rifleId,
                        ammunitionId = ammunitionIds[it.ammunitionId] ?: it.ammunitionId,
                        scopeProfileId = scopeIds[it.scopeProfileId] ?: it.scopeProfileId,
                        referenceAtmosphereId =
                            atmosphereIds[it.referenceAtmosphereId] ?: it.referenceAtmosphereId,
                    )
                },
        )
    }
}
