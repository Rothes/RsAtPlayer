package io.github.rothes.atplayer.bukkit.internal.listeners

import io.github.rothes.atplayer.bukkit.internal.APCache
import io.github.rothes.atplayer.bukkit.internal.TabCompletionsHandler
import io.github.rothes.atplayer.bukkit.user.UserManager
import io.github.rothes.rslib.bukkit.extensions.replacep
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
        for ((type, set) in APCache.playerRelative) {
            set.add(type.format.replacep("PlayerName", event.player.name))
        }
        TabCompletionsHandler.addCustomRecommends(event.player)
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        UserManager.removeUser(event.player.uniqueId)
        for ((type, set) in APCache.playerRelative) {
            set.remove(type.format.replacep("PlayerName", event.player.name))
        }
    }

}