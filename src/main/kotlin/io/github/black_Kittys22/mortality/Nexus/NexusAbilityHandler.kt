package io.github.black_Kittys22.mortality.Nexus

import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

class NexusAbilityHandler(private val plugin: JavaPlugin) : Listener {

    private val activeAbilities = mutableMapOf<String, MutableSet<String>>()

}