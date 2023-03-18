package io.github.rothes.atplayer.bukkit.internal.packetlistener.tabcomplete

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketEvent
import io.github.rothes.atplayer.bukkit.config.PlayerRelativeAtType
import io.github.rothes.atplayer.bukkit.config.RsAtPlayerConfigManager
import io.github.rothes.atplayer.bukkit.extensions.get
import io.github.rothes.atplayer.bukkit.extensions.set
import io.github.rothes.atplayer.bukkit.internal.TabCompletionsHandler
import io.github.rothes.atplayer.bukkit.internal.util.CompatibilityUtils
import io.github.rothes.atplayer.bukkit.user.UserManager
import org.bukkit.Bukkit
import java.util.*

class RemovePost19R2 : BaseTabCompletePacketListener(PacketType.Play.Server.PLAYER_INFO_REMOVE) {

    override fun onPacketSending(event: PacketEvent) {
        val uuidsRaw = event.packet.modifier.withType<List<UUID>>(List::class.java)[0]

        val names = mutableListOf<String>().apply {
            uuidsRaw.forEach {
                Bukkit.getOfflinePlayer(it).run {
                    if (hasPlayedBefore()) add(name!!)
                }
            }
        }

        val user = UserManager[event.player]
        if (CompatibilityUtils.supportCustomCompletions(event.player)) {
            TabCompletionsHandler.removeChatCompletions(event.player, mutableListOf<String>().apply {
                RsAtPlayerConfigManager.data.atTypes.forEach {
                    if (!it.recommendGroup.addRecommend) return@forEach
                    when (it) {
                        is PlayerRelativeAtType -> {
                            for (name in names) {
                                val format = it.format.replace("<\$PlayerName>", name)
                                user.removeRecommend(format)?.let { add(format) }
                            }
                        }
                    }
                }
            })
            return
        }

        // Legacy method
        val modifiedList = ArrayList(uuidsRaw)
        RsAtPlayerConfigManager.data.atTypes.forEach { atType ->
            if (!(atType.recommendGroup.addRecommend && atType.recommendGroup.addForLegacy)) return@forEach
            when (atType) {
                is PlayerRelativeAtType ->
                    for (name in names)
                        user.removeRecommend(name)?.let {
                            modifiedList.add(it as UUID)
                        }
            }
        }
        event.packet.modifier.withType<List<UUID>>(List::class.java)[0] = modifiedList
    }

}