package de.will_smith_007.tntrun.game_stats;

/**
 * This class is used to collect simply all statistics from a player in one SQL query.
 *
 * @param playerGameWins      Game wins of a player.
 * @param playerGameLoses     Game loses of a player.
 * @param longestSurvivedTime Longest survived time in the game in milliseconds.
 * @see de.will_smith_007.tntrun.managers.StatsManager
 */
public record GameStatistics(int playerGameWins,
                             int playerGameLoses,
                             long longestSurvivedTime) {
}
