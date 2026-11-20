package dev.dash.dmentions.config;

import dev.dash.dmentions.mention.SoundSpec;
import dev.yoshi.xconfig.ConfigKey;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;

import java.time.Duration;

public final class ConfigKeys {

    public static final ConfigKey<Integer> MENTION_LIMIT = ConfigKey.builder("mention_limit", Integer.class)
            .defaultValue(2)
            .validate(limit -> limit >= 0, "Mention limit must be 0 or greater")
            .build();

    public static final ConfigKey<Boolean> CASE_SENSITIVE = ConfigKey.builder("case_sensitive", Boolean.class)
            .defaultValue(false)
            .build();

    public static final ConfigKey<Boolean> PLAYER_ENABLED = ConfigKey.builder("player.enabled", Boolean.class)
            .defaultValue(true)
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

    public static final ConfigKey<String> PLAYER_SENDER_MESSAGE = ConfigKey.builder("player.sender_message", String.class)
            .defaultValue("<#f4e4c0>You mentioned <#ddf7a6><target>")
            .build();

    public static final ConfigKey<String> PLAYER_TARGET_MESSAGE = ConfigKey.builder("player.target_message", String.class)
            .defaultValue("<#ddf7a6><sender> <#f4e4c0>mentioned you")
            .build();

    public static final ConfigKey<String> PLAYER_COOLDOWN_WARN = ConfigKey.builder("player.cooldown_warn", String.class)
            .defaultValue("<red>Wait <remaining> before mentioning again")
            .build();

}
