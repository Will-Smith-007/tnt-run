package de.will_smith_007.tntrun.listeners;

import com.google.inject.Inject;
import de.will_smith_007.tntrun.enums.GameState;
import de.will_smith_007.tntrun.enums.Message;
import de.will_smith_007.tntrun.game_config.GameConfiguration;
import de.will_smith_007.tntrun.managers.StatsManager;
import de.will_smith_007.tntrun.schedulers.EndingCountdownScheduler;
import de.will_smith_007.tntrun.utilities.GameAssets;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PlayerMoveListener implements Listener {

    private final JavaPlugin javaPlugin;
    private final GameAssets gameAssets;
    private final EndingCountdownScheduler endingCountdownScheduler;
    private final StatsManager statsManager;
    private final BukkitScheduler bukkitScheduler = Bukkit.getScheduler();
    private final List<Material> removingGameMaterials;

    @Inject
    public PlayerMoveListener(@NonNull JavaPlugin javaPlugin,
                              @NonNull GameAssets gameAssets,
                              @NonNull EndingCountdownScheduler endingCountdownScheduler,
                              @NonNull StatsManager statsManager) {
        this.javaPlugin = javaPlugin;
        this.gameAssets = gameAssets;
        this.removingGameMaterials = gameAssets.getRemovingGameMaterials();
        this.endingCountdownScheduler = endingCountdownScheduler;
        this.statsManager = statsManager;
    }

    @EventHandler
    public void onPlayerMove(@NonNull PlayerMoveEvent playerMoveEvent) {
        final Player player = playerMoveEvent.getPlayer();

        if (gameAssets.getGameState() != GameState.INGAME) return;

        final List<Player> playersAlive = gameAssets.getOnlinePlayersAlive();

        if (!playersAlive.contains(player)) return;

        final GameConfiguration gameConfiguration = gameAssets.getGameConfiguration();

        if (gameConfiguration == null) return;

        final String playedMapName = gameConfiguration.gameMap().getName();
        final String playerMapName = player.getWorld().getName();

        if (!playedMapName.equalsIgnoreCase(playerMapName)) return;

        final Location playerLocation = player.getLocation();
        final int deathHeight = gameConfiguration.gameDeathHeight();
        final int playerHeight = (int) playerLocation.getY();

        //Blocks are only going to be removed if the player touches the ground.
        if (((LivingEntity) player).isOnGround()) {
            final Block blockBelowPlayer = playerLocation.getBlock().getRelative(BlockFace.DOWN);
            final Block blockBelowRemovingBlock = blockBelowPlayer.getRelative(BlockFace.DOWN);

            if (removingGameMaterials.contains(blockBelowPlayer.getType())) {
                //Player who is walking shouldn't fell into their own path for this reason a delay is required.
                bukkitScheduler.runTaskLater(javaPlugin, () -> {
                    blockBelowPlayer.setType(Material.AIR);
                    blockBelowRemovingBlock.setType(Material.AIR);
                }, 1L);
            }
        }

        //Player elimination when the current player height is below the configured death height of the map.
        if (playerHeight <= deathHeight) {
            final Location gameMapSpawn = gameConfiguration.gameSpawnLocation();
            player.setGameMode(GameMode.SPECTATOR);
            player.teleport(gameMapSpawn);
            playersAlive.remove(player);

            final int playersAliveSize = playersAlive.size();

            Bukkit.getOnlinePlayers().forEach(onlinePlayer ->
                    onlinePlayer.sendPlainMessage(Message.PREFIX + "§e" + player.getName() + "§c fell to death! " +
                            (playersAliveSize > 1 ? playersAliveSize + "§c players remaining." :
                                    "§e" + playersAlive.get(0).getName() + "§a won the game!"))
            );

            final long currentTimeMillis = System.currentTimeMillis();
            final long startedGameTimeMillis = gameConfiguration.startedGameTimeMillis();
            final long differenceTimeMillis = (currentTimeMillis - startedGameTimeMillis);

            player.sendPlainMessage(Message.PREFIX + "§aYou've survived §e" + getTimerFormat(differenceTimeMillis));

            //The eliminated player gets a loss and the survived time milliseconds is updating if it's higher than before.
            if (statsManager.isDatabaseEnabled()) {
                final UUID playerUUID = player.getUniqueId();
                statsManager.addGameLoseAsync(playerUUID);
                statsManager.updateLongestSurvivedTimeAsync(playerUUID, differenceTimeMillis);
            }

            //If there's only one player left or alive in this game, the winner receives a win and the game ends.
            if (playersAlive.size() == 1) {
                gameAssets.setGameState(GameState.ENDING);

                if (endingCountdownScheduler.isRunning()) return;
                endingCountdownScheduler.start();

                final Player winnerPlayer = playersAlive.get(0);
                winnerPlayer.sendPlainMessage(Message.PREFIX + "§aYou've survived §e" + getTimerFormat(differenceTimeMillis));

                //The winner gets a win and the survived time milliseconds is updating if it's higher than before.
                if (statsManager.isDatabaseEnabled()) {
                    final UUID winnerPlayerUUID = winnerPlayer.getUniqueId();
                    statsManager.addGameWinAsync(winnerPlayerUUID);
                    statsManager.updateLongestSurvivedTimeAsync(winnerPlayerUUID, differenceTimeMillis);
                }
            }
        }
    }

    /**
     * Gets the formatted timer of the specified timeMillis.
     *
     * @param timeMillis Time Milliseconds which should be formatted.
     * @return The formatted String timer format in minutes and seconds.
     */
    private @NonNull String getTimerFormat(long timeMillis) {
        final long timerMinutes = TimeUnit.MILLISECONDS.toMinutes(timeMillis);
        return String.format("%02d minutes and %02d seconds",
                timerMinutes,
                (TimeUnit.MILLISECONDS.toSeconds(timeMillis) - TimeUnit.MINUTES.toSeconds(timerMinutes)));
    }
}
