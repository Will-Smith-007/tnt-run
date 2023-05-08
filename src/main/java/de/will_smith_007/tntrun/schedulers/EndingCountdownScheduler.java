package de.will_smith_007.tntrun.schedulers;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.will_smith_007.tntrun.enums.Message;
import de.will_smith_007.tntrun.schedulers.interfaces.ICountdownOptions;
import de.will_smith_007.tntrun.schedulers.interfaces.IScheduler;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;

@Singleton
public final class EndingCountdownScheduler implements IScheduler, ICountdownOptions {

    private int taskID, countdown;
    private boolean isRunning = false;
    private final JavaPlugin javaPlugin;

    @Inject
    public EndingCountdownScheduler(@NonNull JavaPlugin javaPlugin) {
        this.javaPlugin = javaPlugin;
    }

    @Override
    public void start() {
        if (isRunning) return;

        isRunning = true;
        countdown = 10;
        taskID = BUKKIT_SCHEDULER.scheduleSyncRepeatingTask(javaPlugin, () -> {
            switch (countdown) {
                case 10, 5, 3, 2, 1 -> {
                    final Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();

                    onlinePlayers.forEach(player -> {
                        player.sendPlainMessage(Message.PREFIX + getCountdownMessage(countdown));
                        playCountdownSound(player);
                    });
                }
                case 0 -> Bukkit.getServer().shutdown();
            }
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
     * @param currentCountdown The current countdown until the game ends.
     * @return The ending countdown message which is going to send to the players.
     */
    @Override
    public @NonNull String getCountdownMessage(int currentCountdown) {
        return "The game is ending in §c" + countdown + (countdown == 1 ? " second§7." : " seconds§7.");
    }

    @Override
    public void playCountdownSound(@NonNull Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
    }
}
