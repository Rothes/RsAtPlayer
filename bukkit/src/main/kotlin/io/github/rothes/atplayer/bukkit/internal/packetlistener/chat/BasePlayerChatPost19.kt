package io.github.rothes.atplayer.bukkit.internal.packetlistener.chat

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.wrappers.WrappedChatComponent
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer

abstract class BasePlayerChatPost19 : BaseChatPacketListener(PacketType.Play.Server.CHAT) {

    fun handleComponent(event: PacketEvent, component: WrappedChatComponent): WrappedChatComponent =
        WrappedChatComponent.fromJson(
            GsonComponentSerializer.gson().serialize(
                handleAtTypes(
                    event.sender,
                    event.receiver,
                    GsonComponentSerializer.gson().deserialize(component.json)
                )
            )
        )

    final override fun process(event: PacketEvent) {
        if (event.chatType != ChatType.TELLRAW) {
            return
        }
        handleComponent(event)
    }

    abstract fun handleComponent(event: PacketEvent)

    abstract val PacketEvent.chatType: ChatType

    enum class ChatType {
        PLAYER_CHAT,
        SYSTEM_CHAT,
        GAME_INFO,
        SAY,
        MSG_INCOMING,
        MSG_OUTGOING,
        TEAM_MSG_INCOMING,
        TEAM_MSG_OUTGOING,
        EMOTE,
        TELLRAW
    }

}