package dev.dash.dmentions.storage;

import com.zaxxer.hikari.HikariConfig;
import dev.dash.dmentions.user.UserData;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SqliteUserRepository extends SqlUserRepository {

    private final Path file;

    public SqliteUserRepository(Path file, String tablePrefix) {
        super(tablePrefix);
        this.file = file;
    }

    @Override
    public void init() {
        try {
            Files.createDirectories(file.getParent());
        } catch (IOException e) {
            throw new UncheckedIOException("Could not create the folder of " + file + ".", e);
        }
        super.init();
    }

    @Override
    protected HikariConfig createConfig() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:sqlite:" + file.toAbsolutePath());
        config.setDriverClassName("org.sqlite.JDBC");
        config.setPoolName("DMentions-SQLite");
        config.setMaximumPoolSize(1);
        return config;
    }

    @Override
    protected String upsertSql() {
        return """
                INSERT INTO %s (id, mentionable, custom_name)
                VALUES (?,?,?)
                ON CONFLICT(id) DO UPDATE SET
                mentionable = excluded.mentionable,
                custom_name = excluded.custom_name
                """.formatted(table);
    }
}
