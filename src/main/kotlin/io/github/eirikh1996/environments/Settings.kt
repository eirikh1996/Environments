package io.github.eirikh1996.environments

import io.github.eirikh1996.environments.temperature.TemperatureUnit

object Settings {
    val TemperatureTickCooldown : Int get() = config.getInt("TemperatureTickCooldown", 100)
    private val config = Environments.instance.config
    val SuffocationDamage : Double get() = config.getDouble("SuffocationDamage", 2.0)
    val SuffocationTickCooldown : Int get() = config.getInt("SuffocationTickCooldown", 100)
    val GravityTickCooldown : Int get() = config.getInt("GravityTickCooldown", 120)
    val MaxSpaceSize : Int get() = config.getInt("MaxSpaceSize", 500)
    val UseTemperatureUnit : TemperatureUnit get() = TemperatureUnit.valueOf(config.getString("useTemperatureUnit", "Celsius")!!.toUpperCase())
}