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
public class CustomizeCommand extends SubCommand {

    @Override
    public boolean onSubCommand(@NotNull CommandSender sender, @NotNull String s, String[] args) {
        if(!(sender instanceof Player)) {
            Message.COMMAND_MUST_BE_PLAYER.sendMessage(sender);
            return true;
        }
        Player player = (Player) sender;
        String display = args[1];
        if(display == null || !display.matches("^[a-zA-Z0-9_]{3,16}$")) {
            Message.COMMAND_CUSTOMIZE_INVALID_DISPLAY.sendMessage(sender);
            return true;
        }
        DMentions plugin = DMentions.inst();
        String oldDisplay = plugin.getUserManager().getMentionDisplay(player);
        plugin.getUserManager().setMentionDisplay(player, display);
        Message.COMMAND_CUSTOMIZE_SUCCESS
                .replaceValue("{old_value}", oldDisplay)
                .replaceValue("{value}", display)
                .sendMessage(player);
        return true;
    }
}
