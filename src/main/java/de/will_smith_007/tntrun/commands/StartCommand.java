package de.will_smith_007.tntrun.commands;

import de.will_smith_007.tntrun.enums.GameState;
import de.will_smith_007.tntrun.enums.Message;
import de.will_smith_007.tntrun.managers.GameAssets;
import de.will_smith_007.tntrun.managers.GameManager;
import lombok.NonNull;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class StartCommand implements CommandExecutor {

    private final GameAssets GAME_ASSETS;
    private final GameManager GAME_MANAGER;

    public StartCommand(@NonNull GameAssets gameAssets,
                        @NonNull GameManager gameManager) {
        this.GAME_ASSETS = gameAssets;
        this.GAME_MANAGER = gameManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("tntrun.start")) {
            sender.sendPlainMessage(Message.PREFIX + "§cYou don't have enough permissions to execute this command.");
            return true;
        }

        if (args.length != 0) {
            sender.sendPlainMessage(Message.PREFIX + "§cPlease only use the following command: §e/start");
            return true;
        }

        final GameState gameState = GAME_ASSETS.getGameState();

        if (gameState != GameState.LOBBY) {
            sender.sendPlainMessage(Message.PREFIX + "§cThis command can only be used in the§e lobby phase§c of the game.");
            return true;
        }

        if (!GAME_MANAGER.shortenCountdownIfEnoughPlayers()) {
            sender.sendPlainMessage(Message.PREFIX + "§cThe countdown couldn't be shorten because there aren't enough " +
                    "players to start or the countdown is already running at less than 10 seconds.");
            return true;
        }

        sender.sendPlainMessage(Message.PREFIX + "§aYou've set the countdown to §e10 seconds§a.");
        return false;
    }
}
