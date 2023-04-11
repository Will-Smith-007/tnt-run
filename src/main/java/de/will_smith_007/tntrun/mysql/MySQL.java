package de.will_smith_007.tntrun.mysql;

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
public class MySQL {

    private final Logger LOGGER;
    private final String HOST, DATABASE, USERNAME, SECRET;
    private final int PORT;

    private volatile Connection connection;
    private volatile HikariDataSource hikariDataSource;

    /**
     * Uses the database configuration file to establish a connection to the database.
     *
     * @param databaseFileManager Database file manager which contains the information about the database file.
     * @param logger              Logger which should be used to log important information.
     */
    public MySQL(@NonNull DatabaseFileManager databaseFileManager,
                 @NonNull Logger logger) {
        this.LOGGER = logger;

        this.HOST = databaseFileManager.getHostAddress();
        this.DATABASE = databaseFileManager.getDatabaseName();
        this.USERNAME = databaseFileManager.getDatabaseUsername();
        this.SECRET = databaseFileManager.getSecret();
        this.PORT = databaseFileManager.getPort();

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
            hikariConfig.setJdbcUrl("jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE);
            hikariConfig.setUsername(USERNAME);
            hikariConfig.setPassword(SECRET);

            hikariConfig.setConnectionTestQuery("SELECT 1");

            hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
            hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
            hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            hikariConfig.addDataSourceProperty("useServerPrepStmts", "true");
            hikariConfig.addDataSourceProperty("useUnicode", "true");
            hikariConfig.addDataSourceProperty("maxIdleTime", 28800);

            hikariConfig.setPoolName(DATABASE);
            hikariConfig.setMaximumPoolSize(2);
            hikariConfig.setMinimumIdle(5);

            hikariDataSource = new HikariDataSource(hikariConfig);
            connection = hikariDataSource.getConnection();

            LOGGER.info("Connection to the database was successfully established!");
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
        LOGGER.info("Database connection was closed.");
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
