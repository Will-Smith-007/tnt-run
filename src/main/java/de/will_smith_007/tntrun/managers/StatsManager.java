package de.will_smith_007.tntrun.managers;

import de.will_smith_007.tntrun.game_stats.GameStatistics;
import de.will_smith_007.tntrun.mysql.MySQL;
import lombok.Getter;
import lombok.NonNull;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * The {@link StatsManager} saves and gets all game statistics of the players if the database is enabled.
 *
 * @apiNote Only works on MySQL and MariaDB databases.
 */
public class StatsManager {

    @Getter
    private final boolean DATABASE_ENABLED;
    private final MySQL STATS_SQL;

    public StatsManager(@NonNull DatabaseFileManager databaseFileManager,
                        MySQL statsSQL) {
        this.STATS_SQL = statsSQL;
        this.DATABASE_ENABLED = databaseFileManager.isDatabaseEnabled();
    }

    /**
     * Adds a game win for the specified player {@link UUID} asynchronously.
     * Nothing happens if the database is disabled.
     *
     * @param playerUUID The UUID of the player to which the win should be added.
     */
    public void addGameWinAsync(@NonNull UUID playerUUID) {
        if (!DATABASE_ENABLED) return;
        STATS_SQL.preparedStatementAsync("INSERT INTO tntrun(uuid, wins) VALUES (?, ?) ON DUPLICATE KEY " +
                "UPDATE wins= wins + 1;").thenAccept(preparedStatement -> {
            try {
                preparedStatement.setString(1, playerUUID.toString());
                preparedStatement.setInt(2, 1);
                preparedStatement.executeUpdate();
                preparedStatement.close();
            } catch (SQLException sqlException) {
                sqlException.printStackTrace();
            }
        });
    }

    /**
     * Adds a game lose for the specified player {@link UUID} asynchronously.
     * Nothing happens if the database is disabled.
     *
     * @param playerUUID The UUID of the player to which the loss should be added.
     */
    public void addGameLoseAsync(@NonNull UUID playerUUID) {
        if (!DATABASE_ENABLED) return;
        STATS_SQL.preparedStatementAsync("INSERT INTO tntrun(uuid, loses) VALUES (?, ?) ON DUPLICATE KEY " +
                "UPDATE loses= loses + 1;").thenAccept(preparedStatement -> {
            try {
                preparedStatement.setString(1, playerUUID.toString());
                preparedStatement.setInt(2, 1);
                preparedStatement.executeUpdate();
                preparedStatement.close();
            } catch (SQLException sqlException) {
                sqlException.printStackTrace();
            }
        });
    }

    /**
     * Updates the longest survived time milliseconds only if the specified milliseconds are higher
     * than the saved milliseconds for the player {@link UUID} in the database asynchronously.
     * <br> <br>
     * Nothing happens if the database is disabled or if the saved milliseconds are higher than
     * the specified milliseconds.
     *
     * @param playerUUID         The UUID of the player which should be used to update.
     * @param survivedTimeMillis The survived milliseconds from the previous played game.
     */
    public void updateLongestSurvivedTimeAsync(@NonNull UUID playerUUID, long survivedTimeMillis) {
        if (!DATABASE_ENABLED) return;
        STATS_SQL.preparedStatementAsync("INSERT INTO tntrun(uuid, longestSurvivedTime) VALUES (?, ?) ON " +
                        "DUPLICATE KEY UPDATE longestSurvivedTime= IF(longestSurvivedTime < ?, ?, longestSurvivedTime);")
                .thenAccept(preparedStatement -> {
                    try {
                        preparedStatement.setString(1, playerUUID.toString());
                        preparedStatement.setLong(2, survivedTimeMillis);
                        preparedStatement.setLong(3, survivedTimeMillis);
                        preparedStatement.setLong(4, survivedTimeMillis);
                        preparedStatement.executeUpdate();
                        preparedStatement.close();
                    } catch (SQLException sqlException) {
                        sqlException.printStackTrace();
                    }
                });
    }

    /**
     * Gets all saved game statistics for the specified player {@link UUID} asynchronously.
     *
     * @param playerUUID The UUID of the player from which the statistics should be returned.
     * @return A {@link CompletableFuture} which contains the game statistics.
     * Returns null if the database is disabled or the specified {@link UUID} hasn't played the game before.
     */
    public CompletableFuture<GameStatistics> getGameStatisticsAsync(@NonNull UUID playerUUID) {
        if (!DATABASE_ENABLED) return null;
        return STATS_SQL.preparedStatementAsync("SELECT * FROM tntrun WHERE uuid= ?")
                .thenApply(preparedStatement -> {
                    try {
                        preparedStatement.setString(1, playerUUID.toString());
                        final ResultSet resultSet = preparedStatement.executeQuery();

                        if (resultSet.next()) {
                            final int playerGameWins = resultSet.getInt("wins");
                            final int playerGameLoses = resultSet.getInt("loses");
                            final long longestSurvivedTimeMillis = resultSet.getLong("longestSurvivedTime");

                            preparedStatement.close();

                            return new GameStatistics(playerGameWins, playerGameLoses, longestSurvivedTimeMillis);
                        }

                        preparedStatement.close();
                    } catch (SQLException sqlException) {
                        sqlException.printStackTrace();
                    }
                    return null;
                });
    }

    /**
     * Gets the current rank of the specified player {@link UUID} asynchronously.
     *
     * @param playerUUID The UUID of the player from which the rank should be returned.
     * @return A {@link CompletableFuture} which contains the current rank of the player.
     * Returns 0 if the database is disabled and returns -1 if the specified {@link UUID} hasn't
     * played the game before.
     */
    public CompletableFuture<Integer> getPlayerRankingAsync(@NonNull UUID playerUUID) {
        if (!DATABASE_ENABLED) return CompletableFuture.supplyAsync(() -> 0);
        return STATS_SQL.preparedStatementAsync("SELECT uuid FROM tntrun ORDER BY wins DESC").thenApply(preparedStatement -> {
            try {
                final ArrayList<String> playerUUIDs = new ArrayList<>();
                final ResultSet resultSet = preparedStatement.executeQuery();

                while (resultSet.next()) {
                    playerUUIDs.add(resultSet.getString("uuid"));
                }

                preparedStatement.close();

                //Collections.binarySearch doesn't work here because the list isn't sorted und shouldn't be sorted.
                return (playerUUIDs.indexOf(playerUUID.toString()) + 1);
            } catch (SQLException sqlException) {
                sqlException.printStackTrace();
            }
            return -1;
        });
    }

    /**
     * Gets the current rank of the specified {@link UUID} synchronously.
     *
     * @param playerUUID The UUID of the player from which the rank should be returned.
     * @return The current rank of the player. Returns 0 if the database is disabled and
     * returns -1 if the specified {@link UUID} hasn't played the game before.
     * @apiNote Is only used in asynchronously threads.
     */
    public int getPlayerRanking(@NonNull UUID playerUUID) {
        if (!DATABASE_ENABLED) return 0;
        final PreparedStatement preparedStatement = STATS_SQL.preparedStatement(
                "SELECT uuid FROM tntrun ORDER BY wins DESC");
        try {
            final ArrayList<String> playerUUIDs = new ArrayList<>();
            final ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                playerUUIDs.add(resultSet.getString("uuid"));
            }

            preparedStatement.close();

            //Collections.binarySearch doesn't work here because the list isn't sorted und shouldn't be sorted.
            return (playerUUIDs.indexOf(playerUUID.toString()) + 1);
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
        return -1;
    }
}
