package io.github.black_Kittys22.mortality.language

import io.github.black_Kittys22.mortality.Main
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.File
import java.util.UUID

class LanguageManager(private val plugin: Main) {

    private val playerLanguages = mutableMapOf<UUID, String>()
    private val languages = mutableMapOf<String, YamlConfiguration>()
    private val DEFAULT_LANGUAGE = "de"
    private val languageNames = mutableMapOf<String, String>()

    init {
        loadLanguages()
        loadPlayerLanguages()
    }

    private fun loadLanguages() {
        val languagesFolder = File(plugin.dataFolder, "languages")
        if (!languagesFolder.exists()) {
            languagesFolder.mkdirs()
            plugin.saveResource("languages/de.yml", false)
            plugin.saveResource("languages/en.yml", false)
        }

        val files = languagesFolder.listFiles { _, name -> name.endsWith(".yml") }
        if (files != null) {
            for (file in files) {
                try {
                    val config = YamlConfiguration.loadConfiguration(file)
                    val code = file.nameWithoutExtension
                    val name = config.getString("name") ?: code
                    languages[code] = config
                    languageNames[code] = name
                    plugin.logger.info("Sprache geladen: $name ($code)")
                } catch (e: Exception) {
                    plugin.logger.warning("Fehler beim Laden von ${file.name}: ${e.message}")
                }
            }
        }

        if (languages.isEmpty()) {
            plugin.logger.warning("Keine Sprachen geladen! Erstelle Default-Konfiguration.")
            val defaultConfig = YamlConfiguration()
            defaultConfig.set("name", "Deutsch")
            defaultConfig.set("code", "de")
            languages["de"] = defaultConfig
            languageNames["de"] = "Deutsch"
        }

        plugin.logger.info("${languages.size} Sprachen geladen.")
    }

    private fun loadPlayerLanguages() {
        val dataFile = File(plugin.dataFolder, "player_languages.yml")
        if (!dataFile.exists()) return

        try {
            val config = YamlConfiguration.loadConfiguration(dataFile)
            for (key in config.getKeys(false)) {
                try {
                    val uuid = UUID.fromString(key)
                    val lang = config.getString(key) ?: DEFAULT_LANGUAGE
                    if (languages.containsKey(lang)) {
                        playerLanguages[uuid] = lang
                    }
                } catch (e: IllegalArgumentException) {
                    // Ignore invalid UUIDs
                }
            }
        } catch (e: Exception) {
            plugin.logger.warning("Fehler beim Laden der Spieler-Sprachen: ${e.message}")
        }
    }

    fun savePlayerLanguages() {
        try {
            val dataFile = File(plugin.dataFolder, "player_languages.yml")
            val config = YamlConfiguration()
            for ((uuid, lang) in playerLanguages) {
                config.set(uuid.toString(), lang)
            }
            config.save(dataFile)
        } catch (e: Exception) {
            plugin.logger.warning("Fehler beim Speichern der Spieler-Sprachen: ${e.message}")
        }
    }

    fun getLanguage(player: Player): String {
        return playerLanguages[player.uniqueId] ?: DEFAULT_LANGUAGE
    }

    fun getLanguageDisplayName(player: Player): String {
        val lang = getLanguage(player)
        return languageNames[lang] ?: lang
    }

    fun setLanguage(player: Player, languageCode: String): Boolean {
        if (!languages.containsKey(languageCode)) return false
        playerLanguages[player.uniqueId] = languageCode
        savePlayerLanguages()
        return true
    }

    fun getAvailableLanguages(): Map<String, String> = languageNames

    fun getMessage(player: Player, key: String, vararg args: Any): String {
        val lang = getLanguage(player)
        return getMessage(lang, key, *args)
    }

    fun getMessage(languageCode: String, key: String, vararg args: Any): String {
        var message = languages[languageCode]?.getString("messages.$key")
            ?: languages[DEFAULT_LANGUAGE]?.getString("messages.$key")
            ?: "Message not found: $key"

        for (i in args.indices) {
            val value = args[i].toString()
            message = message.replace("%${i + 1}\$s", value)
            message = message.replace("%${i + 1}\$d", value)
            if (i == 0) {
                message = message.replace("%s", value)
                message = message.replace("%d", value)
            }
        }
        return message
    }

    fun getColoredMessage(player: Player, key: String, vararg args: Any): String {
        val message = getMessage(player, key, *args)
        return message.replace("&", "§")
    }

    fun getColoredMessage(languageCode: String, key: String, vararg args: Any): String {
        val message = getMessage(languageCode, key, *args)
        return message.replace("&", "§")
    }

    fun getActionBar(player: Player, hearts: Double): String {
        val lang = getLanguage(player)
        val key = when {
            hearts >= 3.0 -> "hearts_actionbar.3"
            hearts >= 2.0 -> "hearts_actionbar.2"
            hearts >= 1.0 -> "hearts_actionbar.1"
            else -> "hearts_actionbar.0"
        }
        return languages[lang]?.getString("messages.$key")
            ?: languages[DEFAULT_LANGUAGE]?.getString("messages.$key")
            ?: ""
    }

    fun getPrefix(player: Player): String {
        val lang = getLanguage(player)
        return languages[lang]?.getString("messages.prefix")
            ?: languages[DEFAULT_LANGUAGE]?.getString("messages.prefix")
            ?: "§8[§6Mortality§8] §r"
    }
}