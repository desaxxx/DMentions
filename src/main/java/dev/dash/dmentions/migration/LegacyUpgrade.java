package dev.dash.dmentions.migration;

import dev.dash.dmentions.storage.UserRepository;
import dev.yoshi.xconfig.WritableXConfig;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

public final class LegacyUpgrade {

    public static final String BACKUP_FOLDER = "backup-1.8.4";
    // stays in the backup until every step succeeded, so a migration cut short runs again on the next start
    static final String PENDING_MARKER = ".pending";
    // only a 1.8 config.yml has this key; 2.0 (and 2.0-beta) files never do
    static final String LEGACY_VERSION_KEY = "config_version";

    // backups: the copies 1.8 itself made before updating its files
    private static final List<String> LEGACY_FILES = List.of("config.yml", "lang", "players", "players.yml", "gui.yml", "backups");
    private static final List<String> LEFTOVERS = List.of("players", "players.yml", "gui.yml", "backups");

    private final Path dataFolder;
    private final Path backup;
    private final Logger logger;
    private boolean failed;

    private LegacyUpgrade(Path dataFolder, Logger logger) {
        this.dataFolder = dataFolder;
        this.backup = dataFolder.resolve(BACKUP_FOLDER);
        this.logger = logger;
    }

    public static Optional<LegacyUpgrade> detect(Path dataFolder, Logger logger) {
        boolean pending = Files.exists(dataFolder.resolve(BACKUP_FOLDER).resolve(PENDING_MARKER));
        if (!pending && !isLegacyConfig(dataFolder.resolve("config.yml"))) {
            return Optional.empty();
        }
        return Optional.of(new LegacyUpgrade(dataFolder, logger));
    }

    private static boolean isLegacyConfig(Path config) {
        if (!Files.isRegularFile(config)) return false;
        YamlConfiguration yaml = new YamlConfiguration();
        try {
            yaml.loadFromString(Files.readString(config));
        } catch (Exception e) {
            return false; // a broken file is reported by the normal config loading
        }
        return yaml.contains(LEGACY_VERSION_KEY);
    }

    // every step reads the old files from the backup, never from the live folder, so running them twice is safe
    public void prepare() throws IOException {
        Path marker = backup.resolve(PENDING_MARKER);
        if (Files.exists(marker)) {
            logger.warning("A migration from DMentions 1.8 was not finished; running it again from " + BACKUP_FOLDER + ".");
        } else {
            logger.info("Found a DMentions 1.8 setup; migrating it to 2.0. The old files are kept in " + BACKUP_FOLDER + ".");
            if (Files.exists(backup)) {
                // left by an earlier, finished migration: keep it next to the new one
                Files.move(backup, dataFolder.resolve(BACKUP_FOLDER + "-" + System.currentTimeMillis()));
            }
            Files.createDirectories(backup);
            for (String name : LEGACY_FILES) {
                Path source = dataFolder.resolve(name);
                if (Files.exists(source)) {
                    copyRecursively(source, backup.resolve(name));
                }
            }
            Files.createFile(marker);
        }
        // xconfig then creates a fresh 2.0 file with its own comments, and the old values are set on it
        Files.deleteIfExists(dataFolder.resolve("config.yml"));
    }

    public void migrateConfig(WritableXConfig config) {
        step("config.yml", () -> {
            Path oldConfig = backup.resolve("config.yml");
            if (!Files.exists(oldConfig)) return;
            LegacyConfigMigrator.migrate(YamlConfiguration.loadConfiguration(oldConfig.toFile()), config, logger);
        });
    }

    public void migrateLang(Path langFolder, Runnable saveBundled) {
        step("language files", () -> {
            Path oldLang = backup.resolve("lang");
            if (!Files.isDirectory(oldLang)) return;
            LegacyLangMigrator.migrate(oldLang, langFolder, saveBundled, logger);
        });
    }

    public void migrateUsers(UserRepository repository, Function<UUID, String> knownNames) {
        step("player data", () -> {
            int count = LegacyUserMigrator.migrate(backup, repository, knownNames, logger);
            logger.info("Migrated the settings of " + count + " player(s) from DMentions 1.8.");
        });
    }

    public void finish() {
        if (failed) {
            logger.warning("The migration from DMentions 1.8 did not finish; it runs again on the next start.");
            return;
        }
        try {
            for (String name : LEFTOVERS) {
                deleteRecursively(dataFolder.resolve(name));
            }
            Files.deleteIfExists(backup.resolve(PENDING_MARKER));
            logger.info("Migration from DMentions 1.8 finished. The old files are in " + BACKUP_FOLDER + ".");
        } catch (IOException e) {
            logger.log(Level.WARNING, "Migrated from DMentions 1.8, but could not remove the old files.", e);
        }
    }

    private void step(String name, Runnable action) {
        try {
            action.run();
        } catch (RuntimeException e) {
            failed = true;
            logger.log(Level.SEVERE, "Could not migrate the " + name + " from DMentions 1.8.", e);
        }
    }

    private static void copyRecursively(Path source, Path target) throws IOException {
        if (!Files.isDirectory(source)) {
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            return;
        }
        try (Stream<Path> paths = Files.walk(source)) {
            for (Path path : (Iterable<Path>) paths::iterator) {
                Path destination = target.resolve(source.relativize(path).toString());
                if (Files.isDirectory(path)) {
                    Files.createDirectories(destination);
                } else {
                    Files.copy(path, destination, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    private static void deleteRecursively(Path path) throws IOException {
        if (!Files.exists(path)) return;
        try (Stream<Path> paths = Files.walk(path)) {
            for (Path each : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.delete(each);
            }
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
    }
}
