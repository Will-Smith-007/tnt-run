package de.will_smith_007.tntrun.commands;

import de.will_smith_007.tntrun.enums.Message;
import de.will_smith_007.tntrun.managers.MapManager;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class TNTRunCommand implements TabExecutor {

    private final MapManager MAP_MANAGER;
    @Getter
    private final HashSet<Player> PLAYERS_IN_DEATH_HEIGHT_SETUP = new HashSet<>();

    public TNTRunCommand(@NonNull MapManager mapManager) {
        this.MAP_MANAGER = mapManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("tntrun.setup")) {
            sender.sendPlainMessage(Message.PREFIX + "§cYou don't have enough permissions to execute this command.");
            return true;
        }

        if (!(sender instanceof final Player player)) {
            sender.sendPlainMessage(Message.PREFIX + "§cYou need to be a player to execute this command.");
            return true;
        }

        if (args.length == 1) {
            final String subCommand = args[0];
            if (subCommand.equalsIgnoreCase("help")) {
                sendHelpDescription(player);
            } else if (subCommand.equalsIgnoreCase("setGameSpawn")) {
                final World world = player.getWorld();
                final String mapName = world.getName();
                final List<String> gameMapNames = MAP_MANAGER.getMapList();

                if (!gameMapNames.contains(mapName)) {
                    player.sendPlainMessage(Message.PREFIX + "§cThe map were you are isn't a configured game map.");
                    return true;
                }

                final Location playerLocation = player.getLocation();

                MAP_MANAGER.setMapSpawnPoint(mapName, playerLocation);

                player.sendPlainMessage(Message.PREFIX + "§aYou've set the game spawn point for the game map named §e" +
                        "\"" + mapName + "\"");
            } else if (subCommand.equalsIgnoreCase("setWaitSpawn")) {
                final World world = player.getWorld();
                final String mapName = world.getName();
                final String configuredWaitingMapName = MAP_MANAGER.getWaitingMapName();

                if (configuredWaitingMapName == null || !configuredWaitingMapName.equals(mapName)) {
                    player.sendPlainMessage(Message.PREFIX + "§cThe map were you are isn't the configured waiting map.");
                    return true;
                }

                final Location playerLocation = player.getLocation();

                MAP_MANAGER.setMapSpawnPoint(mapName, playerLocation);

                player.sendPlainMessage(Message.PREFIX + "§aYou've set the waiting map spawn point for §e\"" +
                        mapName + "\"");
            } else if (subCommand.equalsIgnoreCase("setDeathHeight")) {
                if (PLAYERS_IN_DEATH_HEIGHT_SETUP.contains(player)) {
                    PLAYERS_IN_DEATH_HEIGHT_SETUP.remove(player);

                    player.sendPlainMessage(Message.PREFIX + "§cYou can no longer break a random §eTNT block §c" +
                            "to set the death height.");
                } else {
                    PLAYERS_IN_DEATH_HEIGHT_SETUP.add(player);
                    player.sendPlainMessage(Message.PREFIX + "§aYou can now break a random §eTNT block §aon the last " +
                            "level to set the death height for this map.");
                }
            }
        } else if (args.length == 2) {
            final String subCommand = args[0];
            final String mapName = args[1];
            if (subCommand.equalsIgnoreCase("addMap")) {
                MAP_MANAGER.addMap(mapName);
                player.sendPlainMessage(Message.PREFIX + "§aYou added the map §e\"" + mapName + "\"§a to the game map pool.");
            } else if (subCommand.equalsIgnoreCase("removeMap")) {
                MAP_MANAGER.removeMap(mapName);
                player.sendPlainMessage(Message.PREFIX + "§aYou removed the map §e\"" + mapName + "\"§a from the game map pool.");
            } else if (subCommand.equalsIgnoreCase("setWaitMap")) {
                final World waitingMap = MAP_MANAGER.loadMap(mapName);

                if (waitingMap == null) {
                    player.sendPlainMessage(Message.PREFIX + "§cThe waiting map named §e\"" + mapName + "\"§c couldn't be found.");
                    return true;
                }

                MAP_MANAGER.setWaitingMap(mapName);

                player.sendPlainMessage(Message.PREFIX + "§aYou've set the waiting map to §e\"" + mapName + "\"");
            } else if (subCommand.equalsIgnoreCase("load")) {
                final World world = MAP_MANAGER.loadMap(mapName);

                if (world == null) {
                    player.sendPlainMessage(Message.PREFIX + "§cThe map named §e\"" + mapName + "\"§c couldn't be found.");
                    return true;
                }

                player.sendPlainMessage(Message.PREFIX + "§aThe map named §e\"" + mapName + "\"§a was successfully loaded.");
            } else if (subCommand.equalsIgnoreCase("tp")) {
                final World world = MAP_MANAGER.loadMap(mapName);

                if (world == null) {
                    player.sendPlainMessage(Message.PREFIX + "§cThe map named §e\"" + mapName + "\"§c couldn't be found.");
                    return true;
                }

                final Location configuredMapLocation = MAP_MANAGER.getMapSpawnPoint(mapName);
                final Location mapSpawnLocation =
                        (configuredMapLocation == null ? world.getSpawnLocation() : configuredMapLocation);

                player.teleport(mapSpawnLocation);
                player.sendPlainMessage(Message.PREFIX + "§aYou've been teleported to the map named §e\"" +
                        mapName + "\"");
            } else {
                sendHelpDescription(player);
            }
        } else {
            sendHelpDescription(player);
        }
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("tntrun.setup")) return null;
        if (args.length == 1) {
            return Arrays.asList("setGameSpawn", "setWaitSpawn", "setDeathHeight", "addMap",
                    "removeMap", "setWaitMap", "load", "tp");
        } else if (args.length == 2) {
            return List.of("MapName");
        }
        return null;
    }

    /**
     * Sends the help description of this command to the specified command sender.
     *
     * @param commandSender Command sender to which the help description should be sent.
     */
    private void sendHelpDescription(@NonNull CommandSender commandSender) {
        commandSender.sendMessage("",
                Message.PREFIX + "§e/tntrun setGameSpawn §8- §7Sets the spawn of a game map",
                Message.PREFIX + "§e/tntrun setWaitSpawn §8- §7Sets the spawn of the waiting map",
                Message.PREFIX + "§e/tntrun setDeathHeight §8- §7Setup the death height of a game map",
                Message.PREFIX + "§e/tntrun addMap [MapName] §8- §7Adds a map to the game map pool",
                Message.PREFIX + "§e/tntrun removeMap [MapName] §8- §7Removes a map from the game map pool",
                Message.PREFIX + "§e/tntrun setWaitMap [MapName] §8- §7Sets the global waiting map",
                Message.PREFIX + "§e/tntrun load [MapName] §8- §7Loads the map",
                Message.PREFIX + "§e/tntrun tp [MapName] §8- §7Teleports you into the map",
                "");
    }
}
