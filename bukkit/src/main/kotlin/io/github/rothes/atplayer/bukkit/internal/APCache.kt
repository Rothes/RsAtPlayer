package io.github.rothes.atplayer.bukkit.internal

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.stream.Collectors

object APCache {

    val pingNames: MutableSet<String> = Bukkit.getOnlinePlayers().stream().map { "@" + it.name } .collect(Collectors.toSet())
    val mentionNames: MutableSet<String> = Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toSet())

    private val uuidCache = ConcurrentHashMap<UUID, UUID>()
    private val stringCache = ConcurrentHashMap<String, UUID>()

    fun getFakeUuid(real: UUID): UUID {
        return uuidCache[real] ?: run {
            var randomUUID = UUID.randomUUID()
            while (Bukkit.getOfflinePlayer(randomUUID).hasPlayedBefore()) {
                randomUUID = UUID.randomUUID()
            }
            return randomUUID.also { uuidCache[real] = it }
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

    fun getFakeUuidIfPresent(real: UUID): UUID? {
        return uuidCache[real]
    }

}