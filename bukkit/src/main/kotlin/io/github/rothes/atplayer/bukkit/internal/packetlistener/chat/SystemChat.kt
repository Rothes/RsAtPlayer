package io.github.rothes.atplayer.bukkit.internal.packetlistener.chat

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketEvent
import io.github.rothes.atplayer.bukkit.extensions.get
import io.github.rothes.atplayer.bukkit.extensions.set
import io.github.rothes.atplayer.bukkit.internal.util.ServerComponentConverter
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer

class SystemChat : BaseSystemChatPacketListener(PacketType.Play.Server.SYSTEM_CHAT) {

    override fun process(event: PacketEvent) {
        val strings = event.packet.strings

        strings[0]?.let {
            val original = GsonComponentSerializer.gson().deserialize(it)
            strings[0] = GsonComponentSerializer.gson().serialize(handleAtTypes(searchSender(original).player, event.receiver, original))
        } ?: run {
            val modifier = event.packet.modifier
            val original = ServerComponentConverter.getPaperComponent(modifier)!!
            ServerComponentConverter.setPaperComponent(modifier, handleAtTypes(searchSender(original).player, event.receiver, original))
        }
    }

}