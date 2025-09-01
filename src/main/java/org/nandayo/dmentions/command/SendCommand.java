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
public class SendCommand extends SubCommand {

    @Override
    public boolean onSubCommand(@NotNull CommandSender sender, @NotNull String s, String[] args) {
        if(!(sender instanceof Player)) {
            Message.COMMAND_MUST_BE_PLAYER.sendMessage(sender);
            return true;
        }
        Player player = (Player) sender;
        String keyword = args[1];
        if(!DMentions.inst().getMentionManager().getMentionHolders().containsKey(keyword)) {
            Message.COMMAND_SEND_INVALID_KEYWORD.sendMessage(sender);
            return true;
        }
        player.chat(keyword);
        return true;
    }
}
