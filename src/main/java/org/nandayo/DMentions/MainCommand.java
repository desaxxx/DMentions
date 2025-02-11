package org.nandayo.DMentions;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.nandayo.DAPI.HexUtil;
import org.nandayo.DMentions.GUI.GeneralSettingsMenu;
import org.nandayo.DMentions.utils.ConfigManager;
import org.nandayo.DMentions.utils.GUIManager;
import org.nandayo.DMentions.utils.LangManager;

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
        LangManager langManager = Main.inst().langManager;
        /*
         * Toggle command
         */
        if(args.length >= 1 && args[0].equalsIgnoreCase("toggle") && sender.hasPermission("dmentions.toggle")) {
            if(!(sender instanceof Player p)) {
                sender.sendMessage(HexUtil.color(langManager.getMsg("command.must_be_player")));
                return true;
            }
            boolean value = Main.inst().userManager.getMentionMode(p);
            Main.inst().userManager.setMentionMode(p, !value);
            if(value) {
                p.sendMessage(HexUtil.color(langManager.getMsg("command.toggle.no_longer_mentioned")));
            }else {
                p.sendMessage(HexUtil.color(langManager.getMsg("command.toggle.will_now_mentioned")));
            }
        }
        /*
         * Customize mention display command
         * /dms customize <display>
         */
        else if (args.length >= 2 && args[0].equalsIgnoreCase("customize") && sender.hasPermission("dmentions.customize")) {
            if(!(sender instanceof Player p)) {
                sender.sendMessage(HexUtil.color(langManager.getMsg("command.must_be_player")));
                return true;
            }
            String display = args[1];
            if(display == null || !display.matches("^[a-zA-Z0-9_]{3,16}$")) {
                p.sendMessage(HexUtil.color(langManager.getMsg("command.customize.invalid_display")));
                return true;
            }
            Main plugin = Main.inst();
            String oldDisplay = plugin.userManager.getMentionDisplay(p);
            plugin.userManager.setMentionDisplay(p, display);
            p.sendMessage(HexUtil.color(langManager.getMsg("command.customize.success").replace("{old_value}", oldDisplay).replace("{value}", display)));
        }
        /*
         * Send mention via command
         * /dms send <keyword>
         */
        else if(args.length >= 2 && args[0].equalsIgnoreCase("send") && sender.hasPermission("dmentions.send")) {
            if(!(sender instanceof Player p)) {
                sender.sendMessage(HexUtil.color(langManager.getMsg("command.must_be_player")));
                return true;
            }
            String keyword = args[1];
            if(!Main.inst().mentionManager.getValidKeywords().contains(keyword)) {
                p.sendMessage(HexUtil.color(langManager.getMsg("command.send.invalid_keyword")));
                return true;
            }
            p.chat(keyword);
        }
        /*
         * Config reload
         * /dms reload
         */
        else if(args.length >= 1 && args[0].equalsIgnoreCase("reload")  && sender.hasPermission("dmentions.reload")) {
            Main.inst().updateVariables();
            sender.sendMessage(HexUtil.color(Main.inst().langManager.getMsg("command.reload.success")));
        }
        /*
         * User commands
         * /dms user <player> mentions <boolean>
         * /dms user <player> display <text>
         */
        else if (args.length >= 4 && args[0].equalsIgnoreCase("user") && sender.hasPermission("dmentions.admin")) {
            Player player = Bukkit.getPlayerExact(args[1]);
            if (player == null) {
                sender.sendMessage(HexUtil.color(langManager.getMsg("command.player_not_found")));
                return true;
            }
            String var = args[2];
            String value = args[3];
            if(var.equalsIgnoreCase("mentions")) {
                boolean val = Boolean.parseBoolean(value);
                Main.inst().userManager.setMentionMode(player, val);
                String success = langManager.getMsg("command.user.mentions.success").replace("{p}", player.getName()).replace("{value}", String.valueOf(val));
                sender.sendMessage(HexUtil.color(success));
            }
            else if (var.equalsIgnoreCase("display")) {
                if(value == null || !value.matches("^[a-zA-Z0-9_]{3,16}$")) {
                    sender.sendMessage(HexUtil.color(langManager.getMsg("command.user.display.invalid_display")));
                    return true;
                }
                Main.inst().userManager.setMentionDisplay(player, value);
                String success = langManager.getMsg("command.user.display.success").replace("{p}", player.getName()).replace("{value}", value);
                sender.sendMessage(HexUtil.color(success));
            }
            else {
                sender.sendMessage(HexUtil.color(langManager.getMsg("command.user.unknown")));
            }
        }
        /*
         * Config manager
         * /dms config
         */
        else if(args.length >= 1 && args[0].equalsIgnoreCase("config") && sender.hasPermission("dmentions.configure")) {
            if(!(sender instanceof Player p)) {
                sender.sendMessage(HexUtil.color(langManager.getMsg("command.must_be_player")));
                return true;
            }
            Main plugin = Main.inst();
            if(plugin.guiConfigEditor != null) {
                p.sendMessage(HexUtil.color(langManager.getMsg("command.config.already_configuring")));
                return true;
            }
            plugin.guiConfigEditor = p;
            ConfigManager configManager = plugin.configManager;
            configManager.resetGuiConfig();
            GUIManager manager = new GUIManager(plugin, plugin.configManager, p);
            new GeneralSettingsMenu(plugin, p, manager);
        }
        /*
         * Help | Command list
         */
        else if(args.length >= 1 && args[0].equalsIgnoreCase("help") && sender.hasPermission("dmentions.help")) {
            sender.sendMessage(HexUtil.color(langManager.getMsg("command.help.description")));
            for(String c : commands) {
                String msg = langManager.getMsg("command.help.list").replace("{commands}", c);
                sender.sendMessage(HexUtil.color(msg));
            }
        }
        /*
         * Unknown command
         */
        else {
            sender.sendMessage(HexUtil.color(langManager.getMsg("command.unknown")));
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
            return Main.inst().mentionManager.getValidKeywords().stream().toList();
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
