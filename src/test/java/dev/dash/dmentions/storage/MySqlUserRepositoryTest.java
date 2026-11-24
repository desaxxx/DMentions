package dev.dash.dmentions.storage;

import com.zaxxer.hikari.HikariConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks the Hikari settings without opening a connection.
 * Real database behaviour is covered by {@link MySqlUserRepositoryIntegrationTest}.
 */
class MySqlUserRepositoryTest {

    private static HikariConfig configFor(boolean useSsl) {
        MySqlSettings settings = new MySqlSettings("db.example.com", 3307, "mentions", "steve", "secret", useSsl);
        return new MySqlUserRepository(settings, "dmentions_").createConfig();
    }

    @Test
    void configUsesTheGivenConnectionDetails() {
        HikariConfig config = configFor(false);

        assertTrue(config.getJdbcUrl().startsWith("jdbc:mysql://db.example.com:3307/mentions"), config.getJdbcUrl());
        assertEquals("steve", config.getUsername());
        assertEquals("secret", config.getPassword());
        assertEquals("com.mysql.cj.jdbc.Driver", config.getDriverClassName());
        assertEquals("DMentions-MySQL", config.getPoolName());
    }

    @Test
    void sslSettingIsPassedToTheUrl() {
        assertTrue(configFor(true).getJdbcUrl().contains("useSSL=true"), configFor(true).getJdbcUrl());
        assertTrue(configFor(false).getJdbcUrl().contains("useSSL=false"), configFor(false).getJdbcUrl());
    }

    @Test
    void publicKeyRetrievalIsAllowedWithoutSsl() {
        // MySQL 8+ (caching_sha2_password) refuses a non-SSL login without it.
        assertTrue(configFor(false).getJdbcUrl().contains("allowPublicKeyRetrieval=true"), configFor(false).getJdbcUrl());
    }
}
