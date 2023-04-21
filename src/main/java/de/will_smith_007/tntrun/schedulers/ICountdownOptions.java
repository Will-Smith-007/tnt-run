package de.will_smith_007.tntrun.schedulers;

import lombok.NonNull;
import org.bukkit.entity.Player;

public interface ICountdownOptions {

    @NonNull String getCountdownMessage(int currentCountdown);

    void playCountdownSound(@NonNull Player player);
}
