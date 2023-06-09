package io.github.rothes.atplayer.bukkit.config

import org.bukkit.entity.Player

data class CustomAtType(
    val formats: Array<String> = arrayOf(),
    override val notifyGroups: Array<NotifyGroup> = arrayOf(),
    override val recommendGroup: RecommendGroup = RecommendGroup(false)
) : AtType {

    override fun matches(sender: Player?, receiver: Player, string: String, group: NotifyGroup?): Boolean {
        group ?: return false

        return formats.contains(string)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CustomAtType

        if (!formats.contentEquals(other.formats)) return false
        if (!notifyGroups.contentEquals(other.notifyGroups)) return false
        if (recommendGroup != other.recommendGroup) return false

        return true
    }

    override fun hashCode(): Int {
        var result = formats.contentHashCode()
        result = 31 * result + notifyGroups.contentHashCode()
        result = 31 * result + recommendGroup.hashCode()
        return result
    }

}
