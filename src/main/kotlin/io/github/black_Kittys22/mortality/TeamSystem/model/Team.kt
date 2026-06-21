package io.github.black_Kittys22.mortality.TeamSystem.model

import java.util.UUID

data class Team(
    val name: String,
    val displayName: String,
    val colorCode: String,
    val leader: UUID,
    val members: MutableSet<UUID> = mutableSetOf(leader)
) {
    fun coloredName(): String = "$colorCode$displayName§r"
    fun isLeader(uuid: UUID) = uuid == leader
    fun isMember(uuid: UUID) = uuid in members

    companion object {
        val AVAILABLE_COLORS: Map<String, String> = mapOf(
            "rot"          to "§c",
            "dunkelrot"    to "§4",
            "gruen"        to "§a",
            "dunkelgruen"  to "§2",
            "blau"         to "§9",
            "dunkelblau"   to "§1",
            "aqua"         to "§b",
            "gelb"         to "§e",
            "gold"         to "§6",
            "lila"         to "§5",
            "pink"         to "§d",
            "weiss"        to "§f",
            "grau"         to "§7",
            "schwarz"      to "§0"
        )
    }
}