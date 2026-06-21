package io.github.black_Kittys22.mortality.AntiCheat.CombatLog

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import io.github.black_Kittys22.mortality.HeartSystem.CombatSystem

class LeaveListener(
    private val CombatManager: CombatManager,
    private val combatSystem: CombatSystem
) : Listener {

    @EventHandler
    fun onplayerQuit(event: PlayerQuitEvent) {
        val player = event.player

        if (combatSystem.isInCombat(player)) {
            CombatManager.spawnNpc(player)
        }

        combatSystem.removeCombatTimer(player)
    }
}