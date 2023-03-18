package io.github.rothes.atplayer.bukkit.internal.packetlistener.chat

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.wrappers.WrappedChatComponent
import io.github.rothes.atplayer.bukkit.extensions.get
import io.github.rothes.atplayer.bukkit.extensions.set
import io.github.rothes.atplayer.bukkit.internal.util.ServerComponentConverter
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer

class Chat : BaseSystemChatPacketListener(PacketType.Play.Server.CHAT) {

    override fun process(event: PacketEvent) {
        val componentStructureModifier = event.packet.chatComponents

        componentStructureModifier[0]?.let {
            val original = GsonComponentSerializer.gson().deserialize(it.json)
            componentStructureModifier[0] = WrappedChatComponent.fromJson(GsonComponentSerializer.gson().serialize(handleAtTypes(searchSender(original).player, event.receiver, original)))
        } ?: run {
            val modifier = event.packet.modifier
            ServerComponentConverter.getSpigotComponents(modifier)?.let {
                ServerComponentConverter.setSpigotComponents(modifier, handleAtTypes(searchSender(it).player, event.receiver, it))
            } ?: ServerComponentConverter.getPaperComponent(modifier)!!.let {
                ServerComponentConverter.setPaperComponent(modifier, handleAtTypes(searchSender(it).player, event.receiver, it))
            }
        }
    }

}