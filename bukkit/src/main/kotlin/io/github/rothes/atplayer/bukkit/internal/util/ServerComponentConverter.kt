package io.github.rothes.atplayer.bukkit.internal.util

import com.comphenix.protocol.reflect.StructureModifier
import io.github.rothes.atplayer.bukkit.extensions.get
import io.github.rothes.atplayer.bukkit.extensions.set
import io.github.rothes.atplayer.bukkit.extensions.typed
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.chat.ComponentSerializer

object ServerComponentConverter {

    fun getSpigotComponents(modifier: StructureModifier<Any>): Component? {
        if (!CompatibilityUtils.isSpigot) return null

        val componentModifier = modifier.typed(Array<BaseComponent>::class.java)
        return if (componentModifier.size() == 0) null
        else GsonComponentSerializer.gson().deserialize(ComponentSerializer.toString(componentModifier[0]))
    }

    fun setSpigotComponents(modifier: StructureModifier<Any>, component: Component) {
        if (!CompatibilityUtils.isSpigot) return

        val componentModifier = modifier.typed(Array<BaseComponent>::class.java)
        componentModifier[0] = ComponentSerializer.parse(GsonComponentSerializer.gson().serialize(component))
    }

    private val serverComponent by lazy {
        Class.forName(
            // To avoid being relocated.
            StringBuilder("net.").append("kyori.adventure.text.Component").toString()
        ) as Class<*>
    }
    private val serverSerializer by lazy {
        Class.forName(
            // To avoid being relocated.
            StringBuilder("net.").append("kyori.adventure.text.serializer.gson.GsonComponentSerializer").toString()
        ).getDeclaredMethod("gson").invoke(null)
    }
    private val serverSerialize by lazy { serverSerializer.javaClass.getDeclaredMethod("serialize", serverComponent).apply { isAccessible = true } }
    private val serverDeserialize by lazy { serverSerializer.javaClass.getDeclaredMethod("deserialize", String::class.java).apply { isAccessible = true } }

    fun getPaperComponent(modifier: StructureModifier<Any>): Component? {
        if (!CompatibilityUtils.hasPaperComponent) return null

        val componentModifier = modifier.typed(serverComponent)
        return if (componentModifier.size() == 0) null
        else GsonComponentSerializer.gson().deserialize(serverSerialize(serverSerializer, componentModifier[0]) as String)
    }

    fun setPaperComponent(modifier: StructureModifier<Any>, component: Component) {
        if (!CompatibilityUtils.hasPaperComponent) return

        val componentModifier = modifier.typed<Any>(serverComponent, null)
        componentModifier[0] = serverDeserialize(serverSerializer, GsonComponentSerializer.gson().serialize(component))
    }

}