package io.github.black_Kittys22.mortality.AntiCheat.Blocking

import io.github.black_Kittys22.mortality.Main
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPhysicsEvent
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason
import org.bukkit.event.entity.EntityResurrectEvent
import org.bukkit.event.entity.EntityTransformEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerPortalEvent
import org.bukkit.inventory.meta.ItemMeta

class DenySpawnManager(val plugin: Main) : Listener {

    @EventHandler
    fun onCreatureSpawn(event: CreatureSpawnEvent) {
        if (event.entityType == EntityType.WITHER && event.spawnReason == SpawnReason.BUILD_WITHER) {
            event.isCancelled = true

            val nearbyPlayers = event.location.world?.getNearbyPlayers(event.location, 5.0)
            nearbyPlayers?.forEach { player ->
            }
        }

        if (event.entityType == EntityType.IRON_GOLEM && event.spawnReason == SpawnReason.BUILD_IRONGOLEM) {
            event.isCancelled = true

            val nearbyPlayers = event.location.world?.getNearbyPlayers(event.location, 5.0)
            nearbyPlayers?.forEach { player ->
            }
        }

        if (event.entityType == EntityType.SNOW_GOLEM && event.spawnReason == SpawnReason.BUILD_SNOWMAN) {
            event.isCancelled = true

            val nearbyPlayers = event.location.world?.getNearbyPlayers(event.location, 5.0)
            nearbyPlayers?.forEach { player ->
            }
        }

    }

    // Zombie Villigar
    @EventHandler
    fun onEntityTransform(event: EntityTransformEvent) {
        if (event.entity.type == EntityType.ZOMBIE_VILLAGER) {
            event.isCancelled = true
        }
    }

    // Knockback Verbot
    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val item = event.currentItem ?: return
        val meta = item.itemMeta ?: return

        if (meta.hasEnchants() && meta.enchants.keys.any { it.key.key.toString().contains("knockback")}) {
            val newMeta: ItemMeta = meta
            newMeta.enchants.keys.filter {it.key.key.toString().contains("knockback")}
                .forEach { newMeta.removeEnchant(it) }
            item.itemMeta = newMeta

        }
    }

}

