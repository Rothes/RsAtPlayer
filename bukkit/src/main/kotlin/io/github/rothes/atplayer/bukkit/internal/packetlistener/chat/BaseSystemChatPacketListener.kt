package io.github.rothes.atplayer.bukkit.internal.packetlistener.chat

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.TranslatableComponent
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.lang.UnsupportedOperationException

abstract class BaseSystemChatPacketListener(
    packetType: PacketType,
    priority: ListenerPriority = ListenerPriority.LOW,
) : BaseChatPacketListener(packetType, priority) {

    protected fun searchSender(msg: Component, fInBracket: Boolean = false): SearchResult {
        var bracket = fInBracket
        when (msg) {
            is TextComponent         -> {
                if (bracket) {
                    matchPlayer(msg.content())?.let { return SearchResult(it, true) }
                } else {
                    var matching = false
                    var builder: StringBuilder? = null
                    for (c in msg.content()) {
                        if (c == '<') {
                            bracket = true

                            matching = true
                            builder = StringBuilder()
                        } else if (c == '>') {
                            bracket = false

                            if (matching) {
                                matchPlayer(builder.toString())?.let { return SearchResult(it, true) }
                                matching = false
                            }
                        } else if (matching) {
                            builder!!.append(c)
                        }
                    }
                }
            }

            is TranslatableComponent -> {
                for (arg in msg.args()) {
                    with (searchSender(arg, bracket)) {
                        player?.let { return this }
                        bracket = inBracket
                    }
                }
            }
        }
        for (child in msg.children()) {
            with (searchSender(child, bracket)) {
                player?.let { return this }
                bracket = inBracket
            }
        }
        return SearchResult(null, bracket)
    }

    private fun matchPlayer(string: String): Player? {
        for (player in Bukkit.getOnlinePlayers()) {
            if (string == player.name || string == player.displayName) {
                return player
            }
        }
        return null
    }

    class SearchResult(val player: Player?, val inBracket: Boolean)


    override val PacketEvent.sender: Player?
        get() = throw UnsupportedOperationException("Property is not supported for this packet.")

}