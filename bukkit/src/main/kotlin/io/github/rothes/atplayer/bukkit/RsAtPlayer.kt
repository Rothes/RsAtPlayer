package io.github.rothes.atplayer.bukkit

import io.github.rothes.atplayer.bukkit.config.RsAtPlayerConfigManager
import io.github.rothes.atplayer.bukkit.internal.listeners.Listeners
import io.github.rothes.atplayer.bukkit.internal.packetlistener.PacketListenerManager
import io.github.rothes.rslib.bukkit.RsLibPlugin
import io.github.rothes.rslib.bukkit.Updater
import io.github.rothes.rslib.bukkit.i18n.I18n
import org.bukkit.Bukkit

class RsAtPlayer : RsLibPlugin() {

    init {
        plugin = this
    }

    override val i18n = I18n(this)
    override val configManager = RsAtPlayerConfigManager

    override fun onEnable() {
        super.onEnable()
        configManager.load()
        i18n.load()

        PacketListenerManager.register()
        Bukkit.getPluginManager().registerEvents(Listeners(), this)
        Updater(this, 17924, "/Rothes/RsAtPlayer/master/Version%20Info.json").start()
    }

    override fun onDisable() {
        PacketListenerManager.unregister()
        super.onDisable()
    }

    companion object API {
        lateinit var plugin: RsAtPlayer
            private set
    }

}