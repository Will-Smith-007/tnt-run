package de.will_smith_007.tntrun.listeners;

import de.will_smith_007.tntrun.enums.Message;
import de.will_smith_007.tntrun.managers.MapManager;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashSet;
import java.util.List;

public class PlayerSetupDeathHeightListener implements Listener {

    private final HashSet<Player> PLAYERS_IN_DEATH_HEIGHT_SETUP;
    private final MapManager MAP_MANAGER;

    public PlayerSetupDeathHeightListener(@NonNull HashSet<Player> playersInDeathHeightSetup,
                                          @NonNull MapManager mapManager) {
        this.PLAYERS_IN_DEATH_HEIGHT_SETUP = playersInDeathHeightSetup;
        this.MAP_MANAGER = mapManager;
    }

    @EventHandler
    public void onBlockClick(@NonNull PlayerInteractEvent playerInteractEvent) {
        final Player player = playerInteractEvent.getPlayer();

        if (!PLAYERS_IN_DEATH_HEIGHT_SETUP.contains(player)) return;
        if (playerInteractEvent.getAction() != Action.LEFT_CLICK_BLOCK) return;

        final World world = player.getWorld();
        final String mapName = world.getName();
        final List<String> gameMapNames = MAP_MANAGER.getMapList();

        if (!gameMapNames.contains(mapName)) {
            player.sendPlainMessage(Message.PREFIX + "§cThe map were you are isn't a configured game map.");
            return;
        }

        final Block clickedBlock = playerInteractEvent.getClickedBlock();

        if (clickedBlock == null) return;

        if (clickedBlock.getType() != Material.TNT) {
            player.sendPlainMessage(Message.PREFIX + "§cThe clicked block for the death height setup needs to be a " +
                    "§eTNT block§c.");
            return;
        }

        final Location clickedBlockLocation = clickedBlock.getLocation();
        final int clickedBlockHeight = clickedBlockLocation.getBlockY();

        MAP_MANAGER.setDeathHeight(mapName, clickedBlockHeight);

        player.sendPlainMessage(Message.PREFIX + "§aYou've successfully set the death height for the game map §e" +
                "\"" + mapName + "\"§a to §e" + clickedBlockHeight);

        PLAYERS_IN_DEATH_HEIGHT_SETUP.remove(player);
        playerInteractEvent.setCancelled(true);
    }
}
