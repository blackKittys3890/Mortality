package io.github.black_Kittys22.mortality.TeamSystem.manager

import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.util.UUID
import io.github.black_Kittys22.mortality.language.sendLang

class InviteManager(private val plugin: JavaPlugin) {

    private val pendingInvites: MutableMap<UUID, String> = mutableMapOf()
    private val INVITE_TIMEOUT_SECONDS = 60L

    fun sendInvite(invitedUUID: UUID, teamName: String) {
        pendingInvites[invitedUUID] = teamName
        Bukkit.getScheduler().runTaskLater(plugin, Runnable {
            if (pendingInvites[invitedUUID] == teamName) {
                pendingInvites.remove(invitedUUID)
                val player = Bukkit.getPlayer(invitedUUID)
                player?.sendLang("team_invite_expired", teamName)
            }
        }, INVITE_TIMEOUT_SECONDS * 20L)
    }

    fun getInvite(uuid: UUID): String? = pendingInvites[uuid]
    fun acceptInvite(uuid: UUID): String? = pendingInvites.remove(uuid)
    fun removeInvite(uuid: UUID) { pendingInvites.remove(uuid) }
    fun hasInvite(uuid: UUID): Boolean = pendingInvites.containsKey(uuid)
}