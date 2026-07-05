package io.github.black_Kittys22.mortality.HeartSystem

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.event.Listener
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerQuitEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.bossbar.BossBar
import io.github.black_Kittys22.mortality.Main

class CombatSystem(private val plugin: Plugin, private val heartSystem: HeartSystem?) : Listener {

    private val playerCombatEndTimes = mutableMapOf<String, Long>()
    private val playerBossBars = mutableMapOf<String, BossBar>()

    private val COMBAT_DURATION_MS = 30000L
    private val DAMAGE_TO_MS_RATIO = 200L

    private fun getLanguageManager(): io.github.black_Kittys22.mortality.language.LanguageManager? {
        return if (plugin is Main) plugin.languageManager else null
    }

    fun start() {
        Bukkit.getPluginManager().registerEvents(this, plugin)
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, { updateCombatBossBars() }, 0L, 20L)
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, { cleanupExpiredCombat() }, 0L, 10L)
    }

    fun isInCombat(player: Player): Boolean = isInCombat(player.uniqueId.toString())
    fun isInCombat(uuid: String): Boolean {
        val combatEndTime = playerCombatEndTimes[uuid] ?: return false
        return System.currentTimeMillis() < combatEndTime
    }

    private fun getRemainingCombatTime(player: Player): Double {
        val uuid = player.uniqueId.toString()
        val combatEndTime = playerCombatEndTimes[uuid] ?: return 0.0
        val remainingMs = (combatEndTime - System.currentTimeMillis()).coerceAtLeast(0L)
        return remainingMs / 1000.0
    }

    @EventHandler
    fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
        if (event.entity !is Player || event.damager !is Player) return
        val victim = event.entity as Player
        val attacker = event.damager as Player
        val damage = event.damage
        val currentTime = System.currentTimeMillis()
        val additionalMs = (damage * DAMAGE_TO_MS_RATIO).toLong()

        val victimUuid = victim.uniqueId.toString()
        val newVictimEndTime = currentTime + COMBAT_DURATION_MS + additionalMs
        val currentVictimEndTime = playerCombatEndTimes[victimUuid] ?: 0L
        playerCombatEndTimes[victimUuid] = maxOf(currentVictimEndTime, newVictimEndTime)
        showOrUpdateBossBar(victim)

        val attackerUuid = attacker.uniqueId.toString()
        val newAttackerEndTime = currentTime + COMBAT_DURATION_MS + additionalMs
        val currentAttackerEndTime = playerCombatEndTimes[attackerUuid] ?: 0L
        playerCombatEndTimes[attackerUuid] = maxOf(currentAttackerEndTime, newAttackerEndTime)
        showOrUpdateBossBar(attacker)
    }

    private fun showOrUpdateBossBar(player: Player) {
        val uuid = player.uniqueId.toString()
        if (!playerBossBars.containsKey(uuid)) {
            val langManager = getLanguageManager()
            val title = if (langManager != null) {
                langManager.getColoredMessage(player, "combat_bossbar_title")
            } else {
                "⚔ COMBAT ⚔"
            }
            val bossBar = BossBar.bossBar(Component.text(title, NamedTextColor.RED), 1.0f, BossBar.Color.RED, BossBar.Overlay.PROGRESS)
            bossBar.addViewer(player)
            playerBossBars[uuid] = bossBar
        }
    }

    private fun updateCombatBossBars() {
        playerCombatEndTimes.forEach { (uuidStr, _) ->
            val player = Bukkit.getPlayer(java.util.UUID.fromString(uuidStr))
            if (player != null && player.isOnline) {
                val remainingTime = getRemainingCombatTime(player)
                val bossBar = playerBossBars[uuidStr]
                if (bossBar != null && remainingTime > 0) {
                    val langManager = getLanguageManager()
                    val title = if (langManager != null) {
                        langManager.getColoredMessage(player, "combat_bossbar", remainingTime.toInt())
                    } else {
                        "⚔ COMBAT: ${remainingTime.toInt()}s ⚔"
                    }
                    bossBar.name(Component.text(title, NamedTextColor.RED))
                    val progress = (remainingTime / (COMBAT_DURATION_MS / 1000.0)).coerceIn(0.0, 1.0).toFloat()
                    bossBar.progress(progress)
                }
            }
        }
    }

    private fun cleanupExpiredCombat() {
        val currentTime = System.currentTimeMillis()
        val iterator = playerCombatEndTimes.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            val uuidStr = entry.key
            val endTime = entry.value
            val player = Bukkit.getPlayer(java.util.UUID.fromString(uuidStr))
            if (player == null || !player.isOnline || currentTime >= endTime) {
                val bossBar = playerBossBars[uuidStr]
                if (bossBar != null && player != null) {
                    bossBar.removeViewer(player)
                }
                playerBossBars.remove(uuidStr)
                iterator.remove()
            }
        }
    }

    fun removeCombatTimer(player: Player) {
        val uuid = player.uniqueId.toString()
        val bossBar = playerBossBars[uuid]
        if (bossBar != null) {
            bossBar.removeViewer(player)
        }
        playerBossBars.remove(uuid)
        playerCombatEndTimes.remove(uuid)
    }
}