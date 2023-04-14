package de.will_smith_007.tntrun;

import de.will_smith_007.tntrun.commands.StartCommand;
import de.will_smith_007.tntrun.commands.StatsCommand;
import de.will_smith_007.tntrun.commands.TNTRunCommand;
import de.will_smith_007.tntrun.listeners.CancelListener;
import de.will_smith_007.tntrun.listeners.PlayerConnectionListener;
import de.will_smith_007.tntrun.listeners.PlayerMoveListener;
import de.will_smith_007.tntrun.listeners.PlayerSetupDeathHeightListener;
import de.will_smith_007.tntrun.managers.DatabaseFileManager;
import de.will_smith_007.tntrun.managers.GameManager;
import de.will_smith_007.tntrun.managers.MapManager;
import de.will_smith_007.tntrun.managers.StatsManager;
import de.will_smith_007.tntrun.mysql.MySQL;
import de.will_smith_007.tntrun.schedulers.EndingCountdownScheduler;
import de.will_smith_007.tntrun.schedulers.LobbyCountdownScheduler;
import de.will_smith_007.tntrun.schedulers.PlayerAFKScannerScheduler;
import de.will_smith_007.tntrun.schedulers.ProtectionCountdownScheduler;
import de.will_smith_007.tntrun.utilities.GameAssets;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.logging.Logger;

public class TNTRun extends JavaPlugin {

    private final Logger LOGGER = getLogger();
    private MySQL statsSQL;

    @Override
    public void onEnable() {

        //Dependencies
        final GameAssets gameAssets = new GameAssets();

        //Injection
        final MapManager mapManager = new MapManager(this);
        final DatabaseFileManager databaseFileManager = new DatabaseFileManager(this);

        //Database initialization
        statsSQL = null;
        if (databaseFileManager.isDatabaseEnabled()) {
            statsSQL = new MySQL(
                    databaseFileManager,
                    LOGGER
            );

            statsSQL.update("CREATE TABLE IF NOT EXISTS tntrun(uuid VARCHAR(64) PRIMARY KEY, " +
                    "wins INT(11) DEFAULT 0, loses INT(11) DEFAULT 0, longestSurvivedTime BIGINT(20) DEFAULT 0);");
        }

        //Injection
        final StatsManager statsManager = new StatsManager(
                databaseFileManager,
                statsSQL
        );

        final PlayerAFKScannerScheduler playerAFKScannerScheduler = new PlayerAFKScannerScheduler(
                this,
                gameAssets
        );

        final ProtectionCountdownScheduler protectionCountdownScheduler = new ProtectionCountdownScheduler(
                this,
                gameAssets,
                playerAFKScannerScheduler
        );

        final LobbyCountdownScheduler lobbyCountdownScheduler = new LobbyCountdownScheduler(
                this,
                gameAssets,
                mapManager,
                protectionCountdownScheduler
        );

        final EndingCountdownScheduler endingCountdownScheduler = new EndingCountdownScheduler(this);

        final GameManager gameManager = new GameManager(lobbyCountdownScheduler);

        //Initializing commands
        final TNTRunCommand tntRunCommand = new TNTRunCommand(mapManager);
        final StartCommand startCommand = new StartCommand(gameAssets, gameManager);
        final StatsCommand statsCommand = new StatsCommand(statsManager);

        //Command registration
        registerCommand("tntrun", tntRunCommand);
        registerCommand("start", startCommand);
        registerCommand("stats", statsCommand);

        //Initializing listeners
        final PlayerConnectionListener playerConnectionListener = new PlayerConnectionListener(
                gameAssets,
                gameManager,
                mapManager
        );

        final PlayerSetupDeathHeightListener playerSetupDeathHeightListener = new PlayerSetupDeathHeightListener(
                tntRunCommand.getPLAYERS_IN_DEATH_HEIGHT_SETUP(),
                mapManager
        );

        final PlayerMoveListener playerMoveListener = new PlayerMoveListener(
                this,
                gameAssets,
                endingCountdownScheduler,
                statsManager
        );

        final CancelListener cancelListener = new CancelListener();

        //Listener registration
        registerListeners(
                playerConnectionListener,
                playerSetupDeathHeightListener,
                playerMoveListener,
                cancelListener
        );

        //Loads the waiting map
        loadWaitingMap(mapManager);

        LOGGER.info("TNT-Run was started.");
    }

    @Override
    public void onDisable() {
        statsSQL.closeConnection();
        LOGGER.info("TNT-Run was stopped.");
    }

    /**
     * Registers all specified listeners from this listener Array automatically.
     *
     * @param listeners Listeners which should be registered.
     */
    private void registerListeners(Listener @NonNull ... listeners) {
        final PluginManager pluginManager = Bukkit.getPluginManager();
        for (Listener listener : listeners) {
            pluginManager.registerEvents(listener, this);
        }
    }

    /**
     * Registers the specified command.
     *
     * @param command         Command which can be typed ingame.
     * @param commandExecutor Class of the command which implements the {@link CommandExecutor} interface.
     */
    private void registerCommand(@NonNull String command, @NonNull CommandExecutor commandExecutor) {
        final PluginCommand pluginCommand = getCommand(command);
        if (pluginCommand == null) return;
        pluginCommand.setExecutor(commandExecutor);
    }

    /**
     * Loads the configured waiting map and prints a warning if there isn't any
     * configured game map.
     *
     * @param mapManager {@link MapManager} to get map information.
     */
    private void loadWaitingMap(@NonNull MapManager mapManager) {
        //Map information and waiting map loading.
        final String waitingMap = mapManager.getWaitingMapName();
        final List<String> gameMaps = mapManager.getMapList();

        if (waitingMap == null) {
            LOGGER.warning("There is currently no configured waiting map.");
        } else {
            final World world = mapManager.loadMap(waitingMap);
            LOGGER.info("The waiting map named \"" + waitingMap + "\" " +
                    (world == null ? "couldn't be loaded." : "was successfully loaded."));
        }

        if (gameMaps.isEmpty()) {
            LOGGER.warning("There is currently no configured game map.");
        }
    }
}
