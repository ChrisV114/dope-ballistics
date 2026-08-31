package za.co.dope.ballistics.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/** Append-only completed-session snapshot. No DAO update or delete operation is exposed. */
@Serializable
@Entity(
    tableName = "session_snapshots",
    indices = [Index("completedAtEpochMillis"), Index("rifleId"), Index("savedRangeId")],
)
data class SessionSnapshotEntity(
    @PrimaryKey val id: String,
    val schemaVersion: Int = 1,
    val sessionName: String,
    val startedAtEpochMillis: Long,
    val completedAtEpochMillis: Long,
    val savedRangeId: String? = null,
    val savedRangeName: String? = null,
    val preciseLocationIncluded: Boolean = false,
    val locationSnapshotJson: String? = null,
    val rifleId: String,
    val rifleRevision: Long,
    val ammunitionId: String,
    val ammunitionRevision: Long,
    val scopeProfileId: String,
    val scopeProfileRevision: Long,
    val zeroProfileId: String,
    val zeroProfileRevision: Long,
    val profileSnapshotJson: String,
    val referenceEnvironmentJson: String,
    val currentEnvironmentJson: String,
    val fieldSourcesJson: String,
    val distanceMetres: Double,
    val distanceSource: String,
    val distanceUncertaintyMetres: Double,
    val directionOfFireTrueDegrees: Double? = null,
    val inclinationDegrees: Double,
    val windSnapshotJson: String,
    val calculationResultJson: String,
    val calculationTraceJson: String,
    val engineVersion: String,
    val scopeRoundingJson: String,
    val warningsJson: String,
    val notes: String? = null,
    val photoUrisJson: String = "[]",
    val trainingVideoUrisJson: String = "[]",
    val rangeAnalystStringsJson: String = "[]",
    val contentSha256: String,
)

/**
 * Append-only field observation. Corrections create a new record linked through supersedesRecordId
 * so calculated and observed values are never silently rewritten.
 */
@Serializable
@Entity(
    tableName = "verified_dope_records",
    indices = [Index("createdAtEpochMillis"), Index("rifleId"), Index("sessionSnapshotId")],
)
data class VerifiedDopeRecordEntity(
    @PrimaryKey val id: String,
    val schemaVersion: Int = 1,
    val sessionSnapshotId: String? = null,
    val supersedesRecordId: String? = null,
    val createdAtEpochMillis: Long,
    val rifleId: String,
    val rifleRevision: Long,
    val ammunitionId: String,
    val ammunitionRevision: Long,
    val scopeProfileId: String,
    val scopeProfileRevision: Long,
    val zeroProfileId: String,
    val zeroProfileRevision: Long,
    val profileSnapshotJson: String,
    val distanceMetres: Double,
    val distanceSource: String,
    val distanceUncertaintyMetres: Double,
    val calculatedUnit: String,
    val calculatedRawValue: Double,
    val calculatedDialValue: Double,
    val calculatedClicks: Int,
    val actualDialUnit: String,
    val actualDialValue: Double,
    val actualDialClicks: Int? = null,
    val observedGroupCentreVerticalMetres: Double? = null,
    val observedGroupCentreHorizontalMetres: Double? = null,
    val groupSizeMetres: Double? = null,
    val numberOfShots: Int,
    val conditionsJson: String,
    val confidence: String,
    val status: String,
    val engineVersion: String,
    val notes: String? = null,
    val evidenceSha256: String,
)
