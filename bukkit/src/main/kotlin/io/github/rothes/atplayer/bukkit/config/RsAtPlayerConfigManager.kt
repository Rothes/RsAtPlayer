package io.github.rothes.atplayer.bukkit.config

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
        val mentionEnabled = yamlFile.getBoolean("At-Types.Mention.Enabled", false)
        val pingEnabled = yamlFile.getBoolean("At-Types.Ping.Enabled", false)

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

        val mentionGroups = getNotifyGroups(yamlFile.getStringList("At-Types.Mention.Notify-Groups"))
        val pingGroups = getNotifyGroups(yamlFile.getStringList("At-Types.Ping.Notify-Groups"))

        val pingRecommendGroup = recommendGroups[yamlFile.getString("At-Types.Ping.Recommend-Group")] ?: RecommendGroup()

        val customTypes = mutableListOf<CustomAtType>().apply { yamlFile.getMapList("At-Types.Custom").forEach {
            try {
                @Suppress("UNCHECKED_CAST")
                add(
                    CustomAtType(
                        (it["Formats"] as List<String>).toTypedArray(),
                        getNotifyGroups(it["Notify-Groups"] as List<String>),
                        recommendGroups[it["Recommend-Group"]] ?: RecommendGroup(false),
                    )
                )
            } catch (t: Throwable) {
                t.printStackTrace()
            } } }

        private fun getNotifyGroups(list: List<String>) = mutableListOf<NotifyGroup>().apply {
            for (name in list) {
                notifyGroups[name]?.let(::add)
                    ?: plugin.i18n.getLocaled("Console-Sender.Message.Initialize.Configuration.Unknown-Notify-Group", "<Name>", name)
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