package dev.dash.dmentions.message;

import dev.yoshi.xconfig.ConfigException;
import dev.yoshi.xconfig.TypeAdapter;

import java.util.Locale;
import java.util.regex.Pattern;

public class NotificationAdapter implements TypeAdapter<Notification> {

    public static final NotificationAdapter INSTANCE = new NotificationAdapter();

    private static final String CHANNEL_DELIMITER = "=";
    private static final String TEXT_DELIMITER = "||";

    private NotificationAdapter() {
    }

    @Override
    public Notification deserialize(Object raw, String path) {
        String str = raw.toString();

        Channel channel = Channel.CHAT;
        String text = str;
        for (Channel c : Channel.values()) {
            String compare = c.name() + CHANNEL_DELIMITER;
            if (str.regionMatches(true, 0, compare, 0, compare.length())) {
                channel = c;
                text = str.substring(c.name().length() + CHANNEL_DELIMITER.length());
                break;
            }
        }

        if (channel == Channel.TITLE) {
            String[] titles = text.split(Pattern.quote(TEXT_DELIMITER), 2);
            String subtitle = titles.length > 1 ? titles[1] : "";
            if (titles[0].isBlank() && subtitle.isBlank()) {
                throw new ConfigException("Notification title and subtitle cannot be blank");
            }
            return new Notification(channel, titles[0], subtitle);
        } else {
            if (text.isBlank()) {
                throw new ConfigException("Notification text cannot be blank");
            }
            return new Notification(channel, text, "");
        }
    }

    @Override
    public Object serialize(Notification value) {
        if (value.channel() == Channel.TITLE) {
            return value.channel().name() + CHANNEL_DELIMITER + value.text() + TEXT_DELIMITER + value.subtitle();
        }
        return value.channel().name() + CHANNEL_DELIMITER + value.text();
    }
}
