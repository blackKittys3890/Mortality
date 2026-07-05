package io.github.black_Kittys22.mortality.TeamSystem.listener

import io.github.black_Kittys22.mortality.Main
import io.github.black_Kittys22.mortality.language.sendLangError
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent

class TeamChatListener(private val plugin: Main) : Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    fun onChat(event: AsyncPlayerChatEvent) {
        val message = event.message
        if (!message.startsWith("#")) return

        val tm = plugin.teamManager
        val team = tm.getTeamByPlayer(event.player.uniqueId)
        if (team == null) {
            event.isCancelled = true
            event.player.sendLangError("team_not_in")
            return
        }

        event.isCancelled = true
        val text = message.removePrefix("#").trimStart()
        if (text.isEmpty()) {
            event.player.sendLangError("team_chat_empty")
            return
        }

        val formatted = "§8[§6Mortality§8] ${team.colorCode}[${team.displayName}]§r §7${event.player.name}§8: §f${text}"
        team.members.forEach { uuid ->
            Bukkit.getPlayer(uuid)?.sendMessage(formatted)
        }
        plugin.logger.info("[Teamchat] [${team.name}] ${event.player.name}: $text")
    }
}