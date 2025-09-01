package org.nandayo.dmentions.command;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.nandayo.dapi.command.SubCommand;
import org.nandayo.dmentions.DMentions;
import org.nandayo.dmentions.service.message.Message;

/**
 * @since 1.8.3
 */
public class ReloadCommand extends SubCommand {

    @Override
    public boolean onSubCommand(@NotNull CommandSender sender, @NotNull String s, String[] args) {
        DMentions.inst().updateVariables();
        Message.COMMAND_RELOAD_SUCCESS.sendMessage(sender);
        return true;
    }
}
