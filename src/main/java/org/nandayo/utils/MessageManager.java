package org.nandayo.utils;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.nandayo.ConfigManager;

import static org.nandayo.utils.HexUtil.color;

public class MessageManager {

    private final ConfigManager configManager;
    public MessageManager(ConfigManager configManager) {
        this.configManager = configManager;
    }

    //SORTED MESSAGE
    public void sendSortedMessage(Player player, String msg) {
        if (msg == null || msg.isEmpty()) return;
        String[] parts = msg.split("=");
        switch (parts[0]) {
            case "CHAT" -> sendMessage(player, parts[1]);
            case "ACTION_BAR" -> sendActionBar(player, parts[1]);
            case "TITLE" -> sendTitle(player, parts[1]);
        }
    }

    //CHAT MESSAGE
    public void sendMessage(Player player, String msg) {
        if(msg == null || msg.isEmpty()) return;
        String formattedText = color(prefixedString(msg));
        player.sendMessage(formattedText);
    }
    //ACTION BAR
    public void sendActionBar(Player player, String msg) {
        if(msg == null || msg.isEmpty()) return;
        String formattedText = color(prefixedString(msg));
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(formattedText));
    }
    //TITLE
    public void sendTitle(Player player, String msg) {
        if(msg == null || msg.isEmpty()) return;
        String[] lines = msg.split("\\|\\|");
        String title = color(prefixedString(lines[0]));
        String subtitle = lines.length > 1 ? color(prefixedString(lines[1])) : "";

        player.sendTitle(title, subtitle, 10, 30, 20);
    }

    //PREFIX REPLACE
    public String prefixedString(String str) {
        if(str == null || str.isEmpty()) return "";
        String prefix = configManager.getString("prefix", "");
        return str.replaceAll("\\{PREFIX}", prefix);
    }
}
