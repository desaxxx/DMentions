package dev.dash.dmentions.storage;

import dev.dash.dmentions.user.UserData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class YamlUserRepositoryTest {

    private static final Logger SILENT = Logger.getLogger("YamlUserRepositoryTest");

    static {
        SILENT.setLevel(Level.OFF);
    }

    private static final UUID STEVE = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID ALEX = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @TempDir
    Path dir;

    private Path file() {
        return dir.resolve("data/users.yml");
    }

    private YamlUserRepository open() {
        YamlUserRepository repository = new YamlUserRepository(file(), SILENT);
        repository.init();
        return repository;
    }

    @Test
    void missingFileStartsEmptyAndIsCreatedByTheFirstSave() {
        YamlUserRepository repository = open();

        assertTrue(Files.isDirectory(file().getParent()));
        assertFalse(Files.exists(file()));
        assertEquals(Optional.empty(), repository.load(STEVE));

        repository.save(new UserData(STEVE, false, null));
        assertTrue(Files.isRegularFile(file()));
    }

    @Test
    void savedUserIsReadBackAfterReopening() {
        open().save(new UserData(STEVE, false, null));

        YamlUserRepository reopened = open();

        assertEquals(Optional.of(new UserData(STEVE, false, null)), reopened.load(STEVE));
        assertEquals(Optional.empty(), reopened.load(ALEX));
        assertFalse(Files.exists(file().resolveSibling("users.yml.tmp")), "temp file left behind");
    }

    @Test
    void userBackToDefaultsIsRemovedFromTheFile() throws IOException {
        YamlUserRepository repository = open();
        repository.save(new UserData(STEVE, false, null));
        repository.save(new UserData(ALEX, false, null));

        repository.save(UserData.defaults(STEVE));

        assertEquals(Optional.empty(), repository.load(STEVE));
        String content = Files.readString(file());
        assertFalse(content.contains(STEVE.toString()));
        assertTrue(content.contains(ALEX.toString()));
        assertEquals(Optional.empty(), open().load(STEVE));
    }

    @Test
    void saveAllStoresEveryUserAndSkipsDefaults() {
        open().saveAll(java.util.List.of(new UserData(STEVE, false, "Stevie"), UserData.defaults(ALEX)));

        YamlUserRepository reopened = open();
        assertEquals(Optional.of(new UserData(STEVE, false, "Stevie")), reopened.load(STEVE));
        assertEquals(Optional.empty(), reopened.load(ALEX));
    }

    @Test
    void defaultUserThatWasNeverStoredDoesNotCreateTheFile() {
        open().save(UserData.defaults(STEVE));

        assertFalse(Files.exists(file()));
    }

    @Test
    void brokenFileFailsInitAndIsLeftUntouched() throws IOException {
        Files.createDirectories(file().getParent());
        String broken = "users: [unclosed\n  : :";
        Files.writeString(file(), broken);

        YamlUserRepository repository = new YamlUserRepository(file(), SILENT);

        assertThrows(RuntimeException.class, repository::init);
        assertEquals(broken, Files.readString(file()));
    }

    @Test
    void invalidIdsAreSkippedAndTheRestIsLoaded() throws IOException {
        Files.createDirectories(file().getParent());
        Files.writeString(file(), """
                users:
                  not-a-uuid:
                    mentionable: false
                  %s:
                    mentionable: false
                """.formatted(STEVE));

        assertEquals(Optional.of(new UserData(STEVE, false, null)), open().load(STEVE));
    }

    @Test
    void fileWithoutUsersSectionIsEmpty() throws IOException {
        Files.createDirectories(file().getParent());
        Files.writeString(file(), "something_else: 1\n");

        assertEquals(Optional.empty(), open().load(STEVE));
    }

    @Test
    void customNameIsSavedAndRemovedAgain() throws IOException {
        open().save(new UserData(STEVE, true, "Stevo"));

        assertEquals(Optional.of(new UserData(STEVE, true, "Stevo")), open().load(STEVE));
        assertTrue(Files.readString(file()).contains("custom_name: Stevo"));

        open().save(UserData.defaults(STEVE));

        assertEquals(Optional.empty(), open().load(STEVE));
        assertFalse(Files.readString(file()).contains("Stevo"));
    }
}
