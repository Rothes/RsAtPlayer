package io.github.rothes.atplayer.bukkit.config

import org.bukkit.entity.Player

interface AtType {

    val notifyGroups: Array<NotifyGroup>
    val recommendGroup: RecommendGroup

    fun matches(sender: Player?, receiver: Player, string: String, group: NotifyGroup? = RsAtPlayerConfigManager.getMatchedNotifyGroup(sender, receiver, notifyGroups)): Boolean

}