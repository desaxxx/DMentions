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
        if(args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if(!sender.hasPermission("dmentions.reload")) {
                sender.sendMessage(color("&cBu komudu kullanmaya yetkin yok."));
                return true;
            }
            Main.config = new Config();
            sender.sendMessage(color("&aKonfigurasyon dosyası yenilendi."));
        }else {
            sender.sendMessage(color("&cBilinmeyen komut."));
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
