package dev.dash.dmentions.storage;

import com.zaxxer.hikari.HikariConfig;

public class MySqlUserRepository extends SqlUserRepository {

    private final MySqlSettings settings;

    public MySqlUserRepository(MySqlSettings settings, String tablePrefix) {
        super(tablePrefix);
        this.settings = settings;
    }

    @Override
    protected HikariConfig createConfig() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + settings.host() + ":" + settings.port() + "/" + settings.database() + "?useSSL=" + settings.useSsl() + "&characterEncoding=utf8&allowPublicKeyRetrieval=true");
        config.setUsername(settings.username());
        config.setPassword(settings.password());
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setPoolName("DMentions-MySQL");
        config.setMaximumPoolSize(2);
        return config;
    }

    @Override
    protected String upsertSql() {
        return """
                INSERT INTO %s (id, mentionable, custom_name)
                VALUES (?,?,?)
                ON DUPLICATE KEY UPDATE
                mentionable = VALUES(mentionable),
                custom_name = VALUES(custom_name)
                """.formatted(table);
    }
}
