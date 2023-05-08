package de.will_smith_007.tntrun.schedulers;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.will_smith_007.tntrun.enums.GameState;
import de.will_smith_007.tntrun.enums.Message;
import de.will_smith_007.tntrun.game_config.GameConfiguration;
import de.will_smith_007.tntrun.managers.MapManager;
import de.will_smith_007.tntrun.schedulers.interfaces.ICountdownOptions;
import de.will_smith_007.tntrun.schedulers.interfaces.IScheduler;
import de.will_smith_007.tntrun.utilities.GameAssets;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

@Singleton
public final class LobbyCountdownScheduler implements IScheduler, ICountdownOptions {

    private int taskID;
    @Setter
    @Getter
    private int countdown;
    private boolean isRunning = false;
    private final JavaPlugin javaPlugin;
    private final Logger logger;
    private final GameAssets gameAssets;
    private final MapManager mapManager;
    private final ProtectionCountdownScheduler protectionCountdownScheduler;

    @Inject
    public LobbyCountdownScheduler(@NonNull JavaPlugin javaPlugin,
                                   @NonNull GameAssets gameAssets,
                                   @NonNull MapManager mapManager,
                                   @NonNull ProtectionCountdownScheduler protectionCountdownScheduler) {
        this.javaPlugin = javaPlugin;
        this.logger = javaPlugin.getLogger();
        this.gameAssets = gameAssets;
        this.mapManager = mapManager;
        this.protectionCountdownScheduler = protectionCountdownScheduler;
    }

    @Override
    public void start() {
        if (isRunning) return;

        countdown = 60;
        isRunning = true;
        logger.info("The countdown is starting...");

        taskID = BUKKIT_SCHEDULER.scheduleSyncRepeatingTask(javaPlugin, () -> {
            final Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();

            onlinePlayers.forEach(player -> player.setLevel(countdown));

            switch (countdown) {
                //Lobby phase mechanics.
                case 30, 10, 5, 3, 2, 1 -> onlinePlayers.forEach(player -> {
                    player.sendPlainMessage(Message.PREFIX + getCountdownMessage(countdown));
                    playCountdownSound(player);
                });
                case 0 -> {
                    //Starting game mechanics.
                    onlinePlayers.forEach(player ->
                            player.sendPlainMessage(Message.PREFIX + "The game is starting now."));

                    gameAssets.setGameState(GameState.PROTECTION);
                    gameAssets.getOnlinePlayersAlive().addAll(onlinePlayers);

                    final List<String> gameMaps = mapManager.getMapList();
                    Collections.shuffle(gameMaps);

                    final String selectedGameMapName = gameMaps.get(0);
                    final World selectedGameMap = mapManager.loadMap(selectedGameMapName);

                    if (selectedGameMap == null) {
                        logger.severe("The game map named \"" + selectedGameMapName + "\" couldn't be found.");
                        stop();
                        return;
                    }

                    final Location gameMapSpawn = mapManager.getMapSpawnPoint(selectedGameMapName);

                    if (gameMapSpawn == null) {
                        logger.severe("There isn't a configured spawn point for the map named \"" +
                                selectedGameMapName + "\"");
                        stop();
                        return;
                    }

                    final int deathHeight = mapManager.getDeathHeight(selectedGameMapName);

                    gameAssets.setGameConfiguration(
                            new GameConfiguration(selectedGameMap, gameMapSpawn, deathHeight, System.currentTimeMillis())
                    );

                    onlinePlayers.forEach(player -> {
                        player.teleport(gameMapSpawn.toCenterLocation());
                        player.setGameMode(GameMode.ADVENTURE);
                    });

                    if (!protectionCountdownScheduler.isRunning()) {
                        protectionCountdownScheduler.start();
                    }

                    stop();
                }
            }

            countdown--;
        }, 0L, 20L);
    }

    @Override
    public void stop() {
        if (!isRunning) return;

        isRunning = false;
        BUKKIT_SCHEDULER.cancelTask(taskID);
        logger.info("The starting countdown was cancelled.");
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Gets the message of the current countdown.
     *
     * @param currentCountdown The current countdown until the game starts.
     * @return The game countdown message which is going to send to the players.
     */
    @Override
    public @NonNull String getCountdownMessage(int currentCountdown) {
        return "The game is starting in §c" + countdown + (countdown == 1 ? " second§7." : " seconds§7.");
    }

    @Override
    public void playCountdownSound(@NonNull Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
    }
}
