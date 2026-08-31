package za.co.dope.ballistics.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class StandardBallisticsEngineTest {
    private val engine = StandardBallisticsEngine()

    @Test
    fun `BRL drag tables retain reference values and interpolate`() {
        assertEquals(0.4805, StandardDragTables.coefficient(DragModel.G1, 1.0), 0.0)
        assertEquals(0.3803, StandardDragTables.coefficient(DragModel.G7, 1.0), 0.0)
        assertEquals(0.3398, StandardDragTables.coefficient(DragModel.G7, 0.9875), 0.0001)
    }

    @Test
    fun `Lapua 139 grain G7 fixture is deterministic and zero is root solved`() {
        val input = creedmoorInput()
        val first = engine.solve(input)
        val second = engine.solve(input)

        assertEquals(ResultConfidence.CONFIDENT, first.confidence)
        assertEquals(first, second)
        val solution = requireNotNull(first.solution)
        assertEquals(0.0, solution.verticalOffsetMeters, 0.001)
        assertEquals(0, solution.elevationScope.clicks)
        assertTrue(first.trace.zeroIterations > 0)
        assertTrue(first.trace.deterministic)
    }

    @Test
    fun `Hornady 53 grain G1 benchmark fixture retains plausible downrange values`() {
        val result = engine.solve223(targetRangeMeters = 300.0)
        val solution = requireNotNull(result.solution)

        assertEquals(ResultConfidence.CONFIDENT, result.confidence)
        assertTrue(solution.timeOfFlightSeconds in 0.30..0.60)
        assertTrue(solution.remainingVelocityMps in 450.0..850.0)
        assertTrue(solution.remainingEnergyJoules in 300.0..1_300.0)
        assertTrue(solution.elevationScope.raw > 0.0)
        assertEquals(AngularUnit.MOA, solution.elevationScope.unit)
    }

    @Test
    fun `RK4 result converges when time step is halved`() {
        val coarse = requireNotNull(engine.solve(creedmoorInput(700.0, 0.002)).solution)
        val fine = requireNotNull(engine.solve(creedmoorInput(700.0, 0.001)).solution)

        assertEquals(fine.elevationScope.raw, coarse.elevationScope.raw, 0.002)
        assertEquals(fine.remainingVelocityMps, coarse.remainingVelocityMps, 0.25)
        assertEquals(fine.timeOfFlightSeconds, coarse.timeOfFlightSeconds, 0.0005)
    }

    @Test
    fun `invalid coefficient blocks confident output`() {
        val result =
            engine.solve(
                creedmoorInput().copy(
                    projectile = creedmoorInput().projectile.copy(ballisticCoefficient = Double.NaN),
                ),
            )

        assertEquals(ResultConfidence.BLOCKED, result.confidence)
        assertNull(result.solution)
        assertTrue(result.issues.any { it.contains("ballistic coefficient") })
    }

    @Test
    fun `current atmosphere produces explicit environmental deviation`() {
        val reference = Atmosphere(15.0, 101_325.0, 0.0)
        val current = Atmosphere(35.0, 85_000.0, 35.0)
        val solution =
            requireNotNull(
                engine
                    .solve(
                        creedmoorInput(800.0).copy(referenceAtmosphere = reference, currentAtmosphere = current),
                    ).solution,
            )

        assertEquals(
            solution.currentElevationRadians - solution.referenceElevationRadians,
            solution.environmentalDeviationRadians,
            0.0,
        )
        assertTrue(abs(solution.environmentalDeviationRadians) > 0.00001)
    }

    @Test
    fun `scope output preserves raw rounded clicks residual travel and revolutions`() {
        val solution = requireNotNull(engine.solve(creedmoorInput(800.0)).solution)
        val output = solution.elevationScope

        assertEquals(output.clicks * 0.1, output.rounded, 0.0)
        assertEquals(output.raw - output.rounded, output.residual, 0.0)
        assertEquals(abs(output.clicks) / 100.0, output.revolutions!!, 0.0)
        assertTrue(output.withinTravel)
    }

    @Test
    fun `positive crosswind moves projectile right and requires negative correction`() {
        val noWind = requireNotNull(engine.solve(creedmoorInput(500.0)).solution)
        val wind =
            requireNotNull(
                engine.solve(creedmoorInput(500.0).copy(wind = Wind(crosswindMps = 5.0))).solution,
            )

        assertEquals(0.0, noWind.horizontalOffsetMeters, 0.000001)
        assertTrue(wind.horizontalOffsetMeters > 0.0)
        assertTrue(wind.windageRadians < 0.0)
    }

    @Test
    fun `uncertainty reports a combined band and dominant contributors`() {
        val input =
            creedmoorInput(600.0).copy(
                uncertainty =
                    InputUncertainty(
                        distanceMeters = 2.0,
                        muzzleVelocityMps = 5.0,
                        ballisticCoefficient = 0.01,
                        pressurePascal = 300.0,
                        temperatureCelsius = 2.0,
                        crosswindMps = 1.0,
                    ),
            )
        val uncertainty = requireNotNull(engine.solve(input).solution).elevationUncertainty

        assertTrue(uncertainty.angularOneSigma > 0.0)
        assertFalse(uncertainty.contributions.isEmpty())
        assertTrue(uncertainty.lower < uncertainty.upper)
        assertEquals(
            uncertainty.contributions.sortedByDescending { it.angularOneSigma },
            uncertainty.contributions,
        )
    }

    @Test
    fun `range card sorts and deduplicates requested distances`() {
        val result = engine.rangeCard(RangeCardInput(creedmoorInput(), listOf(500.0, 100.0, 300.0, 300.0)))

        assertEquals(ResultConfidence.CONFIDENT, result.confidence)
        assertEquals(listOf(100.0, 300.0, 500.0), result.rows.map { it.solution!!.rangeMeters })
    }

    private fun StandardBallisticsEngine.solve223(targetRangeMeters: Double): TrajectoryResult =
        solve(
            TrajectoryInput(
                projectile =
                    Projectile(
                        massGrains = 53.0,
                        ballisticCoefficient = 0.290,
                        dragModel = DragModel.G1,
                        muzzleVelocityMps = 920.0,
                        profileName = "Hornady 53 gr V-MAX",
                    ),
                geometry =
                    ShotGeometry(
                        sightHeightMeters = 0.06,
                        zeroRangeMeters = 50.0,
                        targetRangeMeters = targetRangeMeters,
                    ),
                currentAtmosphere = STANDARD_ATMOSPHERE,
                scope = ScopeAdjustment(AngularUnit.MOA, clickValue = 0.25),
            ),
        )

    private fun creedmoorInput(
        targetRangeMeters: Double = 100.0,
        stepSeconds: Double = 0.001,
    ) = TrajectoryInput(
        projectile =
            Projectile(
                massGrains = 139.0,
                ballisticCoefficient = 0.290,
                dragModel = DragModel.G7,
                muzzleVelocityMps = 809.0,
                profileName = "Lapua 139 gr Scenar GB458",
            ),
        geometry =
            ShotGeometry(
                sightHeightMeters = 0.06,
                zeroRangeMeters = 100.0,
                targetRangeMeters = targetRangeMeters,
            ),
        currentAtmosphere = STANDARD_ATMOSPHERE,
        scope =
            ScopeAdjustment(
                unit = AngularUnit.MIL,
                clickValue = 0.1,
                maximumTravel = 30.0,
                clicksPerRevolution = 100,
                hasZeroStop = true,
            ),
        integrationStepSeconds = stepSeconds,
    )

    companion object {
        private val STANDARD_ATMOSPHERE = Atmosphere(15.0, 101_325.0, 0.0)
    }
}
