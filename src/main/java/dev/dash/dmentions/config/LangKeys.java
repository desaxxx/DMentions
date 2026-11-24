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

    public static final ConfigKey<List<Notification>> NEARBY_SENDER_MESSAGE = ConfigKey.elementList("nearby.sender_message", Notification.class, NotificationAdapter.INSTANCE)
            .defaultValue(List.of(new Notification(Channel.ACTION_BAR, "<prefix> <#f4e4c0>You mentioned nearby players.", "")))
            .build();

    public static final ConfigKey<List<Notification>> NEARBY_TARGET_MESSAGE = ConfigKey.elementList("nearby.target_message", Notification.class, NotificationAdapter.INSTANCE)
            .defaultValue(List.of(new Notification(Channel.ACTION_BAR, "<prefix> <#f4e4c0>Player <#ddf7a6><sender><#f4e4c0> mentioned nearby players.", ""),
                    new Notification(Channel.TITLE, "<prefix>","<#f4e4c0>You have been mentioned in chat.")))
            .build();

    public static final ConfigKey<List<Notification>> GROUP_SENDER_MESSAGE = ConfigKey.elementList("group.sender_message", Notification.class, NotificationAdapter.INSTANCE)
            .defaultValue(List.of(new Notification(Channel.ACTION_BAR, "<prefix> <#f4e4c0>You mentioned <#ddf7a6><group><#f4e4c0> members.", "")))
            .build();

    public static final ConfigKey<List<Notification>> GROUP_TARGET_MESSAGE = ConfigKey.elementList("group.target_message", Notification.class, NotificationAdapter.INSTANCE)
            .defaultValue(List.of(new Notification(Channel.ACTION_BAR, "<prefix> <#f4e4c0>Player <#ddf7a6><sender><#f4e4c0> mentioned <#ddf7a6><group><#f4e4c0> members.", ""),
                    new Notification(Channel.TITLE, "<prefix>","<#f4e4c0>You have been mentioned in chat.")))
            .build();

    public static final ConfigKey<List<Notification>> COMMAND_HELP_HEADER = ConfigKey.elementList("command.help.header", Notification.class, NotificationAdapter.INSTANCE)
            .defaultValue(List.of(new Notification(Channel.CHAT, "<prefix> <gray>Commands:", "")))
            .build();

    public static final ConfigKey<List<Notification>> COMMAND_HELP_VERSION = ConfigKey.elementList("command.help.version", Notification.class, NotificationAdapter.INSTANCE)
            .defaultValue(List.of(new Notification(Channel.CHAT, "<#ffd16d>/dms version <gray>- Show the plugin version", "")))
            .build();

    public static final ConfigKey<List<Notification>> COMMAND_HELP_TOGGLE = ConfigKey.elementList("command.help.toggle", Notification.class, NotificationAdapter.INSTANCE)
            .defaultValue(List.of(new Notification(Channel.CHAT, "<#ffd16d>/dms toggle <gray>- Turn being mentioned on or off", "")))
            .build();

    public static final ConfigKey<List<Notification>> COMMAND_HELP_SEND =ConfigKey.elementList("command.help.send", Notification.class, NotificationAdapter.INSTANCE)
            .defaultValue(List.of(new Notification(Channel.CHAT, "<#ffd16d>/dms send <keyword> <gray>- Send a mention", "")))
            .build();

    public static final ConfigKey<List<Notification>> COMMAND_HELP_RELOAD = ConfigKey.elementList("command.help.reload", Notification.class, NotificationAdapter.INSTANCE)
            .defaultValue(List.of(new Notification(Channel.CHAT, "<#ffd16d>/dms reload <gray>- Reload the config and language files", "")))
            .build();

    public static final ConfigKey<List<Notification>> COMMAND_HELP_USER = ConfigKey.elementList("command.help.user", Notification.class, NotificationAdapter.INSTANCE)
            .defaultValue(List.of(new Notification(Channel.CHAT, "<#ffd16d>/dms user <player> mentionable <true|false> <gray>- Set whether a player can be mentioned", "")))
            .build();

    public static final ConfigKey<List<Notification>> COMMAND_MUST_BE_PLAYER = ConfigKey.elementList("command.must_be_player", Notification.class, NotificationAdapter.INSTANCE)
            .defaultValue(List.of(new Notification(Channel.CHAT, "<prefix> <red>You must be a player to use this command.", "")))
            .build();

    public static final ConfigKey<List<Notification>> COMMAND_PLAYER_NOT_FOUND = ConfigKey.elementList("command.player_not_found", Notification.class, NotificationAdapter.INSTANCE)
            .defaultValue(List.of(new Notification(Channel.CHAT, "<prefix> <red>Player not found: <white><player>", "")))
            .build();

    public static final ConfigKey<List<Notification>> COMMAND_VERSION = ConfigKey.elementList("command.version", Notification.class, NotificationAdapter.INSTANCE)
            .defaultValue(List.of(new Notification(Channel.CHAT, "<prefix> <gray>Running DMentions <white><version>", "")))
            .build();

    public static final ConfigKey<List<Notification>> COMMAND_TOGGLE_ENABLED = ConfigKey.elementList("command.toggle.enabled", Notification.class, NotificationAdapter.INSTANCE)
            .defaultValue(List.of(new Notification(Channel.CHAT, "<prefix> <green>You will now be mentioned.", "")))
            .build();

    public static final ConfigKey<List<Notification>> COMMAND_TOGGLE_DISABLED = ConfigKey.elementList("command.toggle.disabled", Notification.class, NotificationAdapter.INSTANCE)
            .defaultValue(List.of(new Notification(Channel.CHAT, "<prefix> <yellow>You will no longer be mentioned.", "")))
            .build();

    public static final ConfigKey<List<Notification>> COMMAND_USER_MENTIONABLE_ENABLED = ConfigKey.elementList("command.user.mentionable.enabled", Notification.class, NotificationAdapter.INSTANCE)
            .defaultValue(List.of(new Notification(Channel.CHAT, "<prefix> <white><target> <green>can now be mentioned.", "")))
            .build();

    public static final ConfigKey<List<Notification>> COMMAND_USER_MENTIONABLE_DISABLED = ConfigKey.elementList("command.user.mentionable.disabled", Notification.class, NotificationAdapter.INSTANCE)
            .defaultValue(List.of(new Notification(Channel.CHAT, "<prefix> <white><target> <yellow>can no longer be mentioned.", "")))
            .build();

    public static final ConfigKey<List<Notification>> COMMAND_SEND_INVALID_KEYWORD =ConfigKey.elementList("command.send.invalid_keyword", Notification.class, NotificationAdapter.INSTANCE)
            .defaultValue(List.of(new Notification(Channel.CHAT, "<prefix> <red>Invalid keyword: <white><keyword>", "")))
            .build();

    public static final ConfigKey<List<Notification>> COMMAND_RELOAD_SUCCESS = ConfigKey.elementList("command.reload.success", Notification.class, NotificationAdapter.INSTANCE)
            .defaultValue(List.of(new Notification(Channel.CHAT, "<prefix> <green>Reloaded the config and language files.", "")))
            .build();

    public static final ConfigKey<List<Notification>> COMMAND_RELOAD_FAILED = ConfigKey.elementList("command.reload.failed", Notification.class, NotificationAdapter.INSTANCE)
            .defaultValue(List.of(new Notification(Channel.CHAT, "<prefix> <red>Reload failed. See the console for details.", "")))
            .build();
}
