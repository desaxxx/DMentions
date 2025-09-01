package org.nandayo.dmentions.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.nandayo.dapi.command.SubCommand;
import org.nandayo.dmentions.DMentions;
import org.nandayo.dmentions.service.message.Message;

/**
 * @since 1.8.3
 */
public class ToggleCommand extends SubCommand {

    @Override
    public boolean onSubCommand(@NotNull CommandSender sender, @NotNull String s, String[] args) {
        if(!(sender instanceof Player)) {
            Message.COMMAND_MUST_BE_PLAYER.sendMessage(sender);
            return true;
        }
        Player player = (Player) sender;
        boolean value = DMentions.inst().getUserManager().getMentionMode(player);
        DMentions.inst().getUserManager().setMentionMode(player, !value);
        if(value) {
            Message.COMMAND_TOGGLE_NO_LONGER_MENTIONED.sendMessage(player);
        }else {
            Message.COMMAND_TOGGLE_WILL_NOW_MENTIONED.sendMessage(player);
        }
        return true;
    }
}
