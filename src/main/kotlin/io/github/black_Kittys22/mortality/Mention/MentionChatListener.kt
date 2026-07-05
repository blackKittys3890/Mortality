package io.github.black_Kittys22.mortality.Mention

import io.github.black_Kittys22.mortality.Main
import io.github.black_Kittys22.mortality.Mention.settings.MentionSettings
import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

class MentionChatListener(private val plugin: Main, private val settings: MentionSettings) : Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onChat(event: AsyncChatEvent) {
        val rawMessage = PlainTextComponentSerializer.plainText().serialize(event.message())
        val mentionedPlayers = plugin.server.onlinePlayers.filter { player ->
            player != event.player && rawMessage.contains(player.name, ignoreCase = true)
        }
        if (mentionedPlayers.isEmpty()) return

        val highlightedMessage = buildHighlightedMessage(rawMessage, mentionedPlayers.map { it.name })
        event.message(highlightedMessage)
        plugin.server.scheduler.runTask(plugin, Runnable {
            mentionedPlayers.forEach { player ->
                if (settings.isSoundEnabled(player.uniqueId)) {
                    player.playSound(
                        player.location,
                        org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING,
                        2.0f,
                        1.5f
                    )
                }
            }
        })
    }

    private fun buildHighlightedMessage(rawMessage: String, playerNames: List<String>): TextComponent {
        val result = Component.text()
        var remaining = rawMessage
        while (remaining.isNotEmpty()) {
            val match = playerNames
                .mapNotNull { name ->
                    val idx = remaining.lowercase().indexOf(name.lowercase())
                    if (idx != -1) idx to name else null
                }
                .minByOrNull { it.first }

            if (match == null) {
                result.append(Component.text(remaining))
                break
            }
            val (index, name) = match
            if (index > 0) {
                result.append(Component.text(remaining.substring(0, index)))
            }
            val nameSegment = remaining.substring(index, index + name.length)
            result.append(
                Component.text(nameSegment)
                    .color(NamedTextColor.YELLOW)
                    .decorate(TextDecoration.BOLD)
            )
            remaining = remaining.substring(index + name.length)
        }
        return result.build()
    }
}