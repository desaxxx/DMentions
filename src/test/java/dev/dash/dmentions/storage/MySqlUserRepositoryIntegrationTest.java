package dev.dash.dmentions.storage;

import dev.dash.dmentions.user.UserData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mariadb.MariaDBContainer;
import org.testcontainers.mysql.MySQLContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Runs against real MySQL and MariaDB servers in Docker. Skipped when Docker is not available.
 * Both servers are driven through MySQL Connector/J, the driver Paper ships with.
 */
@Testcontainers(disabledWithoutDocker = true)
class MySqlUserRepositoryIntegrationTest {

    @Container
    private static final MySQLContainer MYSQL = new MySQLContainer("mysql:8.4");

    @Container
    private static final MariaDBContainer MARIADB = new MariaDBContainer("mariadb:11.4");

    private static final UUID STEVE = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID ALEX = UUID.fromString("00000000-0000-0000-0000-000000000002");

    // The containers are shared by every test, so each test works on its own tables.
    // autoCloseArguments = false: JUnit would otherwise stop the containers after the first test.
    private static final AtomicInteger TABLE_COUNTER = new AtomicInteger();

    private final List<MySqlUserRepository> opened = new ArrayList<>();

    static Stream<Arguments> databases() {
        return Stream.of(Arguments.of("MySQL", MYSQL), Arguments.of("MariaDB", MARIADB));
    }

    @AfterEach
    void closeAll() {
        opened.forEach(MySqlUserRepository::close);
    }

    private static String newPrefix() {
        return "t" + TABLE_COUNTER.incrementAndGet() + "_";
    }

    private MySqlUserRepository open(JdbcDatabaseContainer<?> db, String prefix) {
        MySqlSettings settings = new MySqlSettings(
                db.getHost(), db.getMappedPort(3306), db.getDatabaseName(), db.getUsername(), db.getPassword(), false);
        MySqlUserRepository repository = new MySqlUserRepository(settings, prefix);
        opened.add(repository);
        repository.init();
        return repository;
    }

    @ParameterizedTest(name = "{0}", autoCloseArguments = false)
    @MethodSource("databases")
    void savedUserIsReadBack(String name, JdbcDatabaseContainer<?> db) {
        MySqlUserRepository repository = open(db, newPrefix());

        assertEquals(Optional.empty(), repository.load(STEVE));

        repository.save(new UserData(STEVE, false, null));

        assertEquals(Optional.of(new UserData(STEVE, false, null)), repository.load(STEVE));
        assertEquals(Optional.empty(), repository.load(ALEX));
    }

    @ParameterizedTest(name = "{0}", autoCloseArguments = false)
    @MethodSource("databases")
    void savingTheSameUserAgainUpdatesTheRow(String name, JdbcDatabaseContainer<?> db) {
        MySqlUserRepository repository = open(db, newPrefix());
        repository.save(new UserData(STEVE, false, null));

        repository.save(new UserData(STEVE, true, null));

        assertEquals(Optional.of(new UserData(STEVE, true, null)), repository.load(STEVE));
    }

    @ParameterizedTest(name = "{0}", autoCloseArguments = false)
    @MethodSource("databases")
    void saveAllInsertsAndUpdatesInOneGo(String name, JdbcDatabaseContainer<?> db) {
        MySqlUserRepository repository = open(db, newPrefix());
        repository.save(new UserData(STEVE, true, null));

        repository.saveAll(java.util.List.of(new UserData(STEVE, false, "Stevie"), new UserData(ALEX, false, null)));

        assertEquals(Optional.of(new UserData(STEVE, false, "Stevie")), repository.load(STEVE));
        assertEquals(Optional.of(new UserData(ALEX, false, null)), repository.load(ALEX));
    }

    @ParameterizedTest(name = "{0}", autoCloseArguments = false)
    @MethodSource("databases")
    void dataSurvivesReopening(String name, JdbcDatabaseContainer<?> db) {
        String prefix = newPrefix();
        MySqlUserRepository repository = open(db, prefix);
        repository.save(new UserData(STEVE, false, null));
        repository.close();

        MySqlUserRepository reopened = open(db, prefix);

        assertEquals(Optional.of(new UserData(STEVE, false, null)), reopened.load(STEVE));
    }

    @ParameterizedTest(name = "{0}", autoCloseArguments = false)
    @MethodSource("databases")
    void differentPrefixesUseSeparateTables(String name, JdbcDatabaseContainer<?> db) {
        MySqlUserRepository first = open(db, newPrefix());
        first.save(new UserData(STEVE, false, null));

        MySqlUserRepository second = open(db, newPrefix());

        assertEquals(Optional.empty(), second.load(STEVE));
    }

    @ParameterizedTest(name = "{0}", autoCloseArguments = false)
    @MethodSource("databases")
    void customNameIsStoredAndCanBeCleared(String name, JdbcDatabaseContainer<?> db) {
        MySqlUserRepository repository = open(db, newPrefix());
        repository.save(new UserData(STEVE, false, "Stevo"));
        assertEquals(Optional.of(new UserData(STEVE, false, "Stevo")), repository.load(STEVE));

        repository.save(new UserData(STEVE, false, null));
        assertEquals(Optional.of(new UserData(STEVE, false, null)), repository.load(STEVE));
    }
}
