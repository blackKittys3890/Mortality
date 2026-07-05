package io.github.black_Kittys22.mortality.AntiCheat.CombatLog

import org.bukkit.attribute.Attribute
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Mannequin
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.potion.PotionEffectType
import kotlin.math.max

class NpcDamageListener(private val combatManager: CombatManager) : Listener {

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    fun onAttack(event: EntityDamageByEntityEvent) {
        val entity = event.entity
        val player = event.damager
        if (entity !is Mannequin) return
        if (player !is Player) return
        if (!combatManager.isMannequin(entity.uniqueId)) return

        val damage = calculateDamage(player)
        combatManager.recordDamage(entity.uniqueId, damage)

        event.isCancelled = true
        entity.playHurtAnimation(player.location.yaw)
        entity.world.playSound(entity.location, org.bukkit.Sound.ENTITY_PLAYER_HURT, 1f, 1f)
        entity.world.spawnParticle(org.bukkit.Particle.DAMAGE_INDICATOR, entity.location.add(0.0, 1.0, 0.0), 3, 0.2, 0.2, 0.2, 0.1)
    }

    private fun calculateDamage(player: Player): Double {
        var damage = player.getAttribute(Attribute.ATTACK_DAMAGE)?.value ?: 1.0
        damage *= player.attackCooldown.toDouble()
        player.getPotionEffect(PotionEffectType.STRENGTH)?.let {
            damage += 3.0 * (it.amplifier + 1)
        }
        player.getPotionEffect(PotionEffectType.WEAKNESS)?.let {
            damage -= 4.0 * (it.amplifier + 1)
        }
        val sharpness = player.inventory.itemInMainHand.getEnchantmentLevel(Enchantment.SHARPNESS)
        if (sharpness > 0) damage += 0.5 + sharpness * 0.5
        return max(0.0, damage)
    }
}