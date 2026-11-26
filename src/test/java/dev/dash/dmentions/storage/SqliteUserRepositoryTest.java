package dev.dash.dmentions.storage;

import dev.dash.dmentions.user.UserData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SqliteUserRepositoryTest {

    private static final UUID STEVE = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID ALEX = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @TempDir
    Path dir;

    private final List<SqliteUserRepository> opened = new ArrayList<>();

    @AfterEach
    void closeAll() {
        opened.forEach(SqliteUserRepository::close);
    }

    private Path file() {
        return dir.resolve("data/users.db");
    }

    private SqliteUserRepository open(String prefix) {
        SqliteUserRepository repository = new SqliteUserRepository(file(), prefix);
        opened.add(repository);
        repository.init();
        return repository;
    }

    private SqliteUserRepository open() {
        return open("dmentions_");
    }

    @Test
    void missingFolderIsCreatedAndRepositoryIsUsable() {
        SqliteUserRepository repository = open();

        assertTrue(Files.isRegularFile(file()));
        assertEquals(Optional.empty(), repository.load(STEVE));

        repository.save(new UserData(STEVE, false, null));
        assertEquals(Optional.of(new UserData(STEVE, false, null)), repository.load(STEVE));
    }

    @Test
    void savedUserIsReadBackAfterReopening() {
        SqliteUserRepository repository = open();
        repository.save(new UserData(STEVE, false, null));
        repository.close();

        SqliteUserRepository reopened = open();

        assertEquals(Optional.of(new UserData(STEVE, false, null)), reopened.load(STEVE));
        assertEquals(Optional.empty(), reopened.load(ALEX));
    }

    @Test
    void savingTheSameUserAgainUpdatesTheRow() {
        SqliteUserRepository repository = open();
        repository.save(new UserData(STEVE, false, null));

        repository.save(new UserData(STEVE, true, null));

        assertEquals(Optional.of(new UserData(STEVE, true, null)), repository.load(STEVE));
    }

    @Test
    void saveAllInsertsAndUpdatesInOneGo() {
        SqliteUserRepository repository = open();
        repository.save(new UserData(STEVE, true, null));

        repository.saveAll(List.of(new UserData(STEVE, false, "Stevie"), new UserData(ALEX, false, null)));

        assertEquals(Optional.of(new UserData(STEVE, false, "Stevie")), repository.load(STEVE));
        assertEquals(Optional.of(new UserData(ALEX, false, null)), repository.load(ALEX));
    }

    @Test
    void differentPrefixesUseSeparateTables() {
        SqliteUserRepository first = open("first_");
        first.save(new UserData(STEVE, false, null));
        first.close();

        SqliteUserRepository second = open("second_");

        assertEquals(Optional.empty(), second.load(STEVE));
    }

    @Test
    void invalidPrefixIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new SqliteUserRepository(file(), "x; DROP TABLE y"));
    }

    @Test
    void closeIsSafeBeforeInitAndTwice() {
        SqliteUserRepository repository = new SqliteUserRepository(file(), "dmentions_");
        assertDoesNotThrow(repository::close);

        repository.init();
        repository.close();
        assertDoesNotThrow(repository::close);
    }

    @Test
    void customNameIsStoredAndCanBeCleared() {
        SqliteUserRepository repository = open();
        repository.save(new UserData(STEVE, false, "Stevo"));
        repository.close();

        SqliteUserRepository reopened = open();
        assertEquals(Optional.of(new UserData(STEVE, false, "Stevo")), reopened.load(STEVE));

        reopened.save(new UserData(STEVE, false, null));
        assertEquals(Optional.of(new UserData(STEVE, false, null)), reopened.load(STEVE));
    }
}
