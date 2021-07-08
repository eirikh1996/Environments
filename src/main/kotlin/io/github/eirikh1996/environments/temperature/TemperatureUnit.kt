package io.github.eirikh1996.environments.temperature

enum class TemperatureUnit constructor(symbol : String) {
    FAHRENHEIT("°F") {
        override fun fromMinecraftTemp(mcTemp: Double) : Double {
            return (24.44 * mcTemp) + 44.28
        }
    },
    CELSIUS("°C") {
        override fun fromMinecraftTemp(mcTemp: Double) : Double {
            return (13.65 * mcTemp) + 7.1
        }

    },
    KELVIN("K") {
        override fun fromMinecraftTemp(mcTemp: Double): Double {
            return CELSIUS.fromMinecraftTemp(mcTemp) + 273.15
        }
    },
    RANKINE("°R") {
        override fun fromMinecraftTemp(mcTemp: Double): Double {
            return FAHRENHEIT.fromMinecraftTemp(mcTemp) + 459.67
        }
    };

    abstract fun fromMinecraftTemp(mcTemp : Double) : Double

}