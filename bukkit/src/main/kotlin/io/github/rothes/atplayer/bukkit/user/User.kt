package io.github.rothes.atplayer.bukkit.user

import org.bukkit.Bukkit
import java.util.UUID

class User(
    val uuid: UUID,
) {
    private val present = Object()
    val addedRecommends: HashMap<String, Any> = HashMap()
    val player = Bukkit.getPlayer(uuid)

    fun addRecommend(string: String, uuid: Any = present) {
        addedRecommends[string] = uuid
    }

    fun hasRecommend(string: String) : Boolean {
        return addedRecommends.contains(string)
    }

    fun removeRecommend(string: String): Any? {
        return addedRecommends.remove(string)
    }

}