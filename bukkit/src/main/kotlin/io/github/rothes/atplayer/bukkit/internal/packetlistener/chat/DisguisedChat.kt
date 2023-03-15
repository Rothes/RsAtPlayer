package io.github.rothes.atplayer.bukkit.internal.packetlistener.chat

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.wrappers.WrappedChatComponent
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer

class DisguisedChat : BaseSystemChatPacketListener(PacketType.Play.Server.DISGUISED_CHAT) {

    override fun process(event: PacketEvent) {
        val componentStructureModifier = event.packet.chatComponents

        val original = GsonComponentSerializer.gson().deserialize(componentStructureModifier[0].json)
        componentStructureModifier[0] = WrappedChatComponent.fromJson(GsonComponentSerializer.gson().serialize(handleAtTypes(searchSender(original).player, event.receiver, original)))
    }

}