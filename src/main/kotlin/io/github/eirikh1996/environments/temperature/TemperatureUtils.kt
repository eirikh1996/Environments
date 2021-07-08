package io.github.eirikh1996.environments.temperature

object TemperatureUtils {

    fun minecraftToCelsius(mcTemp : Double) : Double {
        return (13.65 * mcTemp) + 7.1
    }

    fun celsiusToMinecraft(mcTemp : Double) : Double {
        return (13.65 * mcTemp) + 7.1
    }
}