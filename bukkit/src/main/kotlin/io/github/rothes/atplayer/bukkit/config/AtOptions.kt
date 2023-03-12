package io.github.rothes.atplayer.bukkit.config

import io.github.rothes.atplayer.bukkit.internal.audience
import io.github.rothes.atplayer.bukkit.internal.plugin
import io.github.rothes.rslib.bukkit.extensions.replaceRaw
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable

data class AtOptions(
    val allowUnknown: Boolean = false,
    val replacement: Component? = null,
    val permission: String? = null,
    val sound: Sound? = null,

    val titleMsg: Title? = null,
    val chatMsg: Component? = null,
    val actionBarMsg: Component? = null,
    val bossBarMsg: Component? = null,
)

fun AtOptions.apply(sender: Player?, receiver: Player, player: Player) {
    with(player.audience) {
        sound?.apply { playSound(sound) }

        formatTitle(sender, receiver, titleMsg)?.apply(this::showTitle)
        formatComponent(sender, receiver, actionBarMsg)?.apply(this::sendActionBar)
        formatComponent(sender, receiver, chatMsg)?.apply(this::sendMessage)
        formatComponent(sender, receiver, actionBarMsg)?.apply(this::sendActionBar)
        formatComponent(sender, receiver, bossBarMsg)?.apply {
            with(BossBar.bossBar(this, 0.0F, BossBar.Color.BLUE, BossBar.Overlay.PROGRESS)) Bar@ {
                showBossBar(this)

                object : BukkitRunnable() {
                    private var progress = 0.0F
                    override fun run() {
                        if (progress >= 1) {
                            hideBossBar(this@Bar)
                            cancel()
                        }
                        progress += 0.02F

                        progress(if (progress > 1) 1.0F else progress)
                    }
                }.runTaskTimerAsynchronously(plugin, 1, 1)
            }
        }
    }
}

private fun formatComponent(sender: Player?, receiver: Player, component: Component?) = component?.replaceRaw(
    "<\$Sender>", sender?.name ?: "Unknown",
    "<\$Receiver>", receiver.name
)

private fun formatTitle(sender: Player?, receiver: Player, title: Title?) = title?.let {
    Title.title(formatComponent(sender, receiver, it.title())!!,
        formatComponent(sender, receiver, it.subtitle())!!,
        it.times())
}
