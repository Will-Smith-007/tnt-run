package de.will_smith_007.tntrun.schedulers;

import de.will_smith_007.tntrun.enums.GameState;
import de.will_smith_007.tntrun.enums.Message;
import de.will_smith_007.tntrun.managers.GameAssets;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.Collection;

public class ProtectionCountdownScheduler extends Scheduler {

    private int taskID, countdown;
    private boolean isRunning;
    private final BukkitScheduler BUKKIT_SCHEDULER = Bukkit.getScheduler();
    private final JavaPlugin JAVA_PLUGIN;
    private final GameAssets GAME_ASSETS;
    private final PlayerAFKScannerScheduler PLAYER_AFK_SCANNER_SCHEDULER;

    public ProtectionCountdownScheduler(@NonNull JavaPlugin javaPlugin,
                                        @NonNull GameAssets gameAssets,
                                        @NonNull PlayerAFKScannerScheduler playerAFKScannerScheduler) {
        this.JAVA_PLUGIN = javaPlugin;
        this.GAME_ASSETS = gameAssets;
        this.PLAYER_AFK_SCANNER_SCHEDULER = playerAFKScannerScheduler;
    }

    @Override
    public void start() {
        countdown = 5;
        isRunning = true;
        taskID = BUKKIT_SCHEDULER.scheduleSyncRepeatingTask(JAVA_PLUGIN, () -> {

            final Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();

            if (countdown == 0) {
                GAME_ASSETS.setGameState(GameState.INGAME);
                PLAYER_AFK_SCANNER_SCHEDULER.start();
                stop();
                return;
            }

            onlinePlayers.forEach(player ->
                    player.sendPlainMessage(Message.PREFIX + "Protection ends in §c" + countdown +
                            (countdown == 1 ? " second§7." : " seconds§7.") ));

            countdown--;
        }, 0L, 20L);
    }

    @Override
    public void stop() {
        isRunning = false;
        BUKKIT_SCHEDULER.cancelTask(taskID);
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }
}
