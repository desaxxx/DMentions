package org.nandayo;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.nandayo.HexUtil.color;

public class MainCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if(sender instanceof Player p) {
            if(!p.hasPermission("oyuncuetiket.reload")) {
                p.sendMessage(color("&cYou cannot use this command."));
            }
        }
        if(args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            Main.config = new Config();
            sender.sendMessage(color("&aConfig dosyası yenilendi."));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
        if(args.length == 1) {
            return Arrays.asList("reload");
        }
        return new ArrayList<>();
    }
}
