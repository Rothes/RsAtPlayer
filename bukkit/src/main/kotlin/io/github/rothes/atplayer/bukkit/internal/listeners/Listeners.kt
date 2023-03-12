package io.github.rothes.atplayer.bukkit.internal.listeners

import io.github.rothes.atplayer.bukkit.internal.APCache
import io.github.rothes.atplayer.bukkit.user.UserManager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class Listeners: Listener {

    @EventHandler
    fun onChat(event: AsyncPlayerChatEvent) {
        // We want to force TELLRAW chat type on 1.19+, so we can modify the components but not just a text string.
        event.format += " "
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        UserManager.removeUser(event.player.uniqueId)
        APCache.pingNames.add("@" + event.player.name)
        APCache.mentionNames.add(event.player.name)
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        UserManager.removeUser(event.player.uniqueId)
        APCache.pingNames.remove("@" + event.player.name)
        APCache.mentionNames.remove(event.player.name)
    }

}