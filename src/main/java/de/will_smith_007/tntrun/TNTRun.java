package de.will_smith_007.tntrun;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import de.will_smith_007.tntrun.commands.StartCommand;
import de.will_smith_007.tntrun.commands.StatsCommand;
import de.will_smith_007.tntrun.commands.TNTRunCommand;
import de.will_smith_007.tntrun.dependency_injection.InjectionModule;
import de.will_smith_007.tntrun.listeners.CancelListener;
import de.will_smith_007.tntrun.listeners.PlayerConnectionListener;
import de.will_smith_007.tntrun.listeners.PlayerMoveListener;
import de.will_smith_007.tntrun.listeners.PlayerSetupDeathHeightListener;
import de.will_smith_007.tntrun.managers.MapManager;
import de.will_smith_007.tntrun.mysql.MySQL;
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

    private final Logger logger = getLogger();
    private MySQL statsSQL;

    @Override
    public void onEnable() {
        final Injector injector = Guice.createInjector(new InjectionModule(this));

        statsSQL = injector.getInstance(MySQL.class);

        //Command registration
        registerCommand("tntrun", injector.getInstance(TNTRunCommand.class));
        registerCommand("start", injector.getInstance(StartCommand.class));
        registerCommand("stats", injector.getInstance(StatsCommand.class));

        //Listener registration
        registerListeners(
                injector.getInstance(PlayerConnectionListener.class),
                injector.getInstance(PlayerSetupDeathHeightListener.class),
                injector.getInstance(PlayerMoveListener.class),
                new CancelListener()
        );

        logger.info("TNT-Run was started.");
    }

    @Override
    public void onDisable() {
        statsSQL.closeConnection();
        logger.info("TNT-Run was stopped.");
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
    @Inject
    private void loadWaitingMap(@NonNull MapManager mapManager) {
        //Map information and waiting map loading.
        final String waitingMap = mapManager.getWaitingMapName();
        final List<String> gameMaps = mapManager.getMapList();

        if (waitingMap == null) {
            logger.warning("There is currently no configured waiting map.");
        } else {
            final World world = mapManager.loadMap(waitingMap);
            logger.info("The waiting map named \"" + waitingMap + "\" " +
                    (world == null ? "couldn't be loaded." : "was successfully loaded."));
        }

        if (gameMaps.isEmpty()) {
            logger.warning("There is currently no configured game map.");
        }
    }
}
