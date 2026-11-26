package dev.dash.dmentions.migration;

import dev.dash.dmentions.storage.UserRepository;
import dev.dash.dmentions.storage.YamlUserRepository;
import dev.dash.dmentions.user.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LegacyUpgradeTest {

    private static final Logger SILENT = Logger.getLogger("LegacyUpgradeTest");
    private static final UUID STEVE = UUID.fromString("0f6a2c55-1111-2222-3333-444455556666");

    static {
        SILENT.setLevel(Level.OFF);
    }

    @TempDir
    Path data;

    @BeforeEach
    void write184Folder() throws IOException {
        Files.writeString(data.resolve("config.yml"), "config_version: \"1.8.4\"\n");
        Files.writeString(data.resolve("gui.yml"), "x: 1\n");
        Files.createDirectories(data.resolve("players"));
        Files.writeString(data.resolve("players/" + STEVE + ".yml"), """
                "==":
                  uuid: %s
                  mention_mode: false
                """.formatted(STEVE));
    }

    private Path marker() {
        return data.resolve(LegacyUpgrade.BACKUP_FOLDER).resolve(LegacyUpgrade.PENDING_MARKER);
    }

    private static final UserRepository BROKEN = new UserRepository() {
        @Override
        public void init() {
        }

        @Override
        public Optional<UserData> load(UUID userId) {
            return Optional.empty();
        }

        @Override
        public void save(UserData userData) {
        }

        @Override
        public void saveAll(Collection<UserData> users) {
            throw new IllegalStateException("database is down");
        }

        @Override
        public void close() {
        }
    };

    @Test
    void aFailedStepKeepsTheMarkerAndTheOldFilesAndTheNextStartFinishes() throws IOException {
        LegacyUpgrade first = LegacyUpgrade.detect(data, SILENT).orElseThrow();
        first.prepare();
        first.migrateUsers(BROKEN, id -> null);
        first.finish();

        assertTrue(Files.exists(marker()), "the migration is not done");
        assertTrue(Files.exists(data.resolve("players/" + STEVE + ".yml")), "nothing is removed while a step failed");
        assertTrue(Files.exists(data.resolve("gui.yml")));

        // config.yml is already gone; the marker alone brings the migration back
        LegacyUpgrade second = LegacyUpgrade.detect(data, SILENT).orElseThrow();
        second.prepare();
        YamlUserRepository repository = new YamlUserRepository(data.resolve("data/users.yml"), SILENT);
        repository.init();
        second.migrateUsers(repository, id -> null);
        second.finish();

        assertFalse(Files.exists(marker()));
        assertFalse(Files.exists(data.resolve("players")));
        assertFalse(Files.exists(data.resolve("gui.yml")));
        assertEquals(Optional.of(new UserData(STEVE, false, null)), repository.load(STEVE));
    }
}
