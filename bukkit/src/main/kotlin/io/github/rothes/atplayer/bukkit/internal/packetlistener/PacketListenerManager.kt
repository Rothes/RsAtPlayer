package io.github.rothes.atplayer.bukkit.internal.packetlistener

import com.comphenix.protocol.ProtocolLibrary
import io.github.rothes.atplayer.bukkit.internal.packetlistener.chat.Chat
import io.github.rothes.atplayer.bukkit.internal.packetlistener.chat.PlayerChatPost19
import io.github.rothes.atplayer.bukkit.internal.packetlistener.chat.PlayerChatPost19R1
import io.github.rothes.atplayer.bukkit.internal.packetlistener.chat.PlayerChatPost19R2
import io.github.rothes.atplayer.bukkit.internal.packetlistener.chat.SystemChat
import io.github.rothes.atplayer.bukkit.internal.packetlistener.tabcomplete.RemovePost19R2
import io.github.rothes.atplayer.bukkit.internal.packetlistener.tabcomplete.Update
import io.github.rothes.atplayer.bukkit.internal.packetlistener.tabcomplete.UpdatePost19R2
import io.github.rothes.atplayer.bukkit.internal.plugin
import io.github.rothes.rslib.bukkit.util.VersionUtils
import io.github.rothes.rslib.bukkit.util.version.VersionRange

object PacketListenerManager {

    fun register() {
        regListener(VersionRange("1.19.3", "2"     )) { PlayerChatPost19R2() }
        regListener(VersionRange("1.19.1", "1.19.2")) { PlayerChatPost19R1() }
        regListener(VersionRange("1.19"  , "1.19"  )) { PlayerChatPost19() }

        regListener(VersionRange("1.19"  , "2"     )) { SystemChat() }
        regListener(VersionRange("1.8"   , "1.18.2")) { Chat() }


        regListener(VersionRange("1.19.2", "2"     )) { UpdatePost19R2() }
        regListener(VersionRange("1.19.2", "2"     )) { RemovePost19R2() }
        regListener(VersionRange("1.8"   , "1.19.1")) { Update() }
    }

    fun unregister() {
        ProtocolLibrary.getProtocolManager().removePacketListeners(plugin)
    }

    private fun regListener(range: VersionRange, action: () -> Unit) {
        if (range.matches(VersionUtils.serverVersion))
            action.invoke()
    }

}