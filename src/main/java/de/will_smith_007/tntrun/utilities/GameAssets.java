package de.will_smith_007.tntrun.utilities;

import com.google.inject.Singleton;
import de.will_smith_007.tntrun.enums.GameState;
import de.will_smith_007.tntrun.game_config.GameConfiguration;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * This class holds and sets all required information about the current game.
 */
@Getter
@Setter
@Singleton
public class GameAssets {

    private GameState gameState = GameState.LOBBY;
    private GameConfiguration gameConfiguration = null;
    private final List<Player> onlinePlayersAlive = new ArrayList<>();
    @Setter(AccessLevel.NONE)
    private final List<Material> removingGameMaterials = List.of(Material.SAND, Material.RED_SAND, Material.GRAVEL);
}
