package io.github.rothes.atplayer.bukkit.internal

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.wrappers.EnumWrappers
import com.comphenix.protocol.wrappers.PlayerInfoData
import com.comphenix.protocol.wrappers.WrappedChatComponent
import com.comphenix.protocol.wrappers.WrappedGameProfile
import io.github.rothes.atplayer.bukkit.config.CustomAtType
import io.github.rothes.atplayer.bukkit.config.PlayerRelativeAtType
import io.github.rothes.atplayer.bukkit.config.RsAtPlayerConfigManager
import io.github.rothes.atplayer.bukkit.extensions.set
import io.github.rothes.atplayer.bukkit.internal.util.CompatibilityUtils
import io.github.rothes.atplayer.bukkit.internal.util.CompatibilityUtils.supportCustomCompletions
import io.github.rothes.atplayer.bukkit.user.User
import io.github.rothes.atplayer.bukkit.user.UserManager
import io.github.rothes.rslib.bukkit.extensions.replacep
import io.github.rothes.rslib.bukkit.util.VersionUtils
import io.github.rothes.rslib.bukkit.util.version.VersionRange
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

object TabCompletionsHandler {

    @Suppress("UNCHECKED_CAST") private val addEnum by lazy { (PacketType.Play.Server.CUSTOM_CHAT_COMPLETIONS.packetClass.declaredFields[0].type.enumConstants as Array<Enum<*>>).first { it.name == "ADD" } }
    @Suppress("UNCHECKED_CAST") private val removeEnum by lazy { (PacketType.Play.Server.CUSTOM_CHAT_COMPLETIONS.packetClass.declaredFields[0].type.enumConstants as Array<Enum<*>>).first { it.name == "REMOVE" } }

    @Suppress("UNCHECKED_CAST") private val updateEnums by lazy { EnumSet.allOf(PacketType.Play.Server.PLAYER_INFO.packetClass.declaredClasses.first { it.isEnum } as Class<out Enum<*>>) }

    fun addCustomRecommends(player: Player) = addCustomRecommends(UserManager[player])
    fun addCustomRecommends(user: User) {
        addRecommends(user, mutableListOf<Pair<String, Component>>().apply {
            RsAtPlayerConfigManager.data.atTypes.forEach { atType ->
                if (!atType.recommendGroup.addRecommend) return@forEach
                when (atType) {
                    is CustomAtType     -> {
                        atType.formats.forEach { format ->
                            if (!user.hasRecommend(format)) {
                                add(Pair(
                                    format,
                                    atType.recommendGroup.tabName.let {
                                        it.type.getComponent(
                                            CompatibilityUtils.parsePapi(null, it.message.replacep("Name", format))
                                        )
                                    }))
                                user.addRecommend(format)
                            }
                        }
                    }
                }
            }

        })
    }

    fun addPlayerRecommends(player: Player) = addPlayerRecommends(UserManager[player])
    fun addPlayerRecommends(user: User) {
        addRecommends(user, mutableListOf<Pair<String, Component>>().apply {
            RsAtPlayerConfigManager.data.atTypes.forEach { atType ->
                if (!atType.recommendGroup.addRecommend) return@forEach
                when (atType) {
                    is PlayerRelativeAtType     -> {
                        Bukkit.getOnlinePlayers().forEach { player ->
                            val format = atType.format.replacep("PlayerName", player.name)
                            add(Pair(
                                format,
                                atType.recommendGroup.tabName.let {
                                    it.type.getComponent(
                                        CompatibilityUtils.parsePapi(player, it.message.replacep("Name", atType.format))
                                    )
                                }))
                            user.addRecommend(format)
                        }
                    }
                }
            }

        })
    }

    fun removeRecommends(player: Player) = removeRecommends(UserManager[player])
    fun removeRecommends(user: User) {
        removeRecommends(user, user.addedRecommends.map { it.key })
        user.addedRecommends.clear()
    }

    fun addRecommends(user: User, list: List<Pair<String, Component>>) {
        if (supportCustomCompletions(user.player!!)) {
            addCompletions(user, list.map { it.first })
        } else if (VersionRange("1.19.3", "2").matches(VersionUtils.serverVersion)) {
            val packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.PLAYER_INFO)
            packet.modifier[0] = updateEnums
            packet.modifier[1] = mutableListOf<Any>().apply {
                for (pair in list) {
                    add(
                        PlayerInfoData.getConverter().getGeneric(
                            PlayerInfoData(
                                APCache.getFakeUuid(pair.first),
                                9999,
                                false, // Hide In TabList For 1.19.3+
                                EnumWrappers.NativeGameMode.SPECTATOR,
                                WrappedGameProfile(APCache.getFakeUuid(pair.first), pair.first),
                                WrappedChatComponent.fromJson(
                                    GsonComponentSerializer.gson().serialize(pair.second)
                                ),
                                null,
                            )
                        )
                    )
                }
            }
            ProtocolLibrary.getProtocolManager().sendServerPacket(user.player, packet)
        } else {
            val packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.PLAYER_INFO)
            packet.playerInfoAction[0] = EnumWrappers.PlayerInfoAction.ADD_PLAYER
            packet.playerInfoDataLists[0] = mutableListOf<PlayerInfoData>().apply {
                for (pair in list) {
                    add(PlayerInfoData(
                        WrappedGameProfile(APCache.getFakeUuid(pair.first), pair.first),
                        9999,
                        EnumWrappers.NativeGameMode.SPECTATOR,
                        WrappedChatComponent.fromJson(GsonComponentSerializer.gson().serialize(pair.second))
                    ))
                }
            }
            ProtocolLibrary.getProtocolManager().sendServerPacket(user.player, packet)
        }
    }

    fun removeRecommends(user: User, list: List<String>) {
        if (supportCustomCompletions(user.player!!)) {
            removeCompletions(user, list)
        } else if (VersionRange("1.19.3", "2").matches(VersionUtils.serverVersion)) {
            val packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.PLAYER_INFO_REMOVE)
            packet.modifier[0] = mutableListOf<UUID>().apply {
                for (format in list) {
                    add(APCache.getFakeUuid(format))
                }
            }
        } else {
            val packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.PLAYER_INFO)
            @Suppress("DEPRECATION")
            packet.playerInfoAction[0] = EnumWrappers.PlayerInfoAction.REMOVE_PLAYER
            packet.playerInfoDataLists[0] = mutableListOf<PlayerInfoData>().apply {
                for (format in list) {
                    add(PlayerInfoData(
                        WrappedGameProfile(APCache.getFakeUuid(format), format),
                        9999,
                        EnumWrappers.NativeGameMode.SPECTATOR,
                        null,
                    ))
                }
            }
        }
    }

    fun addCompletions(user: User, completions: List<String>) {
        if (completions.isEmpty()) return
        val packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.CUSTOM_CHAT_COMPLETIONS)
        packet.modifier[0] = addEnum
        packet.modifier[1] = completions
        ProtocolLibrary.getProtocolManager().sendServerPacket(user.player, packet)
    }

    fun removeCompletions(user: User, completions: List<String>) {
        val packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.CUSTOM_CHAT_COMPLETIONS)
        packet.modifier[0] = removeEnum
        packet.modifier[1] = completions
        ProtocolLibrary.getProtocolManager().sendServerPacket(user.player, packet)
    }

}