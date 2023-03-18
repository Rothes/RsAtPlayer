package io.github.rothes.atplayer.bukkit.internal.packetlistener.chat

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketEvent
import io.github.rothes.atplayer.bukkit.config.AtType
import io.github.rothes.atplayer.bukkit.config.NotifyGroup
import io.github.rothes.atplayer.bukkit.config.PlayerRelativeAtType
import io.github.rothes.atplayer.bukkit.config.RsAtPlayerConfigManager
import io.github.rothes.atplayer.bukkit.config.apply
import io.github.rothes.atplayer.bukkit.internal.packetlistener.BasePacketListener
import io.github.rothes.rslib.bukkit.extensions.placeholder
import io.github.rothes.rslib.bukkit.extensions.replaceRaw
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.TranslatableComponent
import org.bukkit.entity.Player

abstract class BaseChatPacketListener(
    packetType: PacketType,
    priority: ListenerPriority = ListenerPriority.LOW,
) : BasePacketListener(packetType, priority) {

    override fun onPacketSending(event: PacketEvent) {
        if (event.isPlayerTemporary) return

        process(event)
    }

//    fun handleAtTypes(sender: Player?, receiver: Player, msg: String): String {
//        TODO("Need to implement")
//        val builder = StringBuilder()
//        val atGroup = if (!RsAtPlayerConfigManager.data.pingEnabled) null else
//            RsAtPlayerConfigManager.getMatchedNotifyGroup(sender, receiver, RsAtPlayerConfigManager.data.pingGroups)
//        val mentionGroup = if (!RsAtPlayerConfigManager.data.mentionEnabled) null else
//            RsAtPlayerConfigManager.getMatchedNotifyGroup(sender, receiver, RsAtPlayerConfigManager.data.mentionGroups)
//        val customs = RsAtPlayerConfigManager.data.customTypes
//
//        msg.split(" ").forEach { part ->
//            if (atGroup != null && part == "@${receiver.name}") {
//                atGroup.apply(sender, receiver)
//                builder.append(atGroup.receiverOptions.replacement ?: part)
//                return@forEach
//            }
//            if (mentionGroup != null && part == receiver.name) {
//                mentionGroup.apply(sender, receiver)
//                builder.append(mentionGroup.receiverOptions.replacement ?: part)
//                return@forEach
//            }
//            for (custom in customs) {
//                RsAtPlayerConfigManager.getMatchedNotifyGroup(sender, receiver, custom.notifyGroups)?.let { group ->
//                    if (custom.formats.contains(part)) {
//                        group.apply(sender, receiver)
//                        builder.append(group.receiverOptions.replacement)
//                    }
//                }
//            }
//        }
//        return builder.toString()
//    }

    fun handleAtTypes(sender: Player?, receiver: Player, msg: Component): Component {
        val types = RsAtPlayerConfigManager.data.atTypes.toMutableList()

        val groups = mutableListOf<NotifyGroup>().apply {
            with(types.iterator()) {
                while (hasNext()) {
                    RsAtPlayerConfigManager.getMatchedNotifyGroup(sender, receiver, next().notifyGroups)?.let(::add) ?: remove()
                }
            }
        }

        return handleMsg(msg, sender, receiver, types, groups)
    }

    private fun handleMsg(msg: Component, sender: Player?, receiver: Player, types: List<AtType>, groups: List<NotifyGroup>): Component {
        val childrenEdited = arrayListOf<Component>()
        for (children in msg.children()) {
            childrenEdited.add(handleMsg(children, sender, receiver, types, groups))
        }
        when (msg) {
            is TextComponent -> {
                val builder = Component.text()
                var left = 0
                var right = 0

                var target: Player? = null
                for (part in msg.content().split(" ")) {
                    var toApply: NotifyGroup? = null
                    for ((index, type) in types.withIndex()) {
                        if (type.matches(sender, receiver, part)) {
                            toApply = groups[index]
                            if (type is PlayerRelativeAtType) {
                                target = if (sender == receiver) type.getTarget(part) else receiver
                            }
                            break
                        }
                    }

                    if (toApply != null) {
                        toApply.apply(sender, receiver)
                        (if (sender == receiver) toApply.senderOptions.replacement else toApply.receiverOptions.replacement)?.let {
                            builder.append(Component.text(msg.content().substring(left, right)).style(msg.style()))
                            builder.append(formatReplacement(sender, receiver, target, it, part))
                            left = right + part.length
                        }
                    }
                    right += part.length + 1
                }
                if (left != right) {
                    builder.append(Component.text(msg.content().substring(left, right - 1)).style(msg.style()))
                }
                return builder.append(childrenEdited).build()

            }
            is TranslatableComponent -> {
                val edited = arrayListOf<Component>()
                for (arg in msg.args()) {
                    edited.add(handleMsg(arg, sender, receiver, types, groups))
                }
                return Component.translatable().key(msg.key()).args(edited).style(msg.style()).append(childrenEdited).build()
            }
            else -> {
                return msg.children(childrenEdited)
            }
        }
    }

    private fun formatReplacement(sender: Player?, receiver: Player, target: Player?, component: Component, message: String): Component {
        return component.replaceRaw(
            "Message".placeholder, message,
            "Sender".placeholder, sender?.name ?: "Unknown",
            "Receiver".placeholder, receiver.name,
            "Target".placeholder, (target ?: receiver).name,
        )
    }

    abstract fun process(event: PacketEvent)

    abstract val PacketEvent.sender: Player?
    open val PacketEvent.receiver: Player
        get() = this.player

}