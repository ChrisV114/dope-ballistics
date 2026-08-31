package za.co.dope.ballistics.data

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import za.co.dope.ballistics.data.db.DopeDatabase
import za.co.dope.ballistics.data.db.SessionSnapshotEntity
import za.co.dope.ballistics.data.db.VerifiedDopeRecordEntity
import java.security.MessageDigest

enum class VerifiedDataStatus {
    CALCULATED,
    VERIFIED,
    BLENDED,
    DO_NOT_USE,
}

enum class ObservationConfidence {
    LOW,
    MEDIUM,
    HIGH,
}

@Serializable
data class SessionExportMetadataV1(
    val schemaVersion: Int = 1,
    val appVersion: String,
    val engineVersion: String,
    val createdAtEpochMillis: Long,
    val units: String,
    val preciseLocationIncluded: Boolean,
)

@Serializable
data class SessionExportV1(
    val metadata: SessionExportMetadataV1,
    val session: SessionSnapshotEntity,
    val verifiedRecords: List<VerifiedDopeRecordEntity>,
)

object SessionExportCodec {
    private val json = Json { prettyPrint = true }

    fun encode(value: SessionExportV1): String = json.encodeToString(value)
}

class SessionRepository(
    database: DopeDatabase,
) {
    private val dao = database.profileDao()

    fun observeSessions(): Flow<List<SessionSnapshotEntity>> = dao.observeSessionSnapshots()

    fun observeVerifiedDope(): Flow<List<VerifiedDopeRecordEntity>> = dao.observeVerifiedDopeRecords()

    suspend fun appendSession(value: SessionSnapshotEntity): SessionSnapshotEntity {
        validateSession(value)
        val canonical = value.copy(contentSha256 = "")
        val stored = value.copy(contentSha256 = sha256(Json.encodeToString(canonical)))
        dao.insertSessionSnapshot(stored)
        return stored
    }

    suspend fun appendVerifiedDope(value: VerifiedDopeRecordEntity): VerifiedDopeRecordEntity {
        validateVerifiedDope(value)
        value.sessionSnapshotId?.let { requireNotNull(dao.sessionSnapshot(it)) { "Unknown session snapshot" } }
        value.supersedesRecordId?.let { requireNotNull(dao.verifiedDopeRecord(it)) { "Unknown superseded record" } }
        val canonical = value.copy(evidenceSha256 = "")
        val stored = value.copy(evidenceSha256 = sha256(Json.encodeToString(canonical)))
        dao.insertVerifiedDopeRecord(stored)
        return stored
    }

    suspend fun exportSession(
        sessionId: String,
        appVersion: String,
        createdAtEpochMillis: Long,
        units: String,
    ): String {
        val session = requireNotNull(dao.sessionSnapshot(sessionId)) { "Unknown session snapshot" }
        val records = dao.verifiedDopeRecordsForSession(sessionId)
        return SessionExportCodec.encode(
            SessionExportV1(
                metadata =
                    SessionExportMetadataV1(
                        appVersion = appVersion,
                        engineVersion = session.engineVersion,
                        createdAtEpochMillis = createdAtEpochMillis,
                        units = units,
                        preciseLocationIncluded = session.preciseLocationIncluded,
                    ),
                session = session,
                verifiedRecords = records,
            ),
        )
    }

    private fun validateSession(value: SessionSnapshotEntity) {
        require(value.sessionName.isNotBlank()) { "Session name is required" }
        require(value.completedAtEpochMillis >= value.startedAtEpochMillis) { "Session end precedes start" }
        require(value.distanceMetres > 0.0 && value.distanceUncertaintyMetres >= 0.0) { "Invalid distance" }
        require(value.profileSnapshotJson.isNotBlank()) { "Profile snapshot is required" }
        require(value.referenceEnvironmentJson.isNotBlank() && value.currentEnvironmentJson.isNotBlank()) {
            "Environment snapshots are required"
        }
        require(value.calculationResultJson.isNotBlank() && value.calculationTraceJson.isNotBlank()) {
            "Calculation result and trace are required"
        }
        require(!value.preciseLocationIncluded || !value.locationSnapshotJson.isNullOrBlank()) {
            "Included location requires a location snapshot"
        }
    }

    private fun validateVerifiedDope(value: VerifiedDopeRecordEntity) {
        require(value.distanceMetres > 0.0 && value.distanceUncertaintyMetres >= 0.0) { "Invalid distance" }
        require(value.numberOfShots > 0) { "Shot count must be positive" }
        require(value.calculatedUnit in SUPPORTED_UNITS && value.actualDialUnit in SUPPORTED_UNITS) {
            "Unsupported angular unit"
        }
        requireNotNull(runCatching { ObservationConfidence.valueOf(value.confidence) }.getOrNull()) {
            "Unsupported confidence"
        }
        requireNotNull(runCatching { VerifiedDataStatus.valueOf(value.status) }.getOrNull()) {
            "Unsupported verified-data status"
        }
        require(value.profileSnapshotJson.isNotBlank() && value.conditionsJson.isNotBlank()) {
            "Profile and condition snapshots are required"
        }
    }

    private fun sha256(value: String): String =
        MessageDigest
            .getInstance("SHA-256")
            .digest(value.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }

    private companion object {
        val SUPPORTED_UNITS = setOf("MIL", "MOA")
    }
}
