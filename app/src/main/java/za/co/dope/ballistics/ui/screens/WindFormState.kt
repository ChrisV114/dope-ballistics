package za.co.dope.ballistics.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import za.co.dope.ballistics.engine.BearingReference
import za.co.dope.ballistics.engine.WindObservation
import za.co.dope.ballistics.engine.WindSpeedSelection

class WindFormState {
    var windFromDegrees by mutableStateOf("0")
    var directionOfFireDegrees by mutableStateOf("0")
    var clockDirection by mutableStateOf("12")
    var minimumSpeedMps by mutableStateOf("0")
    var averageSpeedMps by mutableStateOf("0")
    var maximumSpeedMps by mutableStateOf("0")
    var gustSpeedMps by mutableStateOf("")
    var magneticDeclinationDegrees by mutableStateOf("")
    var source by mutableStateOf("MANUAL")
    var notes by mutableStateOf("")
    var reference by mutableStateOf(BearingReference.TRUE)
    var selectedSpeed by mutableStateOf(WindSpeedSelection.AVERAGE)
    var locked by mutableStateOf(false)
    var timestampEpochMillis by mutableStateOf(System.currentTimeMillis())

    fun observation(): WindObservation? =
        runCatching {
            WindObservation(
                windFromDegrees = requireNotNull(windFromDegrees.toDoubleOrNull()),
                directionOfFireDegrees = requireNotNull(directionOfFireDegrees.toDoubleOrNull()),
                bearingReference = reference,
                magneticDeclinationDegrees = magneticDeclinationDegrees.toDoubleOrNull(),
                minimumSpeedMps = requireNotNull(minimumSpeedMps.toDoubleOrNull()),
                averageSpeedMps = requireNotNull(averageSpeedMps.toDoubleOrNull()),
                maximumSpeedMps = requireNotNull(maximumSpeedMps.toDoubleOrNull()),
                gustSpeedMps = gustSpeedMps.toDoubleOrNull(),
                selectedSpeed = selectedSpeed,
            )
        }.getOrNull()
}
