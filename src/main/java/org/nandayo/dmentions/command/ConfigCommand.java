package org.nandayo.dmentions.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.nandayo.dapi.command.SubCommand;
import org.nandayo.dmentions.DMentions;
import org.nandayo.dmentions.menu.GeneralSettingsMenu;
import org.nandayo.dmentions.service.message.Message;

/**
 * @since 1.8.3
 */
public class ConfigCommand extends SubCommand {

    @Override
    public boolean onSubCommand(@NotNull CommandSender sender, @NotNull String s, String[] args) {
        if(!(sender instanceof Player)) {
            Message.COMMAND_MUST_BE_PLAYER.sendMessage(sender);
            return true;
        }
        Player player = (Player) sender;
        DMentions plugin = DMentions.inst();
        if(plugin.getGuiConfigEditor() != null) {
            Message.COMMAND_CONFIG_ALREADY_CONFIGURING.sendMessage(player);
            return true;
        }
        plugin.setGuiConfigEditor(player);
        plugin.getConfiguration().resetUnsavedConfig();
        new GeneralSettingsMenu(plugin, player);
        return true;
    }
}
