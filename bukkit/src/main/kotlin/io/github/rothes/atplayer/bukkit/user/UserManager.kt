package io.github.rothes.atplayer.bukkit.user

import org.bukkit.entity.Player
import java.util.UUID

object UserManager {

    private val userMap = hashMapOf<UUID, User>()

    operator fun get(uuid: UUID) = getUser(uuid)

    operator fun get(player: Player) = getUser(player.uniqueId)

    fun getUser(uuid: UUID) = userMap.computeIfAbsent(uuid) { User(it) }
    fun removeUser(uuid: UUID) = userMap.remove(uuid)

}