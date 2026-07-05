package io.github.black_Kittys22.mortality.Mention.settings

import io.github.black_Kittys22.mortality.Main
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class MentionSettingsCommand(
    private val plugin: Main,
    private val gui: MentionSettingsGUI
) : CommandExecutor {

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        if (sender !is Player) {
            sender.sendMessage("Nur für Spieler!")
            return true
        }
        gui.openGUI(sender)
        return true
    }
}