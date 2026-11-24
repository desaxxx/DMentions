package dev.dash.dmentions.storage;

import dev.dash.dmentions.user.UserData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * All users in one YAML file, mirrored in memory. Only users that differ from the defaults are kept,
 * and every change rewrites the file atomically (temp file + move).
 */
public final class YamlUserRepository implements UserRepository {

    private final Path file;
    private final Logger logger;
    private final Map<UUID, UserData> userDataMap = new HashMap<>();

    public YamlUserRepository(Path file, Logger logger) {
        this.file = file;
        this.logger = logger;
    }

    /**
     * @throws RuntimeException if the users file cannot be read; the file is left untouched
     */
    @Override
    public synchronized void init() {
        userDataMap.clear();
        if (!Files.exists(file)) {
            try {
                Files.createDirectories(file.getParent());
            } catch (IOException e) {
                throw new UncheckedIOException("Could not create the folder of " + file + ".", e);
            }
            return; // created by the first save
        }

        YamlConfiguration config = new YamlConfiguration();
        try {
            config.load(file.toFile());
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException("Could not load " + file + ".", e);
        }

        ConfigurationSection usersSection = config.getConfigurationSection("users");
        if (usersSection == null) return;

        for (String userIdStr : usersSection.getKeys(false)) {
            UUID userId;
            try {
                userId = UUID.fromString(userIdStr);
            } catch (IllegalArgumentException e) {
                logger.warning("Skipping invalid user id in " + file.getFileName() + ": " + userIdStr);
                continue;
            }

            boolean mentionable = usersSection.getBoolean(userIdStr + ".mentionable", true);
            String customName = usersSection.getString(userIdStr + ".custom_name");

            UserData user = new UserData(userId, mentionable, customName);
            if (!user.isDefault()) {
                userDataMap.put(userId, user);
            }
        }
    }

    @Override
    public synchronized Optional<UserData> load(UUID userId) {
        return Optional.ofNullable(userDataMap.get(userId));
    }

    /**
     * @throws UncheckedIOException if the file cannot be written
     */
    @Override
    public synchronized void save(UserData userData) {
        UserData previous = userData.isDefault()
                ? userDataMap.remove(userData.id())
                : userDataMap.put(userData.id(), userData);

        // a default user that was not stored, or the same values again: the file would not change
        boolean dirty = userData.isDefault() ? previous != null : !Objects.equals(previous, userData);
        if (dirty) {
            writeFile();
        }
    }

    private void writeFile() {
        YamlConfiguration config = new YamlConfiguration();
        for (UserData user : userDataMap.values()) {
            config.set("users." + user.id() + ".mentionable", user.mentionable());
            config.set("users." + user.id() + ".custom_name", user.customName());
        }

        Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
        try {
            Files.writeString(tmp, config.saveToString());
            try {
                Files.move(tmp, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Could not write " + file + ".", e);
        }
    }

    @Override
    public synchronized void close() {}
}
