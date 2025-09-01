package org.nandayo.dmentions.service;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.nandayo.dapi.message.ChannelTitleMessage;
import org.nandayo.dapi.message.ChannelType;
import org.nandayo.dmentions.service.message.MessageRouter;
import org.nandayo.dmentions.util.DUtil;

@Deprecated(since = "1.8.3", forRemoval = true)
public class MessageManager {

    @Deprecated(since = "1.8.3", forRemoval = true)
    static public void sendSortedMessage(@NotNull Player player, String msg) {
        MessageRouter.sendResolved(player, msg);
    }

    @Deprecated(since = "1.8.3", forRemoval = true)
    static public void sendMessage(@NotNull Player player, String msg) {
        if(msg == null || msg.isEmpty()) return;
        ChannelType.CHAT.send(player, prefixedString(msg));
    }

    @Deprecated(since = "1.8.3", forRemoval = true)
    static public void sendActionBar(@NotNull Player player, String msg) {
        if(msg == null || msg.isEmpty()) return;
        ChannelType.ACTION_BAR.send(player, prefixedString(msg));
    }

    @Deprecated(since = "1.8.3", forRemoval = true)
    static public void sendTitle(@NotNull Player player, String msg) {
        if(msg == null || msg.isEmpty()) return;
        String[] lines = msg.split("\\|\\|");
        String title = prefixedString(lines[0]);
        String subtitle = lines.length > 1 ? prefixedString(lines[1]) : "";
        ChannelType.TITLE_AND_SUBTITLE.send(player, new ChannelTitleMessage(title, subtitle));
    }

    @Deprecated(since = "1.8.3", forRemoval = true)
    static private String prefixedString(String str) {
        return DUtil.replacePrefixes(str);
    }
}
