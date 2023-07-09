package io.github.rothes.atplayer.bukkit.internal.packetlistener.tabcomplete

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.wrappers.BukkitConverters
import com.comphenix.protocol.wrappers.EnumWrappers
import com.comphenix.protocol.wrappers.PlayerInfoData
import com.comphenix.protocol.wrappers.WrappedChatComponent
import com.comphenix.protocol.wrappers.WrappedGameProfile
import com.comphenix.protocol.wrappers.WrappedRemoteChatSessionData
import io.github.rothes.atplayer.bukkit.config.PlayerRelativeAtType
import io.github.rothes.atplayer.bukkit.config.RsAtPlayerConfigManager
import io.github.rothes.atplayer.bukkit.extensions.get
import io.github.rothes.atplayer.bukkit.extensions.set
import io.github.rothes.atplayer.bukkit.extensions.typed
import io.github.rothes.atplayer.bukkit.internal.APCache
import io.github.rothes.atplayer.bukkit.internal.TabCompletionsHandler
import io.github.rothes.atplayer.bukkit.internal.util.CompatibilityUtils
import io.github.rothes.atplayer.bukkit.user.UserManager
import io.github.rothes.rslib.bukkit.extensions.replacep
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import org.bukkit.Bukkit
import java.util.*

class UpdatePost19R2 : BaseTabCompletePacketListener(PacketType.Play.Server.PLAYER_INFO) {

    override fun onPacketSending(event: PacketEvent) {
        if ((event.packet.modifier.typed(EnumSet::class.java)[0]).size != 6) {
            // This is update player, not add player.
            return
        }
        val modifier = event.packet.modifier.typed(
            List::class.java, BukkitConverters.getListConverter(PlayerInfoData.getConverter())
        )
        val infoList = modifier[0]


        val names = mutableListOf<String>().apply {
            infoList.forEach {
                Bukkit.getPlayer(it.profileId)?.run {
                    add(name)
                }
            }
        }

        val user = UserManager[event.player]
        if (CompatibilityUtils.supportCustomCompletions(event.player)) {
            TabCompletionsHandler.addCompletions(user, mutableListOf<String>().apply {
                RsAtPlayerConfigManager.data.atTypes.forEach {
                    if (!it.recommendGroup.addRecommend) return@forEach
                    when (it) {
                        is PlayerRelativeAtType -> {
                            for (name in names)
                                add(it.format.replacep("PlayerName", name))
                        }
                    }
                }
            })
            return
        }

        // Legacy method
        val modifiedList = ArrayList(infoList)
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
        modifier[0] = modifiedList

    }

    override fun generateInfo(format: String, component: Component): PlayerInfoData {
        val fakeUuid = APCache.getFakeUuid(format)
        return PlayerInfoData(
            fakeUuid,
            9999,
            false, // Hide In TabList For 1.19.3+
            EnumWrappers.NativeGameMode.SPECTATOR,
            WrappedGameProfile(fakeUuid, format),
            WrappedChatComponent.fromJson(GsonComponentSerializer.gson().serialize(component)),
            null as WrappedRemoteChatSessionData?,
        )
    }

}