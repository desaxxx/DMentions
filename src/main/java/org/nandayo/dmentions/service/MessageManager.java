package org.nandayo.dmentions.service;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.nandayo.dapi.message.ChannelTitleMessage;
import org.nandayo.dapi.message.ChannelType;
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
        ChannelType.CHAT.send(player, prefixedString(msg));
    }

    /**
     * Send an action bar message to the player.
     * @param player Player
     * @param msg Message
     */
    static public void sendActionBar(@NotNull Player player, String msg) {
        if(msg == null || msg.isEmpty()) return;
        ChannelType.ACTION_BAR.send(player, prefixedString(msg));
    }

    /**
     * Send a title message to the player.
     * @param player Player
     * @param msg Message
     */
    static public void sendTitle(@NotNull Player player, String msg) {
        if(msg == null || msg.isEmpty()) return;
        String[] lines = msg.split("\\|\\|");
        String title = prefixedString(lines[0]);
        String subtitle = lines.length > 1 ? prefixedString(lines[1]) : "";

        ChannelType.TITLE_AND_SUBTITLE.send(player, new ChannelTitleMessage(prefixedString(title), subtitle));
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
