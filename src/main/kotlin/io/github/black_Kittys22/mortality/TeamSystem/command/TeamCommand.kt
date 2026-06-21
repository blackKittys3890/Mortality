package io.github.black_Kittys22.mortality.TeamSystem.command

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.executors.PlayerCommandExecutor
import io.github.black_Kittys22.mortality.Main
import io.github.black_Kittys22.mortality.TeamSystem.model.Team
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.UUID

object TeamCommand {

    fun register(plugin: Main) {
        val tm = plugin.teamManager
        val im = plugin.inviteManager

        val legacy = LegacyComponentSerializer.legacySection()
        fun Player.msg(text: String) = sendMessage(legacy.deserialize("§8[§6Mortality§8] §r$text"))
        fun Player.err(text: String) = sendMessage(legacy.deserialize("§8[§6Mortality§8] §c$text"))

        val createCmd = CommandAPICommand("create")
            .withArguments(
                StringArgument("name"),
                StringArgument("anzeigename"),
                StringArgument("farbe").replaceSuggestions(
                    ArgumentSuggestions.strings(Team.AVAILABLE_COLORS.keys.toList())
                )
            )
            .executesPlayer(PlayerCommandExecutor { player, args ->
                if (tm.getTeamByPlayer(player.uniqueId) != null) {
                    player.err("Du bist bereits in einem Team!")
                    return@PlayerCommandExecutor
                }
                val name        = args[0] as String
                val displayName = args[1] as String
                val farbName    = (args[2] as String).lowercase()
                val colorCode   = Team.AVAILABLE_COLORS[farbName]
                if (colorCode == null) {
                    player.err("Unbekannte Farbe! Verfügbar: ${Team.AVAILABLE_COLORS.keys.joinToString(", ")}")
                    return@PlayerCommandExecutor
                }
                if (!name.matches(Regex("[a-zA-Z0-9_]{3,16}"))) {
                    player.err("Teamname: 3–16 Zeichen, nur Buchstaben/Zahlen/Unterstrich.")
                    return@PlayerCommandExecutor
                }
                if (tm.createTeam(name, displayName, colorCode, player.uniqueId)) {
                    player.msg("Team §6$displayName §rwurde erstellt! Farbe: ${colorCode}■§r")
                    plugin.teamListener.updateTablist()
                } else {
                    player.err("Ein Team mit diesem Namen existiert bereits.")
                }
            })

        val inviteCmd = CommandAPICommand("invite")
            .withArguments(
                StringArgument("spieler").replaceSuggestions(
                    ArgumentSuggestions.strings { _ ->
                        Bukkit.getOnlinePlayers().map { it.name }.toTypedArray()
                    }
                )
            )
            .executesPlayer(PlayerCommandExecutor { player, args ->
                val team = tm.getTeamByPlayer(player.uniqueId)
                if (team == null) { player.err("Du bist in keinem Team."); return@PlayerCommandExecutor }
                if (!team.isLeader(player.uniqueId)) { player.err("Nur der Team-Admin kann einladen."); return@PlayerCommandExecutor }
                val target = Bukkit.getPlayerExact(args[0] as String)
                if (target == null) { player.err("Spieler nicht online."); return@PlayerCommandExecutor }
                if (target == player) { player.err("Du kannst dich nicht selbst einladen."); return@PlayerCommandExecutor }
                if (tm.getTeamByPlayer(target.uniqueId) != null) {
                    player.err("§e${target.name} §cist bereits in einem Team.")
                    return@PlayerCommandExecutor
                }
                im.sendInvite(target.uniqueId, team.name)
                player.msg("Einladung an §e${target.name} §rgeschickt.")
                target.msg("§e${player.name} §lädt dich ins Team §6${team.coloredName()} §rein!")
                target.msg("Tippe §a/team accept §rum beizutreten oder §c/team deny §rum abzulehnen.")
            })

        val acceptCmd = CommandAPICommand("accept")
            .executesPlayer(PlayerCommandExecutor { player, _ ->
                val teamName = im.acceptInvite(player.uniqueId)
                if (teamName == null) { player.err("Du hast keine offene Einladung."); return@PlayerCommandExecutor }
                if (tm.getTeamByPlayer(player.uniqueId) != null) {
                    player.err("Du bist bereits in einem Team.")
                    return@PlayerCommandExecutor
                }
                if (tm.joinTeam(player.uniqueId, teamName)) {
                    val team = tm.getTeamByName(teamName)!!
                    player.msg("Du bist dem Team §6${team.coloredName()} §rbeigetreten!")
                    team.members.forEach { uuid: UUID ->
                        Bukkit.getPlayer(uuid)?.takeIf { it != player }
                            ?.msg("§e${player.name} §rhat das Team betreten.")
                    }
                    plugin.teamListener.updateTablist()
                } else {
                    player.err("Team nicht gefunden.")
                }
            })

        val denyCmd = CommandAPICommand("deny")
            .executesPlayer(PlayerCommandExecutor { player, _ ->
                if (!im.hasInvite(player.uniqueId)) {
                    player.err("Du hast keine offene Einladung.")
                    return@PlayerCommandExecutor
                }
                val teamName = im.acceptInvite(player.uniqueId)
                player.msg("Einladung zu §6$teamName §rabgelehnt.")
            })

        val leaveCmd = CommandAPICommand("leave")
            .executesPlayer(PlayerCommandExecutor { player, _ ->
                val team = tm.getTeamByPlayer(player.uniqueId)
                if (team == null) { player.err("Du bist in keinem Team."); return@PlayerCommandExecutor }
                if (team.isLeader(player.uniqueId) && team.members.size > 1) {
                    player.err("Du bist der Leader. Löse das Team mit §c/team disband §cauf.")
                    return@PlayerCommandExecutor
                }
                val teamName = team.coloredName()
                tm.leaveTeam(player.uniqueId)
                player.msg("Du hast Team §6$teamName §rverlassen.")
                team.members.forEach { uuid: UUID ->
                    Bukkit.getPlayer(uuid)?.msg("§e${player.name} §rhat das Team verlassen.")
                }
                plugin.teamListener.updateTablist()
            })

        val infoCmd = CommandAPICommand("info")
            .withOptionalArguments(
                StringArgument("teamname").replaceSuggestions(
                    ArgumentSuggestions.strings { _ ->
                        tm.getAllTeams().map { it.name }.toTypedArray()
                    }
                )
            )
            .executesPlayer(PlayerCommandExecutor { player, args ->
                val team = if (args.count() > 0) {
                    tm.getTeamByName(args[0] as String)
                        ?: run { player.err("Team nicht gefunden."); return@PlayerCommandExecutor }
                } else {
                    tm.getTeamByPlayer(player.uniqueId)
                        ?: run { player.err("Du bist in keinem Team."); return@PlayerCommandExecutor }
                }
                val leaderName  = Bukkit.getOfflinePlayer(team.leader).name ?: "Unbekannt"
                val memberNames = team.members.joinToString("§7, §r") { uuid: UUID ->
                    Bukkit.getOfflinePlayer(uuid).name ?: "Unbekannt"
                }
                player.msg("§6──── Team Info ────")
                player.msg("Name:    ${team.coloredName()}")
                player.msg("Leader:  §e$leaderName")
                player.msg("Mitglieder (${team.members.size}): §r$memberNames")
            })

        val listCmd = CommandAPICommand("list")
            .executesPlayer(PlayerCommandExecutor { player, _ ->
                val allTeams = tm.getAllTeams()
                if (allTeams.isEmpty()) { player.msg("Es gibt noch keine Teams."); return@PlayerCommandExecutor }
                player.msg("§6──── Teams (${allTeams.size}) ────")
                allTeams.forEach { team ->
                    player.msg("${team.coloredName()} §7– ${team.members.size} Mitglied(er)")
                }
            })

        val disbandCmd = CommandAPICommand("disband")
            .executesPlayer(PlayerCommandExecutor { player, _ ->
                val team = tm.getTeamByPlayer(player.uniqueId)
                if (team == null) { player.err("Du bist in keinem Team."); return@PlayerCommandExecutor }
                if (!team.isLeader(player.uniqueId)) { player.err("Nur der Leader kann das Team auflösen."); return@PlayerCommandExecutor }
                val coloredName = team.coloredName()
                team.members.forEach { uuid: UUID ->
                    Bukkit.getPlayer(uuid)?.msg("Team §6$coloredName §rwurde aufgelöst.")
                }
                tm.deleteTeam(team.name)
                plugin.teamListener.updateTablist()
            })

        CommandAPICommand("team")
            .withSubcommands(createCmd, inviteCmd, acceptCmd, denyCmd, leaveCmd, infoCmd, listCmd, disbandCmd)
            .executesPlayer(PlayerCommandExecutor { player, _ ->
                player.msg("§6Verfügbare Befehle:")
                listOf(
                    "/team create <name> <anzeige> <farbe>" to "Team erstellen",
                    "/team invite <spieler>"                to "Spieler einladen",
                    "/team accept / deny"                   to "Einladung an-/ablehnen",
                    "/team leave"                           to "Team verlassen",
                    "/team disband"                         to "Team auflösen",
                    "/team info [name]"                     to "Teaminfo anzeigen",
                    "/team list"                            to "Alle Teams auflisten"
                ).forEach { (cmd, desc) ->
                    player.sendMessage("§6$cmd §7– §r$desc")
                }
            })
            .register(plugin)
    }
}