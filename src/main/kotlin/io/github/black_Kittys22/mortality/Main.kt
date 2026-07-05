package io.github.black_Kittys22.mortality

import de.helden.manager.DenySpawnManager
import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPIPaperConfig
import io.github.black_Kittys22.mortality.AntiCheat.CombatLog.CombatManager
import io.github.black_Kittys22.mortality.AntiCheat.CombatLog.JoinListener
import io.github.black_Kittys22.mortality.AntiCheat.CombatLog.LeaveListener
import io.github.black_Kittys22.mortality.AntiCheat.CombatLog.NpcDamageListener
import io.github.black_Kittys22.mortality.HeartSystem.CombatSystem
import io.github.black_Kittys22.mortality.HeartSystem.HeartSystem
import io.github.black_Kittys22.mortality.Mention.MentionChatListener
import io.github.black_Kittys22.mortality.Mention.settings.MentionSettings
import io.github.black_Kittys22.mortality.Mention.settings.MentionSettingsCommand
import io.github.black_Kittys22.mortality.Mention.settings.MentionSettingsGUI
import io.github.black_Kittys22.mortality.TeamSystem.command.TeamChatCommand
import io.github.black_Kittys22.mortality.TeamSystem.command.TeamCommand
import io.github.black_Kittys22.mortality.TeamSystem.listener.ChatListener
import io.github.black_Kittys22.mortality.TeamSystem.listener.TeamChatListener
import io.github.black_Kittys22.mortality.TeamSystem.listener.Teamlistener
import io.github.black_Kittys22.mortality.TeamSystem.manager.InviteManager
import io.github.black_Kittys22.mortality.TeamSystem.manager.Teammanager
import io.github.black_Kittys22.mortality.language.LanguageCommand
import io.github.black_Kittys22.mortality.language.LanguageManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class Main : JavaPlugin() {

    lateinit var teamManager: Teammanager
        private set
    lateinit var inviteManager: InviteManager
        private set
    lateinit var teamListener: Teamlistener
        private set
    private lateinit var heartSystem: HeartSystem
    private lateinit var combatSystem: CombatSystem
    private lateinit var denySpawnManager: DenySpawnManager
    lateinit var languageManager: LanguageManager
        private set

    override fun onLoad() {
        CommandAPI.onLoad(
            CommandAPIPaperConfig(this)
                .verboseOutput(false)
                .silentLogs(false)
        )
    }

    override fun onEnable() {
        CommandAPI.onEnable()

        // Sprache-System zuerst initialisieren
        languageManager = LanguageManager(this)

        heartSystem = HeartSystem(this, null)
        heartSystem.start()

        val combatManager = CombatManager(this)
        val mentionSettings = MentionSettings(this)


        val mentionSettingsGUI = MentionSettingsGUI(mentionSettings)

        teamManager = Teammanager(this)
        inviteManager = InviteManager(this)
        teamListener = Teamlistener(this)

        server.pluginManager.registerEvents(NpcDamageListener(combatManager), this)
        server.pluginManager.registerEvents(JoinListener(combatManager), this)
        server.pluginManager.registerEvents(ResourcePackListener(), this)
        server.pluginManager.registerEvents(teamListener, this)
        server.pluginManager.registerEvents(TeamChatListener(this), this)
        server.pluginManager.registerEvents(ChatListener(this), this)
        server.pluginManager.registerEvents(mentionSettingsGUI, this)
        getCommand("settings")?.setExecutor(MentionSettingsCommand(this, mentionSettingsGUI))
        server.pluginManager.registerEvents(MentionChatListener(this, mentionSettings), this)

        TeamCommand.register(this)
        TeamChatCommand.register(this)
        denySpawnManager = DenySpawnManager(this)
        LanguageCommand.register(this)
        server.pluginManager.registerEvents(denySpawnManager, this)

        teamListener.updateTablist()
        logger.info("Mortality erfolgreich gestartet")
        logger.info("${teamManager.getAllTeams().size} Team(s) geladen.")

        combatSystem = CombatSystem(this, heartSystem)
        combatSystem.start()
        server.pluginManager.registerEvents(LeaveListener(combatManager, combatSystem), this)

        this.getCommand("sethearts")?.setExecutor(heartSystem)

        this.getCommand("resetworld")?.setExecutor { sender, _, _, _ ->
            if (!sender.hasPermission("mortality.admin")) {
                sender.sendMessage("§cDu hast dazu keine Rechte!")
                return@setExecutor true
            }

            server.worlds.forEach { world ->
                world.isThundering = false
                world.setStorm(false)
                world.clearWeatherDuration = 12000
            }

            sender.sendMessage("§a[Mortality] Alle Weltherzen-Zustände wurden erfolgreich zurückgesetzt! Du kannst sie jetzt wieder neu abbauen.")
            true
        }
    }

    override fun onDisable() {
        CommandAPI.onDisable()
        if (::languageManager.isInitialized) {
            languageManager.savePlayerLanguages()
        }
        if (::teamManager.isInitialized) {
            teamManager.saveTeams()
        }
        logger.info("Mortality deaktiviert.")
    }

    private fun fetchResourcePackHash(url: String): ByteArray {
        val bytes = java.net.URL(url).readBytes()
        return java.security.MessageDigest.getInstance("SHA-1").digest(bytes)
    }

    private inner class ResourcePackListener : Listener {
        private val packUrl = "https://kittys-plugins.net/Mortality.zip"
        private val packHash by lazy { fetchResourcePackHash(packUrl) }

        @EventHandler
        fun onPlayerJoin(event: PlayerJoinEvent) {
            event.player.setResourcePack(
                packUrl,
                packHash,
                Component.text("Das Resourcepack ist erforderlich!", NamedTextColor.RED),
                true
            )
        }
    }
}