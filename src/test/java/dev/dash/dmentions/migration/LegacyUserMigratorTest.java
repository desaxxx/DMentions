package dev.dash.dmentions.migration;

import dev.dash.dmentions.storage.YamlUserRepository;
import dev.dash.dmentions.user.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LegacyUserMigratorTest {

    private static final UUID STEVE = UUID.fromString("0f6a2c55-1111-2222-3333-444455556666");
    private static final UUID ALEX = UUID.fromString("1a2b3c4d-1111-2222-3333-444455556666");
    private static final Function<UUID, String> NO_NAMES = id -> null;

    @TempDir
    Path backup;
    @TempDir
    Path storage;

    private final List<String> warnings = new ArrayList<>();
    private Logger logger;
    private YamlUserRepository repository;

    @BeforeEach
    void setUp() {
        logger = Logger.getAnonymousLogger();
        logger.setUseParentHandlers(false);
        logger.addHandler(new Handler() {
            @Override
            public void publish(LogRecord record) {
                warnings.add(record.getMessage());
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        });
        repository = new YamlUserRepository(storage.resolve("users.yml"), logger);
        repository.init();
    }

    private void playerFile(UUID id, boolean mentionMode, String display) throws IOException {
        Files.createDirectories(backup.resolve("players"));
        Files.writeString(backup.resolve("players/" + id + ".yml"), """
                "==":
                  uuid: %s
                  mention_mode: %s
                  customized_display_name: %s
                  module_data:
                    statistics:
                      mentioned: 10
                      mentionedBy: 4
                """.formatted(id, mentionMode, display == null ? "null" : "\"" + display + "\""));
    }

    private int migrate(Function<UUID, String> names) {
        return LegacyUserMigrator.migrate(backup, repository, names, logger);
    }

    @Test
    void playerFilesAreMigrated() throws IOException {
        playerFile(STEVE, false, "animeboi");
        playerFile(ALEX, true, "Lexi");

        assertEquals(2, migrate(NO_NAMES));

        assertEquals(Optional.of(new UserData(STEVE, false, "animeboi")), repository.load(STEVE));
        assertEquals(Optional.of(new UserData(ALEX, true, "Lexi")), repository.load(ALEX));
    }

    @Test
    void playersWithDefaultSettingsAreNotStored() throws IOException {
        playerFile(STEVE, true, null);

        assertEquals(0, migrate(NO_NAMES));
        assertEquals(Optional.empty(), repository.load(STEVE));
    }

    @Test
    void theUuidComesFromTheFileNameWhenTheFileHasNone() throws IOException {
        Files.createDirectories(backup.resolve("players"));
        Files.writeString(backup.resolve("players/" + STEVE + ".yml"), """
                "==":
                  mention_mode: false
                """);

        migrate(NO_NAMES);

        assertEquals(Optional.of(new UserData(STEVE, false, null)), repository.load(STEVE));
    }

    @Test
    void theSingleFileFromBefore1Point6IsMigrated() throws IOException {
        Files.writeString(backup.resolve("players.yml"), """
                players:
                  %s:
                    mentions: false
                    mention_display: "Stevie"
                """.formatted(STEVE));

        migrate(NO_NAMES);

        assertEquals(Optional.of(new UserData(STEVE, false, "Stevie")), repository.load(STEVE));
    }

    @Test
    void theSingleFileIsSkippedWhenMarkedMigrated() throws IOException {
        Files.writeString(backup.resolve("players.yml"), """
                migrated: true
                players:
                  %s:
                    mentions: false
                """.formatted(STEVE));

        assertEquals(0, migrate(NO_NAMES));
    }

    @Test
    void aPlayerFileWinsOverTheOlderSingleFile() throws IOException {
        Files.writeString(backup.resolve("players.yml"), """
                players:
                  %s:
                    mentions: false
                    mention_display: "Old"
                """.formatted(STEVE));
        playerFile(STEVE, true, "New");

        migrate(NO_NAMES);

        assertEquals(Optional.of(new UserData(STEVE, true, "New")), repository.load(STEVE));
    }

    @Test
    void anInvalidCustomNameIsDroppedAndReported() throws IOException {
        playerFile(STEVE, false, "&cKing Steve");

        migrate(NO_NAMES);

        assertEquals(Optional.of(new UserData(STEVE, false, null)), repository.load(STEVE));
        assertTrue(warnings.stream().anyMatch(w -> w.contains("&cKing Steve")), warnings.toString());
    }

    @Test
    void aCustomNameEqualToTheRealNameIsNoCustomName() throws IOException {
        playerFile(STEVE, true, "steve");

        assertEquals(0, migrate(Map.of(STEVE, "Steve")::get));
        assertEquals(Optional.empty(), repository.load(STEVE));
    }

    @Test
    void anInvalidUuidIsSkipped() throws IOException {
        Files.writeString(backup.resolve("players.yml"), """
                players:
                  not-a-uuid:
                    mentions: false
                """);

        assertEquals(0, migrate(NO_NAMES));
        assertTrue(warnings.stream().anyMatch(w -> w.contains("not-a-uuid")), warnings.toString());
    }
}
