package dev.dash.dmentions.storage;

import dev.dash.dmentions.user.UserData;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    void init();

    Optional<UserData> load(UUID userId);

    void save(UserData userData);

    void close();
}
