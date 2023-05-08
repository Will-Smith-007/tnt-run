package de.will_smith_007.tntrun.commands;

import com.google.inject.Inject;
import de.will_smith_007.tntrun.enums.Message;
import de.will_smith_007.tntrun.managers.StatsManager;
import lombok.NonNull;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class StatsCommand implements CommandExecutor {

    private final StatsManager statsManager;
    private final boolean isDatabaseEnabled;

    @Inject
    public StatsCommand(@NonNull StatsManager statsManager) {
        this.statsManager = statsManager;
        this.isDatabaseEnabled = statsManager.isDatabaseEnabled();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof final Player player)) {
            sender.sendPlainMessage(Message.PREFIX + "§cYou need to be a player to execute this command.");
            return true;
        }

        if (!isDatabaseEnabled) {
            player.sendPlainMessage(Message.PREFIX + "§cThe statistics system is currently disabled by the server admin.");
            return true;
        }

        if (args.length != 0) {
            player.sendPlainMessage(Message.PREFIX + "§cPlease use the following command: §e/stats");
            return true;
        }

        final UUID playerUUID = player.getUniqueId();

        statsManager.getGameStatisticsAsync(playerUUID).thenAccept(gameStatistics -> {
            if (gameStatistics == null) {
                player.sendPlainMessage(Message.PREFIX + "§cYou don't have played this game before.");
                return;
            }

            player.sendMessage(Message.PREFIX + "§8§m-------[§cYour Statistics§8§m]-------",
                    "",
                    Message.PREFIX + "§eYour wins: §7" + gameStatistics.playerGameWins(),
                    Message.PREFIX + "§eYour loses: §7" + gameStatistics.playerGameLoses(),
                    Message.PREFIX + "§eLongest survived time: §7" + getTimerFormat(gameStatistics.longestSurvivedTime()));

            final int currentRanking = statsManager.getPlayerRanking(playerUUID);

            player.sendPlainMessage(Message.PREFIX + "§eYour ranking: §7" + currentRanking);
        });

        return false;
    }

    /**
     * Gets the formatted timer of the specified timeMillis.
     *
     * @param timeMillis Time Milliseconds which should be formatted.
     * @return The formatted String timer format in minutes and seconds.
     */
    private @NonNull String getTimerFormat(long timeMillis) {
        final long timerMinutes = TimeUnit.MILLISECONDS.toMinutes(timeMillis);
        return String.format("%02d minutes and %02d seconds",
                timerMinutes,
                (TimeUnit.MILLISECONDS.toSeconds(timeMillis) - TimeUnit.MINUTES.toSeconds(timerMinutes)));
    }
}
