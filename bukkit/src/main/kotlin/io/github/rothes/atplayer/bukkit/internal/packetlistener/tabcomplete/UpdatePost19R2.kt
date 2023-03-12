package io.github.rothes.atplayer.bukkit.internal.packetlistener.tabcomplete

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.wrappers.EnumWrappers
import com.comphenix.protocol.wrappers.PlayerInfoData
import com.comphenix.protocol.wrappers.WrappedChatComponent
import com.comphenix.protocol.wrappers.WrappedGameProfile
import io.github.rothes.atplayer.bukkit.config.RsAtPlayerConfigManager
import io.github.rothes.atplayer.bukkit.internal.APCache
import io.github.rothes.atplayer.bukkit.internal.util.CompatibilityUtils
import io.github.rothes.atplayer.bukkit.user.UserManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import org.bukkit.Bukkit
import java.lang.reflect.InvocationTargetException
import java.util.*

class UpdatePost19R2 : BaseTabCompletePacketListener(PacketType.Play.Server.PLAYER_INFO) {

//    private val constructor: Constructor<*>
//    private val getUuid: Method

//    init {
//        PacketType.Play.Server.PLAYER_INFO.packetClass.declaredClasses.first { it.simpleName == "b" }.run {
//            constructor = constructors.first { it.parameters.size == 7 }
//            getUuid = declaredMethods.first { it.returnType == UUID::class.java }
//        }
//    }

    override fun onPacketSending(event: PacketEvent) {
        if ((event.packet.modifier.typed(EnumSet::class.java)[0]).size != 6) {
            // This is update player, not add player.
            return
        }
        try {
            val infoList = event.packet.modifier.typed(List::class.java)[0]
            if (CompatibilityUtils.supportCustomCompletions(event.player)) {
                addChatCompletions(event.player, mutableListOf<String>().apply {
                    if (RsAtPlayerConfigManager.data.pingRecommendGroup.addRecommend) {
                        for (info in infoList) {
//                        Bukkit.getPlayer(getUuid.invoke(info) as UUID)?.run {
//                            add("@${this.name}")
//                        }
                            Bukkit.getPlayer(PlayerInfoData.getConverter().getSpecific(info).profileId)?.run {
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
                return
            }

            // Legacy method
            val modifiedList = ArrayList(infoList)

            if (RsAtPlayerConfigManager.data.pingRecommendGroup.addRecommend && RsAtPlayerConfigManager.data.pingRecommendGroup.addForLegacy) {
                for (info in infoList) {
                    modifiedList.add(
                        PlayerInfoData.getConverter().getGeneric(
                            createInfo(
                                PlayerInfoData.getConverter().getSpecific(info).profileId,
                                RsAtPlayerConfigManager.data.pingRecommendGroup.tabName
                            ) ?: continue
                        )
                    )
                }
            }

            // Custom formats
            val user = UserManager[event.player].addedCustomRecommends
            for (custom in RsAtPlayerConfigManager.data.customTypes) {
                if (custom.recommendGroup.addRecommend && custom.recommendGroup.addForLegacy) {
                    for (format in custom.formats) {
                        if (!user.contains(format)) {
                            user.add(format)
                            modifiedList.add(PlayerInfoData.getConverter().getGeneric(
                                createInfo(format, custom.recommendGroup.tabName)))
                        }
                    }
                }
            }
            event.packet.modifier.typed(List::class.java)[0] = modifiedList
        } catch (e: InstantiationException) {
            throw RuntimeException(e)
        } catch (e: IllegalAccessException) {
            throw RuntimeException(e)
        } catch (e: InvocationTargetException) {
            throw RuntimeException(e)
        }

    }

    override fun generateInfo(fakeUuid: UUID, name: String, component: Component): PlayerInfoData {
        return PlayerInfoData(
            fakeUuid,
            9999,
            false, // Hide In TabList For 1.19.3+
            EnumWrappers.NativeGameMode.SPECTATOR,
            WrappedGameProfile(APCache.getFakeUuid(fakeUuid), name),
            WrappedChatComponent.fromJson(GsonComponentSerializer.gson().serialize(component)),
            null,
        )
//        return constructor.newInstance(
//            fakeUuid,
//            WrappedGameProfile(fakeUuid, name).handle,
//            false,  // Hide In TabList For 1.19.3+
//            9999,
//            EnumWrappers.getGameModeConverter().getGeneric(EnumWrappers.NativeGameMode.SPECTATOR),
//            WrappedChatComponent.fromJson(GsonComponentSerializer.gson().serialize(component)).handle,
//            null
//        )
    }

}