package io.github.rothes.atplayer.bukkit.internal.packetlistener

import com.comphenix.protocol.ProtocolLibrary
import io.github.rothes.atplayer.bukkit.internal.packetlistener.chat.PlayerChatPost19
import io.github.rothes.atplayer.bukkit.internal.packetlistener.chat.PlayerChatPost19R1
import io.github.rothes.atplayer.bukkit.internal.packetlistener.chat.PlayerChatPost19R2
import io.github.rothes.atplayer.bukkit.internal.packetlistener.tabcomplete.RemovePost19R2
import io.github.rothes.atplayer.bukkit.internal.packetlistener.tabcomplete.Update
import io.github.rothes.atplayer.bukkit.internal.packetlistener.tabcomplete.UpdatePost19R2
import io.github.rothes.atplayer.bukkit.internal.plugin
import io.github.rothes.rslib.bukkit.util.VersionUtils.serverMajorVersion
import io.github.rothes.rslib.bukkit.util.VersionUtils.serverMinorVersion

object PacketListenerManager {

    fun register() {
        if (serverMajorVersion == 19.toByte() && serverMinorVersion >= 3
            || serverMajorVersion >= 20) {
            PlayerChatPost19R2().register()
        } else if (serverMajorVersion == 19.toByte() && serverMinorVersion >= 1){
            PlayerChatPost19R1().register()
        } else if (serverMajorVersion == 19.toByte()){
            PlayerChatPost19().register()
        }

        if (serverMajorVersion == 19.toByte() && serverMinorVersion >= 3
            || serverMajorVersion >= 20) {
            UpdatePost19R2().register()
            RemovePost19R2().register()
        } else {
            Update().register()
        }
    }

    fun unregister() {
        ProtocolLibrary.getProtocolManager().removePacketListeners(plugin)
    }
}