package io.github.black_Kittys22.mortality.TeamSystem.command

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.arguments.GreedyStringArgument
import dev.jorel.commandapi.executors.PlayerCommandExecutor
import io.github.black_Kittys22.mortality.Main
import io.github.black_Kittys22.mortality.language.sendLangError
import org.bukkit.Bukkit

object TeamChatCommand {

    fun register(plugin: Main) {
        val tm = plugin.teamManager

        fun formatMessage(senderName: String, colorCode: String, displayName: String, message: String): String {
            return "-> $colorCode[${displayName}]§r §7${senderName}§8: §f${message}"
        }

        CommandAPICommand("tc")
            .withArguments(GreedyStringArgument("nachricht"))
            .executesPlayer(PlayerCommandExecutor { player, args ->
                val team = tm.getTeamByPlayer(player.uniqueId)
                if (team == null) {
                    player.sendLangError("team_not_in")
                    return@PlayerCommandExecutor
                }
                val message = args[0] as String
                val formatted = formatMessage(player.name, team.colorCode, team.displayName, message)
                team.members.forEach { uuid ->
                    Bukkit.getPlayer(uuid)?.sendMessage(formatted)
                }
                plugin.logger.info("[Teamchat] [${team.name}] ${player.name}: $message")
            })
            .register(plugin)
    }
}