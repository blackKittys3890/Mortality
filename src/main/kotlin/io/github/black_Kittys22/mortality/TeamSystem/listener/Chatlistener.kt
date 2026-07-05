package io.github.black_Kittys22.mortality.TeamSystem.listener

import io.github.black_Kittys22.mortality.Main
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent

class ChatListener(private val plugin: Main) : Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    fun onChat(event: AsyncPlayerChatEvent) {
        if (event.isCancelled) return

        val player = event.player
        val team = plugin.teamManager.getTeamByPlayer(player.uniqueId)

        val prefix = if (team != null) {
            "${team.colorCode}[${team.displayName}]§r "
        } else {
            plugin.languageManager.getColoredMessage(player, "team_not_in") + " "
        }

        event.format = "$prefix§7%s§8: §f%s"
    }
}