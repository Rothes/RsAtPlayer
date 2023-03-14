package io.github.rothes.atplayer.bukkit.internal.packetlistener.tabcomplete

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.wrappers.PlayerInfoData
import io.github.rothes.atplayer.bukkit.internal.APCache
import io.github.rothes.atplayer.bukkit.internal.packetlistener.BasePacketListener
import io.github.rothes.atplayer.bukkit.internal.util.CompatibilityUtils
import io.github.rothes.rslib.bukkit.config.ComponentType
import io.github.rothes.rslib.bukkit.util.VersionUtils
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import sun.audio.AudioPlayer.player
import java.lang.UnsupportedOperationException
import java.util.*

abstract class BaseTabCompletePacketListener(
    packetType: PacketType,
    priority: ListenerPriority = ListenerPriority.LOW,
) : BasePacketListener(packetType, priority) {

    private val addEnum: Enum<*>?
    private val removeEnum: Enum<*>?

    init {
        if (VersionUtils.serverMajorVersion >= 19) {
            @Suppress("UNCHECKED_CAST")
            with (PacketType.Play.Server.CUSTOM_CHAT_COMPLETIONS.packetClass.declaredFields[0].type.enumConstants as Array<Enum<*>>) {
                addEnum = first { it.name == "ADD" }
                removeEnum = first { it.name == "REMOVE" }
            }
        } else {
            addEnum = null
            removeEnum = null
        }
    }

    fun addChatCompletions(player: Player, completions: List<String>) {
        val packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.CUSTOM_CHAT_COMPLETIONS)
        packet.modifier[0] = addEnum!!
        packet.modifier[1] = completions
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet)
    }

    fun removeChatCompletions(player: Player, completions: List<String>) {
        val packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.CUSTOM_CHAT_COMPLETIONS)
        packet.modifier[0] = removeEnum!!
        packet.modifier[1] = completions
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet)
    }

    protected fun createInfo(uuid: UUID, component: ComponentType): PlayerInfoData? {
        val player = Bukkit.getPlayer(uuid) ?: return null // This may be a fake player. It's not online.

        return generateInfo(APCache.getFakeUuid(uuid), "@${player.name}", component.type.getComponent(
            CompatibilityUtils.parsePapi(player, component.message.replace("<\$Name>", "@${player.name}"))
        ))
    }

    protected fun createInfo(string: String, component: ComponentType): PlayerInfoData {
        return generateInfo(APCache.getFakeUuid(string), string, component.type.getComponent(
            CompatibilityUtils.parsePapi(null, component.message.replace("<\$Name>", string))
        ))
    }

    open fun generateInfo(fakeUuid: UUID, name: String, component: Component): PlayerInfoData {
        throw UnsupportedOperationException("Not implemented yet.")
    }

}