package io.github.rothes.atplayer.bukkit

import io.github.rothes.atplayer.bukkit.config.RsAtPlayerConfigManager
import io.github.rothes.atplayer.bukkit.internal.APCache
import io.github.rothes.atplayer.bukkit.internal.TabCompletionsHandler
import io.github.rothes.atplayer.bukkit.internal.command.APCommandHandler
import io.github.rothes.atplayer.bukkit.internal.listeners.Listeners
import io.github.rothes.atplayer.bukkit.internal.packetlistener.PacketListenerManager
import io.github.rothes.rslib.bukkit.RsLibPlugin
import io.github.rothes.rslib.bukkit.Updater
import io.github.rothes.rslib.bukkit.i18n.I18n
import org.bukkit.Bukkit

class RsAtPlayer : RsLibPlugin() {

    init {
        nameLibLogger()
        plugin = this
    }

    override val i18n = I18n(this)
    override val configManager = RsAtPlayerConfigManager

    override fun onEnable() {
        super.onEnable()
        configManager.load()
        i18n.load()
        APCache.load()
        Bukkit.getOnlinePlayers().forEach(TabCompletionsHandler::addCustomRecommends)

        APCommandHandler().register()
        PacketListenerManager.register()
        Bukkit.getPluginManager().registerEvents(Listeners(), this)
        Updater(this, 17924, "/Rothes/RsAtPlayer/master/Version_Info.json").start()
    }

    fun reload() {
        Bukkit.getOnlinePlayers().forEach(TabCompletionsHandler::removeRecommends)
        configManager.load()
        i18n.load()
        APCache.load()
        Bukkit.getScheduler().runTaskLaterAsynchronously(this, Runnable {
            Bukkit.getOnlinePlayers().forEach(TabCompletionsHandler::addCustomRecommends)
            Bukkit.getOnlinePlayers().forEach(TabCompletionsHandler::addPlayerRecommends)
        }, 1)
    }

    override fun onDisable() {
        PacketListenerManager.unregister()
        Bukkit.getOnlinePlayers().forEach(TabCompletionsHandler::removeRecommends)
        super.onDisable()
    }

    companion object API {
        lateinit var plugin: RsAtPlayer
            private set
    }

}