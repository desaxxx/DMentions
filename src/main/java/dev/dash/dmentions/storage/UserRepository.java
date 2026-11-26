package dev.dash.dmentions.storage;

import dev.dash.dmentions.user.UserData;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    void init();

    Optional<UserData> load(UUID userId);

    void save(UserData userData);

    default void saveAll(Collection<UserData> users) {
        for (UserData user : users) {
            save(user);
        }
    }

    void close();
}
