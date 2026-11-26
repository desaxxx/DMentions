package dev.dash.dmentions.migration;

import dev.dash.dmentions.config.LangConfigFactory;
import dev.dash.dmentions.config.LangKeys;
import dev.dash.dmentions.message.Notification;
import dev.dash.dmentions.message.NotificationAdapter;
import dev.yoshi.xconfig.ConfigException;
import dev.yoshi.xconfig.ConfigKey;
import dev.yoshi.xconfig.WritableXConfig;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Stream;

public final class LegacyLangMigrator {

    static final Map<String, String> PLACEHOLDERS = Map.of(
            "PREFIX", "prefix",
            "sender", "sender",
            "target", "target",
            "remained", "remaining",
            "REMAINED", "remaining", // the tr-TR file 1.8.4 shipped still used the old name
            "group", "group",
            "value", "value",
            "old_value", "old_value"
    );

    // {p} is the mentioned player in these messages and the sender everywhere else, as 1.8 itself converted it
    private static final Set<ConfigKey<List<Notification>>> P_IS_TARGET = Set.of(
            LangKeys.PLAYER_SENDER_MESSAGE,
            LangKeys.COMMAND_USER_CUSTOM_NAME_SUCCESS
    );

    private static Map<String, String> placeholdersFor(ConfigKey<List<Notification>> key) {
        Map<String, String> placeholders = new HashMap<>(PLACEHOLDERS);
        placeholders.put("p", P_IS_TARGET.contains(key) ? "target" : "sender");
        return placeholders;
    }

    // the first 1.8 path present wins
    static final Map<ConfigKey<List<Notification>>, List<String>> MAPPING = mapping();

    private static Map<ConfigKey<List<Notification>>, List<String>> mapping() {
        Map<ConfigKey<List<Notification>>, List<String>> map = new LinkedHashMap<>();
        map.put(LangKeys.COOLDOWN_WARN, List.of("cooldown_warn"));
        map.put(LangKeys.DISABLED_WORLD_WARN, List.of("disabled_world_warn"));
        map.put(LangKeys.PLAYER_SENDER_MESSAGE, List.of("player.sender_message"));
        map.put(LangKeys.PLAYER_TARGET_MESSAGE, List.of("player.target_message"));
        map.put(LangKeys.NEARBY_SENDER_MESSAGE, List.of("nearby.sender_message"));
        map.put(LangKeys.NEARBY_TARGET_MESSAGE, List.of("nearby.target_message"));
        map.put(LangKeys.EVERYONE_SENDER_MESSAGE, List.of("everyone.sender_message"));
        map.put(LangKeys.EVERYONE_TARGET_MESSAGE, List.of("everyone.target_message"));
        // 1.8 had messages per group; 2.0 has one set for all groups
        map.put(LangKeys.GROUP_SENDER_MESSAGE, List.of("group.default.sender_message", "group.__OTHER__.sender_message"));
        map.put(LangKeys.GROUP_TARGET_MESSAGE, List.of("group.default.target_message", "group.__OTHER__.target_message"));
        map.put(LangKeys.COMMAND_MUST_BE_PLAYER, List.of("command.must_be_player"));
        map.put(LangKeys.COMMAND_PLAYER_NOT_FOUND, List.of("command.player_not_found"));
        map.put(LangKeys.COMMAND_TOGGLE_ENABLED, List.of("command.toggle.will_now_mentioned"));
        map.put(LangKeys.COMMAND_TOGGLE_DISABLED, List.of("command.toggle.no_longer_mentioned"));
        map.put(LangKeys.COMMAND_SEND_INVALID_KEYWORD, List.of("command.send.invalid_keyword"));
        map.put(LangKeys.COMMAND_RELOAD_SUCCESS, List.of("command.reload.success"));
        map.put(LangKeys.COMMAND_CUSTOMIZE_INVALID_CUSTOM_NAME, List.of("command.customize.invalid_display"));
        map.put(LangKeys.COMMAND_CUSTOMIZE_SUCCESS, List.of("command.customize.success"));
        map.put(LangKeys.COMMAND_USER_CUSTOM_NAME_SUCCESS, List.of("command.user.display.success"));
        return map;
    }

    private LegacyLangMigrator() {
    }

    public static void migrate(Path backupLang, Path langFolder, Runnable saveBundled, Logger logger) {
        List<Path> oldFiles;
        try (Stream<Path> stream = Files.list(backupLang)) {
            oldFiles = stream.filter(path -> path.getFileName().toString().endsWith(".yml")).sorted().toList();
        } catch (IOException e) {
            throw new UncheckedIOException("Could not list " + backupLang + ".", e);
        }

        // the 1.8 files would be read as 2.0 ones otherwise; the shipped ones come back from the jar
        try {
            Files.createDirectories(langFolder);
            for (Path oldFile : oldFiles) {
                Files.deleteIfExists(langFolder.resolve(oldFile.getFileName().toString()));
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Could not replace the 1.8 language files.", e);
        }
        saveBundled.run();

        for (Path oldFile : oldFiles) {
            migrateFile(oldFile, langFolder.resolve(oldFile.getFileName().toString()), logger);
        }
    }

    static void migrateFile(Path oldFile, Path newFile, Logger logger) {
        YamlConfiguration old = YamlConfiguration.loadConfiguration(oldFile.toFile());
        // creates the file with every 2.0 message if it is not a shipped language
        WritableXConfig lang = LangConfigFactory.create(newFile).logger(logger).buildWritable();

        for (Map.Entry<ConfigKey<List<Notification>>, List<String>> entry : MAPPING.entrySet()) {
            String oldPath = entry.getValue().stream().filter(old::contains).findFirst().orElse(null);
            if (oldPath == null) continue;
            try {
                lang.set(entry.getKey(), toNotifications(old.get(oldPath), oldPath, placeholdersFor(entry.getKey())));
            } catch (ConfigException | IllegalArgumentException e) {
                logger.warning("Could not migrate '" + oldPath + "' of " + oldFile.getFileName()
                        + " from DMentions 1.8 (" + e.getMessage() + "); the 2.0 text is used.");
            }
        }
    }

    static List<Notification> toNotifications(Object raw, String path, Map<String, String> placeholders) {
        List<?> lines = raw instanceof List<?> list ? list : List.of(raw);
        List<Notification> notifications = new ArrayList<>();
        for (Object line : lines) {
            if (line == null) continue;
            Notification parsed;
            try {
                parsed = NotificationAdapter.INSTANCE.deserialize(line.toString(), path);
            } catch (ConfigException e) {
                continue; // blank
            }
            notifications.add(new Notification(parsed.channel(),
                    LegacyText.convert(parsed.text(), placeholders),
                    LegacyText.convert(parsed.subtitle(), placeholders)));
        }
        return notifications;
    }
}
