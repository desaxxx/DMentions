package org.nandayo.dmentions.service;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.nandayo.dapi.HexUtil;
import org.nandayo.dmentions.DMentions;

public class MessageManager {

    /**
     * Send a message to the player.
     * @param player Player
     * @param msg Message.
     */
    static public void sendSortedMessage(@NotNull Player player, String msg) {
        if (msg == null || msg.isEmpty()) return;
        String[] parts = msg.split("=");
        if(parts.length < 2) sendMessage(player, msg);
        switch (parts[0]) {
            case "CHAT": sendMessage(player, parts[1]);break;
            case "ACTION_BAR": sendActionBar(player, parts[1]);break;
            case "TITLE": sendTitle(player, parts[1]);break;
        }
    }

    /**
     * Send a chat message to the player.
     * @param player Player
     * @param msg Message
     */
    static public void sendMessage(@NotNull Player player, String msg) {
        if(msg == null || msg.isEmpty()) return;
        String formattedText = HexUtil.color(prefixedString(msg));
        player.sendMessage(formattedText);
    }

    /**
     * Send an action bar message to the player.
     * @param player Player
     * @param msg Message
     */
    @SuppressWarnings("deprecation")
    static public void sendActionBar(@NotNull Player player, String msg) {
        if(msg == null || msg.isEmpty()) return;
        String formattedText = HexUtil.color(prefixedString(msg));
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(formattedText));
    }

    /**
     * Send a title message to the player.
     * @param player Player
     * @param msg Message
     */
    static public void sendTitle(@NotNull Player player, String msg) {
        if(msg == null || msg.isEmpty()) return;
        String[] lines = msg.split("\\|\\|");
        String title = HexUtil.color(prefixedString(lines[0]));
        String subtitle = lines.length > 1 ? HexUtil.color(prefixedString(lines[1])) : "";

        player.sendTitle(title, subtitle, 10, 30, 20);
    }

    /**
     * Replace prefix placeholders with the prefix.
     * @param str Message
     * @return Replaced Message
     */
    static private String prefixedString(String str) {
        if(str == null || str.isEmpty()) return "";
        String prefix = DMentions.inst().getConfiguration().getConfig().getString("prefix", "");
        return str.replaceAll("\\{PREFIX}", prefix);
    }
}
