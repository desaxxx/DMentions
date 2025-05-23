package org.nandayo.dmentions.service;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.nandayo.dapi.HexUtil;
import org.nandayo.dmentions.DMentions;

public class MessageManager {

    private final @NotNull DMentions plugin;
    public MessageManager(@NotNull DMentions plugin) {
        this.plugin = plugin;
    }

    //SORTED MESSAGE
    public void sendSortedMessage(Player player, String msg) {
        if (msg == null || msg.isEmpty()) return;
        String[] parts = msg.split("=");
        if(parts.length < 2) sendMessage(player, msg);
        switch (parts[0]) {
            case "CHAT": sendMessage(player, parts[1]);break;
            case "ACTION_BAR": sendActionBar(player, parts[1]);break;
            case "TITLE": sendTitle(player, parts[1]);break;
        }
    }

    //CHAT MESSAGE
    public void sendMessage(Player player, String msg) {
        if(msg == null || msg.isEmpty()) return;
        String formattedText = HexUtil.color(prefixedString(msg));
        player.sendMessage(formattedText);
    }
    //ACTION BAR
    @SuppressWarnings("deprecation")
    public void sendActionBar(Player player, String msg) {
        if(msg == null || msg.isEmpty()) return;
        String formattedText = HexUtil.color(prefixedString(msg));
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(formattedText));
    }
    //TITLE
    public void sendTitle(Player player, String msg) {
        if(msg == null || msg.isEmpty()) return;
        String[] lines = msg.split("\\|\\|");
        String title = HexUtil.color(prefixedString(lines[0]));
        String subtitle = lines.length > 1 ? HexUtil.color(prefixedString(lines[1])) : "";

        player.sendTitle(title, subtitle, 10, 30, 20);
    }

    //PREFIX REPLACE
    public String prefixedString(String str) {
        if(str == null || str.isEmpty()) return "";
        String prefix = plugin.getConfiguration().getConfig().getString("prefix", "");
        return str.replaceAll("\\{PREFIX}", prefix);
    }
}
