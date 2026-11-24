package dev.dash.dmentions.storage;

import com.google.common.base.Preconditions;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.dash.dmentions.user.UserData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

public abstract class SqlUserRepository implements UserRepository {

    protected HikariDataSource dataSource;
    protected final String table;

    protected SqlUserRepository(String tablePrefix) {
        Preconditions.checkArgument(tablePrefix.matches("[A-Za-z0-9_]*"), "storage prefix must be alphanumeric or underscore");
        this.table = tablePrefix + "users";
    }

    @Override
    public void init() {
        this.dataSource = new HikariDataSource(createConfig());

        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(
                     "CREATE TABLE IF NOT EXISTS %s ( id VARCHAR(36) PRIMARY KEY, mentionable INTEGER DEFAULT 1 )"
                             .formatted(table))
             ) {
            stmt.execute();
        } catch (SQLException e) {
            close();
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<UserData> load(UUID userId) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(
                     "SELECT mentionable FROM %s WHERE id = ?"
                             .formatted(table));
        ) {
            stmt.setString(1, userId.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(new UserData(userId, rs.getInt(1) != 0)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    protected abstract HikariConfig createConfig();

    protected abstract String upsertSql();

    @Override
    public void save(UserData userData) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(upsertSql());
        ) {
            stmt.setString(1, userData.id().toString());
            stmt.setInt(2, userData.mentionable() ? 1 : 0);
            stmt.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
