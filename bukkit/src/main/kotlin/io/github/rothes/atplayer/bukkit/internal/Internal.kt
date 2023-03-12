package io.github.rothes.atplayer.bukkit.internal

import io.github.rothes.atplayer.bukkit.RsAtPlayer
import net.kyori.adventure.audience.Audience
import org.bukkit.Bukkit
import org.bukkit.entity.Player

val plugin: RsAtPlayer by lazy { RsAtPlayer.plugin }

val Player.audience: Audience
    get() = plugin.adventure.player(this)