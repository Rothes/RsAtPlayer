package io.github.rothes.atplayer.bukkit.internal

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.wrappers.EnumWrappers
import com.comphenix.protocol.wrappers.PlayerInfoData
import com.comphenix.protocol.wrappers.WrappedChatComponent
import com.comphenix.protocol.wrappers.WrappedGameProfile
import io.github.rothes.atplayer.bukkit.config.CustomAtType
import io.github.rothes.atplayer.bukkit.config.RsAtPlayerConfigManager
import io.github.rothes.atplayer.bukkit.extensions.set
import io.github.rothes.atplayer.bukkit.internal.util.CompatibilityUtils
import io.github.rothes.atplayer.bukkit.internal.util.CompatibilityUtils.supportCustomCompletions
import io.github.rothes.atplayer.bukkit.user.UserManager
import io.github.rothes.rslib.bukkit.util.VersionUtils
import io.github.rothes.rslib.bukkit.util.version.VersionRange
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import org.bukkit.entity.Player
import java.util.*

object TabCompletionsHandler {

    private val addEnum by lazy { (PacketType.Play.Server.CUSTOM_CHAT_COMPLETIONS.packetClass.declaredFields[0].type.enumConstants as Array<Enum<*>>).first { it.name == "ADD" } }
    private val removeEnum by lazy { (PacketType.Play.Server.CUSTOM_CHAT_COMPLETIONS.packetClass.declaredFields[0].type.enumConstants as Array<Enum<*>>).first { it.name == "REMOVE" } }

    private val updateEnums by lazy { EnumSet.allOf(PacketType.Play.Server.PLAYER_INFO.packetClass.declaredClasses[0] as Class<out Enum<*>>) }

    fun addCustomCompletions(player: Player) {
        val user = UserManager[player]
        addCompletions(player, mutableListOf<Pair<String, Component>>().apply {
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
                                            CompatibilityUtils.parsePapi(null, it.message.replace("<\$Name>", format))
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

    fun addCompletions(player: Player, list: List<Pair<String, Component>>) {
        if (supportCustomCompletions(player)) {
            addChatCompletions(player, list.map { it.first })
        } else if (VersionRange("1.19.3", "2").matches(VersionUtils.serverVersion)) {
            val packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.PLAYER_INFO)
            packet.modifier[0] = updateEnums
            packet.modifier[1] = mutableListOf<Any>().apply {
                for (pair in list) {
                    add(PlayerInfoData.getConverter().getGeneric(PlayerInfoData(
                        APCache.getFakeUuid(pair.first),
                        9999,
                        false, // Hide In TabList For 1.19.3+
                        EnumWrappers.NativeGameMode.SPECTATOR,
                        WrappedGameProfile(APCache.getFakeUuid(pair.first), pair.first),
                        WrappedChatComponent.fromJson(GsonComponentSerializer.gson().serialize(pair.second)),
                        null,
                    )))
                }
            }
        } else {
            val packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.PLAYER_INFO)
            packet.playerInfoAction[0] = EnumWrappers.PlayerInfoAction.ADD_PLAYER
            packet.playerInfoDataLists[0] = mutableListOf<PlayerInfoData>().apply {
                for (pair in list) {
                    add(PlayerInfoData(
                        APCache.getFakeUuid(pair.first),
                        9999,
                        false, // Hide In TabList For 1.19.3+
                        EnumWrappers.NativeGameMode.SPECTATOR,
                        WrappedGameProfile(APCache.getFakeUuid(pair.first), pair.first),
                        WrappedChatComponent.fromJson(GsonComponentSerializer.gson().serialize(pair.second)),
                        null,
                    ))
                }
            }
        }
    }

    fun addChatCompletions(player: Player, completions: List<String>) {
        val packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.CUSTOM_CHAT_COMPLETIONS)
        packet.modifier[0] = addEnum
        packet.modifier[1] = completions
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet)
    }

    fun removeChatCompletions(player: Player, completions: List<String>) {
        val packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.CUSTOM_CHAT_COMPLETIONS)
        packet.modifier[0] = removeEnum
        packet.modifier[1] = completions
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet)
    }

}