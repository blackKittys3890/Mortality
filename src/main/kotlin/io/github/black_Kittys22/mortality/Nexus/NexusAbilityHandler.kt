// NexusAbilityHandler.kt
package io.github.black_Kittys22.mortality.Nexus

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.entity.Player
import org.bukkit.entity.LivingEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.enchantment.EnchantItemEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.plugin.java.JavaPlugin

class NexusAbilityHandler(private val plugin: JavaPlugin) : Listener {

    private val active = mutableMapOf<String, MutableSet<String>>()
    private val frozen = mutableSetOf<Player>()
    private val shields = mutableMapOf<Player, BukkitRunnable>()

    init {
        object : BukkitRunnable() {
            override fun run() {
                Bukkit.getOnlinePlayers().forEach { p ->
                    active[p.name]?.let {
                        if ("nightvision" in it && p.location.block.lightLevel <= 5)
                            p.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 100, 1, true, false))
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L)
    }

    @EventHandler fun onQuit(e: PlayerQuitEvent) {
        val p = e.player
        active.remove(p.name)
        frozen.remove(p)
        shields.remove(p)?.cancel()
        if (!p.isOp) { p.allowFlight = false; p.isFlying = false }
    }

    @EventHandler fun onHeld(e: PlayerItemHeldEvent) {
        update(e.player, e.player.inventory.getItem(e.newSlot))
    }

    private fun update(p: Player, item: ItemStack?) {
        active.remove(p.name)
        shields.remove(p)?.cancel()
        if (item?.itemMeta?.itemModel?.key?.startsWith("nexus_") != true) {
            reset(p)
            return
        }

        val key = item.itemMeta.itemModel.key
        val ab = mutableSetOf<String>()
        when {
            key == "nexus_affenreich" -> ab.add("copy")
            key == "nexus_atlantis" -> ab.add("underwater")
            key == "nexus_berg" -> ab.add("immortal")
            key == "nexus_candyinsel" -> ab.add("freeze")
            key == "nexus_enddimension" -> ab.add("teleport")
            key == "nexus_gift" -> ab.add("poison")
            key == "nexus_glaskuppel" -> ab.add("shield")
            key == "nexus_himmelsreich" -> ab.add("fly")
            key == "nexus_hoelle" -> ab.add("resize")
            key == "nexus_mittelalter" -> ab.add("enchant")
            key == "nexus_schokolade" -> ab.add("saturation")
            key == "nexus_unterstadt" -> ab.add("nightvision")
            key == "nexus_void" -> ab.add("void")
            key == "nexus_zeit" -> ab.add("time")
            key == "nexus_zukunftsstadt" -> ab.add("haste")
            key == "nexus_industriepunk" -> ab.add("industry")
        }
        active[p.name] = ab
        apply(p, ab)
        if ("shield" in ab) createShield(p)
    }

    private fun reset(p: Player) {
        p.potionEffects.forEach { p.removePotionEffect(it.type) }
        if (!p.isOp) { p.allowFlight = false; p.isFlying = false }
    }

    private fun apply(p: Player, ab: Set<String>) {
        p.potionEffects.forEach { if (it.type != PotionEffectType.SPEED) p.removePotionEffect(it.type) }
        if ("underwater" in ab) p.addPotionEffect(PotionEffect(PotionEffectType.WATER_BREATHING, Int.MAX_VALUE, 0, true, false))
        if ("immortal" in ab) {
            p.addPotionEffect(PotionEffect(PotionEffectType.RESISTANCE, Int.MAX_VALUE, 3, true, false))
            p.addPotionEffect(PotionEffect(PotionEffectType.REGENERATION, Int.MAX_VALUE, 1, true, false))
        }
        if ("fly" in ab) p.allowFlight = true
        else if (!p.isOp) { p.allowFlight = false; p.isFlying = false }
        if ("nightvision" in ab) p.addPotionEffect(PotionEffect(PotionEffectType.NIGHT_VISION, Int.MAX_VALUE, 0, true, false))
        if ("haste" in ab) p.addPotionEffect(PotionEffect(PotionEffectType.HASTE, Int.MAX_VALUE, 2, true, false))
        if ("poison" in ab) p.removePotionEffect(PotionEffectType.POISON)
    }

    @EventHandler fun onFood(e: FoodLevelChangeEvent) {
        (e.entity as? Player)?.let { p ->
            if ("saturation" in (active[p.name] ?: emptySet())) {
                e.foodLevel = 20
                p.saturation = 10f
            }
        }
    }

    @EventHandler fun onDamageByEntity(e: EntityDamageByEntityEvent) {
        val d = e.damager as? Player ?: return
        val t = e.entity as? LivingEntity ?: return
        val ab = active[d.name] ?: return
        if ("freeze" in ab && t is Player) freezePlayer(t, 60)
        if ("poison" in ab) t.addPotionEffect(PotionEffect(PotionEffectType.POISON, 100, 1, true, false))
    }

    private fun freezePlayer(p: Player, dur: Int) {
        frozen.add(p)
        p.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS, dur, 100, true, false))
        p.addPotionEffect(PotionEffect(PotionEffectType.JUMP_BOOST, dur, 128, true, false))
        Bukkit.getScheduler().runTaskLater(plugin, { frozen.remove(p) }, dur.toLong())
    }

    @EventHandler fun onMove(e: PlayerMoveEvent) {
        if (e.player in frozen) e.isCancelled = true
    }

    @EventHandler fun onDamage(e: EntityDamageEvent) {
        (e.entity as? Player)?.let { p ->
            if ("shield" in (active[p.name] ?: emptySet())) e.damage *= 0.2
        }
    }

    @EventHandler fun onInteract(e: PlayerInteractEvent) {
        val p = e.player
        val ab = active[p.name] ?: return
        if ("void" in ab && e.action.isRightClick) {
            (p.getTargetEntity(10) as? Player)?.let {
                it.teleport(it.location.add(0.0, -100.0, 0.0))
                e.isCancelled = true
            }
        }
        if ("teleport" in ab && e.action.isRightClick) {
            val loc = p.location.add((Math.random() * 20 - 10).toInt().toDouble(), 0.0, (Math.random() * 20 - 10).toInt().toDouble())
            loc.y = p.world.getHighestBlockYAt(loc) + 1.0
            p.teleport(loc)
            e.isCancelled = true
        }
    }

    private fun createShield(p: Player) {
        shields[p]?.cancel()
        val task = object : BukkitRunnable() {
            override fun run() {
                if (!p.isOnline || "shield" !in (active[p.name] ?: emptySet())) {
                    this.cancel()
                    shields.remove(p)
                    return
                }
                val loc = p.location
                for (i in 0..360 step 15) {
                    for (j in 0..360 step 30) {
                        val t = Math.toRadians(i.toDouble())
                        val ph = Math.toRadians(j.toDouble())
                        p.world.spawnParticle(Particle.SPELL_WITCH,
                            loc.x + 1.5 * Math.sin(t) * Math.cos(ph),
                            loc.y + 1.5 * Math.sin(t) * Math.sin(ph) + 1.0,
                            loc.z + 1.5 * Math.cos(t), 1, 0.0, 0.0, 0.0, 0.0)
                    }
                }
            }
        }
        task.runTaskTimer(plugin, 0L, 5L)
        shields[p] = task
    }

    @EventHandler fun onBreak(e: BlockBreakEvent) {
        val p = e.player
        if ("industry" in (active[p.name] ?: emptySet())) {
            val drops = e.block.drops
            val newDrops = mutableListOf<ItemStack>()
            drops.forEach { drop ->
                val smelted = smelt(drop)
                newDrops.add(smelted)
                newDrops.add(smelted.clone())
            }
            e.isDropItems = false
            newDrops.forEach { p.world.dropItemNaturally(e.block.location, it) }
        }
    }

    private fun smelt(i: ItemStack): ItemStack = when (i.type) {
        Material.IRON_ORE, Material.RAW_IRON -> ItemStack(Material.IRON_INGOT, i.amount)
        Material.GOLD_ORE, Material.RAW_GOLD -> ItemStack(Material.GOLD_INGOT, i.amount)
        Material.COPPER_ORE, Material.RAW_COPPER -> ItemStack(Material.COPPER_INGOT, i.amount)
        Material.COAL_ORE -> ItemStack(Material.COAL, i.amount)
        Material.ANCIENT_DEBRIS -> ItemStack(Material.NETHERITE_SCRAP, i.amount)
        else -> i
    }

    @EventHandler fun onEnchant(e: EnchantItemEvent) {
        if ("enchant" in (active[e.enchanter.name] ?: emptySet())) {
            val enchants = e.enchantsToAdd.keys.toList()
            e.enchantsToAdd.clear()
            enchants.forEach { e.enchantsToAdd[it] = 1 }
        }
    }
}