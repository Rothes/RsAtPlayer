package io.github.rothes.atplayer.bukkit.extensions

import com.comphenix.protocol.reflect.EquivalentConverter
import com.comphenix.protocol.reflect.StructureModifier

operator fun <T> StructureModifier<T>.get(fieldIndex: Int): T = read(fieldIndex)
operator fun <T> StructureModifier<T>.set(fieldIndex: Int, value: T) = write(fieldIndex, value)!!

fun <T> StructureModifier<Any>.typed(fieldType: Class<T>): StructureModifier<T> {
    return withType(fieldType, null)
}

fun <T> StructureModifier<Any>.typed(fieldType: Class<*>, converter: EquivalentConverter<T>?): StructureModifier<T> {
    return withType(fieldType, converter)
}