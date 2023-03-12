package io.github.rothes.atplayer.bukkit.internal.packetlistener.tabcomplete

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketEvent
import io.github.rothes.atplayer.bukkit.config.RsAtPlayerConfigManager
import io.github.rothes.atplayer.bukkit.internal.APCache
import io.github.rothes.atplayer.bukkit.internal.util.CompatibilityUtils
import io.github.rothes.atplayer.bukkit.user.UserManager
import org.bukkit.Bukkit
import java.util.*

class RemovePost19R2 : BaseTabCompletePacketListener(PacketType.Play.Server.PLAYER_INFO_REMOVE) {

    override fun onPacketSending(event: PacketEvent) {
        val uuids = event.packet.modifier.withType<List<UUID>>(List::class.java)[0]
        if (CompatibilityUtils.supportCustomCompletions(event.player)) {
            removeChatCompletions(event.player, mutableListOf<String>().apply {
                for (uuid in uuids) {
                    Bukkit.getOfflinePlayer(uuid).run {
                        if (hasPlayedBefore())
                            add("@${this.name}")
                    }
                }

                // Custom formats
                val all = mutableListOf<String>().apply {
                    RsAtPlayerConfigManager.data.customTypes.forEach {
                        addAll(it.formats)
                    }
                }
                val user = UserManager[event.player].addedCustomRecommends
                for (recommend in user) {
                    if (!all.contains(recommend)) {
                        user.remove(recommend)
                        add(recommend)
                    }
                }
            })
            return
        }

        // Legacy method
        val modifiedList = ArrayList(uuids)

        for (uuid in uuids) {
            APCache.getFakeUuidIfPresent(uuid)?.run {
                modifiedList.add(this)
            }
        }

        // Custom formats
        val all = mutableListOf<String>().apply {
            RsAtPlayerConfigManager.data.customTypes.forEach {
                addAll(it.formats)
            }
        }
        val user = UserManager[event.player].addedCustomRecommends
        for (recommend in user) {
            if (!all.contains(recommend)) {
                user.remove(recommend)
                modifiedList.add(APCache.getFakeUuid(recommend))
            }
        }
        event.packet.modifier.withType<List<UUID>>(List::class.java)[0] = modifiedList
    }

}