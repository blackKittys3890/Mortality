package io.github.black_Kittys22.mortality.TeamSystem.manager

import io.github.black_Kittys22.mortality.Main
import io.github.black_Kittys22.mortality.TeamSystem.model.Team
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.util.UUID

class Teammanager(private val plugin: Main) {

    private val teams: MutableMap<String, Team> = mutableMapOf()
    private val dataFile = File(plugin.dataFolder, "teams.yml")
    private var config = YamlConfiguration()

    init { loadTeams() }

    fun createTeam(name: String, displayName: String, colorCode: String, leader: UUID): Boolean {
        val key = name.lowercase()
        if (teams.containsKey(key)) return false
        teams[key] = Team(key, displayName, colorCode, leader)
        saveTeams()
        return true
    }

    fun getTeamByPlayer(uuid: UUID): Team? = teams.values.find { it.isMember(uuid) }
    fun getTeamByName(name: String): Team? = teams[name.lowercase()]
    fun getAllTeams(): List<Team> = teams.values.toList()

    fun joinTeam(uuid: UUID, teamName: String): Boolean {
        val team = getTeamByName(teamName) ?: return false
        if (team.isMember(uuid)) return false
        team.members.add(uuid)
        saveTeams()
        return true
    }

    fun leaveTeam(uuid: UUID): Boolean {
        val team = getTeamByPlayer(uuid) ?: return false
        team.members.remove(uuid)
        if (team.members.isEmpty()) teams.remove(team.name)
        saveTeams()
        return true
    }

    fun deleteTeam(teamName: String): Boolean {
        val removed = teams.remove(teamName.lowercase()) != null
        if (removed) saveTeams()
        return removed
    }

    fun saveTeams() {
        plugin.dataFolder.mkdirs()
        config = YamlConfiguration()
        teams.forEach { (key, team) ->
            val p = "teams.$key"
            config["$p.displayName"] = team.displayName
            config["$p.colorCode"] = team.colorCode
            config["$p.leader"] = team.leader.toString()
            config["$p.members"] = team.members.map(UUID::toString)
        }
        config.save(dataFile)
    }

    private fun loadTeams() {
        if (!dataFile.exists()) return
        config = YamlConfiguration.loadConfiguration(dataFile)
        val section = config.getConfigurationSection("teams") ?: return
        for (key in section.getKeys(false)) {
            val p = "teams.$key"
            val displayName = config.getString("$p.displayName") ?: key
            val colorCode = config.getString("$p.colorCode") ?: "§f"
            val leaderStr = config.getString("$p.leader") ?: continue
            val memberList = config.getStringList("$p.members")
            val leader = runCatching { UUID.fromString(leaderStr) }.getOrNull() ?: continue
            val members = memberList.mapNotNull {
                runCatching { UUID.fromString(it) }.getOrNull()
            }.toMutableSet()
            teams[key] = Team(key, displayName, colorCode, leader, members)
        }
        plugin.logger.info("${teams.size} Team(s) geladen.")
    }
}