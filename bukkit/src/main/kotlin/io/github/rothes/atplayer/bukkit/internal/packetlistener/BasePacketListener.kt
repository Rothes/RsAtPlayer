package io.github.rothes.atplayer.bukkit.internal.packetlistener

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.reflect.StructureModifier
import io.github.rothes.atplayer.bukkit.RsAtPlayer

abstract class BasePacketListener(
    packetType: PacketType,
    priority: ListenerPriority = ListenerPriority.NORMAL,
): PacketAdapter(RsAtPlayer.plugin, priority, packetType) {

    fun register() = ProtocolLibrary.getProtocolManager().addPacketListener(this)


    operator fun <T> StructureModifier<T>.get(fieldIndex: Int): T = read(fieldIndex)
    operator fun <T> StructureModifier<T>.set(fieldIndex: Int, value: T) = write(fieldIndex, value)!!

    fun <T> StructureModifier<Any>.typed(fieldType: Class<T>): StructureModifier<T> {
        return withType(fieldType, null)
    }

}
