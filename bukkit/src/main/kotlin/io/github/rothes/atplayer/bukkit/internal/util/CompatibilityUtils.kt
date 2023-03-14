package io.github.rothes.atplayer.bukkit.internal.util

import com.viaversion.viaversion.api.Via
import io.github.rothes.rslib.bukkit.util.VersionUtils
import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player

object CompatibilityUtils {

    private val hasVia by lazy { Bukkit.getPluginManager().getPlugin("ViaVersion") != null }

    val isSpigot = try {
        Class.forName("org.bukkit.entity.Player\$Spigot")
        true
    } catch (throwable: Throwable) {
        false;
    }
    val hasPaperComponent = try {
        Class.forName("io.papermc.paper.text.PaperComponents")
        true
    } catch (throwable: Throwable) {
        false;
    }

    fun supportCustomCompletions(player: Player): Boolean {
        return ((VersionUtils.serverMajorVersion == 19.toByte() && VersionUtils.serverMinorVersion >= 1)
                || VersionUtils.serverMinorVersion >= 20)
                && (!hasVia || Via.getAPI().getPlayerVersion(player) >= 761)
    }

    fun supportTabHide(player: Player): Boolean {
        return Via.getAPI().getPlayerVersion(player) >= 761
    }

    fun parsePapi(player: OfflinePlayer, string: String): String {
        return PlaceholderAPI.setPlaceholders(player, string)
    }

}