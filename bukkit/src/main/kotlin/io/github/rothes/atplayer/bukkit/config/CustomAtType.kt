package io.github.rothes.atplayer.bukkit.config

data class CustomAtType(
    val formats: Array<String> = arrayOf(),
    val notifyGroups: Array<NotifyGroup> = arrayOf(),
    val recommendGroup: RecommendGroup = RecommendGroup(false)
) {

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
