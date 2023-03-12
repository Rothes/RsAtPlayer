package io.github.rothes.atplayer.bukkit.config

import org.bukkit.entity.Player

data class NotifyGroup(
    val name: String,
    val senderOptions: AtOptions,
    val receiverOptions: AtOptions,
)

fun NotifyGroup.apply(sender: Player?, receiver: Player) {
    if (sender != receiver) {
        receiver.apply { receiverOptions.apply(sender, receiver, this) }
    } else {
        sender.apply { senderOptions.apply(sender, receiver, this) }
    }
}