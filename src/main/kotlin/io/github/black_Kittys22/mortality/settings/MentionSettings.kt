package io.github.black_Kittys22.mortality.Mention.settings

import io.github.black_Kittys22.mortality.Main
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.util.UUID

class MentionSettings(private val plugin: Main) {

    private val settingsFile = File(plugin.dataFolder, "mention_settings.yml")
    private val config = YamlConfiguration()

    // Cache: UUID → Sound an/aus
    private val soundEnabled = mutableMapOf<UUID, Boolean>()

    init {
        load()
    }

    fun isSoundEnabled(uuid: UUID): Boolean {
        return soundEnabled.getOrDefault(uuid, true) // Standard: an
    }

    fun setSoundEnabled(uuid: UUID, enabled: Boolean) {
        soundEnabled[uuid] = enabled
        save()
    }

    fun toggleSound(uuid: UUID): Boolean {
        val newValue = !isSoundEnabled(uuid)
        setSoundEnabled(uuid, newValue)
        return newValue
    }

    private fun load() {
        if (!settingsFile.exists()) return
        config.load(settingsFile)

        config.getKeys(false).forEach { key ->
            val uuid = UUID.fromString(key)
            soundEnabled[uuid] = config.getBoolean("$key.sound", true)
        }
    }

    private fun save() {
        soundEnabled.forEach { (uuid, enabled) ->
            config.set("$uuid.sound", enabled)
        }
        config.save(settingsFile)
    }
}