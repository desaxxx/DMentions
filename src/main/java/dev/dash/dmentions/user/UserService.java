package dev.dash.dmentions.user;

import dev.dash.dmentions.storage.UserRepository;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class UserService {

    private static final long SHUTDOWN_TIMEOUT_SECONDS = 10;

    private final UserRepository repository;
    private final Logger logger;
    private final ConcurrentHashMap<UUID, UserData> users = new ConcurrentHashMap<>();
    private final ExecutorService executor;

    public UserService(UserRepository repository, Logger logger) {
        this.repository = repository;
        this.logger = logger;
        this.executor = Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable, "DMentions User Storage");
            thread.setDaemon(true);
            return thread;
        });
    }

    public boolean isMentionable(UUID userId) {
        UserData user = users.get(userId);
        return user == null || user.mentionable();
    }

    public void setMentionable(UUID userId, boolean mentionable) {
        users.compute(userId, (id, data) -> {
            UserData updated = (data == null ? UserData.defaults(id) : data).withMentionable(mentionable);
            submitSave(updated);
            return updated;
        });
    }

    public Optional<String> customName(UUID userId) {
        UserData user = users.get(userId);
        return user == null ? Optional.empty() : Optional.ofNullable(user.customName());
    }

    public void setCustomName(UUID userId, @Nullable String customName) {
        users.compute(userId, (id, data) -> {
            UserData updated = (data == null ? UserData.defaults(id) : data).withCustomName(customName);
            submitSave(updated);
            return updated;
        });
    }

    private void submitSave(UserData data) {
        try {
            executor.execute(() -> {
                try {
                    repository.save(data);
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Could not save user data of " + data.id() + ".", e);
                }
            });
        } catch (RejectedExecutionException e) {
            logger.warning("User data of " + data.id() + " was changed after shutdown and will not be saved.");
        }
    }

    public void load(UUID userId) {
        Optional<UserData> data;
        try {
            data = executor.submit(() -> repository.load(userId)).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        } catch (ExecutionException e) {
            logger.log(Level.SEVERE, "Could not load user data of " + userId + ", using defaults.", e.getCause());
            return;
        } catch (RejectedExecutionException e) {
            return; // shutting down
        }
        users.putIfAbsent(userId, data.orElseGet(() -> UserData.defaults(userId)));
    }

    public void evict(UUID userId) {
        users.remove(userId);
    }

    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                logger.warning("User data saves did not finish in time; some changes may be lost.");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        try {
            repository.close();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Could not close the user storage.", e);
        }
        users.clear();
    }
}
