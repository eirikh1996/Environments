package io.github.eirikh1996.environments

import io.github.eirikh1996.environments.listener.PlayerListener
import io.github.eirikh1996.environments.temperature.TemperatureUnit
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.World
import org.bukkit.entity.Entity
import org.bukkit.entity.FallingBlock
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Scoreboard
import org.bukkit.util.BlockVector
import org.bukkit.util.Vector
import java.lang.System.currentTimeMillis
import java.math.MathContext
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.HashSet
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max

object EnvironmentTask : BukkitRunnable() {
    private var lastSuffocationCheck = 0L
    private var lastGravityCheck = 0L
    private var lastTemperatureCheck = 0L
    private var gravityCheckProcessing = false
    internal val indoorSpaceCache = HashMap<Entity, Set<BlockVector>>()
    internal val playerTemperatures = HashMap<UUID, Double>()
    private val playerTemperatureUnits = HashMap<UUID, TemperatureUnit>()
    private val playerScoreboard = HashMap<UUID, Scoreboard>()
    override fun run() {
        processVaccuumSuffocation()
        processEntityGravity()
        applyStatusEffects()
        processTemperature()
        sendEnvironmentData()
    }
    fun sendEnvironmentData() {
        for (p in Bukkit.getOnlinePlayers()) {
            val scoreboard = Bukkit.getScoreboardManager()!!.newScoreboard

            val currentTemp = playerTemperatures.getOrDefault(p.uniqueId, 37.0)
            val objective = scoreboard.registerNewObjective("Temperature", "dummy","Temperature")
            objective.displaySlot = DisplaySlot.SIDEBAR
            objective.getScore(String.format("Environment temperature: %.2f", TemperatureUnit.CELSIUS.fromMinecraftTemp(p.location.block.temperature))).score = 1
            objective.getScore(String.format("Body temperature: %.2f", currentTemp)).score = 2
            p.scoreboard = scoreboard

        }
    }

    fun processTemperature() {
        val ticksSince = (((currentTimeMillis() - lastTemperatureCheck) / 1000) * 20).toInt()
        if (ticksSince < Settings.TemperatureTickCooldown)
        for (p in Bukkit.getOnlinePlayers()) {
            if (p.gameMode != GameMode.SURVIVAL)
                continue
            var currentTemp = playerTemperatures.getOrDefault(p.uniqueId, 37.0)
            val packageName = Bukkit.getServer()::class.java.`package`.name
            val version = packageName.substring(packageName.lastIndexOf(".") + 1)
            val pLoc = p.location
            var locTemp = pLoc.block.temperature
            //Do nothing if body temperature remains



            if (currentTemp < 35.0) {
                p.addPotionEffect(PotionEffect(PotionEffectType.CONFUSION, 10, max(currentTemp % 2.0, 1.0).toInt()))
            }
            if (currentTemp < 30.0) {
                p.addPotionEffect(PotionEffect(PotionEffectType.SLOW, 10, max(currentTemp % 2.0, 1.0).toInt()))
            }
            if (currentTemp < 25.0) {
                p.damage(25.0 - currentTemp)
            }
        }
    }

    fun processVaccuumSuffocation() {
        val ticksSince = (((currentTimeMillis() - lastSuffocationCheck) / 1000) * 20).toInt()
        if (ticksSince < Settings.SuffocationTickCooldown)
            return
        for (w in Bukkit.getWorlds()) {
            val worldData = WorldSettings.getWorldData(w)
            if (worldData.oxygen) {
                continue
            }
            for (e in w.entities) {
                if (e !is LivingEntity)
                    continue
                if (isInsideStructure(e))
                    continue
                if (e is Player) {
                    if (e.gameMode != GameMode.SURVIVAL)
                        continue
                    if (worldData.requiredArmor.any { armorType -> e.inventory.armorContents.any { item -> item != null && item.type == armorType } }){
                        continue
                    }
                    if (e.health <= Settings.SuffocationDamage)
                        PlayerListener.suffocatedPlayers.add(e)
                }
                e.damage(Settings.SuffocationDamage)
            }

        }
        lastSuffocationCheck = currentTimeMillis()
    }

    fun processEntityGravity() {
        val ticksSince = (((currentTimeMillis() - lastGravityCheck) / 1000) * 20).toInt()
        if (ticksSince < Settings.GravityTickCooldown)
            return
        if (gravityCheckProcessing)
            return
        gravityCheckProcessing = true
        for (w in Bukkit.getWorlds()) {
            val worldData = WorldSettings.getWorldData(w)
            if (worldData.gravity) {
                for (e in w.entities) {
                    if (e.hasGravity())
                        continue
                    e.setGravity(true)

                }
                continue
            }
            for (e in w.entities) {
                if ( isInsideStructure(e) && !e.hasGravity()) {
                    e.setGravity(true)
                    continue
                }
                if (!e.hasGravity())
                    continue

                e.setGravity(false)
                if (e is FallingBlock)
                    e.velocity = Vector(0,0,0)
            }
        }
        lastGravityCheck = currentTimeMillis()
        gravityCheckProcessing = false
    }

    private fun applyStatusEffects() {
        for (w in Bukkit.getWorlds()) {
            val worldData = WorldSettings.getWorldData(w)
            val worldTime = w.fullTime
            for (e in w.getEntitiesByClass(LivingEntity::class.java)) {
                for (pe in worldData.statusEffects)
                    e.removePotionEffect(pe.type)
                e.addPotionEffects(worldData.statusEffects)
            }
        }
    }

    private fun spaceChanged(space : Collection<BlockVector>, world: World) : Boolean {
        for (loc in space) {
            for (shift in SHIFTS) {
                val test = loc.clone().add(shift)
                if (space.contains(test))
                    continue
                val block = test.toLocation(world).block
                if (block.isEmpty)
                    return true
            }
        }
        return false
    }

    internal fun isInsideStructure(e : Entity) : Boolean {
        val eLoc = e.location
        val stack = Stack<BlockVector>()
        stack.push(eLoc.toVector().toBlockVector().clone())
        if (indoorSpaceCache.containsKey(e) && !spaceChanged(indoorSpaceCache[e]!!, e.world) && indoorSpaceCache[e]!!.contains(e.location.toVector().toBlockVector())) {
            return true
        }
        val visited = HashSet<BlockVector>()
        do {
            val node = stack.pop()
            if (visited.contains(node))
                continue
            visited.add(node)
            if (visited.size > Settings.MaxSpaceSize) {
                return false
            }
            for (shift in SHIFTS) {
                val test = node.clone().add(shift).toBlockVector()
                if (!test.toLocation(e.world).block.isEmpty)
                    continue
                stack.push(test)
            }
        } while (!stack.isEmpty())
        indoorSpaceCache.put(e, visited)
        return visited.contains(eLoc.toVector().toBlockVector())
    }

    private val SHIFTS = arrayOf(
            BlockVector(0,1,0),
            BlockVector(0,-1,0),
            BlockVector(1, 0, 0),
            BlockVector(-1, 0, 0),
            BlockVector(0, 0, 1),
            BlockVector(0, 0, -1)
    )
}