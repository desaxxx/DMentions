package dev.dash.dmentions.storage;

import org.jetbrains.annotations.NotNull;

public record MySqlSettings(String host, int port, String database, String username, String password, boolean useSsl) {

    @Override
    public @NotNull String toString() {
        // Keeps the password out of logs and exception messages.
        return "MySqlSettings[host=" + host + ", port=" + port + ", database=" + database
                + ", username=" + username + ", password=****, useSsl=" + useSsl + "]";
    }
}
