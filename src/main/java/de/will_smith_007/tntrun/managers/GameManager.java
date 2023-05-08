package de.will_smith_007.tntrun.managers;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.will_smith_007.tntrun.enums.Message;
import de.will_smith_007.tntrun.schedulers.LobbyCountdownScheduler;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;

/**
 * Manages some game mechanics automatically such as starting or stopping the start countdown if there are or aren't
 * enough players to start.
 */
@Singleton
public class GameManager {

    private final LobbyCountdownScheduler lobbyCountdownScheduler;

    @Inject
    public GameManager(@NonNull LobbyCountdownScheduler lobbyCountdownScheduler) {
        this.lobbyCountdownScheduler = lobbyCountdownScheduler;
    }

    /**
     * Starts the game start countdown only if there are enough players to play this game.
     * <br> The game requires a minimum of two players.
     */
    public void startCountdownIfEnoughPlayers() {
        if (lobbyCountdownScheduler.isRunning()) return;
        final Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
        if (onlinePlayers.size() < 2) return;
        lobbyCountdownScheduler.start();
    }

    /**
     * Shortens the start countdown only if the lobby countdown is running and isn't less than or equals 10 seconds.
     *
     * @return True if the countdown was successfully shortened.
     */
    public boolean shortenCountdownIfEnoughPlayers() {
        if (!lobbyCountdownScheduler.isRunning()) return false;
        final int currentCountdown = lobbyCountdownScheduler.getCountdown();
        if (currentCountdown <= 10) return false;
        lobbyCountdownScheduler.setCountdown(10);
        return true;
    }

    /**
     * Cancels the start countdown only if there aren't enough players to play this game.
     * <br> The game requires a minimum of two players.
     *
     * @param players Amount of current players on the game server.
     */
    public void cancelCountdownIfNotEnoughPlayers(int players) {
        if (!lobbyCountdownScheduler.isRunning()) return;

        final Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();

        if (players < 2) {
            onlinePlayers.forEach(player -> {
                player.sendPlainMessage(Message.PREFIX + "§cThe countdown was cancelled because the game needs " +
                        "a minimum of §e2 players §cto start.");
                player.setLevel(0);
            });
            lobbyCountdownScheduler.stop();
        }
    }
}
