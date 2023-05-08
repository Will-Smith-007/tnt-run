package de.will_smith_007.tntrun.listeners;

import com.google.inject.Inject;
import de.will_smith_007.tntrun.enums.GameState;
import de.will_smith_007.tntrun.enums.Message;
import de.will_smith_007.tntrun.game_config.GameConfiguration;
import de.will_smith_007.tntrun.managers.GameManager;
import de.will_smith_007.tntrun.managers.MapManager;
import de.will_smith_007.tntrun.utilities.GameAssets;
import lombok.NonNull;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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

    private final GameAssets gameAssets;
    private final GameManager gameManager;
    private final MapManager mapManager;

    private Team playerTeam;

    @Inject
    public PlayerConnectionListener(@NonNull GameAssets gameAssets,
                                    @NonNull GameManager gameManager,
                                    @NonNull MapManager mapManager) {
        this.gameAssets = gameAssets;
        this.gameManager = gameManager;
        this.mapManager = mapManager;

        final Scoreboard mainScoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        if ((playerTeam = mainScoreboard.getTeam("players")) == null) {
            playerTeam = mainScoreboard.registerNewTeam("players");
        }
        playerTeam.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        playerTeam.color(NamedTextColor.GRAY);
    }

    @EventHandler
    public void onPlayerJoin(@NonNull PlayerJoinEvent playerJoinEvent) {
        final Player player = playerJoinEvent.getPlayer();

        playerTeam.addEntry(player.getName());

        if (gameAssets.getGameState() == GameState.LOBBY) {
            player.setGameMode(GameMode.ADVENTURE);

            playerJoinEvent.joinMessage(Component.text(
                    Message.PREFIX + "§e" + player.getName() + " §7joined the game!"
            ));

            final String waitingMapName = mapManager.getWaitingMapName();

            if (waitingMapName == null) return;

            final Location waitingMapSpawn = mapManager.getMapSpawnPoint(waitingMapName);

            if (waitingMapSpawn == null) return;

            player.teleport(waitingMapSpawn);

            gameManager.startCountdownIfEnoughPlayers();
        } else {
            playerJoinEvent.joinMessage(null);
            player.setGameMode(GameMode.SPECTATOR);

            final GameConfiguration gameConfiguration = gameAssets.getGameConfiguration();
            final Location gameSpawnLocation = gameConfiguration.gameSpawnLocation();

            player.teleport(gameSpawnLocation);
        }
    }

    @EventHandler
    public void onPlayerQuit(@NonNull PlayerQuitEvent playerQuitEvent) {
        final Player player = playerQuitEvent.getPlayer();

        final GameState currentGameState = gameAssets.getGameState();

        switch (currentGameState) {
            case LOBBY -> {
                playerQuitEvent.quitMessage(Component.text(
                        Message.PREFIX + "§e" + player.getName() + " §7left the game."
                ));
                //This event is called before player removing from the online player collection.
                gameManager.cancelCountdownIfNotEnoughPlayers((Bukkit.getOnlinePlayers().size() - 1));
            }
            case INGAME, PROTECTION -> {
                playerQuitEvent.quitMessage(null);
                gameAssets.getOnlinePlayersAlive().remove(player);
            }
            case ENDING -> playerQuitEvent.quitMessage(null);
        }
    }
}
