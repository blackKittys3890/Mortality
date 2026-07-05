package io.github.black_Kittys22.mortality.AntiCheat.CombatLog

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class JoinListener(private val combatManager: CombatManager) : Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    fun onPlayerJoin(event: PlayerJoinEvent) {
        combatManager.handlePlayerJoin(event.player)

        event.joinMessage = ""
    }
}