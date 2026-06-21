package io.github.black_Kittys22.mortality.AntiCheat.CombatLog

import org.bukkit.Location
import org.bukkit.entity.EntityType
import org.bukkit.entity.Mannequin
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.util.UUID
import io.papermc.paper.datacomponent.item.ResolvableProfile
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

class CombatManager(private val plugin: JavaPlugin) {

    // Mannequin UUID → owning Player UUID
    private val activeNpcs = mutableMapOf<UUID, Mannequin>()

    // Player UUID → accumulated damage dealt to their NPC while they were offline
    private val pendingDamage = mutableMapOf<UUID, Double>()

    // Reverse-lookup: Mannequin entity UUID → Player UUID (needed in the damage listener)
    private val mannequinToPlayer = mutableMapOf<UUID, UUID>()

    // -------------------------------------------------------------------------
    // Spawn / remove
    // -------------------------------------------------------------------------

    fun spawnNpc(player: Player) {
        val location: Location = player.location
        val mannequin = location.world.spawnEntity(location, EntityType.MANNEQUIN) as Mannequin

        mannequin.apply {
            setProfile(ResolvableProfile.resolvableProfile(player.playerProfile))
            customName(player.displayName())
            isCustomNameVisible = true
            isInvulnerable = false          // must be damageable so we can track hits
            setAI(false)
            health = player.health
            getAttribute(org.bukkit.attribute.Attribute.KNOCKBACK_RESISTANCE)
                ?.baseValue = 1.0
            equipment?.apply {
                helmet = player.inventory.helmet
                chestplate = player.inventory.chestplate
                leggings = player.inventory.leggings
                boots = player.inventory.boots
                setItemInMainHand(player.inventory.itemInMainHand)
                setItemInOffHand(player.inventory.itemInOffHand)
            }
        }

        // Clean up any previous NPC for this player first
        removeNpc(player.uniqueId)

        activeNpcs[player.uniqueId] = mannequin
        mannequinToPlayer[mannequin.uniqueId] = player.uniqueId

        plugin.logger.info("NPC für ${player.name} gespawnt (Entity-UUID: ${mannequin.uniqueId})")
    }

    fun removeNpc(uuid: UUID) {
        val mannequin = activeNpcs.remove(uuid) ?: return
        mannequinToPlayer.remove(mannequin.uniqueId)
        mannequin.remove()
    }

    fun hasNpc(uuid: UUID): Boolean = activeNpcs.containsKey(uuid)

    // -------------------------------------------------------------------------
    // Damage tracking
    // -------------------------------------------------------------------------

    /**
     * Called by [NpcDamageListener] every time the Mannequin is hit.
     * @param mannequinUuid The UUID of the Mannequin entity.
     * @param damage        Raw damage value from the damage event.
     */
    fun recordDamage(mannequinUuid: UUID, damage: Double) {
        val playerUuid = mannequinToPlayer[mannequinUuid] ?: return
        pendingDamage[playerUuid] = (pendingDamage[playerUuid] ?: 0.0) + damage
        plugin.logger.fine("NPC-Schaden für $playerUuid +$damage (gesamt: ${pendingDamage[playerUuid]})")
    }

    fun handlePlayerJoin(player: Player) {
        if (hasNpc(player.uniqueId)) {
            removeNpc(player.uniqueId)
            plugin.logger.info("NPC für ${player.name} beim Join entfernt.")
        }

        // Apply pending damage
        val damage = pendingDamage.remove(player.uniqueId) ?: return
        if (damage <= 0.0) return

        val newHealth = (player.health - damage).coerceAtLeast(0.0)
        player.health = newHealth

        player.sendMessage(
            Component.text("Du hast während deiner Abwesenheit ")
                .color(NamedTextColor.RED)
                .append(Component.text("%.1f".format(damage)).color(NamedTextColor.DARK_RED))
                .append(Component.text(" Schaden erhalten!").color(NamedTextColor.RED))
        )

        plugin.logger.info("${player.name} erhielt $damage ausstehenden NPC-Schaden (neue HP: $newHealth).")

        if (newHealth <= 0.0) {
            player.health = 0.0
        }
    }

    fun isMannequin(entityUuid: UUID): Boolean = mannequinToPlayer.containsKey(entityUuid)
}