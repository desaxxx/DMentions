package dev.dash.dmentions.config;

import dev.dash.dmentions.mention.type.GroupSettings;
import dev.dash.dmentions.storage.StorageType;
import dev.yoshi.xconfig.ConfigKey;
import dev.yoshi.xconfig.TypeToken;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static dev.yoshi.xconfig.ConfigKey.builder;
import static dev.yoshi.xconfig.ConfigKey.elementMap;

public final class ConfigKeys {

    public static final ConfigKey<String> LANG_FILE = builder("lang_file", String.class)
            .comment("This file should exist within lang directory.")
            .defaultValue("en-US")
            .build();

    public static final ConfigKey<String> PREFIX = builder("prefix", String.class)
            .defaultValue("<dark_gray>[<#ffd16d>DMentions<dark_gray>]<reset>")
            .build();

    public static final ConfigKey<Integer> MENTION_LIMIT = builder("mention_limit", Integer.class)
            .comment("Maximum number of mentions per message.")
            .defaultValue(2)
            .validate(limit -> limit >= 0, "Mention limit must be 0 or greater")
            .build();

    public static final ConfigKey<Boolean> CASE_SENSITIVE = builder("case_sensitive", Boolean.class)
            .comment("Whether mentions are case sensitive.")
            .defaultValue(false)
            .build();

    public static final ConfigKey<Boolean> ALLOW_MULTIPLE_NOTIFICATIONS = builder("allow_multiple_notifications", Boolean.class)
            .comment(
                    "If true, a player mentioned more than once in the same message",
                    "(e.g. \"@everyone Steve\") gets a notification for each mention.",
                    "If false, only the first one.")
            .defaultValue(false)
            .build();

    public static final ConfigKey<List<String>> DISABLED_WORLDS = builder("disabled_worlds", new TypeToken<List<String>>() {})
            .comment("Worlds that players cannot mention anybody.")
            .defaultValue(List.of("no_mention_world_1","no_mention_world_2"))
            .build();

    public static final ConfigKey<StorageType> STORAGE_TYPE = builder("storage.type", StorageType.class)
            .comment("The type of storage to store user data.")
            .defaultValue(StorageType.YAML)
            .build();

    public static final ConfigKey<String> STORAGE_HOST = builder("storage.host", String.class)
            .defaultValue("localhost")
            .build();

    public static final ConfigKey<Integer> STORAGE_PORT = builder("storage.port", Integer.class)
            .defaultValue(3306)
            .build();

    public static final ConfigKey<String> STORAGE_NAME = builder("storage.name", String.class)
            .defaultValue("dmentions")
            .build();

    public static final ConfigKey<String> STORAGE_USERNAME = builder("storage.username", String.class)
            .defaultValue("root")
            .build();

    public static final ConfigKey<String> STORAGE_PASSWORD = builder("storage.password", String.class)
            .defaultValue("password")
            .build();

    public static final ConfigKey<Boolean> STORAGE_USE_SSL = builder("storage.use_ssl", Boolean.class)
            .defaultValue(false)
            .build();

    public static final ConfigKey<String> STORAGE_PREFIX = builder("storage.prefix", String.class)
            .comment("The prefix of the database table.")
            .defaultValue("dmentions_")
            .build();

    public static final ConfigKey<Boolean> PLAYER_ENABLED = builder("player.enabled", Boolean.class)
            .defaultValue(true)
            .build();

    public static final ConfigKey<String> PLAYER_PERMISSION = builder("player.permission", String.class)
            .defaultValue("dmentions.mention.player")
            .build();

    public static final ConfigKey<String> PLAYER_KEYWORD = builder("player.keyword", String.class)
            .defaultValue("<player>")
            .validate(keyword -> keyword.contains("<player>"), "Player keyword must contain <player>")
            .build();

    public static final ConfigKey<String> PLAYER_DISPLAY = builder("player.display", String.class)
            .defaultValue("<#a9e871>@<player>")
            .build();

    public static final ConfigKey<String> PLAYER_SOUND = builder("player.sound", String.class)
            .defaultValue("minecraft:block.note_block.pling;1.0;1.0")
            .build();

    public static final ConfigKey<Duration> PLAYER_COOLDOWN = builder("player.cooldown", Duration.class)
            .defaultValue(Duration.ofSeconds(5))
            .build();

    public static final ConfigKey<Boolean> EVERYONE_ENABLED = builder("everyone.enabled", Boolean.class)
            .defaultValue(true)
            .build();

    public static final ConfigKey<String> EVERYONE_PERMISSION = builder("everyone.permission", String.class)
            .defaultValue("dmentions.mention.everyone")
            .build();

    public static final ConfigKey<String> EVERYONE_KEYWORD = builder("everyone.keyword", String.class)
            .defaultValue("@everyone")
            .validate(k -> !k.isBlank(), "Everyone keyword cannot be blank")
            .build();

    public static final ConfigKey<String> EVERYONE_DISPLAY = builder("everyone.display", String.class)
            .defaultValue("<#8fb56c>@everyone")
            .build();

    public static final ConfigKey<String> EVERYONE_SOUND = builder("everyone.sound", String.class)
            .defaultValue("minecraft:entity.wither.spawn;1.0;1.0")
            .build();

    public static final ConfigKey<Duration> EVERYONE_COOLDOWN = builder("everyone.cooldown", Duration.class)
            .defaultValue(Duration.ofSeconds(10))
            .build();

    public static final ConfigKey<Boolean> NEARBY_ENABLED = builder("nearby.enabled", Boolean.class)
            .defaultValue(true)
            .build();

    public static final ConfigKey<String> NEARBY_PERMISSION = builder("nearby.permission", String.class)
            .defaultValue("dmentions.mention.nearby")
            .build();

    public static final ConfigKey<String> NEARBY_KEYWORD = builder("nearby.keyword", String.class)
            .defaultValue("@nearby")
            .validate(k -> !k.isBlank(), "Nearby keyword cannot be blank")
            .build();

    public static final ConfigKey<String> NEARBY_DISPLAY = builder("nearby.display", String.class)
            .defaultValue("<#ea79b8>@nearby")
            .build();

    public static final ConfigKey<String> NEARBY_SOUND = builder("nearby.sound", String.class)
            .defaultValue("minecraft:entity.player.levelup;1.0;1.0")
            .build();

    public static final ConfigKey<Duration> NEARBY_COOLDOWN = builder("nearby.cooldown", Duration.class)
            .defaultValue(Duration.ofSeconds(10))
            .build();

    public static final ConfigKey<Integer> NEARBY_RADIUS = builder("nearby.radius", Integer.class)
            .comment("Checked as a box: each axis separately.")
            .defaultValue(20)
            .validate(r -> r > 0, "Nearby radius must be greater than 0")
            .build();

    public static final ConfigKey<Boolean> GROUP_ENABLED = builder("group.enabled", Boolean.class)
            .defaultValue(true)
            .build();

    public static final ConfigKey<String> GROUP_PERMISSION = builder("group.permission", String.class)
            .defaultValue("dmentions.mention.group.<group>")
            .build();

    public static final ConfigKey<String> GROUP_KEYWORD = builder("group.keyword", String.class)
            .defaultValue("@<group>")
            .validate(k -> k.contains("<group>"), "Group keyword must contain <group>")
            .build();

    public static final ConfigKey<String> GROUP_DISPLAY = builder("group.display", String.class)
            .defaultValue("<#73c7dc>@<group>")
            .build();

    public static final ConfigKey<String> GROUP_SOUND = builder("group.sound", String.class)
            .defaultValue("minecraft:item.totem.use;1.0;1.0")
            .build();

    public static final ConfigKey<Duration> GROUP_COOLDOWN = builder("group.cooldown", Duration.class)
            .defaultValue(Duration.ofSeconds(5))
            .build();

    public static final ConfigKey<List<String>> GROUP_DISABLED_GROUPS = builder("group.disabled_groups", new TypeToken<List<String>>() {})
            .defaultValue(List.of("admin","owner"))
            .build();

    public static final ConfigKey<Map<String, GroupSettings>> GROUP_LIST = elementMap("group.list", GroupSettings.class, GroupSettings.ADAPTER)
            .defaultValue(groupListDefaults())
            .build();

    private static Map<String, GroupSettings> groupListDefaults() {
        Map<String, GroupSettings> defaults = new LinkedHashMap<>();
        defaults.put("default", new GroupSettings(
                "<#c0dee6>@<group>",
                "minecraft:item.totem.use;1.0;1.0",
                Duration.ofSeconds(5)
        ));
        return defaults;
    }
}
