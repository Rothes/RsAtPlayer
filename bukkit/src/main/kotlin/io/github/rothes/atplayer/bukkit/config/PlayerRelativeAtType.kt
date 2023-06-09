package io.github.rothes.atplayer.bukkit.config

import io.github.rothes.atplayer.bukkit.internal.APCache
import io.github.rothes.rslib.bukkit.extensions.placeholder
import io.github.rothes.rslib.bukkit.extensions.replacep
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class PlayerRelativeAtType(
    val format: String = "PlayerName".placeholder,
    override val notifyGroups: Array<NotifyGroup> = arrayOf(),
    override val recommendGroup: RecommendGroup = RecommendGroup(false)
) : AtType {

    override fun matches(sender: Player?, receiver: Player, string: String, group: NotifyGroup?): Boolean {
        group ?: return false

        return (sender == null || string != format.replacep("PlayerName", sender.name)) // At self not allowed
                && ((string == format.replacep("PlayerName", receiver.name))
                || (sender == receiver && APCache.playerRelative[this]?.contains(string) ?: false))
    }

    fun getTarget(string: String): Player? {
        val start = format.indexOf("PlayerName".placeholder)
        val end = format.substring(start + "PlayerName".placeholder.length).length
        return Bukkit.getPlayer(string.substring(start, string.length - end))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PlayerRelativeAtType

        if (format != other.format) return false
        if (!notifyGroups.contentEquals(other.notifyGroups)) return false
        if (recommendGroup != other.recommendGroup) return false

        return true
    }

    override fun hashCode(): Int {
        var result = format.hashCode()
        result = 31 * result + notifyGroups.contentHashCode()
        result = 31 * result + recommendGroup.hashCode()
        return result
    }

}