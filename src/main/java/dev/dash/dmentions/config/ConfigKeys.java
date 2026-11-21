package dev.dash.dmentions.config;

import dev.yoshi.xconfig.ConfigKey;
import dev.yoshi.xconfig.TypeToken;

import java.time.Duration;
import java.util.List;

public final class ConfigKeys {

    public static final ConfigKey<String> LANG_FILE = ConfigKey.builder("lang_file", String.class)
            .comment("This file should exist within lang directory.")
            .defaultValue("en-US")
            .build();

    public static final ConfigKey<String> PREFIX = ConfigKey.builder("prefix", String.class)
            .defaultValue("<dark_gray>[<#ffd16d>DMentions<dark_gray>]<reset>")
            .build();

    public static final ConfigKey<Integer> MENTION_LIMIT = ConfigKey.builder("mention_limit", Integer.class)
            .comment("Maximum number of mentions per message.")
            .defaultValue(2)
            .validate(limit -> limit >= 0, "Mention limit must be 0 or greater")
            .build();

    public static final ConfigKey<Boolean> CASE_SENSITIVE = ConfigKey.builder("case_sensitive", Boolean.class)
            .comment("Whether mentions are case sensitive.")
            .defaultValue(false)
            .build();

    public static final ConfigKey<List<String>> DISABLED_WORLDS = ConfigKey.builder("disabled_worlds", new TypeToken<List<String>>() {})
            .comment("Worlds that players cannot mention anybody.")
            .defaultValue(List.of("no_mention_world_1","no_mention_world_2"))
            .build();

    public static final ConfigKey<Boolean> PLAYER_ENABLED = ConfigKey.builder("player.enabled", Boolean.class)
            .defaultValue(true)
            .build();

    public static final ConfigKey<String> PLAYER_PERMISSION = ConfigKey.builder("player.permission", String.class)
            .defaultValue("dmentions.mention.player")
            .build();

    public static final ConfigKey<String> PLAYER_KEYWORD = ConfigKey.builder("player.keyword", String.class)
            .defaultValue("<player>")
            .validate(keyword -> keyword.contains("<player>"), "Player keyword must contain <player>")
            .build();

    public static final ConfigKey<String> PLAYER_DISPLAY = ConfigKey.builder("player.display", String.class)
            .defaultValue("<#a9e871>@<player>")
            .build();

    public static final ConfigKey<String> PLAYER_SOUND = ConfigKey.builder("player.sound", String.class)
            .defaultValue("minecraft:block.note_block.pling;1.0;1.0")
            .build();

    public static final ConfigKey<Duration> PLAYER_COOLDOWN = ConfigKey.builder("player.cooldown", Duration.class)
            .defaultValue(Duration.ofSeconds(5))
            .build();

    public static final ConfigKey<Boolean> EVERYONE_ENABLED = ConfigKey.builder("everyone.enabled", Boolean.class)
            .defaultValue(true)
            .build();

    public static final ConfigKey<String> EVERYONE_PERMISSION = ConfigKey.builder("everyone.permission", String.class)
            .defaultValue("dmentions.mention.everyone")
            .build();

    public static final ConfigKey<String> EVERYONE_KEYWORD = ConfigKey.builder("everyone.keyword", String.class)
            .defaultValue("@everyone")
            .validate(k -> !k.isBlank(), "Everyone keyword cannot be blank")
            .build();

    public static final ConfigKey<String> EVERYONE_DISPLAY = ConfigKey.builder("everyone.display", String.class)
            .defaultValue("<#8fb56c>@everyone")
            .build();

    public static final ConfigKey<String> EVERYONE_SOUND = ConfigKey.builder("everyone.sound", String.class)
            .defaultValue("minecraft:entity.wither.spawn;1.0;1.0")
            .build();

    public static final ConfigKey<Duration> EVERYONE_COOLDOWN = ConfigKey.builder("everyone.cooldown", Duration.class)
            .defaultValue(Duration.ofSeconds(10))
            .build();
}
