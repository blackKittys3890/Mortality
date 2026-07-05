package io.github.black_Kittys22.mortality.TeamSystem.command

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.PlayerProfileArgument
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.executors.PlayerCommandExecutor
import io.github.black_Kittys22.mortality.Main
import io.github.black_Kittys22.mortality.TeamSystem.model.Team
import io.github.black_Kittys22.mortality.language.sendLang
import io.github.black_Kittys22.mortality.language.sendLangError
import io.github.black_Kittys22.mortality.language.sendLangSuccess
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.UUID

object TeamCommand {

    fun register(plugin: Main) {
        val tm = plugin.teamManager
        val im = plugin.inviteManager

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
                    player.sendLangError("team_already_in")
                    return@PlayerCommandExecutor
                }
                val name = args[0] as String
                val displayName = args[1] as String
                val farbName = (args[2] as String).lowercase()

                val colorCode = Team.AVAILABLE_COLORS[farbName]
                if (colorCode == null) {
                    player.sendLangError("team_color_not_found", Team.AVAILABLE_COLORS.keys.joinToString(", "))
                    return@PlayerCommandExecutor
                }

                if (!name.matches(Regex("^[a-zA-Z0-9_-]{3,16}$"))) {
                    player.sendLangError("team_name_invalid")
                    return@PlayerCommandExecutor
                }

                val success = tm.createTeam(name, displayName, colorCode, player.uniqueId)
                if (success) {
                    player.sendLangSuccess("team_created", displayName, "${colorCode}■§r")
                    plugin.teamListener.updateTablist()
                } else {
                    player.sendLangError("team_exists")
                }
            })

        val inviteCmd = CommandAPICommand("invite")
            .withArguments(PlayerProfileArgument("target"))
            .executesPlayer(PlayerCommandExecutor { player, args ->
                val team = tm.getTeamByPlayer(player.uniqueId)
                if (team == null) {
                    player.sendLangError("team_not_in")
                    return@PlayerCommandExecutor
                }
                if (!team.isLeader(player.uniqueId)) {
                    player.sendLangError("team_leader_only")
                    return@PlayerCommandExecutor
                }

                val profile = args[0] as? com.destroystokyo.paper.profile.PlayerProfile
                val target = profile?.uniqueId?.let { Bukkit.getPlayer(it) }

                if (target == null) {
                    player.sendLangError("player_offline")
                    return@PlayerCommandExecutor
                }

                if (target.uniqueId == player.uniqueId) {
                    player.sendLangError("team_invite_self")
                    return@PlayerCommandExecutor
                }

                if (tm.getTeamByPlayer(target.uniqueId) != null) {
                    player.sendLangError("team_already_in")
                    return@PlayerCommandExecutor
                }

                im.sendInvite(target.uniqueId, team.name)
                player.sendLangSuccess("team_invite_sent", target.name)

                target.sendLang("team_invite_received", player.name, team.coloredName())
                target.sendLang("team_invite_accept")
                target.sendLang("team_invite_deny")
            })

        val acceptCmd = CommandAPICommand("accept")
            .executesPlayer(PlayerCommandExecutor { player, args ->
                if (!im.hasInvite(player.uniqueId)) {
                    player.sendLangError("team_no_invite")
                    return@PlayerCommandExecutor
                }
                if (tm.getTeamByPlayer(player.uniqueId) != null) {
                    player.sendLangError("team_already_in")
                    return@PlayerCommandExecutor
                }

                val teamName = im.acceptInvite(player.uniqueId)
                val team = teamName?.let { tm.getTeamByName(it) }

                if (team != null) {
                    tm.joinTeam(player.uniqueId, team.name)
                    val coloredName = team.coloredName()
                    player.sendLangSuccess("team_joined", coloredName)

                    team.members.forEach { uuid: UUID ->
                        Bukkit.getPlayer(uuid)?.sendLang("team_member_joined", player.name)
                    }
                    plugin.teamListener.updateTablist()
                } else {
                    player.sendLangError("team_not_exists")
                }
            })

        val denyCmd = CommandAPICommand("deny")
            .executesPlayer(PlayerCommandExecutor { player, args ->
                if (!im.hasInvite(player.uniqueId)) {
                    player.sendLangError("team_no_invite")
                    return@PlayerCommandExecutor
                }
                val teamName = im.getInvite(player.uniqueId) ?: "Unbekannt"
                im.removeInvite(player.uniqueId)
                player.sendLangSuccess("team_invite_denied", teamName)
            })

        val leaveCmd = CommandAPICommand("leave")
            .executesPlayer(PlayerCommandExecutor { player, args ->
                val team = tm.getTeamByPlayer(player.uniqueId)
                if (team == null) {
                    player.sendLangError("team_not_in")
                    return@PlayerCommandExecutor
                }
                if (team.isLeader(player.uniqueId)) {
                    player.sendLangError("team_leader_leave")
                    return@PlayerCommandExecutor
                }

                val coloredName = team.coloredName()
                tm.leaveTeam(player.uniqueId)
                player.sendLangSuccess("team_left", coloredName)

                team.members.forEach { uuid: UUID ->
                    Bukkit.getPlayer(uuid)?.sendLang("team_member_left", player.name)
                }
                plugin.teamListener.updateTablist()
            })

        val infoCmd = CommandAPICommand("info")
            .executesPlayer(PlayerCommandExecutor { player, args ->
                val team = tm.getTeamByPlayer(player.uniqueId)
                if (team == null) {
                    player.sendLangError("team_not_in")
                    return@PlayerCommandExecutor
                }
                val leaderName = Bukkit.getOfflinePlayer(team.leader).name ?: "Unbekannt"
                val memberNames = team.members.map { Bukkit.getOfflinePlayer(it).name ?: "Unbekannt" }

                player.sendLang("team_info_header", team.coloredName())
                player.sendLang("team_info_leader", leaderName)
                player.sendLang("team_info_members", memberNames.joinToString(", "))
            })

        val listCmd = CommandAPICommand("list")
            .executesPlayer(PlayerCommandExecutor { player, args ->
                val teams = tm.getAllTeams()
                if (teams.isEmpty()) {
                    player.sendLang("team_list_empty")
                    return@PlayerCommandExecutor
                }
                player.sendLang("team_list_header")
                teams.forEach { t ->
                    player.sendLang("team_list_entry", t.coloredName(), t.members.size.toString())
                }
            })

        val disbandCmd = CommandAPICommand("disband")
            .executesPlayer(PlayerCommandExecutor { player, args ->
                val team = tm.getTeamByPlayer(player.uniqueId)
                if (team == null) {
                    player.sendLangError("team_not_in")
                    return@PlayerCommandExecutor
                }
                if (!team.isLeader(player.uniqueId)) {
                    player.sendLangError("team_leader_only")
                    return@PlayerCommandExecutor
                }

                val coloredName = team.coloredName()
                team.members.forEach { uuid: UUID ->
                    Bukkit.getPlayer(uuid)?.sendLang("team_disbanded", coloredName)
                }
                tm.deleteTeam(team.name)
                plugin.teamListener.updateTablist()
            })

        CommandAPICommand("team")
            .withSubcommands(createCmd, inviteCmd, acceptCmd, denyCmd, leaveCmd, infoCmd, listCmd, disbandCmd)
            .executesPlayer(PlayerCommandExecutor { player, _ ->
                player.sendLang("help_header")
                listOf(
                    "help_team_create" to "/team create <name> <anzeige> <farbe>",
                    "help_team_invite" to "/team invite <spieler>",
                    "help_team_accept" to "/team accept / deny",
                    "help_team_leave" to "/team leave",
                    "help_team_disband" to "/team disband",
                    "help_team_info" to "/team info [name]",
                    "help_team_list" to "/team list",
                    "help_team_chat" to "/tc <nachricht>"
                ).forEach { (key, cmd) ->
                    val desc = plugin.languageManager.getColoredMessage(player, key)
                    player.sendMessage("§6$cmd §7– §r$desc")
                }
            })
            .register(plugin)
    }
}