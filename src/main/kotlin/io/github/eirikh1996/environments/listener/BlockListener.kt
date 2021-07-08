package io.github.eirikh1996.environments.listener

import io.github.eirikh1996.environments.WorldSettings
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockFromToEvent
import org.bukkit.event.block.BlockPhysicsEvent

object BlockListener : Listener {

    @EventHandler
    fun onFlow(event : BlockFromToEvent) {
        val wd = WorldSettings.getWorldData(event.block.world)
        if (wd.gravity)
            return
        event.isCancelled = true
    }

    @EventHandler
    fun onPhysics(event : BlockPhysicsEvent) {
        val type = event.block.type
        if (!type.name.endsWith("SAND") && !type.name.endsWith("CONCRETE_POWDER") && type != Material.GRAVEL)
            return
        if (WorldSettings.getWorldData(event.block.world).gravity)
            return
        event.isCancelled = true
    }
}