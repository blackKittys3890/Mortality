package io.github.black_Kittys22.mortality.HeartSystem

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

class AdminSettingsGUI(private val heartSystem: HeartSystem) : Listener {

    private val title = Component.text("Admin Settings - Herzen", NamedTextColor.DARK_AQUA)

    fun openGUI(player: Player) {
        // Erstellt ein 3-reihiges Chest-Inventar
        val inv = Bukkit.createInventory(null, 9 * 3, title)

        // Grauer Glas-Hintergrund für die Optik
        val grayGlass = ItemStack(Material.GRAY_STAINED_GLASS_PANE).apply {
            itemMeta = itemMeta?.apply { displayName(Component.text(" ")) }
        }
        for (i in 0 until inv.size) {
            inv.setItem(i, grayGlass)
        }

        // -1 Herz Button (Rote Wolle)
        val minusHeart = ItemStack(Material.RED_WOOL).apply {
            itemMeta = itemMeta?.apply { displayName(Component.text("-1 Max Herz", NamedTextColor.RED)) }
        }
        inv.setItem(11, minusHeart)

        // Info-Item in der Mitte (Herz des Meeres)
        val infoItem = ItemStack(Material.HEART_OF_TH_SEA).apply {
            itemMeta = itemMeta?.apply {
                displayName(Component.text("Aktuelles Limit: ${heartSystem.adminSettings.maxHeartsSetting}", NamedTextColor.GOLD))
                lore(listOf(Component.text("Bestimmt das Standard-Limit für neue Setzungen", NamedTextColor.GRAY)))
            }
        }
        inv.setItem(13, infoItem)

        // +1 Herz Button (Hellgrüne Wolle)
        val plusHeart = ItemStack(Material.LIME_WOOL).apply {
            itemMeta = itemMeta?.apply { displayName(Component.text("+1 Max Herz", NamedTextColor.GREEN)) }
        }
        inv.setItem(15, plusHeart)

        player.openInventory(inv)
    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        if (event.view.title() != title) return
        event.isCancelled = true // Items können nicht aus dem Inventar genommen werden

        val player = event.whoClicked as? Player ?: return
        val slot = event.rawSlot

        when (slot) {
            11 -> { // Klick auf -1 Herz
                if (heartSystem.adminSettings.maxHeartsSetting > 1) {
                    heartSystem.adminSettings.maxHeartsSetting--
                    openGUI(player) // Aktualisiert die Anzeige im GUI direkt
                }
            }
            15 -> { // Klick auf +1 Herz
                heartSystem.adminSettings.maxHeartsSetting++
                openGUI(player) // Aktualisiert die Anzeige im GUI direkt
            }
        }
    }
}