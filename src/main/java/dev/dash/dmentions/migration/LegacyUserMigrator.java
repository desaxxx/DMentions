package dev.dash.dmentions.migration;

import dev.dash.dmentions.storage.UserRepository;
import dev.dash.dmentions.user.UserData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class LegacyUserMigrator {

    // names saved before 1.6.0 were never checked and may break this rule
    private static final Pattern NAME = Pattern.compile("^[a-zA-Z0-9_]{3,16}$");

    private LegacyUserMigrator() {
    }

    public static int migrate(Path backup, UserRepository repository, Function<UUID, String> knownNames, Logger logger) {
        Map<UUID, UserData> users = new LinkedHashMap<>();
        // the single file is older; a player in both keeps the newer per-player file
        users.putAll(readLegacyPlayersFile(backup.resolve("players.yml"), knownNames, logger));
        users.putAll(readPlayerFiles(backup.resolve("players"), knownNames, logger));

        List<UserData> changed = users.values().stream().filter(user -> !user.isDefault()).toList();
        repository.saveAll(changed);
        return changed.size();
    }

    // since 1.6: one file per player, everything under the "==" key
    static Map<UUID, UserData> readPlayerFiles(Path folder, Function<UUID, String> knownNames, Logger logger) {
        Map<UUID, UserData> users = new LinkedHashMap<>();
        if (!Files.isDirectory(folder)) return users;

        List<Path> files;
        try (Stream<Path> stream = Files.list(folder)) {
            files = stream.filter(path -> path.getFileName().toString().endsWith(".yml")).sorted().toList();
        } catch (IOException e) {
            throw new UncheckedIOException("Could not list " + folder + ".", e);
        }

        for (Path file : files) {
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file.toFile());
            ConfigurationSection section = yaml.getConfigurationSection("==");
            if (section == null) continue;

            String fileName = file.getFileName().toString();
            UUID id = parseId(section.getString("uuid", fileName.substring(0, fileName.length() - ".yml".length())));
            if (id == null) {
                logger.warning("Skipped the 1.8 player file " + fileName + ": no valid uuid.");
                continue;
            }
            boolean mentionable = section.getBoolean("mention_mode", true);
            String name = normalizeName(id, section.getString("customized_display_name"), knownNames, logger);
            users.put(id, new UserData(id, mentionable, name));
        }
        return users;
    }

    // before 1.6: one players.yml for everyone
    static Map<UUID, UserData> readLegacyPlayersFile(Path file, Function<UUID, String> knownNames, Logger logger) {
        Map<UUID, UserData> users = new LinkedHashMap<>();
        if (!Files.isRegularFile(file)) return users;

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file.toFile());
        // 1.6+ marks it when it could not delete the file after moving it to players/
        if (yaml.getBoolean("migrated")) return users;

        ConfigurationSection players = yaml.getConfigurationSection("players");
        if (players == null) return users;

        for (String key : players.getKeys(false)) {
            UUID id = parseId(key);
            if (id == null) {
                logger.warning("Skipped '" + key + "' in the 1.8 players.yml: not a valid uuid.");
                continue;
            }
            boolean mentionable = players.getBoolean(key + ".mentions", true);
            String name = normalizeName(id, players.getString(key + ".mention_display"), knownNames, logger);
            users.put(id, new UserData(id, mentionable, name));
        }
        return users;
    }

    static String normalizeName(UUID id, String name, Function<UUID, String> knownNames, Logger logger) {
        if (name == null || name.isBlank()) return null;
        if (!NAME.matcher(name).matches()) {
            logger.warning("Custom name '" + name + "' of " + id + " is not a valid name (3-16 letters, digits or _); not migrated.");
            return null;
        }
        String realName = knownNames.apply(id);
        // 2.0 treats a custom name equal to the player's own name as no custom name
        return name.equalsIgnoreCase(realName) ? null : name;
    }

    private static UUID parseId(String text) {
        try {
            return UUID.fromString(text);
        } catch (IllegalArgumentException | NullPointerException e) {
            return null;
        }
    }
}
