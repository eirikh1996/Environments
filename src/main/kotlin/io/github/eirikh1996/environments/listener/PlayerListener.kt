package io.github.eirikh1996.environments.listener

import io.github.eirikh1996.environments.EnvironmentTask
import io.github.eirikh1996.environments.WorldSettings
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerToggleFlightEvent

object PlayerListener : Listener {
    val suffocatedPlayers = ArrayList<Player>()
    val hypothermiaPlayers = ArrayList<Player>()
    val hyperthermiaPlayers = ArrayList<Player>()

    @EventHandler
    fun onDeath(event : PlayerDeathEvent) {
        if (suffocatedPlayers.contains(event.entity)) {
            event.deathMessage = event.entity.name + " suffocated due to lack of oxygen"
            suffocatedPlayers.remove(event.entity)
        }
        if (hypothermiaPlayers.contains(event.entity)) {
            event.deathMessage = event.entity.name + " froze to death"
            hypothermiaPlayers.remove(event.entity)
        }
        if (hyperthermiaPlayers.contains(event.entity)) {
            event.deathMessage = event.entity.name + " died from a heat stroke"
            hyperthermiaPlayers.remove(event.entity)
        }
    }

    @EventHandler
    fun onFlightToggle(event : PlayerToggleFlightEvent) {
        if (event.isFlying)
            return
        if (WorldSettings.getWorldData(event.player.world).gravity)
            return
        if (EnvironmentTask.isInsideStructure(event.player))
            return
        event.isCancelled = true
    }

    @EventHandler
    fun onJoin(event : PlayerJoinEvent) {
        if (EnvironmentTask.playerTemperatures.containsKey(event.player.uniqueId))
            return
        EnvironmentTask.playerTemperatures.put(event.player.uniqueId, 37.0)
    }
}