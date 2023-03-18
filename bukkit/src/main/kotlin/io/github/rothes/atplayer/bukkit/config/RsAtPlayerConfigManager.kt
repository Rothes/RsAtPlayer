package io.github.rothes.atplayer.bukkit.config

import io.github.rothes.atplayer.bukkit.internal.i18n
import io.github.rothes.atplayer.bukkit.internal.plugin
import io.github.rothes.rslib.bukkit.config.ComponentType
import io.github.rothes.rslib.bukkit.config.ConfigManager
import io.github.rothes.rslib.bukkit.extensions.MessageType
import io.github.rothes.rslib.bukkit.extensions.getSound
import io.github.rothes.rslib.bukkit.extensions.getTitle
import io.github.rothes.rslib.bukkit.extensions.getTypedMessage
import io.github.rothes.rslib.bukkit.extensions.getTypedMessageType
import org.bukkit.entity.Player
import org.simpleyaml.configuration.ConfigurationSection
import java.util.*

object RsAtPlayerConfigManager: ConfigManager(plugin) {

    override lateinit var data: ConfigData
        private set

    override fun load() {
        yamlFile.load()
        data = ConfigData()
    }

    fun getMatchedNotifyGroup(sender: Player?, receiver: Player?, groups: Array<NotifyGroup>): NotifyGroup? =
        groups.firstOrNull {
            isPlayerMatchNotifyGroup(sender, it.senderOptions) && isPlayerMatchNotifyGroup(receiver, it.receiverOptions)
        }

    fun isPlayerMatchNotifyGroup(player: Player?, atOptions: AtOptions): Boolean = with(atOptions) {
        player?.let {
            permission?.let {
                return player.hasPermission(permission)
            } ?: return true
        } ?: return allowUnknown
    }

    override fun isInitialized() = this::data.isInitialized

    class ConfigData: ConfigManager.ConfigData() {
        override val locale: String? = yamlFile.getString("Options.Locale")

        val notifyGroups = hashMapOf<String, NotifyGroup>().apply {
            yamlFile.getConfigurationSection("Notify-Groups")?.let { groups ->
                for (key in groups.getKeys(false)) {
                    groups.getConfigurationSection(key).let {
                        this[key] = NotifyGroup(key,
                            it.getConfigurationSection("Sender").toAtOptions(),
                            it.getConfigurationSection("Receiver").toAtOptions(),
                        )
                    }
                }
            }
        }

        val recommendGroups = hashMapOf<String, RecommendGroup>().apply {
            yamlFile.getConfigurationSection("Recommend-Groups")?.let { groups ->
                for (key in groups.getKeys(false)) {
                    groups.getConfigurationSection(key).let {
                        this[key] = RecommendGroup(
                            it.getBoolean("Add-Recommends-In-Chat", true),
                            it.getBoolean("Add-Recommends-Legacy", false),
                            it.getTypedMessageType("Fake-Player-Tab-Name") ?: ComponentType(MessageType.LEGACY, "~RsAtPlayer Fake <\$Name>"),
                        )
                    }
                }
            }
        }

        val atTypes = buildList {
            yamlFile.getConfigurationSection("At-Types")?.getKeys(false)!!.forEach { key ->
                with(yamlFile.getConfigurationSection("At-Types.$key") ?: return@forEach) {
                    val type = getString("Type")
                    when (type.uppercase(Locale.ROOT)) {
                        "PLAYER-RELATIVE" -> add(PlayerRelativeAtType(
                            getString("Format"),
                            getNotifyGroups("Notify-Groups"),
                            recommendGroups[getString("Recommend-Group")] ?: RecommendGroup(),
                        ))
                        "CUSTOM" -> add(CustomAtType(
                            getStringList("Formats").toTypedArray(),
                            getNotifyGroups("Notify-Groups"),
                            recommendGroups[getString("Recommend-Group")] ?: RecommendGroup(),
                        ))
                        else -> return@forEach plugin.warn(i18n.getLocaled("Console-Sender.Load.Config.Unknown-At-Type-Type",
                            "Key", key, "Type", type))
                    }
                }
            }
        }.toTypedArray()

        private fun ConfigurationSection.getNotifyGroups(key: String) = getNotifyGroups(getStringList(key))

        private fun getNotifyGroups(list: List<String>) = mutableListOf<NotifyGroup>().apply {
            for (name in list) {
                notifyGroups[name]?.let(::add)
                    ?: plugin.i18n.getLocaled("Console-Sender.Message.Initialize.Configuration.Unknown-Notify-Group", "Name", name)
            }
        }.toTypedArray()

        private fun ConfigurationSection?.toAtOptions() =
            if (this == null) AtOptions()
            else AtOptions(
                getBoolean("Allow-Unknown", false),
                getTypedMessage("Replacement"),
                getString("Permission")?.let { it.ifEmpty { null } },
                getSound("Sound"),

                getTitle("Title"),
                getTypedMessage("Chat"),
                getTypedMessage("ActionBar"),
                getTypedMessage("Boss-Bar"),
            )

    }

}