package io.github.black_Kittys22.mortality.HeartSystem

import io.github.black_Kittys22.mortality.settings.AdminSettings
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

class HeartColorCommand(private val heartSystem: HeartSystem) : CommandExecutor, TabCompleter {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.isOp && !sender.hasPermission("heartsystem.color")) {
            sender.sendMessage(Component.text("Dazu hast du keine Rechte!", NamedTextColor.RED))
            return true
        }

        if (args.isEmpty()) {
            sender.sendMessage(Component.text("Benutzung: /heartcolor <farbe>", NamedTextColor.RED))
            return true
        }

        try {
            val color = AdminSettings.HeartColor.valueOf(args[0].uppercase())
            heartSystem.adminSettings.setHeartColor(color)

            // ActionBars für alle Spieler aktualisieren
            Bukkit.getOnlinePlayers().forEach { player ->
                heartSystem.sendHeartActionBar(player)
            }

            sender.sendMessage(Component.text("Herzfarbe wurde auf ", NamedTextColor.GREEN)
                .append(Component.text(color.name.lowercase(), NamedTextColor.GOLD))
                .append(Component.text(" gesetzt!", NamedTextColor.GREEN)))

        } catch (e: IllegalArgumentException) {
            val available = AdminSettings.HeartColor.values().joinToString { it.name.lowercase() }
            sender.sendMessage(Component.text("Ungültige Farbe! Verfügbar: ", NamedTextColor.RED)
                .append(Component.text(available, NamedTextColor.WHITE)))
        }

        return true
    }

    // TabCompleter für automatische Vervollständigung
    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): MutableList<String>? {
        if (args.size == 1) {
            // Gib alle verfügbaren Farben zurück (klein geschrieben)
            return AdminSettings.HeartColor.values()
                .map { it.name.lowercase() }
                .filter { it.startsWith(args[0].lowercase()) }
                .toMutableList()
        }
        return mutableListOf()
    }
}