package za.co.dope.ballistics.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import za.co.dope.ballistics.engine.AngularUnit
import za.co.dope.ballistics.engine.Atmosphere
import za.co.dope.ballistics.engine.BearingReference
import za.co.dope.ballistics.engine.DragModel
import za.co.dope.ballistics.engine.Projectile
import za.co.dope.ballistics.engine.ScopeAdjustment
import za.co.dope.ballistics.engine.ShotGeometry
import za.co.dope.ballistics.engine.StandardBallisticsEngine
import za.co.dope.ballistics.engine.TrajectoryInput
import za.co.dope.ballistics.engine.WindObservation
import kotlin.math.abs

class RangeCardAndComparisonTest {
    private val engine = StandardBallisticsEngine()

    @Test
    fun rangeCardIncludesWindBracketAndOfflineCsvMetadata() {
        val document =
            RangeCardGenerator(engine).generate(
                RangeCardRequest(
                    trajectory = trajectory(),
                    startDistanceMetres = 100.0,
                    endDistanceMetres = 300.0,
                    incrementMetres = 100.0,
                    confirmedDistances = listOf(ConfirmedDistance(250.0, "MANUAL", true)),
                    windObservation = wind(),
                    profileLabel = "Test profile",
                    appVersion = "test",
                    createdAtEpochMillis = 1L,
                ),
            )

        assertEquals(listOf(100.0, 200.0, 250.0, 300.0), document.rows.map { it.distanceMetres })
        assertTrue(document.rows.all { abs(it.windMaximum) >= abs(it.windMinimum) })
        val csv = RangeCardCsvExporter.export(document)
        assertTrue(csv.contains("# App version,\"test\""))
        assertTrue(csv.contains("# Engine version,\"dope-point-mass-1.0.0\""))
        assertTrue(csv.contains("distance_m,elevation_raw"))
        assertTrue(csv.contains("250.000000"))
    }

    @Test
    fun unconfirmedDistanceCannotPopulateRangeCard() {
        val request =
            RangeCardRequest(
                trajectory = trajectory(),
                startDistanceMetres = 100.0,
                endDistanceMetres = 300.0,
                incrementMetres = 100.0,
                confirmedDistances = listOf(ConfirmedDistance(250.0, "CAMERA", false)),
                windObservation = wind(),
                profileLabel = "Test",
                appVersion = "test",
                createdAtEpochMillis = 1L,
            )

        assertThrows(IllegalArgumentException::class.java) { RangeCardGenerator(engine).generate(request) }
    }

    @Test
    fun rangeCardHonoursReferenceMoaYardsAndEssentialColumns() {
        val request =
            RangeCardRequest(
                trajectory =
                    trajectory().copy(
                        currentAtmosphere = Atmosphere(35.0, 85_000.0, 20.0),
                        referenceAtmosphere = Atmosphere(15.0, 101_325.0, 0.0),
                    ),
                startDistanceMetres = 91.44,
                endDistanceMetres = 91.44,
                incrementMetres = 91.44,
                windObservation = wind(),
                profileLabel = "Test",
                appVersion = "test",
                createdAtEpochMillis = 1L,
                distanceDisplayUnit = DistanceDisplayUnit.YARDS,
                environmentSelection = EnvironmentSelection.REFERENCE,
                angularDisplayUnit = AngularDisplayUnit.MOA,
                columnSet = RangeCardColumnSet.ESSENTIAL,
                layout = RangeCardLayout.RED_LIGHT,
            )

        val document = RangeCardGenerator(engine).generate(request)
        val csv = RangeCardCsvExporter.export(document)

        assertEquals("MOA", document.metadata.units)
        assertEquals("REFERENCE", document.metadata.environmentSelection)
        assertEquals("RED_LIGHT", document.metadata.layout)
        assertTrue(csv.contains("distance_yards,elevation_dial,clicks,wind_selected"))
        assertTrue(csv.contains("100.000000"))
    }

    @Test
    fun whatIfComparisonDoesNotModifyBaselineAndShowsDifference() {
        val baseline = trajectory()
        val warmer =
            baseline.copy(
                currentAtmosphere = baseline.currentAtmosphere.copy(temperatureCelsius = 35.0),
            )

        val comparison = TrajectoryComparisonService(engine).compare(baseline, warmer)

        assertEquals(15.0, baseline.currentAtmosphere.temperatureCelsius, 0.0)
        assertNotEquals(0.0, comparison.elevationDialDelta)
        assertTrue(comparison.issues.isEmpty())
    }

    private fun trajectory() =
        TrajectoryInput(
            projectile =
                Projectile(
                    massGrains = 139.0,
                    ballisticCoefficient = 0.290,
                    dragModel = DragModel.G7,
                    muzzleVelocityMps = 809.0,
                    profileName = "test-projectile",
                ),
            geometry =
                ShotGeometry(
                    sightHeightMeters = 0.06,
                    zeroRangeMeters = 100.0,
                    targetRangeMeters = 500.0,
                ),
            currentAtmosphere = Atmosphere(15.0, 101_325.0, 0.0),
            scope = ScopeAdjustment(AngularUnit.MIL, 0.1),
        )

    private fun wind() =
        WindObservation(
            windFromDegrees = 270.0,
            directionOfFireDegrees = 0.0,
            bearingReference = BearingReference.TRUE,
            minimumSpeedMps = 2.0,
            averageSpeedMps = 4.0,
            maximumSpeedMps = 6.0,
            gustSpeedMps = 8.0,
        )
}
