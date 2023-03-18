package io.github.rothes.atplayer.bukkit.internal.command

import io.github.rothes.atplayer.bukkit.internal.plugin as pl
import io.github.rothes.rslib.bukkit.command.Command
import io.github.rothes.rslib.bukkit.command.CommandHandler
import io.github.rothes.rslib.bukkit.command.ICommand

class APCommandHandler : CommandHandler(pl, "RsAtPlayer") {

    init {
        commands.add(Command("Reload", { sender, _ ->
            pl.reload()
            sender.sendMessage("reloaded")
            ICommand.Result.COMPLETED
        },
            permission = "RsAtPlayer.Command.Reload"))
    }

}