package de.will_smith_007.tntrun.managers;

import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

/**
 * The {@link MapManager} is used to save important information about game maps and the waiting lobby map in a file.
 * These data is e.g. the spawn locations for game maps and the waiting lobby map and a general list of game maps.
 * Also holds the name of the configured waiting lobby map.
 */
public class MapManager {

    private final Logger LOGGER;
    private final File MAP_CONFIG;
    private final YamlConfiguration YAML_CONFIGURATION;

    /**
     * Creates the "MapConfig.yml" file in the directory of this plugin.
     *
     * @param javaPlugin Java plugin which contains required information such as the directory path of
     *                   this plugin or the configured {@link Logger}
     */
    public MapManager(@NonNull JavaPlugin javaPlugin) {
        this.LOGGER = javaPlugin.getLogger();

        final File mapConfigDirectory = new File(javaPlugin.getDataFolder().getPath());
        final String configName = "MapConfig.yml";
        this.MAP_CONFIG = new File(mapConfigDirectory + "/" + configName);

        if (mapConfigDirectory.mkdirs()) {
            LOGGER.info("World configuration directory was created.");
        }

        if (!MAP_CONFIG.exists()) {
            try {
                if (MAP_CONFIG.createNewFile()) {
                    LOGGER.info("World configuration file was created.");
                }
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }

        YAML_CONFIGURATION = YamlConfiguration.loadConfiguration(MAP_CONFIG);
    }

    /**
     * Gets a list of configured and added game maps.
     *
     * @return The list of configured game maps. Is empty if there isn't any configured game map yet.
     */
    public @NonNull List<String> getMapList() {
        return YAML_CONFIGURATION.getStringList("Maps");
    }

    /**
     * Gets the name of the waiting lobby map.
     *
     * @return The name of the configured waiting lobby map.
     * Returns null if there isn't a configured lobby map.
     */
    public String getWaitingMapName() {
        return YAML_CONFIGURATION.getString("WaitingMap");
    }

    /**
     * Automatically loads a configured map with the specified name and returns it.
     *
     * @param mapName Name of map which should be loaded.
     * @return The loaded map. If there wasn't found a map directory with this name,
     * a new map will be created with this name and can be found in the main server directory.
     */
    public World loadMap(@NonNull String mapName) {
        return Bukkit.createWorld(new WorldCreator(mapName));
    }

    /**
     * Gets the spawn location of a configured map. This can be a game map or the waiting lobby map.
     *
     * @param mapName Name of map from which the location should be searched.
     * @return The location of the configured map.
     * Returns null if there couldn't be found a valid spawn location configuration for this map.
     */
    public Location getMapSpawnPoint(@NonNull String mapName) {
        final World world = Bukkit.getWorld(mapName);

        if (world == null) return null;

        final double x = YAML_CONFIGURATION.getDouble(mapName + ".X");
        final double y = YAML_CONFIGURATION.getDouble(mapName + ".Y");
        final double z = YAML_CONFIGURATION.getDouble(mapName + ".Z");
        final float yaw = (float) YAML_CONFIGURATION.getDouble(mapName + ".Yaw");
        final float pitch = (float) YAML_CONFIGURATION.getDouble(mapName + ".Pitch");

        return new Location(world, x, y, z, yaw, pitch);
    }

    /**
     * Gets the death height of a game map.
     *
     * @param mapName Name of map from which the death height should be returned.
     * @return The death height of the configured game map.
     * Returns 0 if there couldn't be found a valid death height configuration for this map.
     */
    public int getDeathHeight(@NonNull String mapName) {
        return YAML_CONFIGURATION.getInt(mapName + ".DeathHeight");
    }

    /**
     * Adds a game map to the game map configuration pool.
     *
     * @param mapName Name of game map which should be added to the pool.
     */
    public void addMap(@NonNull String mapName) {
        final List<String> mapList = getMapList();

        if (mapList.contains(mapName)) return;
        mapList.add(mapName);

        YAML_CONFIGURATION.set("Maps", mapList);

        saveMapConfiguration();

        LOGGER.info("The map named " + mapName + " was added.");
    }

    /**
     * Removes a game map from the game map configuration pool.
     *
     * @param mapName Name of map which should be removed from the pool.
     */
    public void removeMap(@NonNull String mapName) {
        final List<String> mapList = getMapList();

        if (!mapList.contains(mapName)) return;
        mapList.remove(mapName);

        YAML_CONFIGURATION.set("Maps", mapList);

        saveMapConfiguration();

        LOGGER.info("The map named " + mapName + " was removed.");
    }

    /**
     * Sets the name of waiting lobby map.
     *
     * @param mapName Name of waiting lobby map which should be set.
     */
    public void setWaitingMap(@NonNull String mapName) {
        YAML_CONFIGURATION.set("WaitingMap", mapName);

        saveMapConfiguration();

        LOGGER.info("Waiting map was set to " + mapName);
    }

    /**
     * Sets a map spawn location in the map configuration file.
     *
     * @param mapName       Name of map where the location should be set.
     * @param spawnLocation Location for the map spawn which should be set.
     */
    public void setMapSpawnPoint(@NonNull String mapName, @NonNull Location spawnLocation) {
        YAML_CONFIGURATION.set(mapName + ".X", spawnLocation.getX());
        YAML_CONFIGURATION.set(mapName + ".Y", spawnLocation.getY());
        YAML_CONFIGURATION.set(mapName + ".Z", spawnLocation.getZ());
        YAML_CONFIGURATION.set(mapName + ".Yaw", spawnLocation.getYaw());
        YAML_CONFIGURATION.set(mapName + ".Pitch", spawnLocation.getPitch());

        saveMapConfiguration();

        LOGGER.info("The map spawn point for \"" + mapName + "\" was set.");
    }

    /**
     * Sets the death height of a configured game map.
     *
     * @param mapName     Name of map on which the death height should be set.
     * @param deathHeight Death height of the game map which should be set.
     */
    public void setDeathHeight(@NonNull String mapName, int deathHeight) {
        YAML_CONFIGURATION.set(mapName + ".DeathHeight", deathHeight);

        saveMapConfiguration();

        LOGGER.info("The death height for \"" + mapName + "\" was set to " + deathHeight);
    }

    /**
     * Saves the map configuration file and is only used if all set operations were made.
     */
    private void saveMapConfiguration() {
        try {
            YAML_CONFIGURATION.save(MAP_CONFIG);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
}
