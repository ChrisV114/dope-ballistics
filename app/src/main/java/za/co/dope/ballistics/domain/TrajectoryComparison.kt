package za.co.dope.ballistics.domain

import za.co.dope.ballistics.engine.BallisticsEngine
import za.co.dope.ballistics.engine.ResultConfidence
import za.co.dope.ballistics.engine.TrajectoryInput
import za.co.dope.ballistics.engine.TrajectoryResult

data class WhatIfComparison(
    val baseline: TrajectoryResult,
    val alternative: TrajectoryResult,
    val elevationDialDelta: Double?,
    val windageDialDelta: Double?,
    val timeOfFlightDeltaSeconds: Double?,
    val remainingVelocityDeltaMps: Double?,
    val issues: List<String>,
)

class TrajectoryComparisonService(
    private val engine: BallisticsEngine,
) {
    fun compare(
        baseline: TrajectoryInput,
        alternative: TrajectoryInput,
    ): WhatIfComparison {
        require(baseline.projectile.profileName == alternative.projectile.profileName) {
            "What-if comparison requires the same projectile profile"
        }
        require(baseline.scope.unit == alternative.scope.unit) { "What-if comparison requires the same angular unit" }
        val baselineResult = engine.solve(baseline)
        val alternativeResult = engine.solve(alternative)
        val base = baselineResult.solution
        val changed = alternativeResult.solution
        val comparable =
            baselineResult.confidence == ResultConfidence.CONFIDENT &&
                alternativeResult.confidence == ResultConfidence.CONFIDENT &&
                base != null &&
                changed != null
        return WhatIfComparison(
            baseline = baselineResult,
            alternative = alternativeResult,
            elevationDialDelta = if (comparable) changed.elevationScope.rounded - base.elevationScope.rounded else null,
            windageDialDelta = if (comparable) changed.windageScope.rounded - base.windageScope.rounded else null,
            timeOfFlightDeltaSeconds = if (comparable) changed.timeOfFlightSeconds - base.timeOfFlightSeconds else null,
            remainingVelocityDeltaMps =
                if (comparable) changed.remainingVelocityMps - base.remainingVelocityMps else null,
            issues = (baselineResult.issues + alternativeResult.issues).distinct(),
        )
    }
}
