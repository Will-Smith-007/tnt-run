package de.will_smith_007.tntrun.mysql;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.will_smith_007.tntrun.managers.DatabaseFileManager;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

/**
 * The {@link MySQL} class is used to perform required SQL queries and updates.
 *
 * @apiNote Uses the MySQL Connection method but works also with MariaDB databases.
 */
@Singleton
public class MySQL {

    private final Logger logger;
    private final String host, database, username, secret;
    private final int port;

    private volatile Connection connection;
    private volatile HikariDataSource hikariDataSource;

    /**
     * Uses the database configuration file to establish a connection to the database.
     *
     * @param databaseFileManager Database file manager which contains the information about the database file.
     * @param logger              Logger which should be used to log important information.
     */
    @Inject
    public MySQL(@NonNull DatabaseFileManager databaseFileManager,
                 @NonNull Logger logger) {
        this.logger = logger;

        this.host = databaseFileManager.getHostAddress();
        this.database = databaseFileManager.getDatabaseName();
        this.username = databaseFileManager.getDatabaseUsername();
        this.secret = databaseFileManager.getSecret();
        this.port = databaseFileManager.getPort();

        if (!databaseFileManager.isDatabaseEnabled()) return;

        connect();
    }

    /**
     * Tries to establish a connection to the configured database and sets some
     * {@link HikariConfig} configurations for this connection.
     */
    private void connect() {
        try {
            final HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
            hikariConfig.setUsername(username);
            hikariConfig.setPassword(secret);

            hikariConfig.setConnectionTestQuery("SELECT 1");

            hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
            hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
            hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            hikariConfig.addDataSourceProperty("useServerPrepStmts", "true");
            hikariConfig.addDataSourceProperty("useUnicode", "true");
            hikariConfig.addDataSourceProperty("maxIdleTime", 28800);

            hikariConfig.setPoolName(database);
            hikariConfig.setMaximumPoolSize(2);
            hikariConfig.setMinimumIdle(5);

            hikariDataSource = new HikariDataSource(hikariConfig);
            connection = hikariDataSource.getConnection();

            logger.info("Connection to the database was successfully established!");
        } catch (SQLException | NullPointerException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Closes the established database connection.
     * Nothing happens if the database connection is null or wasn't even established.
     */
    public void closeConnection() {
        if (connection == null) return;
        hikariDataSource.close();
        logger.info("Database connection was closed.");
    }

    /**
     * Performs a SQL update operation synchronously.
     * If the connection is closed or null, the database tries a reconnect/connect.
     *
     * @param query The SQL update query which should be performed by the database.
     */
    public void update(@NotNull String query) {
        try {
            if (connection == null || connection.isClosed()) connect();
            final Statement statement = connection.createStatement();
            statement.executeUpdate(query);
            statement.close();
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
    }

    /**
     * Prepares a safe SQL statement and isn't vulnerable for "SQL Injections".
     *
     * @param sqlQuery The SQL Query which should be performed by the database.
     * @return The {@link PreparedStatement} of the query to work with.
     */
    public PreparedStatement preparedStatement(@NonNull String sqlQuery) {
        try {
            if (connection == null || connection.isClosed()) connect();
            return connection.prepareStatement(sqlQuery);
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
        return null;
    }

    /**
     * Prepares an asynchronously, safe SQL statement and isn't vulnerable for "SQL Injections".
     *
     * @param sqlQuery The SQL Query which should be performed by the database.
     * @return A {@link CompletableFuture} which contains the {@link PreparedStatement} of the query.
     */
    public CompletableFuture<PreparedStatement> preparedStatementAsync(@NonNull String sqlQuery) {
        return CompletableFuture.supplyAsync(() -> preparedStatement(sqlQuery));
    }
}
