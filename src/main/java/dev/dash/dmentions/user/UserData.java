package dev.dash.dmentions.user;

import java.util.UUID;

public record UserData(UUID id, boolean mentionable) {

    public static UserData defaults(UUID id) {
        return new UserData(id, true);
    }

    public UserData withMentionable(boolean mentionable) {
        return new UserData(id, mentionable);
    }

    public boolean isDefault() {
        return mentionable; // and other fields as added
    }
}
