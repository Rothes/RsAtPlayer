package io.github.rothes.atplayer.bukkit.internal.packetlistener

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import io.github.rothes.atplayer.bukkit.RsAtPlayer

abstract class BasePacketListener(
    packetType: PacketType,
    priority: ListenerPriority = ListenerPriority.NORMAL,
): PacketAdapter(RsAtPlayer.plugin, priority, packetType) {

    init {
        register()
    }

    private fun register() {
        ProtocolLibrary.getProtocolManager().addPacketListener(this)
    }

}
