package org.nandayo;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.nandayo.GUI.GeneralSettingsMenu;
import org.nandayo.utils.ConfigManager;
import org.nandayo.utils.GUIManager;
import org.nandayo.utils.LangManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.nandayo.utils.HexUtil.color;

@SuppressWarnings("NullableProblems")
public class MainCommand implements CommandExecutor, TabCompleter {

    private final List<String> commands = Arrays.asList(
            "/dms toggle",
            "/dms send <keyword>",
            "/dms help",
            "/dms reload",
            "/dms config",
            "/dms user <player> mentions true|false");

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        LangManager langManager = Main.inst().langManager;
        if(args.length >= 1 && args[0].equalsIgnoreCase("toggle") && sender.hasPermission("dmentions.toggle")) {
            if(sender instanceof Player p) {
                boolean value = Main.inst().userManager.getMentionMode(p);
                Main.inst().userManager.setMentionMode(p, !value);
                if(value) {
                    p.sendMessage(color(langManager.getMsg("command.toggle.no_longer_mentioned")));
                }else {
                    p.sendMessage(color(langManager.getMsg("command.toggle.will_now_mentioned")));
                }
            }else {
                sender.sendMessage(color(langManager.getMsg("command.must_be_player")));
            }
        }
        /*
         * Send Mention via Command
         * /dms send <keyword>
         */
        else if(args.length >= 2 && args[0].equalsIgnoreCase("send") && sender.hasPermission("dmentions.send")) {
            if(sender instanceof Player p) {
                String keyword = args[1];
                if(Main.inst().mentionManager.getValidKeywords().contains(keyword)) {
                    p.chat(keyword);
                }else {
                    p.sendMessage(color(langManager.getMsg("command.send.invalid_keyword")));
                }
            }else {
                sender.sendMessage(color(langManager.getMsg("command.must_be_player")));
            }
        }
        /*
         * Config Reload
         */
        else if(args.length >= 1 && args[0].equalsIgnoreCase("reload")  && sender.hasPermission("dmentions.reload")) {
            Main.inst().updateVariables();
            sender.sendMessage(color(Main.inst().langManager.getMsg("command.reload.success")));
        }
        /*
         * User Commands
         * /dms user <player> mentions true
         */
        else if (args.length >= 4 && args[0].equalsIgnoreCase("user") && sender.hasPermission("dmentions.admin")) {
            Player player = Bukkit.getPlayerExact(args[1]);
            if (player == null) {
                sender.sendMessage(color(langManager.getMsg("command.player_not_found")));
                return true;
            }
            String var = args[2];
            String value = args[3];
            if(var.equalsIgnoreCase("mentions")) {
                boolean val = Boolean.parseBoolean(value);
                Main.inst().userManager.setMentionMode(player, val);
                String success = langManager.getMsg("command.user.mentions.success").replace("{p}", player.getName()).replace("{value}", String.valueOf(val));
                sender.sendMessage(color(success));
            }else {
                sender.sendMessage(color(langManager.getMsg("command.user.unknown")));
            }
        }
        /*
         * Config Manager
         * /dms config
         */
        else if(args.length >= 1 && args[0].equalsIgnoreCase("config") && sender.hasPermission("dmentions.configure")) {
            if(sender instanceof Player p) {
                Main plugin = Main.inst();
                if(plugin.guiConfigEditor == null) {
                    plugin.guiConfigEditor = p;

                    ConfigManager configManager = plugin.configManager;
                    configManager.resetGuiConfig();
                    GUIManager manager = new GUIManager(plugin, plugin.configManager, p);
                    new GeneralSettingsMenu(plugin, p, manager);
                }else {
                    p.sendMessage(color(langManager.getMsg("command.config.already_configuring")));
                }
            }else {
                sender.sendMessage(color(langManager.getMsg("command.must_be_player")));
            }
        }
        /*
         * Help | Command List
         */
        else if(args.length >= 1 && args[0].equalsIgnoreCase("help") && sender.hasPermission("dmentions.help")) {
            sender.sendMessage(color(langManager.getMsg("command.help.description")));
            for(String c : commands) {
                String msg = langManager.getMsg("command.help.list").replace("{commands}", c);
                sender.sendMessage(color(msg));
            }
        }
        /*
         * Unknown Command
         */
        else {
            sender.sendMessage(color(langManager.getMsg("command.unknown")));
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
                return Arrays.asList("toggle","reload","help","user","send","config");
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
