package io.github.black_Kittys22.mortality.Nexus

import org.bukkit.Bukkit
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import java.util.UUID

class NexusListener(private val plugin: JavaPlugin) : Listener {

    private val sneakTimers = HashMap<UUID, HashMap<UUID, Int>>()

    init {
        startTimerTask()
    }

    private fun isNexus(item: Item): Boolean {
        val itemStack = item.itemStack
        if (!itemStack.hasItemMeta()) return false

        val itemModelKey = itemStack.itemMeta.itemModel
        return itemModelKey?.key?.startsWith("nexus_") == true
    }

    @EventHandler
    fun onPlayerPickup(event: EntityPickupItemEvent) {
        if (event.entity is Player && isNexus(event.item)) {
            event.isCancelled = true
        }
    }

    private fun startTimerTask() {
        object : BukkitRunnable() {
            var counter = 0

            override fun run() {
                if (counter >= 20) {
                    for (world in Bukkit.getWorlds()) {
                        for (entity in world.entities) {
                            if (entity is Item && isNexus(entity)) {
                                entity.ticksLived = 1
                            }
                        }
                    }
                    counter = 0
                }
                counter++

                // 2. SNEAK-LOGIK (läuft jeden Tick für maximale Präzision)
                for (player in Bukkit.getOnlinePlayers()) {
                    val playerUUID = player.uniqueId

                    if (!player.isSneaking) {
                        sneakTimers.remove(playerUUID)
                        continue
                    }

                    val nearbyItems = player.location.world?.getNearbyEntities(player.location, 1.5, 1.5, 1.5) {
                        it is Item && isNexus(it as Item)
                    }?.filterIsInstance<Item>() ?: emptyList()

                    if (nearbyItems.isEmpty()) {
                        sneakTimers.remove(playerUUID)
                        continue
                    }

                    val playerMap = sneakTimers.getOrPut(playerUUID) { HashMap() }

                    for (item in nearbyItems) {
                        val itemUUID = item.uniqueId
                        val currentTicks = playerMap.getOrDefault(itemUUID, 0) + 1
                        playerMap[itemUUID] = currentTicks

                        // 40 Ticks = exakt 2 Sekunden
                        if (currentTicks >= 40) {
                            val leftover = player.inventory.addItem(item.itemStack)
                            if (leftover.isEmpty()) {
                                item.remove()
                            } else {
                            }
                            playerMap.remove(itemUUID)
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1L)
    }
}