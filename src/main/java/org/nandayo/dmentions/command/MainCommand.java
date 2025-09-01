package org.nandayo.dmentions.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.nandayo.dmentions.DMentions;
import org.nandayo.dmentions.service.message.Message;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, String[] args) {
        /*
         * Toggle command
         */
        if(args.length >= 1 && args[0].equalsIgnoreCase("toggle") && sender.hasPermission("dmentions.toggle")) {
            return new ToggleCommand().onSubCommand(sender, s, args);
        }
        /*
         * Customize mention display command
         * /dms customize <display>
         */
        else if (args.length >= 2 && args[0].equalsIgnoreCase("customize") && sender.hasPermission("dmentions.customize")) {
            return new CustomizeCommand().onSubCommand(sender, s, args);
        }
        /*
         * Send mention via command
         * /dms send <keyword>
         */
        else if(args.length >= 2 && args[0].equalsIgnoreCase("send") && sender.hasPermission("dmentions.send")) {
            return new SendCommand().onSubCommand(sender, s, args);
        }
        /*
         * Config reload
         * /dms reload
         */
        else if(args.length >= 1 && args[0].equalsIgnoreCase("reload")  && sender.hasPermission("dmentions.reload")) {
            return new ReloadCommand().onSubCommand(sender, s, args);
        }
        /*
         * User commands
         * /dms user <player> mentions <boolean>
         * /dms user <player> display <text>
         */
        else if (args.length >= 4 && args[0].equalsIgnoreCase("user") && sender.hasPermission("dmentions.admin")) {
            return new UserCommand().onSubCommand(sender, s, args);
        }
        /*
         * Config manager
         * /dms config
         */
        else if(args.length >= 1 && args[0].equalsIgnoreCase("config") && sender.hasPermission("dmentions.configure")) {
            return new ConfigCommand().onSubCommand(sender, s, args);
        }
        /*
         * Help | Command list
         */
        else if(args.length >= 1 && args[0].equalsIgnoreCase("help") && sender.hasPermission("dmentions.help")) {
            return new HelpCommand().onSubCommand(sender, s, args);
        }
        /*
         * Unknown command
         */
        else {
            Message.COMMAND_UNKNOWN.sendMessage(sender);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, String[] args) {
        /*
         * /dms
         */
        if(args.length == 1) {
            if(sender.hasPermission("dmentions.admin")) {
                return Arrays.asList("toggle","reload","help","user","send","config","customize");
            }
            return Arrays.asList("toggle");
        }
        /*
         * /dms send
         */
        else if(args.length == 2 && args[0].equalsIgnoreCase("send") && sender.hasPermission("dmentions.send")) {
            return new ArrayList<>(DMentions.inst().getMentionManager().getMentionHolders().keySet());
        }
        /*
         * /dms user
         */
        else if(args[0].equalsIgnoreCase("user") && sender.hasPermission("dmentions.admin")) {
            if(args.length == 2) {
                return null;
            }else if(args.length == 3) {
                return Arrays.asList("mentions","display");
            }
            else if(args.length == 4) {
                /*
                 * /dms user <player> <var>
                 */
                if(args[2].equalsIgnoreCase("mentions")) {
                    return Arrays.asList("true","false");
                }else if (args[2].equalsIgnoreCase("display")) {
                    return Arrays.asList(args[1]);
                }
            }
        }
        /*
         * /dms customize
         */
        else if (args[0].equalsIgnoreCase("customize") && sender.hasPermission("dmentions.customize")) {
            if (args.length == 2) {
                return Arrays.asList(sender.getName());
            }
        }
        return new ArrayList<>();
    }
}
