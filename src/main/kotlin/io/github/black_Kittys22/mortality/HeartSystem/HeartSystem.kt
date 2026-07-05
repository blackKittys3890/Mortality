package io.github.black_Kittys22.mortality.HeartSystem

import io.github.black_Kittys22.mortality.Main
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.event.Listener
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.OfflinePlayer

class HeartSystem(private val plugin: Plugin, private val combatSystem: CombatSystem? = null) : Listener, CommandExecutor {

    val playerHearts = mutableMapOf<String, Double>()
    private val legacy = LegacyComponentSerializer.legacySection()

    private fun getLanguageManager(): io.github.black_Kittys22.mortality.language.LanguageManager? {
        return if (plugin is Main) plugin.languageManager else null
    }

    private fun Player.sendLang(key: String, vararg args: Any) {
        val langManager = getLanguageManager()
        if (langManager != null) {
            sendMessage(langManager.getColoredMessage(this, key, *args))
        }
    }

    private fun Player.sendLangError(key: String, vararg args: Any) {
        val langManager = getLanguageManager()
        if (langManager != null) {
            val msg = langManager.getColoredMessage(this, key, *args)
            sendMessage("§c$msg")
        }
    }

    private fun Player.sendLangSuccess(key: String, vararg args: Any) {
        val langManager = getLanguageManager()
        if (langManager != null) {
            val msg = langManager.getColoredMessage(this, key, *args)
            sendMessage("§a$msg")
        }
    }

    private fun CommandSender.sendLangError(key: String, vararg args: Any) {
        val langManager = getLanguageManager()
        if (langManager != null && this is Player) {
            val msg = langManager.getColoredMessage(this, key, *args)
            sendMessage("§c$msg")
        } else if (this is Player) {
            sendMessage(Component.text("Dazu hast du keine Rechte!", NamedTextColor.RED))
        }
    }

    fun start() {
        Bukkit.getPluginManager().registerEvents(this, plugin)
        Bukkit.getOnlinePlayers().forEach { player -> initializePlayer(player) }
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, { sendActionBarsToAll() }, 0L, 5L)
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.isOp && !sender.hasPermission("heartsystem.set")) {
            if (sender is Player) {
                sender.sendLangError("no_permission")
            } else {
                sender.sendMessage(Component.text("Dazu hast du keine Rechte!", NamedTextColor.RED))
            }
            return true
        }
        if (args.size < 2) {
            if (sender is Player) {
                sender.sendLangError("help_sethearts")
            } else {
                sender.sendMessage(Component.text("Benutzung: /sethearts <Spieler> <Anzahl>", NamedTextColor.RED))
            }
            return true
        }
        val targetName = args[0]
        val heartAmount = args[1].toIntOrNull()
        if (heartAmount == null || heartAmount !in 0..3) {
            if (sender is Player) {
                sender.sendLangError("invalid_amount")
            } else {
                sender.sendMessage(Component.text("Bitte gib eine gültige Herzzahl zwischen 0 und 3 ein!", NamedTextColor.RED))
            }
            return true
        }

        val onlinePlayer = Bukkit.getPlayer(targetName)
        if (onlinePlayer != null) {
            setHearts(onlinePlayer, heartAmount)

            // LÖSUNG: Wenn der Sender sich selbst die Herzen setzt, reicht die "hearts_current" Meldung aus setHearts().
            // Die "hearts_set" Meldung wird nur gesendet, wenn ein ANDERER Spieler bearbeitet wurde.
            if (sender is Player) {
                if (sender.uniqueId != onlinePlayer.uniqueId) {
                    sender.sendLangSuccess("hearts_set", onlinePlayer.name, heartAmount.toString())
                }
            } else {
                sender.sendMessage(Component.text("Erfolgreich ", NamedTextColor.GREEN)
                    .append(Component.text(onlinePlayer.name, NamedTextColor.GOLD))
                    .append(Component.text(" auf $heartAmount Herzen gesetzt.", NamedTextColor.GREEN)))
            }
            checkAndKickPlayerIfNoHearts(onlinePlayer)
        } else {
            @Suppress("DEPRECATION")
            val offlinePlayer = Bukkit.getOfflinePlayer(targetName)
            if (offlinePlayer.hasPlayedBefore() || offlinePlayer.name != null) {
                setHearts(offlinePlayer, heartAmount)
                if (sender is Player) {
                    sender.sendLangSuccess("hearts_set", offlinePlayer.name ?: targetName, heartAmount.toString())
                } else {
                    sender.sendMessage(Component.text("Erfolgreich ", NamedTextColor.GREEN)
                        .append(Component.text(offlinePlayer.name ?: targetName, NamedTextColor.GOLD))
                        .append(Component.text(" hat jetzt $heartAmount Herzen.", NamedTextColor.GREEN)))
                }
            } else {
                if (sender is Player) {
                    sender.sendLangError("player_not_found")
                } else {
                    sender.sendMessage(Component.text("Spieler '$targetName' wurde nicht gefunden!", NamedTextColor.RED))
                }
            }
        }
        return true
    }
    fun initializePlayer(player: Player) {
        val uuid = player.uniqueId.toString()
        if (!playerHearts.containsKey(uuid)) {
            playerHearts[uuid] = 3.0
        }
        sendHeartActionBar(player)
    }

    fun getHearts(player: Player): Double = playerHearts[player.uniqueId.toString()] ?: 3.0

    fun setHearts(player: Player, hearts: Double) {
        val uuid = player.uniqueId.toString()
        val clampedHearts = hearts.coerceIn(0.0, 3.0)
        playerHearts[uuid] = clampedHearts
        player.sendLangSuccess("hearts_current", clampedHearts.toInt())
        sendHeartActionBar(player)
    }

    fun setHearts(player: Player, hearts: Int) = setHearts(player, hearts.toDouble())

    fun setHearts(offlinePlayer: OfflinePlayer, hearts: Double) {
        val uuid = offlinePlayer.uniqueId.toString()
        val clampedHearts = hearts.coerceIn(0.0, 3.0)
        playerHearts[uuid] = clampedHearts
        val online = Bukkit.getPlayer(offlinePlayer.uniqueId)
        if (online != null && online.isOnline) {
            online.sendLangSuccess("hearts_current", clampedHearts.toInt())
            sendHeartActionBar(online)
        } else {
            plugin.logger.info("setHearts: Offline-Spieler ${offlinePlayer.name} (UUID=$uuid) hat jetzt $clampedHearts Herzen")
        }
    }

    fun setHearts(offlinePlayer: OfflinePlayer, hearts: Int) = setHearts(offlinePlayer, hearts.toDouble())

    private fun sendHeartActionBar(player: Player) {
        val uuid = player.uniqueId.toString()
        val hearts = playerHearts[uuid] ?: 3.0
        val langManager = getLanguageManager()
        val heartDisplay = if (langManager != null) {
            langManager.getActionBar(player, hearts)
        } else {
            getHeartDisplay(hearts)
        }
        if (heartDisplay.isNotEmpty()) {
            player.sendActionBar(Component.text(heartDisplay))
        }
    }

    private fun sendActionBarsToAll() {
        Bukkit.getOnlinePlayers().forEach { player -> sendHeartActionBar(player) }
    }

    private fun getHeartDisplay(hearts: Double): String {
        return when {
            hearts >= 3.0  -> "\uE100"
            hearts >= 2.0  -> "\uE101"
            hearts >= 1.0  -> "\uE102"
            else           -> ""
        }
    }

    private fun teamPrefix(player: Player): String {
        val tm = (plugin as? Main)?.teamManager ?: return ""
        val team = tm.getTeamByPlayer(player.uniqueId) ?: return ""
        return "${team.colorCode}[${team.displayName}]§r "
    }

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val player = event.entity
        val killer = player.killer
        val uuid = player.uniqueId.toString()
        val langManager = getLanguageManager()

        val (componentMessage, plainMessage) = if (killer != null) {
            val victimTeam = teamPrefix(player)
            val killerTeam = teamPrefix(killer)
            val tm = (plugin as? Main)?.teamManager
            val victimTeamName = tm?.getTeamByPlayer(player.uniqueId)?.name ?: "kein Team"
            val killerTeamName = tm?.getTeamByPlayer(killer.uniqueId)?.name ?: "kein Team"

            if (langManager != null) {
                val deathMsg = langManager.getColoredMessage(player, "death_killed",
                    victimTeam + player.name, killerTeam + killer.name)
                val comp = legacy.deserialize(deathMsg)
                val plain = "[Mortality] [$victimTeamName] ${player.name} wurde von [$killerTeamName] ${killer.name} getötet"
                Pair(comp, plain)
            } else {
                val comp = legacy.deserialize(
                    "§8[§6Mortality§8] §r${victimTeam}§c${player.name} §ewurde von ${killerTeam}§c${killer.name} §egetötet"
                )
                val plain = "[Mortality] [$victimTeamName] ${player.name} wurde von [$killerTeamName] ${killer.name} getötet"
                Pair(comp, plain)
            }
        } else {
            val victimTeam = teamPrefix(player)
            val tm = (plugin as? Main)?.teamManager
            val victimTeamName = tm?.getTeamByPlayer(player.uniqueId)?.name ?: "kein Team"

            if (langManager != null) {
                val deathMsg = langManager.getColoredMessage(player, "death_died",
                    victimTeam + player.name)
                val comp = legacy.deserialize(deathMsg)
                val plain = "[Mortality] [$victimTeamName] ${player.name} ist gestorben"
                Pair(comp, plain)
            } else {
                val comp = legacy.deserialize(
                    "§8[§6Mortality§8] §r${victimTeam}§c${player.name} §eist gestorben"
                )
                val plain = "[Mortality] [$victimTeamName] ${player.name} ist gestorben"
                Pair(comp, plain)
            }
        }

        event.deathMessage = ""
        Bukkit.getOnlinePlayers().forEach { it.sendMessage(componentMessage) }
        plugin.logger.info(plainMessage)

        if (killer != null || (combatSystem != null && combatSystem.isInCombat(player))) {
            val current = playerHearts[uuid] ?: 0.0
            if (current > 0.0) {
                playerHearts[uuid] = (current - 1.0).coerceAtLeast(0.0)
            }
        }

        sendHeartActionBar(player)
        checkAndKickPlayerIfNoHearts(player)
    }

    private fun checkAndKickPlayerIfNoHearts(player: Player) {
        val uuid = player.uniqueId.toString()
        val hearts = playerHearts[uuid] ?: 0.0
        if (hearts <= 0.0) {
            try {
                val langManager = getLanguageManager()
                val kickMsg = if (langManager != null) {
                    langManager.getColoredMessage(player, "hearts_zero")
                } else {
                    "Du hast keine Herzen mehr!\nDu wurdest gekickt!"
                }
                player.kick(Component.text(kickMsg, NamedTextColor.RED))
                plugin.logger.info("${player.name} wurde gekickt (keine Herzen mehr)!")
            } catch (ex: Exception) {
                plugin.logger.warning("Fehler beim Kicken von ${player.name}: ${ex.message}")
            }
        }
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        initializePlayer(event.player)
        checkAndKickPlayerIfNoHearts(event.player)
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {}

    fun kickZeroHeartPlayersNow() {
        Bukkit.getOnlinePlayers().forEach { player ->
            val uuid = player.uniqueId.toString()
            val hearts = playerHearts[uuid] ?: 3.0
            if (hearts <= 0.0) {
                plugin.logger.info("kickZeroHeartPlayersNow: Kicke ${player.name} (hearts=$hearts)")
                try {
                    val langManager = getLanguageManager()
                    val kickMsg = if (langManager != null) {
                        langManager.getColoredMessage(player, "hearts_zero")
                    } else {
                        "Du hast keine Herzen mehr!\nDu wurdest gekickt!"
                    }
                    player.kick(Component.text(kickMsg, NamedTextColor.RED))
                } catch (ex: Exception) {
                    plugin.logger.warning("Fehler beim Kicken von ${player.name}: ${ex.message}")
                }
            }
        }
    }

    fun removeHeartForWorldBreak(isLastHeart: Boolean) {
        val amount = if (isLastHeart) 0.5 else 1.0
        Bukkit.getOnlinePlayers().forEach { player ->
            val uuid = player.uniqueId.toString()
            val current = playerHearts[uuid] ?: 3.0
            val newHearts = (current - amount).coerceAtLeast(0.0)
            playerHearts[uuid] = newHearts
            sendHeartActionBar(player)

            val langManager = getLanguageManager()
            if (langManager != null) {
                val heartText = if (isLastHeart) "half" else "one"
                player.sendLang("hearts_lost", heartText)
            } else {
                val amountText = if (isLastHeart) "einem halben" else "einem"
                player.sendMessage(Component.text("💔 Du hast ", NamedTextColor.RED)
                    .append(Component.text("$amountText Herz", NamedTextColor.DARK_RED))
                    .append(Component.text(" verloren!", NamedTextColor.RED)))
            }
            checkAndKickPlayerIfNoHearts(player)
        }
    }
}