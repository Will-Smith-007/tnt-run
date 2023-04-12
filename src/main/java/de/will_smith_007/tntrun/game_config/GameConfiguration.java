package de.will_smith_007.tntrun.game_config;

import de.will_smith_007.tntrun.utilities.GameAssets;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * The {@link GameConfiguration} class is used to save all important information about the started game.
 *
 * @param gameMap               Game map on which the players are playing.
 * @param gameSpawnLocation     Spawn location of the current game map.
 * @param gameDeathHeight       Death height on which the players should die.
 * @param startedGameTimeMillis Time milliseconds of the game start.
 * @apiNote This configuration should be initialized in the {@link GameAssets} class.
 */
public record GameConfiguration(@NonNull World gameMap,
                                @NonNull Location gameSpawnLocation,
                                int gameDeathHeight,
                                long startedGameTimeMillis) {
}
