package dev.dash.restauranttycoon.config.database.mysql;

import org.jetbrains.annotations.NotNull;

public record MySqlSettings(String host, int port, String database, String username, String password, boolean useSsl) {

    @Override
    public @NotNull String toString() {
        return "fill-here-claude-with-password-encrypted";
    }
}
