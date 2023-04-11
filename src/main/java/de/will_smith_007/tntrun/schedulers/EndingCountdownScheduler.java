package de.will_smith_007.tntrun.schedulers;

import de.will_smith_007.tntrun.enums.Message;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.Collection;

public class EndingCountdownScheduler extends Scheduler {

    private int taskID, countdown;
    private boolean isRunning = false;
    private final BukkitScheduler BUKKIT_SCHEDULER = Bukkit.getScheduler();
    private final JavaPlugin JAVA_PLUGIN;

    public EndingCountdownScheduler(@NonNull JavaPlugin javaPlugin) {
        this.JAVA_PLUGIN = javaPlugin;
    }

    @Override
    public void start() {
        isRunning = true;
        countdown = 10;
        taskID = BUKKIT_SCHEDULER.scheduleSyncRepeatingTask(JAVA_PLUGIN, () -> {
            switch (countdown) {
                case 10, 5, 3, 2, 1 -> {
                    final Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();

                    onlinePlayers.forEach(player ->
                            player.sendPlainMessage(Message.PREFIX + getEndingCountdownMessage(countdown)));
                }
                case 0 -> Bukkit.getServer().shutdown();
            }
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

    /**
     * Gets the message of the current countdown.
     *
     * @param countdown The current countdown until the game ends.
     * @return The ending countdown message which is going to send to the players.
     */
    public @NonNull String getEndingCountdownMessage(int countdown) {
        return "The game is ending in §c" + countdown + (countdown == 1 ? " second§7." : " seconds§7.");
    }
}