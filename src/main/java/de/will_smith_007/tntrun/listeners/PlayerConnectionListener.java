package de.will_smith_007.tntrun.listeners;

import de.will_smith_007.tntrun.enums.GameState;
import de.will_smith_007.tntrun.enums.Message;
import de.will_smith_007.tntrun.game_config.GameConfiguration;
import de.will_smith_007.tntrun.utilities.GameAssets;
import de.will_smith_007.tntrun.managers.GameManager;
import de.will_smith_007.tntrun.managers.MapManager;
import lombok.NonNull;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class PlayerConnectionListener implements Listener {

    private final GameAssets GAME_ASSETS;
    private final GameManager GAME_MANAGER;
    private final MapManager MAP_MANAGER;

    private Team playerTeam;

    public PlayerConnectionListener(@NonNull GameAssets gameAssets,
                                    @NonNull GameManager gameManager,
                                    @NonNull MapManager mapManager) {
        this.GAME_ASSETS = gameAssets;
        this.GAME_MANAGER = gameManager;
        this.MAP_MANAGER = mapManager;

        final Scoreboard mainScoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        if ((playerTeam = mainScoreboard.getTeam("players")) == null) {
            playerTeam = mainScoreboard.registerNewTeam("players");
        }
        this.playerTeam.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
    }

    @EventHandler
    public void onPlayerJoin(@NonNull PlayerJoinEvent playerJoinEvent) {
        final Player player = playerJoinEvent.getPlayer();

        playerTeam.addEntry(player.getName());

        if (GAME_ASSETS.getGameState() == GameState.LOBBY) {
            playerJoinEvent.joinMessage(Component.text(
                    Message.PREFIX + "§e" + player.getName() + " §7joined the game!"
            ));

            final String waitingMapName = MAP_MANAGER.getWaitingMapName();

            if (waitingMapName == null) return;

            final Location waitingMapSpawn = MAP_MANAGER.getMapSpawnPoint(waitingMapName);

            if (waitingMapSpawn == null) return;

            player.teleport(waitingMapSpawn);

            GAME_MANAGER.startCountdownIfEnoughPlayers();
        } else {
            playerJoinEvent.joinMessage(null);
            player.setGameMode(GameMode.SPECTATOR);

            final GameConfiguration gameConfiguration = GAME_ASSETS.getGameConfiguration();
            final Location gameSpawnLocation = gameConfiguration.gameSpawnLocation();

            player.teleport(gameSpawnLocation);
        }
    }

    @EventHandler
    public void onPlayerQuit(@NonNull PlayerQuitEvent playerQuitEvent) {
        final Player player = playerQuitEvent.getPlayer();

        final GameState currentGameState = GAME_ASSETS.getGameState();

        switch (currentGameState) {
            case LOBBY -> {
                playerQuitEvent.quitMessage(Component.text(
                        Message.PREFIX + "§e" + player.getName() + " §7left the game."
                ));
                //This event is called before player removing from the online player collection.
                GAME_MANAGER.cancelCountdownIfNotEnoughPlayers((Bukkit.getOnlinePlayers().size() - 1));
            }
            case INGAME, PROTECTION -> {
                playerQuitEvent.quitMessage(null);
                GAME_ASSETS.getONLINE_PLAYERS_ALIVE().remove(player);
            }
            case ENDING -> playerQuitEvent.quitMessage(null);
        }
    }
}
