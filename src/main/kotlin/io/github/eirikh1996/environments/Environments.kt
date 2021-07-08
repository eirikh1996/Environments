package io.github.eirikh1996.environments

import io.github.eirikh1996.environments.listener.BlockListener
import io.github.eirikh1996.environments.listener.PlayerListener
import io.github.eirikh1996.environments.listener.WorldListener
import org.bukkit.plugin.java.JavaPlugin

class Environments : JavaPlugin() {

    override fun onEnable() {
        saveDefaultConfig()
        WorldSettings.load()
        server.pluginManager.registerEvents(BlockListener, this)
        server.pluginManager.registerEvents(WorldListener, this)
        server.pluginManager.registerEvents(PlayerListener, this)
        EnvironmentTask.runTaskTimer(this, 0, 1)
    }

    override fun onLoad() {
        instance = this
    }
    companion object {
        lateinit var instance : Environments
    }
}