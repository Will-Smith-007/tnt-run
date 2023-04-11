package de.will_smith_007.tntrun.managers;

import lombok.NonNull;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * The {@link DatabaseFileManager} is used to create a file in which the server administrator can configure the
 * data which is required to connect to a MySQL or MariaDB database.
 */
public class DatabaseFileManager {

    private final File DATABASE_CONFIG;
    private final YamlConfiguration YAML_CONFIGURATION;

    /**
     * Creates the "DatabaseConfig.yml" file in the directory of this plugin.
     * <br> <br>
     * This also sets the default configuration of the file if it's the first time created or some configuration
     * fields are missing but with disabled database option.
     *
     * @param javaPlugin Java plugin which contains required information such as the directory path of
     *                   this plugin or the configured {@link Logger}
     */
    public DatabaseFileManager(@NonNull JavaPlugin javaPlugin) {
        final Logger logger = javaPlugin.getLogger();

        final File databaseConfigDirectory = new File(javaPlugin.getDataFolder().getPath());
        final String configName = "DatabaseConfig.yml";
        this.DATABASE_CONFIG = new File(databaseConfigDirectory + "/" + configName);

        if (databaseConfigDirectory.mkdirs()) {
            logger.info("Database configuration directory was created.");
        }

        if (!DATABASE_CONFIG.exists()) {
            try {
                if (DATABASE_CONFIG.createNewFile()) {
                    logger.info("Database configuration file was created.");
                }
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }

        YAML_CONFIGURATION = YamlConfiguration.loadConfiguration(DATABASE_CONFIG);

        setConfigDefaults();
    }

    /**
     * Checks if the database option is enabled.
     *
     * @return True if the database is enabled and a connection should be established.
     */
    public boolean isDatabaseEnabled() {
        return YAML_CONFIGURATION.getBoolean("Enabled");
    }

    /**
     * Gets the host address from the database configuration.
     *
     * @return The host address in a String format.
     */
    public String getHostAddress() {
        return YAML_CONFIGURATION.getString("HostAddress");
    }

    /**
     * Gets the port from the database configuration.
     *
     * @return The port as an Integer.
     */
    public int getPort() {
        return YAML_CONFIGURATION.getInt("Port");
    }

    /**
     * Gets the username from the database configuration.
     *
     * @return The username which the database should be using.
     */
    public String getDatabaseUsername() {
        return YAML_CONFIGURATION.getString("Username");
    }

    /**
     * Gets the name of database from the database configuration.
     *
     * @return The name of the database which should be used to store the statistics data.
     */
    public String getDatabaseName() {
        return YAML_CONFIGURATION.getString("DatabaseName");
    }

    /**
     * Gets the password from the database configuration.
     *
     * @return The password for the configured username which is required to establish a connection.
     */
    public String getSecret() {
        return YAML_CONFIGURATION.getString("Secret");
    }

    /**
     * Sets the default configuration for a database connection if the configuration fields aren't set and
     * saves the file after.
     */
    private void setConfigDefaults() {
        if (YAML_CONFIGURATION.get("Enabled") == null) {
            YAML_CONFIGURATION.set("Enabled", false);
        }

        if (getHostAddress() == null) {
            YAML_CONFIGURATION.set("HostAddress", "127.0.0.1");
        }

        if (YAML_CONFIGURATION.get("Port") == null) {
            YAML_CONFIGURATION.set("Port", 3306);
        }

        if (getDatabaseUsername() == null) {
            YAML_CONFIGURATION.set("Username", "development");
        }

        if (getDatabaseName() == null) {
            YAML_CONFIGURATION.set("DatabaseName", "tntrun");
        }

        if (getSecret() == null) {
            YAML_CONFIGURATION.set("Secret", "1234");
        }

        saveDatabaseConfiguration();
    }

    /**
     * Saves the database configuration file and is only used if all set operations were made.
     */
    private void saveDatabaseConfiguration() {
        try {
            YAML_CONFIGURATION.save(DATABASE_CONFIG);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
}
