package io.github.black_Kittys22.mortality.Nexus

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Item
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable

class NexusMerger(private val plugin: JavaPlugin) {

    // Liste der Nexus-Keys (ohne "nexus_" Präfix, falls du das willst)
    private val requiredNexusTypes = listOf(
        "nexus_affenreich", "nexus_atlantis", "nexus_berg", "nexus_candyinsel",
        "nexus_enddimension", "nexus_gift", "nexus_glaskuppel", "nexus_himmelsreich",
        "nexus_hoelle", "nexus_mittelalter", "nexus_schokolade", "nexus_unterstadt",
        "nexus_void", "nexus_zeit", "nexus_zukunftsstadt", "nexus_industriepunk"
    )

    init {
        startMergeTask()
    }

    private fun startMergeTask() {
        object : BukkitRunnable() {
            override fun run() {
                // Gruppiere alle Nexus-Items nach ihrem 1x1x1 Block-Standort
                val nexusMap = mutableMapOf<Location, MutableList<Item>>()

                for (world in Bukkit.getWorlds()) {
                    for (entity in world.entities) {
                        if (entity is Item) {
                            val key = entity.itemStack.itemMeta?.itemModel?.key
                            if (key != null && key.startsWith("nexus_") && key != "nexus_combined") {
                                val loc = entity.location.block.location
                                nexusMap.getOrPut(loc) { mutableListOf() }.add(entity)
                            }
                        }
                    }
                }

                // Prüfen, ob eine Gruppe alle benötigten Nexen enthält
                for ((loc, items) in nexusMap) {
                    val foundKeys = items.mapNotNull { it.itemStack.itemMeta?.itemModel?.key }.toSet()

                    if (foundKeys.containsAll(requiredNexusTypes)) {
                        performMerge(loc, items)
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L) // Prüft einmal pro Sekunde
    }

    private fun performMerge(loc: Location, items: List<Item>) {
        items.forEach { it.remove() }

        val world = loc.world ?: return

        // 1. Erstelle das Item (Paper sollte Material.PAPER sein)
        val combinedItem = org.bukkit.inventory.ItemStack(org.bukkit.Material.PAPER)
        val meta = combinedItem.itemMeta

        // 2. Setze das Item-Model als Data Component
        // Da du den Befehl "/give @s minecraft:paper[minecraft:item_model="minecraft:nexus_combined"]" nutzt:
        // Der Key ist "minecraft:item_model", der Wert ist "minecraft:nexus_combined"
        meta.setItemModel(org.bukkit.NamespacedKey.minecraft("nexus_combined"))

        meta.setDisplayName("§Ultra Nexus")
        combinedItem.itemMeta = meta

        // 3. Spawne es
        world.dropItemNaturally(loc, combinedItem)
        world.spawnParticle(org.bukkit.Particle.EXPLOSION, loc, 5)
    }
}