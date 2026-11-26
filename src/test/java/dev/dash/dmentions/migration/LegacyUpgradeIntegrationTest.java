package dev.dash.dmentions.migration;

import dev.dash.dmentions.DMentions;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LegacyUpgradeIntegrationTest {

    private static final String STEVE = "0f6a2c55-1111-2222-3333-444455556666";

    private DMentions plugin;
    private Path data;

    @BeforeEach
    void setUp() {
        MockBukkit.mock();
        plugin = MockBukkit.load(DMentions.class);
        data = plugin.getDataPath();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private void restart(IoAction change) throws IOException {
        Bukkit.getPluginManager().disablePlugin(plugin);
        change.run();
        plugin = MockBukkit.load(DMentions.class);
    }

    private interface IoAction {
        void run() throws IOException;
    }

    private void write184Folder() throws IOException {
        try (Stream<Path> paths = Files.walk(data)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                if (!path.equals(data)) Files.delete(path);
            }
        }
        for (String name : List.of("config.yml", "gui.yml", "lang/en-US.yml", "lang/tr-TR.yml")) {
            Files.createDirectories(data.resolve(name).getParent());
            try (InputStream in = getClass().getResourceAsStream("/legacy-1.8.4/" + name)) {
                Files.copy(in, data.resolve(name));
            }
        }
        Files.createDirectories(data.resolve("players"));
        Files.writeString(data.resolve("players/" + STEVE + ".yml"), """
                "==":
                  uuid: %s
                  mention_mode: false
                  customized_display_name: "animeboi"
                """.formatted(STEVE));
        Files.createDirectories(data.resolve("backups"));
        Files.writeString(data.resolve("backups/config_2026-03-13_12-09-41.yml"), "config_version: 1.8.3\n");
    }

    private Path backup() {
        return data.resolve(LegacyUpgrade.BACKUP_FOLDER);
    }

    private YamlConfiguration config() {
        return YamlConfiguration.loadConfiguration(data.resolve("config.yml").toFile());
    }

    @Test
    void aFolderFrom1Point8IsMigratedBackedUpAndCleaned() throws IOException {
        restart(this::write184Folder);

        assertTrue(plugin.isEnabled());
        for (String name : List.of("config.yml", "gui.yml", "lang/en-US.yml", "lang/tr-TR.yml", "players/" + STEVE + ".yml",
                "backups/config_2026-03-13_12-09-41.yml")) {
            assertTrue(Files.exists(backup().resolve(name)), "backed up: " + name);
        }
        assertFalse(Files.exists(backup().resolve(LegacyUpgrade.PENDING_MARKER)), "the migration finished");
        assertFalse(Files.exists(data.resolve("players")));
        assertFalse(Files.exists(data.resolve("gui.yml")));
        assertFalse(Files.exists(data.resolve("backups")));

        YamlConfiguration config = config();
        assertFalse(config.contains(LegacyUpgrade.LEGACY_VERSION_KEY));
        assertEquals("<#A9E871>@<player>", config.getString("player.display"));

        YamlConfiguration users = YamlConfiguration.loadConfiguration(data.resolve("data/users.yml").toFile());
        assertFalse(users.getBoolean("users." + STEVE + ".mentionable", true));
        assertEquals("animeboi", users.getString("users." + STEVE + ".custom_name"));

        String lang = Files.readString(data.resolve("lang/en-US.yml"));
        assertTrue(lang.contains("<remaining>") && !lang.contains("{PREFIX}"), lang);
    }

    @Test
    void aStartAfterTheMigrationChangesNothing() throws IOException {
        restart(this::write184Folder);
        restart(() -> Files.writeString(data.resolve("config.yml"),
                Files.readString(data.resolve("config.yml")).replace("mention_limit: 2", "mention_limit: 5")));

        assertEquals(5, config().getInt("mention_limit"), "the admin's later change is kept");
        try (Stream<Path> files = Files.list(data)) {
            assertEquals(1, files.filter(path -> path.getFileName().toString().startsWith(LegacyUpgrade.BACKUP_FOLDER)).count());
        }
    }

    @Test
    void anUnfinishedMigrationRunsAgainFromTheBackup() throws IOException {
        restart(this::write184Folder);
        // as if the server stopped before the player data was saved
        restart(() -> {
            Files.createFile(backup().resolve(LegacyUpgrade.PENDING_MARKER));
            Files.delete(data.resolve("data/users.yml"));
        });

        assertFalse(Files.exists(backup().resolve(LegacyUpgrade.PENDING_MARKER)));
        YamlConfiguration users = YamlConfiguration.loadConfiguration(data.resolve("data/users.yml").toFile());
        assertEquals("animeboi", users.getString("users." + STEVE + ".custom_name"));
        assertEquals("<#A9E871>@<player>", config().getString("player.display"));
    }

    @Test
    void a2Point0FolderIsLeftAlone() throws IOException {
        restart(() -> { });

        assertFalse(Files.exists(backup()));
    }
}
