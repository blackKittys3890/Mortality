package io.github.black_Kittys22.mortality.language

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.executors.PlayerCommandExecutor
import io.github.black_Kittys22.mortality.Main

object LanguageCommand {

    fun register(plugin: Main) {
        val langManager = plugin.languageManager

        CommandAPICommand("language")
            .withAliases("lang", "sprache")
            .withArguments(
                StringArgument("sprache")
                    .replaceSuggestions(
                        ArgumentSuggestions.strings { _ ->
                            langManager.getAvailableLanguages().keys.toTypedArray()
                        }
                    )
                    .setOptional(true)
            )
            .executesPlayer(PlayerCommandExecutor { player, args ->
                if (args.count() == 0) {
                    val currentLang = langManager.getLanguage(player)
                    val langName = langManager.getAvailableLanguages()[currentLang] ?: currentLang
                    player.sendMessage(langManager.getColoredMessage(player, "help_language_current", langName))
                    val available = langManager.getAvailableLanguages()
                        .map { (code, name) -> "$name ($code)" }
                        .joinToString(", ")
                    player.sendMessage(langManager.getColoredMessage(player, "help_language_available", available))
                    player.sendMessage(langManager.getColoredMessage(player, "help_language_usage"))
                    return@PlayerCommandExecutor
                }
                val langCode = args[0] as String
                if (!langManager.getAvailableLanguages().containsKey(langCode)) {
                    player.sendMessage(langManager.getColoredMessage(player, "language_not_available"))
                    return@PlayerCommandExecutor
                }
                if (langManager.setLanguage(player, langCode)) {
                    val langName = langManager.getAvailableLanguages()[langCode] ?: langCode
                    player.sendMessage(langManager.getColoredMessage(langCode, "language_changed", langName))
                }
            })
            .register(plugin)
    }
}