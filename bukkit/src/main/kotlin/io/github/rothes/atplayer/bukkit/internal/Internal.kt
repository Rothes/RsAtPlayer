package io.github.rothes.atplayer.bukkit.internal

import io.github.rothes.atplayer.bukkit.RsAtPlayer
import io.github.rothes.rslib.bukkit.i18n.I18n
import net.kyori.adventure.audience.Audience
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

val plugin: RsAtPlayer by lazy { RsAtPlayer.plugin }
val i18n: I18n by lazy { plugin.i18n }

val CommandSender.audience: Audience
    get() = plugin.adventure.sender(this)