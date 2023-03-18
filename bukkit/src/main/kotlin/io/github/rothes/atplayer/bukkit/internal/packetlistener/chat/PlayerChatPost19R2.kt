package io.github.rothes.atplayer.bukkit.internal.packetlistener.chat

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.utility.MinecraftReflection
import io.github.rothes.atplayer.bukkit.RsAtPlayer
import io.github.rothes.atplayer.bukkit.extensions.get
import io.github.rothes.atplayer.bukkit.extensions.set
import io.github.rothes.atplayer.bukkit.extensions.typed
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.lang.reflect.Field
import java.util.*

class PlayerChatPost19R2 : BasePlayerChatPost19() {

    private val type: Field by lazy {
        PacketType.Play.Server.CHAT.packetClass.declaredFields.first {
            it.type.declaringClass == MinecraftReflection.getMinecraftClass("network.chat.ChatMessageType")
        }.apply { isAccessible = true }
    }
    private val typeId: Field =
        type.type.declaredFields.first { it.type == Int::class.java }.apply { isAccessible = true }

//    private val msgBody: Field = PacketType.Play.Server.CHAT.packetClass.declaredFields.first {
//        it.type.declaringClass == MinecraftReflection.getMinecraftClass("network.chat.SignedMessageBody")
//    }.apply { isAccessible = true }
//    private val msg: Field = msgBody.type.declaredFields.first { it.type == String::class.java }.apply { isAccessible = true }

    override fun handleComponent(event: PacketEvent) {
        event.packet.chatComponents[0] = handleComponent(event, event.packet.chatComponents[0])
    }

    override val PacketEvent.chatType
        get() = when (typeId[type[packet.handle]]) {
            0    -> ChatType.PLAYER_CHAT
            1    -> ChatType.SAY
            2    -> ChatType.MSG_INCOMING
            3    -> ChatType.MSG_OUTGOING
            4    -> ChatType.TEAM_MSG_INCOMING
            5    -> ChatType.TEAM_MSG_OUTGOING
            6    -> ChatType.EMOTE
            7    -> ChatType.TELLRAW
            else -> {
                RsAtPlayer.plugin.warn("Unknown player chat type: ${typeId[type[packet.handle]]}")
                ChatType.SYSTEM_CHAT
            }
        }

    override val PacketEvent.sender: Player?
        get() = Bukkit.getPlayer(packet.modifier.typed(UUID::class.java).read(0))

}