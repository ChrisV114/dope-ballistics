package za.co.dope.ballistics.engine

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

@Suppress("TooManyFunctions")
class StandardBallisticsEngine : BallisticsEngine {
    @Suppress("LongMethod", "ReturnCount")
    override fun solve(input: TrajectoryInput): TrajectoryResult {
        val issues = validate(input)
        if (issues.isNotEmpty()) return blocked(input, issues)

        val referencePhysics = Physics(input.referenceAtmosphere, input.wind, input.geometry.inclinationDegrees)
        val currentPhysics = Physics(input.currentAtmosphere, input.wind, input.geometry.inclinationDegrees)
        val zero =
            solveZero(input, referencePhysics)
                ?: return blocked(input, listOf("A zero solution could not be found for the supplied inputs."))
        val reference = integrate(input, referencePhysics, zero.angle, input.geometry.targetRangeMeters)
        val current = integrate(input, currentPhysics, zero.angle, input.geometry.targetRangeMeters)
        if (reference == null || current == null) {
            return blocked(
                input,
                listOf("The trajectory did not reach the requested distance within the calculation limit."),
            )
        }

        val levelPhysics = Physics(input.currentAtmosphere, input.wind, 0.0)
        val levelZero = solveZero(input.copy(geometry = input.geometry.copy(inclinationDegrees = 0.0)), levelPhysics)
        val level = levelZero?.let { integrate(input, levelPhysics, it.angle, input.geometry.targetRangeMeters) }
        val referenceElevation = elevation(reference)
        val currentElevation = elevation(current)
        val windage = -atan2(current.z, input.geometry.targetRangeMeters)
        val rawScopeElevation = toAngularUnit(currentElevation, input.scope.unit)
        val rawScopeWindage = toAngularUnit(windage, input.scope.unit)
        val elevationScope = scopeResult(rawScopeElevation, input.scope)
        val windageScope = scopeResult(rawScopeWindage, input.scope)
        val velocity = sqrt(current.vs * current.vs + current.vy * current.vy + current.vz * current.vz)
        val speedOfSound = currentPhysics.speedOfSoundMps
        val mach = velocity / speedOfSound
        val uncertainty = uncertainty(input, zero.angle, rawScopeElevation)

        return TrajectoryResult(
            confidence = ResultConfidence.CONFIDENT,
            issues = emptyList(),
            solution =
                TrajectorySolution(
                    rangeMeters = input.geometry.targetRangeMeters,
                    boreAngleRadians = zero.angle,
                    verticalOffsetMeters = current.y,
                    horizontalOffsetMeters = current.z,
                    referenceElevationRadians = referenceElevation,
                    currentElevationRadians = currentElevation,
                    environmentalDeviationRadians = currentElevation - referenceElevation,
                    inclinationContributionRadians = currentElevation - (level?.let(::elevation) ?: currentElevation),
                    windageRadians = windage,
                    timeOfFlightSeconds = current.time,
                    remainingVelocityMps = velocity,
                    remainingEnergyJoules = 0.5 * input.projectile.massGrains * GRAIN_TO_KG * velocity.pow(2),
                    mach = mach,
                    flightState =
                        when {
                            mach >= SUPERSONIC_MACH -> FlightState.SUPERSONIC
                            mach > SUBSONIC_MACH -> FlightState.TRANSONIC
                            else -> FlightState.SUBSONIC
                        },
                    elevationScope = elevationScope,
                    windageScope = windageScope,
                    elevationUncertainty = uncertainty,
                ),
            trace = trace(input, zero.iterations),
        )
    }

    override fun rangeCard(input: RangeCardInput): RangeCardResult {
        val distanceIssues =
            input.distancesMeters.mapNotNull { distance ->
                if (!distance.isFinite() || distance <= 0.0) {
                    "Range-card distances must be finite and greater than zero."
                } else {
                    null
                }
            }
        if (distanceIssues.isNotEmpty() || input.distancesMeters.isEmpty()) {
            return RangeCardResult(
                ResultConfidence.BLOCKED,
                distanceIssues.ifEmpty { listOf("At least one distance is required.") },
                emptyList(),
                VERSION,
            )
        }
        val rows =
            input.distancesMeters.distinct().sorted().map { distance ->
                solve(input.trajectory.copy(geometry = input.trajectory.geometry.copy(targetRangeMeters = distance)))
            }
        val issues = rows.flatMap { it.issues }.distinct()
        return RangeCardResult(
            confidence = if (issues.isEmpty()) ResultConfidence.CONFIDENT else ResultConfidence.BLOCKED,
            issues = issues,
            rows = rows,
            engineVersion = VERSION,
        )
    }

    @Suppress("ReturnCount")
    private fun solveZero(
        input: TrajectoryInput,
        physics: Physics,
    ): ZeroSolution? {
        var low = -MAX_BORE_ANGLE_RADIANS
        var high = MAX_BORE_ANGLE_RADIANS
        var lowValue = integrate(input, physics, low, input.geometry.zeroRangeMeters)?.y ?: return null
        val highValue = integrate(input, physics, high, input.geometry.zeroRangeMeters)?.y ?: return null
        if (lowValue * highValue > 0.0) return null
        repeat(MAX_ZERO_ITERATIONS) { iteration ->
            val mid = (low + high) / 2.0
            val value = integrate(input, physics, mid, input.geometry.zeroRangeMeters)?.y ?: return null
            if (abs(value) <= ZERO_TOLERANCE_METERS || abs(high - low) <= ZERO_TOLERANCE_RADIANS) {
                return ZeroSolution(mid, iteration + 1)
            }
            if (lowValue * value <= 0.0) {
                high = mid
            } else {
                low = mid
                lowValue = value
            }
        }
        return ZeroSolution((low + high) / 2.0, MAX_ZERO_ITERATIONS)
    }

    @Suppress("ReturnCount", "ComplexCondition")
    private fun integrate(
        input: TrajectoryInput,
        physics: Physics,
        boreAngle: Double,
        distanceMeters: Double,
    ): State? {
        val velocity = input.projectile.muzzleVelocityMps
        var state =
            State(
                s = 0.0,
                y = -input.geometry.sightHeightMeters,
                z = 0.0,
                vs = velocity * cos(boreAngle),
                vy = velocity * sin(boreAngle),
                vz = 0.0,
                time = 0.0,
            )
        val maximumTime = max(20.0, input.maximumDistanceMeters / max(velocity * 0.15, 1.0))
        while (
            state.s < distanceMeters &&
            state.s <= input.maximumDistanceMeters &&
            state.time < maximumTime &&
            state.vs > 0.0
        ) {
            val previous = state
            state = rk4(state, input.integrationStepSeconds, input.projectile, physics)
            if (!state.finite()) return null
            if (state.s >= distanceMeters) return interpolate(previous, state, distanceMeters)
        }
        return null
    }

    private fun rk4(
        state: State,
        step: Double,
        projectile: Projectile,
        physics: Physics,
    ): State {
        val k1 = derivative(state, projectile, physics)
        val k2 = derivative(state.plus(k1, step / 2.0), projectile, physics)
        val k3 = derivative(state.plus(k2, step / 2.0), projectile, physics)
        val k4 = derivative(state.plus(k3, step), projectile, physics)
        return state.plus(
            Derivative(
                ds = (k1.ds + 2 * k2.ds + 2 * k3.ds + k4.ds) / 6.0,
                dy = (k1.dy + 2 * k2.dy + 2 * k3.dy + k4.dy) / 6.0,
                dz = (k1.dz + 2 * k2.dz + 2 * k3.dz + k4.dz) / 6.0,
                dvs = (k1.dvs + 2 * k2.dvs + 2 * k3.dvs + k4.dvs) / 6.0,
                dvy = (k1.dvy + 2 * k2.dvy + 2 * k3.dvy + k4.dvy) / 6.0,
                dvz = (k1.dvz + 2 * k2.dvz + 2 * k3.dvz + k4.dvz) / 6.0,
            ),
            step,
        )
    }

    private fun derivative(
        state: State,
        projectile: Projectile,
        physics: Physics,
    ): Derivative {
        val relativeS = state.vs - physics.windAlongLineMps
        val relativeY = state.vy - physics.windNormalMps
        val relativeZ = state.vz - physics.crosswindMps
        val airSpeed = sqrt(relativeS * relativeS + relativeY * relativeY + relativeZ * relativeZ)
        val mach = airSpeed / physics.speedOfSoundMps
        val cd = StandardDragTables.coefficient(projectile.dragModel, mach)
        val dragScale =
            physics.airDensityKgM3 * PI * cd * airSpeed /
                (8.0 * BC_KG_PER_SQUARE_METER * projectile.ballisticCoefficient)
        return Derivative(
            ds = state.vs,
            dy = state.vy,
            dz = state.vz,
            dvs = -dragScale * relativeS + physics.gravityAlongLineMps2,
            dvy = -dragScale * relativeY + physics.gravityNormalMps2,
            dvz = -dragScale * relativeZ,
        )
    }

    @Suppress("LongMethod")
    private fun uncertainty(
        input: TrajectoryInput,
        boreAngle: Double,
        baseRaw: Double,
    ): UncertaintyResult {
        val u = input.uncertainty
        val perturbations =
            buildList {
                val distanceUncertainty = u.distanceMeters + u.cameraDistanceMeters
                addPerturbation("distance", distanceUncertainty) {
                    it.copy(
                        geometry =
                            it.geometry.copy(
                                targetRangeMeters = it.geometry.targetRangeMeters + distanceUncertainty,
                            ),
                    )
                }
                addPerturbation("muzzle velocity", u.muzzleVelocityMps) {
                    it.copy(
                        projectile =
                            it.projectile.copy(
                                muzzleVelocityMps = it.projectile.muzzleVelocityMps + u.muzzleVelocityMps,
                            ),
                    )
                }
                addPerturbation("ballistic coefficient", u.ballisticCoefficient) {
                    it.copy(
                        projectile =
                            it.projectile.copy(
                                ballisticCoefficient = it.projectile.ballisticCoefficient + u.ballisticCoefficient,
                            ),
                    )
                }
                addPerturbation("pressure", u.pressurePascal) {
                    it.copy(
                        currentAtmosphere =
                            it.currentAtmosphere.copy(
                                stationPressurePascal = it.currentAtmosphere.stationPressurePascal + u.pressurePascal,
                            ),
                    )
                }
                addPerturbation("temperature", u.temperatureCelsius) {
                    it.copy(
                        currentAtmosphere =
                            it.currentAtmosphere.copy(
                                temperatureCelsius = it.currentAtmosphere.temperatureCelsius + u.temperatureCelsius,
                            ),
                    )
                }
                addPerturbation("humidity", u.humidityPercent) {
                    it.copy(
                        currentAtmosphere =
                            it.currentAtmosphere.copy(
                                relativeHumidityPercent =
                                    (it.currentAtmosphere.relativeHumidityPercent + u.humidityPercent)
                                        .coerceIn(0.0, 100.0),
                            ),
                    )
                }
                addPerturbation("inclination", u.inclinationDegrees) {
                    it.copy(
                        geometry =
                            it.geometry.copy(
                                inclinationDegrees = it.geometry.inclinationDegrees + u.inclinationDegrees,
                            ),
                    )
                }
                addPerturbation("headwind", u.headwindMps) {
                    it.copy(wind = it.wind.copy(headwindMps = it.wind.headwindMps + u.headwindMps))
                }
                addPerturbation("crosswind", u.crosswindMps) {
                    it.copy(wind = it.wind.copy(crosswindMps = it.wind.crosswindMps + u.crosswindMps))
                }
            }
        val contributions =
            perturbations
                .mapNotNull { perturbation ->
                    val changed = perturbation.mutate(input)
                    if (validate(changed).isNotEmpty()) return@mapNotNull null
                    val physics = Physics(changed.currentAtmosphere, changed.wind, changed.geometry.inclinationDegrees)
                    val result =
                        integrate(changed, physics, boreAngle, changed.geometry.targetRangeMeters)
                            ?: return@mapNotNull null
                    val delta = abs(toAngularUnit(elevation(result), input.scope.unit) - baseRaw)
                    UncertaintyContribution(perturbation.name, delta)
                }.sortedByDescending { it.angularOneSigma }
        val combined = sqrt(contributions.sumOf { it.angularOneSigma * it.angularOneSigma })
        return UncertaintyResult(combined, baseRaw - combined, baseRaw + combined, contributions)
    }

    private fun MutableList<Perturbation>.addPerturbation(
        name: String,
        amount: Double,
        mutate: (TrajectoryInput) -> TrajectoryInput,
    ) {
        if (amount > 0.0 && amount.isFinite()) add(Perturbation(name, mutate))
    }

    @Suppress("CyclomaticComplexMethod")
    private fun validate(input: TrajectoryInput): List<String> =
        buildList {
            val p = input.projectile
            val g = input.geometry
            val a = input.currentAtmosphere
            val r = input.referenceAtmosphere
            if (!p.massGrains.isFinite() || p.massGrains !in 1.0..1_000.0) {
                add("Projectile mass must be between 1 and 1000 grains.")
            }
            if (!p.ballisticCoefficient.isFinite() || p.ballisticCoefficient !in 0.01..2.0) {
                add("The selected G1/G7 ballistic coefficient must be between 0.01 and 2.0.")
            }
            if (!p.muzzleVelocityMps.isFinite() || p.muzzleVelocityMps !in 50.0..1_500.0) {
                add("Muzzle velocity must be between 50 and 1500 m/s.")
            }
            if (!g.sightHeightMeters.isFinite() || g.sightHeightMeters !in 0.001..0.25) {
                add("Sight height must be between 0.001 and 0.25 m.")
            }
            if (!g.zeroRangeMeters.isFinite() || g.zeroRangeMeters !in 1.0..input.maximumDistanceMeters) {
                add("Zero range is outside the calculation limits.")
            }
            if (!g.targetRangeMeters.isFinite() || g.targetRangeMeters !in 1.0..input.maximumDistanceMeters) {
                add("Target range is outside the calculation limits.")
            }
            if (!g.inclinationDegrees.isFinite() || abs(g.inclinationDegrees) > 89.0) {
                add("Inclination must be between -89 and 89 degrees.")
            }
            validateAtmosphere(a, "Current", this)
            validateAtmosphere(r, "Reference", this)
            if (!input.wind.headwindMps.isFinite() || abs(input.wind.headwindMps) > 100.0) {
                add("Headwind must be finite and no greater than 100 m/s.")
            }
            if (!input.wind.crosswindMps.isFinite() || abs(input.wind.crosswindMps) > 100.0) {
                add("Crosswind must be finite and no greater than 100 m/s.")
            }
            if (!input.scope.clickValue.isFinite() || input.scope.clickValue <= 0.0) {
                add("Scope click value must be greater than zero.")
            }
            if (!input.maximumDistanceMeters.isFinite() || input.maximumDistanceMeters !in 10.0..10_000.0) {
                add("Maximum calculation distance must be between 10 and 10000 m.")
            }
            if (!input.integrationStepSeconds.isFinite() || input.integrationStepSeconds !in 0.0001..0.01) {
                add("Integration step must be between 0.0001 and 0.01 seconds.")
            }
            val initialMach = p.muzzleVelocityMps / Physics(a, input.wind, g.inclinationDegrees).speedOfSoundMps
            if (initialMach > 5.0) add("Initial Mach exceeds the validated drag-table range.")
        }

    private fun validateAtmosphere(
        atmosphere: Atmosphere,
        label: String,
        issues: MutableList<String>,
    ) {
        if (!atmosphere.temperatureCelsius.isFinite() || atmosphere.temperatureCelsius !in -80.0..60.0) {
            issues += "$label temperature must be between -80 and 60 C."
        }
        if (!atmosphere.stationPressurePascal.isFinite() || atmosphere.stationPressurePascal !in 30_000.0..110_000.0) {
            issues += "$label station pressure must be between 30000 and 110000 Pa."
        }
        if (!atmosphere.relativeHumidityPercent.isFinite() || atmosphere.relativeHumidityPercent !in 0.0..100.0) {
            issues += "$label humidity must be between 0 and 100 percent."
        }
    }

    private fun blocked(
        input: TrajectoryInput,
        issues: List<String>,
    ) = TrajectoryResult(
        confidence = ResultConfidence.BLOCKED,
        issues = issues,
        solution = null,
        trace = trace(input, 0),
    )

    private fun trace(
        input: TrajectoryInput,
        iterations: Int,
    ) = CalculationTrace(
        engineVersion = VERSION,
        dragReference = StandardDragTables.SOURCE,
        dragModel = input.projectile.dragModel,
        integrationMethod = "fixed-step RK4",
        integrationStepSeconds = input.integrationStepSeconds,
        zeroSolver = "bracketed bisection",
        zeroIterations = iterations,
        deterministic = true,
        notes =
            listOf(
                "SI internal units; manufacturer-declared BC and drag model required",
                "Advanced spin drift, Coriolis and aerodynamic jump disabled",
                "Positive crosswind moves the projectile right; positive headwind is from target to shooter",
            ),
    )

    private fun elevation(state: State) = -atan2(state.y, state.s)

    private fun toAngularUnit(
        radians: Double,
        unit: AngularUnit,
    ) = when (unit) {
        AngularUnit.MIL -> radians * 1_000.0
        AngularUnit.MOA -> radians * 180.0 / PI * 60.0
    }

    private fun scopeResult(
        raw: Double,
        scope: ScopeAdjustment,
    ): ScopeResult {
        val clicks = (raw / scope.clickValue).roundToInt()
        val rounded = clicks * scope.clickValue
        return ScopeResult(
            unit = scope.unit,
            raw = raw,
            rounded = rounded,
            clicks = clicks,
            residual = raw - rounded,
            withinTravel = scope.maximumTravel?.let { abs(rounded) <= it } ?: true,
            revolutions = scope.clicksPerRevolution?.let { abs(clicks).toDouble() / it },
        )
    }

    private fun interpolate(
        before: State,
        after: State,
        distance: Double,
    ): State {
        val fraction = (distance - before.s) / (after.s - before.s)
        return State(
            s = distance,
            y = before.y + (after.y - before.y) * fraction,
            z = before.z + (after.z - before.z) * fraction,
            vs = before.vs + (after.vs - before.vs) * fraction,
            vy = before.vy + (after.vy - before.vy) * fraction,
            vz = before.vz + (after.vz - before.vz) * fraction,
            time = before.time + (after.time - before.time) * fraction,
        )
    }

    private data class ZeroSolution(
        val angle: Double,
        val iterations: Int,
    )

    private data class Perturbation(
        val name: String,
        val mutate: (TrajectoryInput) -> TrajectoryInput,
    )

    private data class State(
        val s: Double,
        val y: Double,
        val z: Double,
        val vs: Double,
        val vy: Double,
        val vz: Double,
        val time: Double,
    ) {
        fun plus(
            derivative: Derivative,
            step: Double,
        ) = State(
            s + derivative.ds * step,
            y + derivative.dy * step,
            z + derivative.dz * step,
            vs + derivative.dvs * step,
            vy + derivative.dvy * step,
            vz + derivative.dvz * step,
            time + step,
        )

        fun finite() = listOf(s, y, z, vs, vy, vz, time).all(Double::isFinite)
    }

    private data class Derivative(
        val ds: Double,
        val dy: Double,
        val dz: Double,
        val dvs: Double,
        val dvy: Double,
        val dvz: Double,
    )

    private class Physics(
        atmosphere: Atmosphere,
        wind: Wind,
        inclinationDegrees: Double,
    ) {
        private val inclinationRadians = inclinationDegrees * PI / 180.0
        val airDensityKgM3 = moistAirDensity(atmosphere)
        val speedOfSoundMps = speedOfSound(atmosphere)
        val gravityAlongLineMps2 = -GRAVITY_MPS2 * sin(inclinationRadians)
        val gravityNormalMps2 = -GRAVITY_MPS2 * cos(inclinationRadians)
        val windAlongLineMps = -wind.headwindMps * cos(inclinationRadians)
        val windNormalMps = wind.headwindMps * sin(inclinationRadians)
        val crosswindMps = wind.crosswindMps

        companion object {
            private fun moistAirDensity(atmosphere: Atmosphere): Double {
                val kelvin = atmosphere.temperatureCelsius + 273.15
                val temperature = atmosphere.temperatureCelsius
                val saturationHpa =
                    6.1121 *
                        kotlin.math.exp(
                            (18.678 - temperature / 234.5) * (temperature / (257.14 + temperature)),
                        )
                val vapourPressure = atmosphere.relativeHumidityPercent / 100.0 * saturationHpa * 100.0
                val dryPressure = atmosphere.stationPressurePascal - vapourPressure
                return dryPressure / (DRY_AIR_GAS_CONSTANT * kelvin) +
                    vapourPressure / (WATER_VAPOUR_GAS_CONSTANT * kelvin)
            }

            private fun speedOfSound(atmosphere: Atmosphere): Double {
                val kelvin = atmosphere.temperatureCelsius + 273.15
                return sqrt(SPECIFIC_HEAT_RATIO * DRY_AIR_GAS_CONSTANT * kelvin)
            }
        }
    }

    companion object {
        const val VERSION = "dope-point-mass-1.0.0"
        private const val GRAVITY_MPS2 = 9.80665
        private const val GRAIN_TO_KG = 0.00006479891
        private const val BC_KG_PER_SQUARE_METER = 703.06957964
        private const val DRY_AIR_GAS_CONSTANT = 287.05
        private const val WATER_VAPOUR_GAS_CONSTANT = 461.495
        private const val SPECIFIC_HEAT_RATIO = 1.4
        private const val MAX_BORE_ANGLE_RADIANS = 0.1
        private const val MAX_ZERO_ITERATIONS = 40
        private const val ZERO_TOLERANCE_METERS = 0.000001
        private const val ZERO_TOLERANCE_RADIANS = 0.000000001
        private const val SUPERSONIC_MACH = 1.2
        private const val SUBSONIC_MACH = 0.8
    }
}
