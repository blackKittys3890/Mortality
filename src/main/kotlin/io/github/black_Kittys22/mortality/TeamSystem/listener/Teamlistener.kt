package io.github.black_Kittys22.mortality.TeamSystem.listener

import io.github.black_Kittys22.mortality.Main
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.scoreboard.Scoreboard
import org.bukkit.scoreboard.Team as ScoreboardTeam
import java.util.UUID

class Teamlistener(private val plugin: Main) : Listener {

    private val scoreboard: Scoreboard = Bukkit.getScoreboardManager().mainScoreboard
    private val legacy = LegacyComponentSerializer.legacySection()

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        Bukkit.getScheduler().runTaskLater(plugin, Runnable {
            updateTablist()
        }, 1L)
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        Bukkit.getScheduler().runTaskLater(plugin, Runnable {
            updateTablist()
        }, 1L)
    }

    fun updateTablist() {
        val tm = plugin.teamManager

        scoreboard.teams
            .filter { it.name.startsWith("tp_") }
            .forEach { it.unregister() }

        tm.getAllTeams().forEach { team ->
            val sbTeamName = "tp_${team.name}".take(16)
            val sbTeam: ScoreboardTeam = scoreboard.registerNewTeam(sbTeamName)
            sbTeam.prefix(legacy.deserialize("${team.colorCode}[${team.displayName}] "))
            sbTeam.color(NamedTextColor.GRAY)
            team.members.forEach { uuid: UUID ->
                val player = Bukkit.getPlayer(uuid)
                if (player != null && player.isOnline) {
                    sbTeam.addEntry(player.name)
                }
            }
        }
    }
}