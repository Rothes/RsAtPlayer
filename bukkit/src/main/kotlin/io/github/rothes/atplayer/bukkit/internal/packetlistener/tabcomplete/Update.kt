package io.github.rothes.atplayer.bukkit.internal.packetlistener.tabcomplete

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.wrappers.EnumWrappers
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerInfoAction
import com.comphenix.protocol.wrappers.PlayerInfoData
import com.comphenix.protocol.wrappers.WrappedChatComponent
import com.comphenix.protocol.wrappers.WrappedGameProfile
import io.github.rothes.atplayer.bukkit.config.RsAtPlayerConfigManager
import io.github.rothes.atplayer.bukkit.internal.APCache
import io.github.rothes.atplayer.bukkit.internal.util.CompatibilityUtils
import io.github.rothes.atplayer.bukkit.user.UserManager
import io.github.rothes.rslib.bukkit.config.ComponentType
import io.github.rothes.rslib.bukkit.extensions.MessageType
import io.github.rothes.rslib.bukkit.util.version.VersionRange
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
        val modifiedList = ArrayList(infoList)

        if (CompatibilityUtils.supportCustomCompletions(event.player)) {
            when (action) {
                PlayerInfoAction.ADD_PLAYER -> {
                    addChatCompletions(event.player, mutableListOf<String>().apply {
                        if (RsAtPlayerConfigManager.data.pingRecommendGroup.addRecommend) {
                            for (info in infoList) {
                                Bukkit.getPlayer(info.profileId)?.run {
                                    add("@${this.name}")
                                }
                            }
                        }
                        // Custom formats
                        val all = mutableListOf<String>().apply {
                            RsAtPlayerConfigManager.data.customTypes.forEach {
                                if (it.recommendGroup.addRecommend) addAll(it.formats)
                            }
                        }
                        val user = UserManager[event.player].addedCustomRecommends
                        for (recommend in all) {
                            if (!user.contains(recommend)) {
                                user.add(recommend)
                                add(recommend)
                            }
                        }
                    })
                }
                PlayerInfoAction.REMOVE_PLAYER -> {
                    removeChatCompletions(event.player, mutableListOf<String>().apply {
                        for (info in infoList) {
                            Bukkit.getOfflinePlayer(info.profileId).run {
                                if (hasPlayedBefore()) add("@${this.name}")
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
                }
                else -> throw AssertionError()
            }
            return
        }

        // Legacy method
        if (RsAtPlayerConfigManager.data.pingRecommendGroup.addRecommend && RsAtPlayerConfigManager.data.pingRecommendGroup.addForLegacy) {
            for (info in infoList) {
                modifiedList.add(
                    createInfo(info.profileId, RsAtPlayerConfigManager.data.pingRecommendGroup.tabName) ?: continue
                )
            }
        }
        // Custom formats
        when (action) {
            PlayerInfoAction.ADD_PLAYER -> {
                val user = UserManager[event.player].addedCustomRecommends
                for (custom in RsAtPlayerConfigManager.data.customTypes) {
                    if (custom.recommendGroup.addRecommend && custom.recommendGroup.addForLegacy) {
                        for (format in custom.formats) {
                            if (!user.contains(format)) {
                                user.add(format)
                                modifiedList.add(createInfo(format, custom.recommendGroup.tabName))
                            }
                        }
                    }
                }
            }
            PlayerInfoAction.REMOVE_PLAYER -> {
                val all = mutableListOf<String>().apply {
                    RsAtPlayerConfigManager.data.customTypes.forEach {
                        addAll(it.formats)
                    }
                }
                val user = UserManager[event.player].addedCustomRecommends
                for (recommend in user) {
                    if (!all.contains(recommend)) {
                        user.remove(recommend)
                        modifiedList.add(createInfo(recommend, ComponentType(MessageType.LEGACY, "")))
                    }
                }
            }
            else -> throw AssertionError()
        }
        packet.playerInfoDataLists[0] = modifiedList
    }

    override fun generateInfo(fakeUuid: UUID, name: String, component: Component): PlayerInfoData {
        return PlayerInfoData(
            WrappedGameProfile(APCache.getFakeUuid(fakeUuid), name),
            9999,
            EnumWrappers.NativeGameMode.SPECTATOR,
            WrappedChatComponent.fromJson(GsonComponentSerializer.gson().serialize(component))
        )
    }

}