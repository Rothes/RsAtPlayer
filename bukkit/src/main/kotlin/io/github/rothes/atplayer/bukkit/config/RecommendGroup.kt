package io.github.rothes.atplayer.bukkit.config

import io.github.rothes.rslib.bukkit.config.ComponentType
import io.github.rothes.rslib.bukkit.extensions.MessageType
import io.github.rothes.rslib.bukkit.extensions.placeholder


data class RecommendGroup(
    val addRecommend: Boolean = false,
    val addForLegacy: Boolean = false,
    val tabName: ComponentType = ComponentType(MessageType.LEGACY, "~RsAtPlayer Fake ${"Name".placeholder}"),
)
