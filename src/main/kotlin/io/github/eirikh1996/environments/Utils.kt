package io.github.eirikh1996.environments

import org.bukkit.Bukkit.broadcastMessage
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Entity
import org.bukkit.util.BlockVector
import org.bukkit.util.Vector
import java.util.*
import kotlin.collections.HashSet

object Utils {


    fun adjacentToAir(b : Block): Boolean {
        for (shift in SHIFTS) {
            if (b.getRelative(shift.blockX, shift.blockY, shift.blockZ).type == Material.AIR)
                return true
        }
        return false
    }

    val SHIFTS = arrayOf(
        BlockVector(1, 0, 0),
        BlockVector(-1, 0, 0),
        BlockVector(0, 0, 1),
        BlockVector(0, 0, -1)
    )
}