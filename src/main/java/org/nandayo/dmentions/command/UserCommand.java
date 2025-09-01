package org.nandayo.dmentions.command;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.nandayo.dapi.command.SubCommand;
import org.nandayo.dmentions.DMentions;
import org.nandayo.dmentions.service.message.Message;

/**
 * @since 1.8.3
 */
public class UserCommand extends SubCommand {

    @Override
    public boolean onSubCommand(@NotNull CommandSender sender, @NotNull String s, String[] args) {
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            Message.COMMAND_PLAYER_NOT_FOUND.sendMessage(sender);
            return true;
        }
        String var = args[2];
        String value = args[3];
        if(var.equalsIgnoreCase("mentions")) {
            boolean val = Boolean.parseBoolean(value);
            DMentions.inst().getUserManager().setMentionMode(target, val);
            Message.COMMAND_USER_MENTIONS_SUCCESS
                    .replaceValue("{target}", target.getName())
                    .replaceValue("{value}", String.valueOf(val))
                    .sendMessage(sender);
        }
        else if (var.equalsIgnoreCase("display")) {
            if(value == null || !value.matches("^[a-zA-Z0-9_]{3,16}$")) {
                Message.COMMAND_USER_DISPLAY_INVALID_DISPLAY.sendMessage(sender);
                return true;
            }
            DMentions.inst().getUserManager().setMentionDisplay(target, value);
            Message.COMMAND_USER_DISPLAY_SUCCESS
                    .replaceValue("{target}", target.getName())
                    .replaceValue("{value}", value)
                    .sendMessage(sender);
        }
        else {
            Message.COMMAND_USER_UNKNOWN.sendMessage(sender);
        }
        return true;
    }
}
