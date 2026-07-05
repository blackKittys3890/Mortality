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



    // Netherite Verbot
    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        val player = event.player
        val loc = player.location
        val world = player.world
        for (x in -3..3) {
            for (y in -3..3) {
                for (z in -3..3) {
                    val block = world.getBlockAt(loc.blockX + x, loc.blockY + y, loc.blockZ + z)
                    if (block.type == Material.ANCIENT_DEBRIS ||
                        block.type == Material.NETHERITE_BLOCK) {

                        block.type = Material.NETHERRACK
                    }
                }
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

    // Elytra Verbot
    @EventHandler
    fun onElytraEquip(event: PlayerItemHeldEvent) {
        val item = event.player.inventory.getItem(event.newSlot)
        if (item != null && item.type == Material.ELYTRA) {
            event.player.inventory.setItem(event.newSlot, null)
        }
    }

    // Mace Verbot
    @EventHandler
    fun onItemHeld(event: PlayerItemHeldEvent) {
        val player = event.player
        val item = player.inventory.getItem(event.newSlot)
        if (item != null && item.type.name.equals("MACE", ignoreCase = true)) {
            player.inventory.remove(item)
        }
    }

    // Totem Verbot
    @EventHandler
    fun onTotemUse(event: EntityResurrectEvent) {
        if (event.entityType == EntityType.PLAYER) {
            val player = event.entity
            event.isCancelled = true
        }
    }

    // Shulker Verbot
    @EventHandler
    fun onShulkerOpen(event: PlayerInteractEvent) {
        if (event.action == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
            val block = event.clickedBlock
            if (block != null && block.type.name.contains("SHULKER_BOX")) {
                event.isCancelled = true
            }
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

    // Portal Verbot 1
    @EventHandler
    fun onPortalUse(event: PlayerPortalEvent) {
        event.isCancelled = true
    }

    // Portal Verbot 2
    @EventHandler
    fun onPortalCreate(event: BlockPhysicsEvent) {
        if (event.block.type == Material.NETHER_PORTAL || event.block.type == Material.END_PORTAL)
            event.isCancelled = true
    }


}

