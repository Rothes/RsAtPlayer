package io.github.rothes.atplayer.bukkit.internal.util

import com.viaversion.viaversion.api.Via
import io.github.rothes.rslib.bukkit.util.VersionUtils
import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player

object CompatibilityUtils {

    private val hasVia by lazy { Bukkit.getPluginManager().getPlugin("ViaVersion") != null }

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