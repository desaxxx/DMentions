package org.nandayo;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.nandayo.utils.HexUtil.color;

@SuppressWarnings("NullableProblems")
public class MainCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if(args.length >= 1 && args[0].equalsIgnoreCase("toggle") && sender.hasPermission("dmentions.toggle")) {
            if(sender instanceof Player p) {
                boolean value = Main.userManager.getMentionMode(p);
                Main.userManager.setMentionMode(p, !value);
                if(value) {
                    p.sendMessage(color("&eYou will no longer be mentioned."));
                }else {
                    p.sendMessage(color("&eYou will now be mentioned."));
                }
            }else {
                sender.sendMessage(color("&cYou must be a player to use this command!"));
            }
        }
        /*
         * Send Mention via Command
         * /dms send <keyword>
         */
        else if(args.length >= 2 && args[0].equalsIgnoreCase("send") && sender.hasPermission("dmentions.send")) {
            if(sender instanceof Player p) {
                String keyword = args[1];
                if(Main.mentionManager.getValidKeywords().contains(keyword)) {
                    p.chat(keyword);
                }else {
                    p.sendMessage(color("&cInvalid keyword."));
                }
            }else {
                sender.sendMessage(color("&cYou must be a player to use this command!"));
            }
        }
        /*
         * Config Reload
         */
        else if(args.length >= 1 && args[0].equalsIgnoreCase("reload")  && sender.hasPermission("dmentions.reload")) {
            Main.updateVariables();
            sender.sendMessage(color("&aReloaded configuration."));
        }
        /*
         * User Commands
         * /dms user <player> mentions true
         */
        else if (args.length >= 4 && args[0].equalsIgnoreCase("user") && sender.hasPermission("dmentions.admin")) {
            Player player = Bukkit.getPlayerExact(args[1]);
            if (player == null) {
                sender.sendMessage(color("&cPlayer not found."));
                return true;
            }
            String var = args[2];
            String value = args[3];
            if(var.equalsIgnoreCase("mentions")) {
                boolean val = Boolean.parseBoolean(value);
                Main.userManager.setMentionMode(player, val);
                sender.sendMessage(color("&eMention mode of &f" + player.getName() + "&e set to " + val + "."));
            }else {
                sender.sendMessage(color("&cUnknown argument."));
            }
        }
        /*
         * Help | Command List
         */
        else if(args.length >= 1 && args[0].equalsIgnoreCase("help") && sender.hasPermission("dmentions.help")) {
            sender.sendMessage(color("&6Here is command list."),
                    color("&7/dms help"),
                    color("&7/dms reload"),
                    color("&7/dms user <player> <mentions> <true|false>"));
        }
        /*
         * Unknown Command
         */
        else {
            sender.sendMessage(color("&cUnknown command."));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
        /*
         * /dms
         */
        if(args.length == 1) {
            if(sender.hasPermission("dmentions.admin")) {
                return Arrays.asList("toggle","reload","help","user","send");
            }
            return Arrays.asList("toggle");
        }
        /*
         * /dms send
         */
        else if(args.length == 2 && args[0].equalsIgnoreCase("send") && sender.hasPermission("dmentions.send")) {
            return Main.mentionManager.getValidKeywords().stream().toList();
        }
        /*
         * /dms user
         */
        else if(args[0].equalsIgnoreCase("user") && sender.hasPermission("dmentions.admin")) {
            if(args.length == 2) {
                return null;
            }else if(args.length == 3) {
                return Arrays.asList("mentions");
            }
            else if(args.length == 4) {
                /*
                 * /dms user <player> <var>
                 */
                if(args[2].equalsIgnoreCase("mentions")) {
                    return Arrays.asList("true","false");
                }
            }
        }
        return new ArrayList<>();
    }
}
