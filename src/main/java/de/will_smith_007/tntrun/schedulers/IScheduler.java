package de.will_smith_007.tntrun.schedulers;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitScheduler;

/**
 * This interface must be inherited if a Bukkit scheduler must be created.
 * <br> <br>
 * Holds basic methods for scheduler creation such as "start", "stop" and "isRunning".
 */
public interface IScheduler {

    BukkitScheduler BUKKIT_SCHEDULER = Bukkit.getScheduler();

    void start();

    void stop();

    boolean isRunning();
}
