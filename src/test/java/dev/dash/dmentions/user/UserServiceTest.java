package dev.dash.dmentions.user;

import dev.dash.dmentions.storage.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserServiceTest {

    private static final UUID STEVE = UUID.fromString("00000000-0000-0000-0000-000000000001");

    /** In-memory repository that records every save; saves are slow so ordering problems would show. */
    static final class FakeRepository implements UserRepository {
        final Map<UUID, UserData> stored = new ConcurrentHashMap<>();
        final List<UserData> saves = new CopyOnWriteArrayList<>();
        volatile boolean failLoads;
        volatile boolean failSaves;
        volatile boolean closed;

        @Override
        public void init() {
        }

        @Override
        public Optional<UserData> load(UUID userId) {
            if (failLoads) throw new IllegalStateException("load failed");
            return Optional.ofNullable(stored.get(userId));
        }

        @Override
        public void save(UserData userData) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            if (failSaves) throw new IllegalStateException("save failed");
            saves.add(userData);
            stored.put(userData.id(), userData);
        }

        @Override
        public void close() {
            closed = true;
        }
    }

    private final FakeRepository repository = new FakeRepository();
    private final List<LogRecord> logs = new ArrayList<>();
    private final Logger logger = Logger.getAnonymousLogger();
    private final UserService service;

    {
        logger.setUseParentHandlers(false);
        logger.addHandler(new Handler() {
            @Override
            public void publish(LogRecord record) {
                synchronized (logs) {
                    logs.add(record);
                }
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        });
        service = new UserService(repository, logger);
    }

    @AfterEach
    void tearDown() {
        service.shutdown();
    }

    private boolean logged(Level level) {
        synchronized (logs) {
            return logs.stream().anyMatch(r -> r.getLevel() == level);
        }
    }

    @Test
    void unknownUsersAreMentionable() {
        assertTrue(service.isMentionable(STEVE));
    }

    @Test
    void changeIsVisibleAtOnceAndSavesAreWrittenInOrder() {
        service.setMentionable(STEVE, false);
        assertFalse(service.isMentionable(STEVE));

        service.setMentionable(STEVE, true);
        service.setMentionable(STEVE, false);
        service.shutdown(); // waits for the queued saves

        assertEquals(List.of(new UserData(STEVE, false, null), new UserData(STEVE, true, null), new UserData(STEVE, false, null)),
                repository.saves);
    }

    @Test
    void loadReadsTheStoredUser() {
        repository.stored.put(STEVE, new UserData(STEVE, false, null));

        service.load(STEVE);

        assertFalse(service.isMentionable(STEVE));
    }

    @Test
    void loadSeesASaveThatWasStillQueued() {
        service.setMentionable(STEVE, false);
        service.evict(STEVE); // quits before the slow save finished, rejoins right away

        service.load(STEVE);

        assertFalse(service.isMentionable(STEVE));
    }

    @Test
    void loadDoesNotOverwriteAChangeMadeMeanwhile() {
        repository.stored.put(STEVE, new UserData(STEVE, false, null));
        service.setMentionable(STEVE, true);

        service.load(STEVE);

        assertTrue(service.isMentionable(STEVE));
    }

    @Test
    void failedLoadIsLoggedAndFallsBackToDefaults() {
        repository.failLoads = true;

        service.load(STEVE);

        assertTrue(service.isMentionable(STEVE));
        assertTrue(logged(Level.SEVERE));
    }

    @Test
    void failedSaveIsLoggedAndKeepsTheCachedValue() {
        repository.failSaves = true;

        service.setMentionable(STEVE, false);
        assertFalse(service.isMentionable(STEVE));
        service.shutdown();

        assertTrue(logged(Level.SEVERE));
        assertTrue(repository.saves.isEmpty());
    }

    @Test
    void evictForgetsTheUser() {
        repository.stored.put(STEVE, new UserData(STEVE, false, null));
        service.load(STEVE);

        service.evict(STEVE);

        assertTrue(service.isMentionable(STEVE));
    }

    @Test
    void shutdownClosesTheRepositoryAndLaterChangesAreNotSaved() {
        service.shutdown();
        assertTrue(repository.closed);

        assertDoesNotThrow(() -> service.setMentionable(STEVE, false));
        assertDoesNotThrow(() -> service.load(STEVE));
        assertTrue(repository.saves.isEmpty());
    }

    @Test
    void customNameIsVisibleAtOnceAndKeepsTheOtherFields() {
        assertEquals(Optional.empty(), service.customName(STEVE));
        service.setMentionable(STEVE, false);

        service.setCustomName(STEVE, "Stevo");
        assertEquals(Optional.of("Stevo"), service.customName(STEVE));
        assertFalse(service.isMentionable(STEVE));

        service.setCustomName(STEVE, null);
        assertEquals(Optional.empty(), service.customName(STEVE));
        service.shutdown();

        assertEquals(List.of(new UserData(STEVE, false, null), new UserData(STEVE, false, "Stevo"), new UserData(STEVE, false, null)),
                repository.saves);
    }
}
