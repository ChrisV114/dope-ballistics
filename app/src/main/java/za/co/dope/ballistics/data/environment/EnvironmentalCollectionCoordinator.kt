package za.co.dope.ballistics.data.environment

import za.co.dope.ballistics.domain.environment.EnvironmentalMath
import za.co.dope.ballistics.domain.environment.EnvironmentalSourcePolicy
import za.co.dope.ballistics.domain.environment.SourcedReading

data class EnvironmentalSelection(
    val temperatureKelvin: SourcedReading,
    val stationPressurePascals: SourcedReading,
    val relativeHumidityFraction: SourcedReading,
    val altitudeMetres: SourcedReading,
)

class EnvironmentalCollectionCoordinator {
    fun select(
        temperatures: List<SourcedReading>,
        pressures: List<SourcedReading>,
        humidities: List<SourcedReading>,
        altitudes: List<SourcedReading>,
    ): EnvironmentalSelection =
        EnvironmentalSelection(
            temperatureKelvin =
                requireNotNull(EnvironmentalSourcePolicy.chooseAmbient(temperatures)) {
                    "Temperature is required"
                },
            stationPressurePascals =
                requireNotNull(EnvironmentalSourcePolicy.choosePressure(pressures)) {
                    "Station pressure is required"
                },
            relativeHumidityFraction =
                requireNotNull(EnvironmentalSourcePolicy.chooseAmbient(humidities)) {
                    "Humidity is required"
                },
            altitudeMetres =
                requireNotNull(EnvironmentalSourcePolicy.chooseAltitude(altitudes)) {
                    "Altitude is required"
                },
        )

    fun calculate(selection: EnvironmentalSelection) =
        EnvironmentalMath.calculate(
            temperatureKelvin = selection.temperatureKelvin.value,
            stationPressurePascals = selection.stationPressurePascals.value,
            relativeHumidityFraction = selection.relativeHumidityFraction.value,
        )
}
