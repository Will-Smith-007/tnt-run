package de.will_smith_007.tntrun.listeners;

import de.will_smith_007.tntrun.enums.GameState;
import de.will_smith_007.tntrun.enums.Message;
import de.will_smith_007.tntrun.game_config.GameConfiguration;
import de.will_smith_007.tntrun.utilities.GameAssets;
import de.will_smith_007.tntrun.managers.StatsManager;
import de.will_smith_007.tntrun.schedulers.EndingCountdownScheduler;
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

    private final JavaPlugin JAVA_PLUGIN;
    private final GameAssets GAME_ASSETS;
    private final EndingCountdownScheduler ENDING_COUNTDOWN_SCHEDULER;
    private final StatsManager STATS_MANAGER;
    private final BukkitScheduler BUKKIT_SCHEDULER = Bukkit.getScheduler();
    private final List<Material> REMOVING_GAME_MATERIALS;

    public PlayerMoveListener(@NonNull JavaPlugin javaPlugin,
                              @NonNull GameAssets gameAssets,
                              @NonNull EndingCountdownScheduler endingCountdownScheduler,
                              @NonNull StatsManager statsManager) {
        this.JAVA_PLUGIN = javaPlugin;
        this.GAME_ASSETS = gameAssets;
        this.REMOVING_GAME_MATERIALS = gameAssets.getREMOVING_GAME_MATERIALS();
        this.ENDING_COUNTDOWN_SCHEDULER = endingCountdownScheduler;
        this.STATS_MANAGER = statsManager;
    }

    @EventHandler
    public void onPlayerMove(@NonNull PlayerMoveEvent playerMoveEvent) {
        final Player player = playerMoveEvent.getPlayer();

        if (GAME_ASSETS.getGameState() != GameState.INGAME) return;

        final List<Player> playersAlive = GAME_ASSETS.getONLINE_PLAYERS_ALIVE();

        if (!playersAlive.contains(player)) return;

        final GameConfiguration gameConfiguration = GAME_ASSETS.getGameConfiguration();

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

            if (REMOVING_GAME_MATERIALS.contains(blockBelowPlayer.getType())) {
                //Player who is walking shouldn't fell into their own path for this reason a delay is required.
                BUKKIT_SCHEDULER.runTaskLater(JAVA_PLUGIN, () -> {
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
            if (STATS_MANAGER.isDATABASE_ENABLED()) {
                final UUID playerUUID = player.getUniqueId();
                STATS_MANAGER.addGameLoseAsync(playerUUID);
                STATS_MANAGER.updateLongestSurvivedTimeAsync(playerUUID, differenceTimeMillis);
            }

            //If there's only one player left or alive in this game, the winner receives a win and the game ends.
            if (playersAlive.size() == 1) {
                GAME_ASSETS.setGameState(GameState.ENDING);

                if (ENDING_COUNTDOWN_SCHEDULER.isRunning()) return;
                ENDING_COUNTDOWN_SCHEDULER.start();

                final Player winnerPlayer = playersAlive.get(0);
                winnerPlayer.sendPlainMessage(Message.PREFIX + "§aYou've survived §e" + getTimerFormat(differenceTimeMillis));

                //The winner gets a win and the survived time milliseconds is updating if it's higher than before.
                if (STATS_MANAGER.isDATABASE_ENABLED()) {
                    final UUID winnerPlayerUUID = winnerPlayer.getUniqueId();
                    STATS_MANAGER.addGameWinAsync(winnerPlayerUUID);
                    STATS_MANAGER.updateLongestSurvivedTimeAsync(winnerPlayerUUID, differenceTimeMillis);
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
