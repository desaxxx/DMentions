package org.nandayo.dmentions;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.nandayo.dapi.HexUtil;
import org.nandayo.dmentions.menu.GeneralSettingsMenu;
import org.nandayo.dmentions.service.LanguageManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("NullableProblems")
public class MainCommand implements CommandExecutor, TabCompleter {

    /*
     * Commands
     */
    private final List<String> commands = Arrays.asList(
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
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        LanguageManager LANGUAGE_MANAGER = DMentions.inst().getLanguageManager();
        /*
         * Toggle command
         */
        if(args.length >= 1 && args[0].equalsIgnoreCase("toggle") && sender.hasPermission("dmentions.toggle")) {
            if(!(sender instanceof Player)) {
                sender.sendMessage(HexUtil.color(LANGUAGE_MANAGER.getString("command.must_be_player")));
                return true;
            }
            Player player = (Player) sender;
            boolean value = DMentions.inst().getUserManager().getMentionMode(player);
            DMentions.inst().getUserManager().setMentionMode(player, !value);
            if(value) {
                player.sendMessage(HexUtil.color(LANGUAGE_MANAGER.getString("command.toggle.no_longer_mentioned")));
            }else {
                player.sendMessage(HexUtil.color(LANGUAGE_MANAGER.getString("command.toggle.will_now_mentioned")));
            }
        }
        /*
         * Customize mention display command
         * /dms customize <display>
         */
        else if (args.length >= 2 && args[0].equalsIgnoreCase("customize") && sender.hasPermission("dmentions.customize")) {
            if(!(sender instanceof Player)) {
                sender.sendMessage(HexUtil.color(LANGUAGE_MANAGER.getString("command.must_be_player")));
                return true;
            }
            Player player = (Player) sender;
            String display = args[1];
            if(display == null || !display.matches("^[a-zA-Z0-9_]{3,16}$")) {
                player.sendMessage(HexUtil.color(LANGUAGE_MANAGER.getString("command.customize.invalid_display")));
                return true;
            }
            DMentions plugin = DMentions.inst();
            String oldDisplay = plugin.getUserManager().getMentionDisplay(player);
            plugin.getUserManager().setMentionDisplay(player, display);
            player.sendMessage(HexUtil.color(LANGUAGE_MANAGER.getString("command.customize.success")
                    .replace("{old_value}", oldDisplay)
                    .replace("{value}", display)
            ));
        }
        /*
         * Send mention via command
         * /dms send <keyword>
         */
        else if(args.length >= 2 && args[0].equalsIgnoreCase("send") && sender.hasPermission("dmentions.send")) {
            if(!(sender instanceof Player)) {
                sender.sendMessage(HexUtil.color(LANGUAGE_MANAGER.getString("command.must_be_player")));
                return true;
            }
            Player player = (Player) sender;
            String keyword = args[1];
            if(!DMentions.inst().getMentionManager().getValidKeywords().contains(keyword)) {
                player.sendMessage(HexUtil.color(LANGUAGE_MANAGER.getString("command.send.invalid_keyword")));
                return true;
            }
            player.chat(keyword);
        }
        /*
         * Config reload
         * /dms reload
         */
        else if(args.length >= 1 && args[0].equalsIgnoreCase("reload")  && sender.hasPermission("dmentions.reload")) {
            DMentions.inst().updateVariables();
            sender.sendMessage(HexUtil.color(LANGUAGE_MANAGER.getString("command.reload.success")));
        }
        /*
         * User commands
         * /dms user <player> mentions <boolean>
         * /dms user <player> display <text>
         */
        else if (args.length >= 4 && args[0].equalsIgnoreCase("user") && sender.hasPermission("dmentions.admin")) {
            Player player = Bukkit.getPlayerExact(args[1]);
            if (player == null) {
                sender.sendMessage(HexUtil.color(LANGUAGE_MANAGER.getString("command.player_not_found")));
                return true;
            }
            String var = args[2];
            String value = args[3];
            if(var.equalsIgnoreCase("mentions")) {
                boolean val = Boolean.parseBoolean(value);
                DMentions.inst().getUserManager().setMentionMode(player, val);
                String success = LANGUAGE_MANAGER.getString("command.user.mentions.success")
                        .replace("{p}", player.getName())
                        .replace("{value}", String.valueOf(val));
                sender.sendMessage(HexUtil.color(success));
            }
            else if (var.equalsIgnoreCase("display")) {
                if(value == null || !value.matches("^[a-zA-Z0-9_]{3,16}$")) {
                    sender.sendMessage(HexUtil.color(LANGUAGE_MANAGER.getString("command.user.display.invalid_display")));
                    return true;
                }
                DMentions.inst().getUserManager().setMentionDisplay(player, value);
                String success = LANGUAGE_MANAGER.getString("command.user.display.success")
                        .replace("{p}", player.getName())
                        .replace("{value}", value);
                sender.sendMessage(HexUtil.color(success));
            }
            else {
                sender.sendMessage(HexUtil.color(LANGUAGE_MANAGER.getString("command.user.unknown")));
            }
        }
        /*
         * Config manager
         * /dms config
         */
        else if(args.length >= 1 && args[0].equalsIgnoreCase("config") && sender.hasPermission("dmentions.configure")) {
            if(!(sender instanceof Player)) {
                sender.sendMessage(HexUtil.color(LANGUAGE_MANAGER.getString("command.must_be_player")));
                return true;
            }
            Player player = (Player) sender;
            DMentions plugin = DMentions.inst();
            if(plugin.getGuiConfigEditor() != null) {
                player.sendMessage(HexUtil.color(LANGUAGE_MANAGER.getString("command.config.already_configuring")));
                return true;
            }
            plugin.setGuiConfigEditor(player);
            plugin.getConfiguration().resetUnsavedConfig();
            new GeneralSettingsMenu(plugin, player);
        }
        /*
         * Help | Command list
         */
        else if(args.length >= 1 && args[0].equalsIgnoreCase("help") && sender.hasPermission("dmentions.help")) {
            sender.sendMessage(HexUtil.color(LANGUAGE_MANAGER.getString("command.help.description")));
            for(String c : commands) {
                String msg = LANGUAGE_MANAGER.getString("command.help.list")
                        .replace("{commands}", c);
                sender.sendMessage(HexUtil.color(msg));
            }
        }
        /*
         * Unknown command
         */
        else {
            sender.sendMessage(HexUtil.color(LANGUAGE_MANAGER.getString("command.unknown")));
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
                return Arrays.asList("toggle","reload","help","user","send","config","customize");
            }
            return Arrays.asList("toggle");
        }
        /*
         * /dms send
         */
        else if(args.length == 2 && args[0].equalsIgnoreCase("send") && sender.hasPermission("dmentions.send")) {
            return new ArrayList<>(DMentions.inst().getMentionManager().getValidKeywords());
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
