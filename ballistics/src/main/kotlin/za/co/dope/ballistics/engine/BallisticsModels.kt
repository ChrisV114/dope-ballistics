package za.co.dope.ballistics.engine

enum class DragModel {
    G1,
    G7,
}

enum class AngularUnit {
    MIL,
    MOA,
}

enum class FlightState {
    SUPERSONIC,
    TRANSONIC,
    SUBSONIC,
}

enum class ResultConfidence {
    CONFIDENT,
    BLOCKED,
}

data class Projectile(
    val massGrains: Double,
    val ballisticCoefficient: Double,
    val dragModel: DragModel,
    val muzzleVelocityMps: Double,
    val profileName: String = "",
)

data class Atmosphere(
    val temperatureCelsius: Double,
    val stationPressurePascal: Double,
    val relativeHumidityPercent: Double,
)

data class ShotGeometry(
    val sightHeightMeters: Double,
    val zeroRangeMeters: Double,
    val targetRangeMeters: Double,
    val inclinationDegrees: Double = 0.0,
)

data class Wind(
    /** Positive values are wind from the target toward the shooter. */
    val headwindMps: Double = 0.0,
    /** Positive values move the projectile to the right. */
    val crosswindMps: Double = 0.0,
)

data class ScopeAdjustment(
    val unit: AngularUnit,
    val clickValue: Double,
    val maximumTravel: Double? = null,
    val clicksPerRevolution: Int? = null,
    val hasZeroStop: Boolean = false,
)

data class InputUncertainty(
    val distanceMeters: Double = 0.0,
    val muzzleVelocityMps: Double = 0.0,
    val ballisticCoefficient: Double = 0.0,
    val pressurePascal: Double = 0.0,
    val temperatureCelsius: Double = 0.0,
    val humidityPercent: Double = 0.0,
    val inclinationDegrees: Double = 0.0,
    val headwindMps: Double = 0.0,
    val crosswindMps: Double = 0.0,
    val cameraDistanceMeters: Double = 0.0,
)

data class TrajectoryInput(
    val projectile: Projectile,
    val geometry: ShotGeometry,
    val currentAtmosphere: Atmosphere,
    val referenceAtmosphere: Atmosphere = currentAtmosphere,
    val wind: Wind = Wind(),
    val scope: ScopeAdjustment,
    val uncertainty: InputUncertainty = InputUncertainty(),
    val maximumDistanceMeters: Double = 3_000.0,
    val integrationStepSeconds: Double = 0.001,
)

data class RangeCardInput(
    val trajectory: TrajectoryInput,
    val distancesMeters: List<Double>,
)

data class ScopeResult(
    val unit: AngularUnit,
    val raw: Double,
    val rounded: Double,
    val clicks: Int,
    val residual: Double,
    val withinTravel: Boolean,
    val revolutions: Double?,
)

data class UncertaintyContribution(
    val input: String,
    val angularOneSigma: Double,
)

data class UncertaintyResult(
    val angularOneSigma: Double,
    val lower: Double,
    val upper: Double,
    val contributions: List<UncertaintyContribution>,
)

data class CalculationTrace(
    val engineVersion: String,
    val dragReference: String,
    val dragModel: DragModel,
    val integrationMethod: String,
    val integrationStepSeconds: Double,
    val zeroSolver: String,
    val zeroIterations: Int,
    val deterministic: Boolean,
    val notes: List<String>,
)

data class TrajectorySolution(
    val rangeMeters: Double,
    val boreAngleRadians: Double,
    val verticalOffsetMeters: Double,
    val horizontalOffsetMeters: Double,
    val referenceElevationRadians: Double,
    val currentElevationRadians: Double,
    val environmentalDeviationRadians: Double,
    val inclinationContributionRadians: Double,
    val windageRadians: Double,
    val timeOfFlightSeconds: Double,
    val remainingVelocityMps: Double,
    val remainingEnergyJoules: Double,
    val mach: Double,
    val flightState: FlightState,
    val elevationScope: ScopeResult,
    val windageScope: ScopeResult,
    val elevationUncertainty: UncertaintyResult,
)

data class TrajectoryResult(
    val confidence: ResultConfidence,
    val issues: List<String>,
    val solution: TrajectorySolution?,
    val trace: CalculationTrace,
)

data class RangeCardResult(
    val confidence: ResultConfidence,
    val issues: List<String>,
    val rows: List<TrajectoryResult>,
    val engineVersion: String,
)

interface BallisticsEngine {
    fun solve(input: TrajectoryInput): TrajectoryResult

    fun rangeCard(input: RangeCardInput): RangeCardResult
}
