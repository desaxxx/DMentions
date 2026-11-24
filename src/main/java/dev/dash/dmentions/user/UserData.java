package dev.dash.dmentions.user;

import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record UserData(UUID id, boolean mentionable, @Nullable String customName) {

    public static UserData defaults(UUID id) {
        return new UserData(id, true, null);
    }

    public UserData withMentionable(boolean mentionable) {
        return new UserData(id, mentionable, customName);
    }

    public UserData withCustomName(@Nullable String customName) {
        return new UserData(id, mentionable, customName);
    }

    public boolean isDefault() {
        return mentionable
                && customName == null;
    }
}
