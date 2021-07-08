package io.github.eirikh1996.environments.listener

import io.github.eirikh1996.environments.WorldSettings
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.weather.ThunderChangeEvent
import org.bukkit.event.weather.WeatherChangeEvent

object WorldListener : Listener {

    @EventHandler
    fun onWeaterChange(event : WeatherChangeEvent) {
        val data = WorldSettings.getWorldData(event.world)
        if (data.rain) {
            return
        }
        if (!event.toWeatherState())
            return
        event.isCancelled = true
    }

    @EventHandler
    fun onThunderChange(event : ThunderChangeEvent) {
        val data = WorldSettings.getWorldData(event.world)
        if (data.rain) {
            return
        }
        if (!event.toThunderState())
            return
        event.isCancelled = true
    }
}