package io.github.rothes.atplayer.bukkit.internal.packetlistener.tabcomplete

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.wrappers.PlayerInfoData
import io.github.rothes.atplayer.bukkit.internal.packetlistener.BasePacketListener
import io.github.rothes.atplayer.bukkit.internal.util.CompatibilityUtils
import io.github.rothes.rslib.bukkit.config.ComponentType
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import java.lang.UnsupportedOperationException
import java.util.*

abstract class BaseTabCompletePacketListener(
    packetType: PacketType,
    priority: ListenerPriority = ListenerPriority.LOW,
) : BasePacketListener(packetType, priority) {

    protected fun createInfo(string: String, component: ComponentType, papiPlayer: OfflinePlayer? = null): PlayerInfoData {
        return generateInfo(string, component.type.getComponent(
            CompatibilityUtils.parsePapi(papiPlayer, component.message.replace("<\$Name>", string))
        ))
    }

    open fun generateInfo(format: String, component: Component): PlayerInfoData {
        throw UnsupportedOperationException("Not implemented yet.")
    }

}