package dev.dash.dmentions.migration;

import dev.dash.dmentions.config.ConfigKeys;
import dev.dash.dmentions.mention.type.GroupSettings;
import dev.yoshi.xconfig.ConfigException;
import dev.yoshi.xconfig.ConfigKey;
import dev.yoshi.xconfig.WritableXConfig;
import dev.yoshi.xconfig.adapters.DurationAdapter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;

public final class LegacyConfigMigrator {

    static final Map<String, String> PLACEHOLDERS = Map.of(
            "p", "player",
            "display", "custom_name",
            "group", "group"
    );

    private static final String OTHER_GROUPS = "__OTHER__";

    private final YamlConfiguration old;
    private final WritableXConfig config;
    private final Logger logger;

    private LegacyConfigMigrator(YamlConfiguration old, WritableXConfig config, Logger logger) {
        this.old = old;
        this.config = config;
        this.logger = logger;
    }

    public static void migrate(YamlConfiguration old, WritableXConfig config, Logger logger) {
        new LegacyConfigMigrator(old, config, logger).migrate();
    }

    private void migrate() {
        copy("lang_file", ConfigKeys.LANG_FILE, this::string);
        copy("check_for_updates", ConfigKeys.CHECK_FOR_UPDATES, this::bool);
        copy("prefix", ConfigKeys.PREFIX, this::text);
        copy("mention_limit", ConfigKeys.MENTION_LIMIT, this::integer);
        copy("vanish_respect", ConfigKeys.VANISH_RESPECT, this::bool);
        copy("afk_respect", ConfigKeys.AFK_RESPECT, this::bool);
        copy("ignore_respect", ConfigKeys.IGNORE_RESPECT, this::bool);
        copy("disabled_worlds", ConfigKeys.DISABLED_WORLDS, this::stringList);

        copy("player.enabled", ConfigKeys.PLAYER_ENABLED, this::bool);
        copy("player.permission", ConfigKeys.PLAYER_PERMISSION, this::plain);
        copy("player.sound", ConfigKeys.PLAYER_SOUND, this::string);
        copy("player.display", ConfigKeys.PLAYER_DISPLAY, this::text);
        copy("player.customized_display", ConfigKeys.PLAYER_CUSTOM_DISPLAY, this::text);
        copy("player.cooldown", ConfigKeys.PLAYER_COOLDOWN, this::duration);

        copy("nearby.enabled", ConfigKeys.NEARBY_ENABLED, this::bool);
        copy("nearby.permission", ConfigKeys.NEARBY_PERMISSION, this::plain);
        copy("nearby.sound", ConfigKeys.NEARBY_SOUND, this::string);
        copy("nearby.keyword", ConfigKeys.NEARBY_KEYWORD, this::plain);
        copy("nearby.display", ConfigKeys.NEARBY_DISPLAY, this::text);
        copy("nearby.cooldown", ConfigKeys.NEARBY_COOLDOWN, this::duration);
        copy("nearby.radius", ConfigKeys.NEARBY_RADIUS, this::integer);

        copy("everyone.enabled", ConfigKeys.EVERYONE_ENABLED, this::bool);
        copy("everyone.permission", ConfigKeys.EVERYONE_PERMISSION, this::plain);
        copy("everyone.sound", ConfigKeys.EVERYONE_SOUND, this::string);
        copy("everyone.keyword", ConfigKeys.EVERYONE_KEYWORD, this::plain);
        copy("everyone.display", ConfigKeys.EVERYONE_DISPLAY, this::text);
        copy("everyone.cooldown", ConfigKeys.EVERYONE_COOLDOWN, this::duration);

        copy("group.enabled", ConfigKeys.GROUP_ENABLED, this::bool);
        copy("group.disabled_groups", ConfigKeys.GROUP_DISABLED_GROUPS, this::stringList);
        copy("group.keyword", ConfigKeys.GROUP_KEYWORD, this::plain);
        copy("group.permission", ConfigKeys.GROUP_PERMISSION, this::plain);
        migrateGroups();
    }

    // 1.8 kept the settings of unlisted groups in group.list.__OTHER__; 2.0 has group.display/sound/cooldown for that
    private void migrateGroups() {
        ConfigurationSection list = old.getConfigurationSection("group.list");
        if (list == null) return;

        Map<String, GroupSettings> overrides = new LinkedHashMap<>();
        for (String group : list.getKeys(false)) {
            String path = "group.list." + group;
            if (group.equals(OTHER_GROUPS)) {
                copy(path + ".display", ConfigKeys.GROUP_DISPLAY, this::text);
                copy(path + ".sound", ConfigKeys.GROUP_SOUND, this::string);
                copy(path + ".cooldown", ConfigKeys.GROUP_COOLDOWN, this::duration);
                continue;
            }
            overrides.put(group, new GroupSettings(
                    read(path + ".display", this::text),
                    read(path + ".sound", this::string),
                    read(path + ".cooldown", this::duration)));
        }
        set(ConfigKeys.GROUP_LIST, overrides, "group.list");
    }

    private <T> void copy(String oldPath, ConfigKey<T> key, Function<String, T> reader) {
        T value = read(oldPath, reader);
        if (value != null) {
            set(key, value, oldPath);
        }
    }

    private <T> T read(String oldPath, Function<String, T> reader) {
        if (!old.contains(oldPath) || old.get(oldPath) == null) return null;
        try {
            return reader.apply(oldPath);
        } catch (RuntimeException e) {
            logger.warning("Could not migrate '" + oldPath + "' from DMentions 1.8 (" + e.getMessage() + "); the default is used.");
            return null;
        }
    }

    private <T> void set(ConfigKey<T> key, T value, String oldPath) {
        try {
            config.set(key, value);
        } catch (ConfigException | IllegalArgumentException e) {
            logger.warning("Could not migrate '" + oldPath + "' from DMentions 1.8 (" + e.getMessage() + "); the default is used.");
        }
    }

    private String string(String path) {
        return String.valueOf(old.get(path));
    }

    private String text(String path) {
        return LegacyText.convert(string(path), PLACEHOLDERS);
    }

    // keywords and permissions are never shown, so only placeholders are renamed
    private String plain(String path) {
        return LegacyText.renamePlaceholders(string(path), PLACEHOLDERS);
    }

    private Boolean bool(String path) {
        Object value = old.get(path);
        if (value instanceof Boolean bool) return bool;
        if (value instanceof String text && (text.equalsIgnoreCase("true") || text.equalsIgnoreCase("false"))) {
            return Boolean.parseBoolean(text);
        }
        throw new IllegalArgumentException("not true or false: " + value);
    }

    private Integer integer(String path) {
        Object value = old.get(path);
        if (value instanceof Number number) return number.intValue();
        return Integer.parseInt(String.valueOf(value).trim());
    }

    // 1.8 cooldowns are plain seconds, which the 2.0 adapter reads as well
    private Duration duration(String path) {
        return DurationAdapter.INSTANCE.deserialize(String.valueOf(old.get(path)), path);
    }

    private List<String> stringList(String path) {
        if (!old.isList(path)) throw new IllegalArgumentException("not a list");
        return List.copyOf(old.getStringList(path));
    }
}
