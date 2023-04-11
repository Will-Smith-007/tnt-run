package de.will_smith_007.tntrun.schedulers;

import de.will_smith_007.tntrun.enums.GameState;
import de.will_smith_007.tntrun.managers.GameAssets;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.List;

public class PlayerAFKScannerScheduler extends Scheduler {

    private int taskID;
    private boolean isRunning;
    private final JavaPlugin JAVA_PLUGIN;
    private final GameAssets GAME_ASSETS;
    private final List<Material> REMOVING_GAME_MATERIALS;

    public PlayerAFKScannerScheduler(@NonNull JavaPlugin javaPlugin,
                                     @NonNull GameAssets gameAssets) {
        this.JAVA_PLUGIN = javaPlugin;
        this.GAME_ASSETS = gameAssets;
        this.REMOVING_GAME_MATERIALS = gameAssets.getREMOVING_GAME_MATERIALS();
    }

    @Override
    public void start() {
        isRunning = true;
        taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(JAVA_PLUGIN, () -> {
            final GameState gameState = GAME_ASSETS.getGameState();

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

                final Location playerLocation = player.getLocation();

                final Block blockBelowPlayer = playerLocation.getBlock().getRelative(BlockFace.DOWN);
                final Block blockBelowRemovingBlock = blockBelowPlayer.getRelative(BlockFace.DOWN);

                if (!REMOVING_GAME_MATERIALS.contains(blockBelowPlayer.getType())) continue;

                blockBelowPlayer.setType(Material.AIR);
                blockBelowRemovingBlock.setType(Material.AIR);
            }
        }, 0L, 5L);
    }

    @Override
    public void stop() {
        isRunning = false;
        Bukkit.getScheduler().cancelTask(taskID);
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }
}
