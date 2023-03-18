package io.github.rothes.atplayer.bukkit.internal

import io.github.rothes.atplayer.bukkit.config.PlayerRelativeAtType
import io.github.rothes.atplayer.bukkit.config.RsAtPlayerConfigManager
import io.github.rothes.rslib.bukkit.extensions.replacep
import org.bukkit.Bukkit
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.stream.Collectors
import kotlin.collections.HashMap

object APCache {

    val playerRelative = HashMap<PlayerRelativeAtType, MutableSet<String>>()
    private val stringCache = ConcurrentHashMap<String, UUID>()

    fun load() {
        playerRelative.clear()
        stringCache.clear()

        RsAtPlayerConfigManager.data.atTypes.filterIsInstance<PlayerRelativeAtType>().forEach { type ->
            playerRelative[type] = Bukkit.getOnlinePlayers().stream().map { type.format.replacep("PlayerName", it.name) } .collect(Collectors.toSet())
        }
    }

    fun getFakeUuid(string: String): UUID {
        return stringCache[string] ?: run {
            var randomUUID = UUID.randomUUID()
            while (Bukkit.getOfflinePlayer(randomUUID).hasPlayedBefore()) {
                randomUUID = UUID.randomUUID()
            }
            return randomUUID.also { stringCache[string] = it }
        }
    }

}