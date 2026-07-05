package io.github.black_Kittys22.mortality.language

import io.github.black_Kittys22.mortality.Main
import org.bukkit.entity.Player

// Extension-Funktionen für Player
fun Player.sendLang(key: String, vararg args: Any) {
    val plugin = this.server.pluginManager.getPlugin("Mortality") as? Main
    plugin?.languageManager?.let { langManager ->
        this.sendMessage(langManager.getColoredMessage(this, key, *args))
    }
}

fun Player.sendLangError(key: String, vararg args: Any) {
    val plugin = this.server.pluginManager.getPlugin("Mortality") as? Main
    plugin?.languageManager?.let { langManager ->
        val msg = langManager.getColoredMessage(this, key, *args)
        this.sendMessage("§c$msg")
    }
}

fun Player.sendLangSuccess(key: String, vararg args: Any) {
    val plugin = this.server.pluginManager.getPlugin("Mortality") as? Main
    plugin?.languageManager?.let { langManager ->
        val msg = langManager.getColoredMessage(this, key, *args)
        this.sendMessage("§a$msg")
    }
}

fun Player.sendLangPrefix(key: String, vararg args: Any) {
    val plugin = this.server.pluginManager.getPlugin("Mortality") as? Main
    plugin?.languageManager?.let { langManager ->
        val prefix = langManager.getPrefix(this)
        val msg = langManager.getColoredMessage(this, key, *args)
        this.sendMessage("$prefix$msg")
    }
}