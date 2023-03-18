package io.github.rothes.atplayer.bukkit.internal.packetlistener.chat

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.utility.MinecraftReflection
import io.github.rothes.atplayer.bukkit.RsAtPlayer
import io.github.rothes.atplayer.bukkit.extensions.get
import io.github.rothes.atplayer.bukkit.extensions.set
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.lang.reflect.Field
import java.util.*

class PlayerChatPost19 : BasePlayerChatPost19() {

    private val senderField: Field = PacketType.Play.Server.CHAT.packetClass.declaredFields.first {
        it.type.declaringClass == MinecraftReflection.getMinecraftClass("network.chat.ChatSender")
    }.apply { isAccessible = true }
    private val uuid: Field =
        senderField.type.declaredFields.first { it.type == UUID::class.java }.apply { isAccessible = true }

    override fun handleComponent(event: PacketEvent) {
        event.packet.chatComponents[0] = handleComponent(event, event.packet.chatComponents[0])
    }

    override val PacketEvent.chatType
        get() = when (packet.integers[0].toInt()) {
            0    -> ChatType.PLAYER_CHAT
            1    -> ChatType.SYSTEM_CHAT
            2    -> ChatType.GAME_INFO
            3    -> ChatType.SAY
            4    -> ChatType.MSG_INCOMING
            5    -> ChatType.TEAM_MSG_INCOMING
            6    -> ChatType.EMOTE
            7    -> ChatType.TELLRAW
            else -> {
                RsAtPlayer.plugin.warn("Unknown player chat type: ${packet.integers[0]}")
                ChatType.SYSTEM_CHAT
            }
        }

    override val PacketEvent.sender: Player?
        get() = Bukkit.getPlayer(uuid[senderField[packet.handle]] as UUID)

}