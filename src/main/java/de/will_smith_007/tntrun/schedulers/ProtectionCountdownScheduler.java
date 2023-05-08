package de.will_smith_007.tntrun.schedulers;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.will_smith_007.tntrun.enums.GameState;
import de.will_smith_007.tntrun.enums.Message;
import de.will_smith_007.tntrun.schedulers.interfaces.ICountdownOptions;
import de.will_smith_007.tntrun.schedulers.interfaces.IScheduler;
import de.will_smith_007.tntrun.utilities.GameAssets;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;

@Singleton
public final class ProtectionCountdownScheduler implements IScheduler, ICountdownOptions {

    private int taskID, countdown;
    private boolean isRunning;
    private final JavaPlugin javaPlugin;
    private final GameAssets gameAssets;
    private final PlayerAFKRemoverScheduler playerAFKRemoverScheduler;

    @Inject
    public ProtectionCountdownScheduler(@NonNull JavaPlugin javaPlugin,
                                        @NonNull GameAssets gameAssets,
                                        @NonNull PlayerAFKRemoverScheduler playerAFKRemoverScheduler) {
        this.javaPlugin = javaPlugin;
        this.gameAssets = gameAssets;
        this.playerAFKRemoverScheduler = playerAFKRemoverScheduler;
    }

    @Override
    public void start() {
        if (isRunning) return;

        countdown = 5;
        isRunning = true;
        taskID = BUKKIT_SCHEDULER.scheduleSyncRepeatingTask(javaPlugin, () -> {

            final Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();

            if (countdown == 0) {
                gameAssets.setGameState(GameState.INGAME);
                playerAFKRemoverScheduler.start();
                stop();
                return;
            }

            onlinePlayers.forEach(player -> {
                player.sendPlainMessage(Message.PREFIX + getCountdownMessage(countdown));
                playCountdownSound(player);
            });

            countdown--;
        }, 0L, 20L);
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
     * Gets the message of the current countdown.
     *
     * @param currentCountdown The current countdown until the game starts.
     * @return The game countdown message which is going to send to the players.
     */
    @Override
    public @NonNull String getCountdownMessage(int currentCountdown) {
        return "Protection ends in §c" + countdown + (countdown == 1 ? " second§7." : " seconds§7.");
    }

    @Override
    public void playCountdownSound(@NonNull Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
    }
}
