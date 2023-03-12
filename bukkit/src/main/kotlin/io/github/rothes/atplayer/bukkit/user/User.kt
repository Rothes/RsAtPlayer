package io.github.rothes.atplayer.bukkit.user

import java.util.UUID

class User(
    val uuid: UUID,
) {
    val addedCustomRecommends: MutableList<String> = mutableListOf()
}