package de.will_smith_007.tntrun.schedulers;

/**
 * This abstract class must be inherited if a Bukkit scheduler must be created.
 * <br> <br>
 * Holds basic methods for scheduler creation such as "start", "stop" and "isRunning".
 */
public interface IScheduler {

    void start();

    void stop();

    boolean isRunning();
}
