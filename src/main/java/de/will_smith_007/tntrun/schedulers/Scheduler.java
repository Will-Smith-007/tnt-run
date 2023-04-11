package de.will_smith_007.tntrun.schedulers;

/**
 * This abstract class must be inherited if a Bukkit scheduler must be created.
 * <br> <br>
 * Holds basic methods for scheduler creation such as "start", "stop" and "isRunning".
 */
public abstract class Scheduler {

    public abstract void start();

    public abstract void stop();

    public abstract boolean isRunning();
}
