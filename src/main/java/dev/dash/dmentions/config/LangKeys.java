package dev.dash.dmentions.config;

import dev.dash.dmentions.message.Channel;
import dev.dash.dmentions.message.Notification;
import dev.dash.dmentions.message.NotificationAdapter;
import dev.yoshi.xconfig.ConfigKey;

import java.util.List;

public final class LangKeys {

    public static final ConfigKey<List<Notification>> COOLDOWN_WARN = ConfigKey.elementList("cooldown_warn", Notification.class, NotificationAdapter.INSTANCE)
            .comment("(!) Three types of text channels are supported: CHAT, ACTION_BAR, TITLE.",
                    "    Usage: \"CHANNEL_TYPE=Any message\"",
                    "    The messages goes to chat if not specified.",
                    "    Examples:",
                    "      - \"ACTION_BAR=<red>Hello there!\"",
                    "      - \"TITLE=<#4bbdbc>Mention||<white>You have been mentioned by a player.\"",
                    "      - \"CHAT=<prefix> <gray>This is a prefixed message which you configured at config.yml\"",
                    "    For the TITLE case, use || as a delimiter to separate the main title from the subtitle.",
                    "(!) Messages are on MiniMessage format: https://docs.papermc.io/adventure/minimessage/format",
                    "(!) Leave an empty array to disable a message: sender_message: []",
                    "")
            .alias("player.cooldown_warn")
            .defaultValue(List.of(new Notification(Channel.ACTION_BAR, "<red>You must wait <remaining> before mentioning.", "")))
            .build();

    public static final ConfigKey<List<Notification>> DISABLED_WORLD_WARN = ConfigKey.elementList("disabled_world_warn", Notification.class, NotificationAdapter.INSTANCE)
            .defaultValue(List.of(new Notification(Channel.ACTION_BAR, "<prefix> <red>You can't mention a player in this world.","")))
            .build();

    public static final ConfigKey<List<Notification>> PLAYER_SENDER_MESSAGE = ConfigKey.elementList("player.sender_message", Notification.class, NotificationAdapter.INSTANCE)
            .defaultValue(List.of(new Notification(Channel.ACTION_BAR, "<prefix> <#f4e4c0>You mentioned <#ddf7a6><target><#f4e4c0>.", "")))
            .build();

    public static final ConfigKey<List<Notification>> PLAYER_TARGET_MESSAGE = ConfigKey.elementList("player.target_message", Notification.class, NotificationAdapter.INSTANCE)
            .defaultValue(List.of(new Notification(Channel.ACTION_BAR, "<prefix> <#ddf7a6><sender> <#f4e4c0>mentioned you.", ""),
                    new Notification(Channel.TITLE, "<prefix>","<#f4e4c0>You have been mentioned in chat.")))
            .build();

    public static final ConfigKey<List<Notification>> EVERYONE_SENDER_MESSAGE = ConfigKey.elementList("everyone.sender_message", Notification.class, NotificationAdapter.INSTANCE)
            .defaultValue(List.of(new Notification(Channel.ACTION_BAR, "<prefix> <#f4e4c0>You mentioned everyone.", "")))
            .build();

    public static final ConfigKey<List<Notification>> EVERYONE_TARGET_MESSAGE = ConfigKey.elementList("everyone.target_message", Notification.class, NotificationAdapter.INSTANCE)
            .defaultValue(List.of(new Notification(Channel.ACTION_BAR, "<prefix> <#f4e4c0>Player <#ddf7a6><sender><#f4e4c0> mentioned everyone.", ""),
                    new Notification(Channel.TITLE, "<prefix>","<#f4e4c0>You have been mentioned in chat.")))
            .build();
}
