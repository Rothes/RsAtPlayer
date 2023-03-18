package io.github.rothes.atplayer.bukkit.internal.packetlistener.tabcomplete

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.wrappers.EnumWrappers
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerInfoAction
import com.comphenix.protocol.wrappers.PlayerInfoData
import com.comphenix.protocol.wrappers.WrappedChatComponent
import com.comphenix.protocol.wrappers.WrappedGameProfile
import io.github.rothes.atplayer.bukkit.config.PlayerRelativeAtType
import io.github.rothes.atplayer.bukkit.config.RsAtPlayerConfigManager
import io.github.rothes.atplayer.bukkit.extensions.get
import io.github.rothes.atplayer.bukkit.extensions.set
import io.github.rothes.atplayer.bukkit.internal.APCache
import io.github.rothes.atplayer.bukkit.user.UserManager
import io.github.rothes.rslib.bukkit.extensions.replacep
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import org.bukkit.Bukkit
import java.util.*

class Update : BaseTabCompletePacketListener(PacketType.Play.Server.PLAYER_INFO) {

    @Suppress("DEPRECATION") // For 1.8-1.18 Support
    override fun onPacketSending(event: PacketEvent) {
        val packet = event.packet
        val action = packet.playerInfoAction[0]
        if (action != PlayerInfoAction.ADD_PLAYER && action != PlayerInfoAction.REMOVE_PLAYER) return

        val infoList = packet.playerInfoDataLists[0]
        val names = mutableListOf<String>().apply {
            infoList.forEach {
                Bukkit.getPlayer(it.profileId)?.run {
                    add(name)
                }
            }
        }
        val modifiedList = ArrayList(infoList)

        val user = UserManager[event.player]
        RsAtPlayerConfigManager.data.atTypes.forEach { atType ->
            if (!(atType.recommendGroup.addRecommend && atType.recommendGroup.addForLegacy)) return@forEach
            when (atType) {
                is PlayerRelativeAtType ->
                    for (name in names) {
                        val format = atType.format.replacep("PlayerName", name)
                        if (!user.hasRecommend(format)) {
                            user.addRecommend(format)
                            modifiedList.add(createInfo(format, atType.recommendGroup.tabName))
                        }
                    }
            }
        }
        packet.playerInfoDataLists[0] = modifiedList
    }

    override fun generateInfo(format: String, component: Component): PlayerInfoData {
        return PlayerInfoData(
            WrappedGameProfile(APCache.getFakeUuid(format), format),
            9999,
            EnumWrappers.NativeGameMode.SPECTATOR,
            WrappedChatComponent.fromJson(GsonComponentSerializer.gson().serialize(component)),
        )
    }

}