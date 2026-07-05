package io.github.black_Kittys22.mortality.HeartSystem

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

// HIER NEU: implements Listener UND CommandExecutor
class AdminSettingsGUI(private val heartSystem: HeartSystem) : Listener, CommandExecutor {

    private val title = Component.text("Admin Settings - Herzen", NamedTextColor.DARK_AQUA)

    // Diese Methode wird aufgerufen, wenn jemand /adminsettings eingibt
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender is Player) {
            if (sender.hasPermission("mortality.admin")) {
                openGUI(sender)
            } else {
                sender.sendMessage("§cDu hast dazu keine Rechte!")
            }
        } else {
            sender.sendMessage("Dieser Befehl kann nur von Spielern ausgeführt werden!")
        }
        return true
    }

    fun openGUI(player: Player) {
        val inv = Bukkit.createInventory(null, 9 * 3, title)

        val grayGlass = ItemStack(Material.GRAY_STAINED_GLASS_PANE).apply {
            val meta = itemMeta
            meta?.displayName(Component.text(" "))
            itemMeta = meta
        }
        for (i in 0 until inv.size) {
            inv.setItem(i, grayGlass)
        }

        val minusHeart = ItemStack(Material.RED_WOOL).apply {
            val meta = itemMeta
            meta?.displayName(Component.text("-1 Max Herz", NamedTextColor.RED))
            itemMeta = meta
        }
        inv.setItem(11, minusHeart)

        val infoItem = ItemStack(Material.HEART_OF_THE_SEA).apply {
            val meta = itemMeta
            meta?.displayName(Component.text("Aktuelles Limit: ${heartSystem.adminSettings.maxHeartsSetting}", NamedTextColor.GOLD))
            meta?.lore(listOf(Component.text("Bestimmt das Standard-Limit für neue Setzungen", NamedTextColor.GRAY)))
            itemMeta = meta
        }
        inv.setItem(13, infoItem)

        val plusHeart = ItemStack(Material.LIME_WOOL).apply {
            val meta = itemMeta
            meta?.displayName(Component.text("+1 Max Herz", NamedTextColor.GREEN))
            itemMeta = meta
        }
        inv.setItem(15, plusHeart)

        player.openInventory(inv)
    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        if (event.view.title() != title) return
        event.isCancelled = true

        val player = event.whoClicked as? Player ?: return
        val slot = event.rawSlot

        when (slot) {
            11 -> {
                if (heartSystem.adminSettings.maxHeartsSetting > 1) {
                    heartSystem.adminSettings.maxHeartsSetting--
                    openGUI(player)
                }
            }
            15 -> {
                heartSystem.adminSettings.maxHeartsSetting++
                openGUI(player)
            }
        }
    }
}