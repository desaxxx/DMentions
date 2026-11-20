package dev.dash.dmentions.config;

import dev.yoshi.xconfig.ConfigKey;

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
}
