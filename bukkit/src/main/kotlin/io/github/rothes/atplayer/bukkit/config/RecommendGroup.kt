package io.github.rothes.atplayer.bukkit.config

import io.github.rothes.rslib.bukkit.config.ComponentType
import io.github.rothes.rslib.bukkit.extensions.MessageType


data class RecommendGroup(
    val addRecommend: Boolean = true,
    val addForLegacy: Boolean = false,
    val tabName: ComponentType = ComponentType(MessageType.LEGACY, "~RsAtPlayer Fake <\$Name>"),
)
