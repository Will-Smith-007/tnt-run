package de.will_smith_007.tntrun.schedulers;

import de.will_smith_007.tntrun.enums.GameState;
import de.will_smith_007.tntrun.enums.Message;
import de.will_smith_007.tntrun.game_config.GameConfiguration;
import de.will_smith_007.tntrun.managers.MapManager;
import de.will_smith_007.tntrun.utilities.GameAssets;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public final class LobbyCountdownScheduler implements IScheduler {

    private int taskID;
    @Setter
    @Getter
    private int countdown;
    private boolean isRunning = false;
    private final JavaPlugin JAVA_PLUGIN;
    private final Logger LOGGER;
    private final GameAssets GAME_ASSETS;
    private final MapManager MAP_MANAGER;
    private final ProtectionCountdownScheduler PROTECTION_COUNTDOWN_SCHEDULER;

    public LobbyCountdownScheduler(@NonNull JavaPlugin javaPlugin,
                                   @NonNull GameAssets gameAssets,
                                   @NonNull MapManager mapManager,
                                   @NonNull ProtectionCountdownScheduler protectionCountdownScheduler) {
        this.JAVA_PLUGIN = javaPlugin;
        this.LOGGER = javaPlugin.getLogger();
        this.GAME_ASSETS = gameAssets;
        this.MAP_MANAGER = mapManager;
        this.PROTECTION_COUNTDOWN_SCHEDULER = protectionCountdownScheduler;
    }

    @Override
    public void start() {
        if (isRunning) return;

        countdown = 60;
        isRunning = true;
        LOGGER.info("The countdown is starting...");

        taskID = BUKKIT_SCHEDULER.scheduleSyncRepeatingTask(JAVA_PLUGIN, () -> {
            final Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();

            onlinePlayers.forEach(player -> player.setLevel(countdown));

            switch (countdown) {
                //Lobby phase mechanics.
                case 30, 10, 5, 3, 2, 1 -> onlinePlayers.forEach(player ->
                        player.sendPlainMessage(Message.PREFIX + getStartingCountdownMessage(countdown)));
                case 0 -> {
                    //Starting game mechanics.
                    onlinePlayers.forEach(player ->
                            player.sendPlainMessage(Message.PREFIX + "The game is starting now."));

                    GAME_ASSETS.setGameState(GameState.PROTECTION);
                    GAME_ASSETS.getONLINE_PLAYERS_ALIVE().addAll(onlinePlayers);

                    final List<String> gameMaps = MAP_MANAGER.getMapList();
                    Collections.shuffle(gameMaps);

                    final String selectedGameMapName = gameMaps.get(0);
                    final World selectedGameMap = MAP_MANAGER.loadMap(selectedGameMapName);

                    if (selectedGameMap == null) {
                        LOGGER.severe("The game map named \"" + selectedGameMapName + "\" couldn't be found.");
                        stop();
                        return;
                    }

                    final Location gameMapSpawn = MAP_MANAGER.getMapSpawnPoint(selectedGameMapName);

                    if (gameMapSpawn == null) {
                        LOGGER.severe("There isn't a configured spawn point for the map named \"" +
                                selectedGameMapName + "\"");
                        stop();
                        return;
                    }

                    final int deathHeight = MAP_MANAGER.getDeathHeight(selectedGameMapName);

                    GAME_ASSETS.setGameConfiguration(
                            new GameConfiguration(selectedGameMap, gameMapSpawn, deathHeight, System.currentTimeMillis())
                    );

                    onlinePlayers.forEach(player -> {
                        player.teleport(gameMapSpawn.toCenterLocation());
                        player.setGameMode(GameMode.ADVENTURE);
                    });

                    if (!PROTECTION_COUNTDOWN_SCHEDULER.isRunning()) {
                        PROTECTION_COUNTDOWN_SCHEDULER.start();
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
        LOGGER.info("The starting countdown was cancelled.");
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Gets the message of the current countdown.
     *
     * @param countdown The current countdown until the game starts.
     * @return The game countdown message which is going to send to the players.
     */
    public @NonNull String getStartingCountdownMessage(int countdown) {
        return "The game is starting in §c" + countdown + (countdown == 1 ? " second§7." : " seconds§7.");
    }
}
