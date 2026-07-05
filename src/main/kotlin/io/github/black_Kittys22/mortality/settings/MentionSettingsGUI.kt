package io.github.black_Kittys22.mortality.Mention.settings

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import io.github.black_Kittys22.mortality.language.sendLangSuccess
import io.github.black_Kittys22.mortality.language.sendLangError

class MentionSettingsGUI(private val settings: MentionSettings) : Listener {

    private val guiTitle = Component.text("Mention Einstellungen", NamedTextColor.DARK_PURPLE)

    fun openGUI(player: Player) {
        val inv = Bukkit.createInventory(null, 27, guiTitle)
        val filler = ItemStack(Material.GRAY_STAINED_GLASS_PANE).apply {
            editMeta { it.displayName(Component.empty()) }
        }
        for (i in 0 until 27) inv.setItem(i, filler)
        inv.setItem(13, buildSoundItem(player))
        player.openInventory(inv)
    }

    private fun buildSoundItem(player: Player): ItemStack {
        val enabled = settings.isSoundEnabled(player.uniqueId)
        val material = if (enabled) Material.BELL else Material.DEAD_BUSH
        val statusText = if (enabled) "§a✔ Aktiviert" else "§c✘ Deaktiviert"
        val toggleHint = if (enabled) "§7Klicken zum Deaktivieren" else "§7Klicken zum Aktivieren"

        return ItemStack(material).apply {
            editMeta { meta ->
                meta.displayName(
                    Component.text("Mention Sound", NamedTextColor.YELLOW)
                        .decoration(TextDecoration.ITALIC, false)
                        .decoration(TextDecoration.BOLD, true)
                )
                meta.lore(listOf(
                    Component.text("Status: $statusText").decoration(TextDecoration.ITALIC, false),
                    Component.text(toggleHint).decoration(TextDecoration.ITALIC, false),
                    Component.empty(),
                    Component.text("Spielt einen Sound wenn du").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
                    Component.text("im Chat erwähnt wirst.").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
                ))
            }
        }
    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        if (event.view.title() != guiTitle) return
        event.isCancelled = true
        val player = event.whoClicked as? Player ?: return
        if (event.slot != 13) return
        settings.toggleSound(player.uniqueId)
        event.inventory.setItem(13, buildSoundItem(player))

        if (settings.isSoundEnabled(player.uniqueId)) {
            player.sendLangSuccess("mention_sound_enabled")
            player.playSound(player.location, org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.5f)
        } else {
            player.sendLangError("mention_sound_disabled")
        }
    }
}