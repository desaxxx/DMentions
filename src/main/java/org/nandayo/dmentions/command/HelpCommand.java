package org.nandayo.dmentions.command;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.nandayo.dapi.command.SubCommand;
import org.nandayo.dmentions.service.message.Message;

import java.util.Arrays;
import java.util.List;

/**
 * @since 1.8.3
 */
public class HelpCommand extends SubCommand {

    static private final List<String> commands = Arrays.asList(
            "/dms toggle",
            "/dms customize <display>",
            "/dms send <keyword>",
            "/dms help",
            "/dms reload",
            "/dms config",
            "/dms user <player> mentions <true|false>",
            "/dms user <player> display <display>"
    );

    @Override
    public boolean onSubCommand(@NotNull CommandSender sender, @NotNull String s, String[] args) {
        Message.COMMAND_HELP_DESCRIPTION.sendMessage(sender);
        for(String c : commands) {
            Message.COMMAND_HELP_LIST
                    .replaceValue("{commands}", c)
                    .sendMessage(sender);
        }
        return true;
    }
}
