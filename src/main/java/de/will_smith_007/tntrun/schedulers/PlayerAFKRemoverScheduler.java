package de.will_smith_007.tntrun.schedulers;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.will_smith_007.tntrun.enums.GameState;
import de.will_smith_007.tntrun.schedulers.interfaces.IScheduler;
import de.will_smith_007.tntrun.utilities.GameAssets;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.BoundingBox;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Singleton
public final class PlayerAFKRemoverScheduler implements IScheduler {

    private int taskID;
    private boolean isRunning;
    private final JavaPlugin javaPlugin;
    private final GameAssets gameAssets;

    @Inject
    public PlayerAFKRemoverScheduler(@NonNull JavaPlugin javaPlugin,
                                     @NonNull GameAssets gameAssets) {
        this.javaPlugin = javaPlugin;
        this.gameAssets = gameAssets;
    }

    @Override
    public void start() {
        if (isRunning) return;

        isRunning = true;
        taskID = BUKKIT_SCHEDULER.scheduleSyncRepeatingTask(javaPlugin, () -> {
            final GameState gameState = gameAssets.getGameState();

            if (gameState == GameState.ENDING) {
                stop();
                return;
            }

            final Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();

            for (Player player : onlinePlayers) {
                final AttributeInstance speedAttributeInstance = player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);

                if (speedAttributeInstance == null) continue;

                //Round speed to 2 digits.
                final double speed = Math.round(speedAttributeInstance.getValue() * 100.0) / 100.0;

                //This is the speed of a player when he's standing still.
                if (speed != 0.10d) continue;

                removeBlocksUnderneath(player);
            }
        }, 0L, 5L);
    }

    @Override
    public void stop() {
        if (!isRunning) return;

        isRunning = false;
        BUKKIT_SCHEDULER.cancelTask(taskID);
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Removes all underneath blocks where the player stands on.
     * @param player Player from which the underneath blocks should be removed.
     */
    public void removeBlocksUnderneath(@NonNull Player player) {
        final Set<Location> locations = new HashSet<>();
        final World playerWorld = player.getWorld();
        final BoundingBox boundingBox = player.getBoundingBox().expand(0.3, 0, 0.3);

        for (double x = boundingBox.getMinX(); x < boundingBox.getMaxX(); x++) {
            for (double z = boundingBox.getMinZ(); z < boundingBox.getMaxZ(); z++) {
                locations.add(new Location(playerWorld, x, boundingBox.getMinY() - 1, z));
                locations.add(new Location(playerWorld, x, boundingBox.getMinY() - 2, z));
            }
        }

        for (Location location : locations) {
            location.getBlock().setType(Material.AIR);
        }
    }
}
