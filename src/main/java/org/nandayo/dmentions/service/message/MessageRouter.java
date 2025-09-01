package org.nandayo.dmentions.service.message;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.nandayo.dapi.message.ChannelMessage;
import org.nandayo.dapi.message.ChannelTitleMessage;
import org.nandayo.dapi.message.ChannelType;
import org.nandayo.dmentions.util.DUtil;

import java.util.Locale;

/**
 * @since 1.8.3
 */
public class MessageRouter {

    /**
     * Send a message with unresolved String. The message is being resolved via {@link #resolveChannelMessage(String)}.
     * @param receiver Receiver
     * @param unresolvedMessage Unresolved String
     * @since 1.8.3
     */
    static public void sendResolved(@NotNull CommandSender receiver, @NotNull String unresolvedMessage) {
        String prefixedMsg = DUtil.replacePrefixes(unresolvedMessage);
        send(receiver, resolveChannelMessage(prefixedMsg), resolveChannelType(prefixedMsg));
    }

    /**
     * Send a {@link ChannelMessage} from given {@link ChannelType} to the receiver.
     * @param receiver Receiver
     * @param channelMessage ChannelMessage
     * @param channelType ChannelType
     * @since 1.8.3
     */
    static public void send(@NotNull CommandSender receiver, @NotNull ChannelMessage channelMessage, @NotNull ChannelType channelType) {
        channelType.send(receiver, channelMessage);
    }

    /**
     * Get a {@link ChannelMessage} from given String.
     * @param unresolvedMessage Unresolved String
     * @return ChannelMessage
     * @since 1.8.3
     */
    static private ChannelMessage resolveChannelMessage(@NotNull String unresolvedMessage) {
        String[] parts =  unresolvedMessage.split("=",-1);
        // doesn't contain "=", so return a plain ChannelMessage
        if(parts.length < 2) return new ChannelMessage(unresolvedMessage);

        if (parts[0].equalsIgnoreCase("TITLE")) {
            String[] titleParts = parts[1].split("\\|\\|",-1);
            String title = titleParts[0];
            String subtitle = titleParts.length > 1 ? titleParts[1] : "";
            return new ChannelTitleMessage(title, subtitle);
        }
        // for CHAT and ACTION_BAR it returns plain ChannelMessage
        return new ChannelMessage(parts[1]);
    }

    /**
     * Get a {@link ChannelType} from given String.
     * @param unresolvedMessage Unresolved String
     * @return ChannelType
     * @since 1.8.3
     */
    static private ChannelType resolveChannelType(@NotNull String unresolvedMessage) {
        String[] parts =  unresolvedMessage.split("=",-1);
        // doesn't contain "=", so return CHAT
        if(parts.length < 2) return ChannelType.CHAT;

        switch (parts[0].toUpperCase(Locale.ENGLISH)) {
            case "ACTION_BAR":
                return ChannelType.ACTION_BAR;
            case "TITLE":
                return ChannelType.TITLE_AND_SUBTITLE;
            default:
                return ChannelType.CHAT;
        }
    }
}
