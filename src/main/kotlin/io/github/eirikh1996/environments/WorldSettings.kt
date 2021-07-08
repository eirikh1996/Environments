package io.github.eirikh1996.environments

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.FileInputStream

object WorldSettings : Iterable<Map.Entry<World, WorldSettings.WorldData>> {
    val worldDataMap = HashMap<World, WorldData>()

    fun getWorldData(world: World) : WorldData {
        return worldDataMap.getOrDefault(world, WorldData(true, true, true))
    }

    data class WorldData(val oxygen : Boolean = true,
                         val gravity : Boolean = true,
                         val rain : Boolean = true,
                         val radiationLevel : Double = 0.0,
                         val statusEffects : Set<PotionEffect> = HashSet(),
                         val requiredArmor : Set<Material> = HashSet()) : ConfigurationSerializable {
        override fun serialize(): MutableMap<String, Any> {
            val data = HashMap<String, Any>()
            data.put("oxygen", oxygen)
            data.put("gravity", gravity)
            data.put("rain", rain)
            data.put("radiationLevel", radiationLevel)
            val statusEffects = ArrayList<String>()
            this.statusEffects.forEach({ se -> statusEffects.add(se.type.name + ":" + se.amplifier)})
            val requiredArmor = ArrayList<String>()
            this.requiredArmor.forEach({ ra -> requiredArmor.add(ra.name)})
            data.put("statusEffects", statusEffects)
            data.put("requiredArmor", requiredArmor)
            return data
        }

        companion object {
            @JvmStatic fun deserialize(data : Map<String, Any>) : WorldData {
                return WorldData(
                    data.getOrDefault("oxygen", true) as Boolean,
                    data.getOrDefault("gravity", true) as Boolean,
                    data.getOrDefault("rain", true) as Boolean,
                    data.getOrDefault("radiationLevel", 0.0) as Double,
                    potionEffectsFromObject(data.getOrDefault("statusEffects", ArrayList<String>())),
                    itemListFromObject(data.getOrDefault("requiredArmor", ArrayList<String>()))
                    )
            }
        }

    }

    fun load() {
        val wsFile = File(Environments.instance.dataFolder, "worldsettings.yml")
        if (!wsFile.exists())
            Environments.instance.saveResource("worldsettings.yml", false)
        val worldSettings = YamlConfiguration()
        worldSettings.load(wsFile)
        for (world in Bukkit.getWorlds()) {
            worldDataMap.put(world, worldSettings.getSerializable(world.name, WorldData::class.java, WorldData())!!)
        }
    }

    fun potionEffectsFromObject (o : Any?) : Set<PotionEffect> {
        val effects = HashSet<PotionEffect>()
        if (o is List<*>) {
            for (i in o) {
                if (i !is String)
                    continue
                val type : PotionEffectType
                val multiplier : Int
                if (i.contains(":")) {
                    val parts = i.split(":")
                    type = PotionEffectType.getByName(parts[0])!!
                    multiplier = parts[1].toInt()
                } else {
                    type = PotionEffectType.getByName(i)!!
                    multiplier = 1
                }
                effects.add(PotionEffect(type, 100, multiplier))


            }

        }
        return effects
    }

    fun itemListFromObject(o : Any?) : Set<Material> {
        val items = HashSet<Material>()
        if (o is List<*>) {
            for (i in o) {
                if (i is String) {
                    items.add(Material.getMaterial(i)!!)
                }
            }
        }
        return items
    }

    override fun iterator(): Iterator<Map.Entry<World, WorldData>> {
        return worldDataMap.entries.iterator()
    }
}