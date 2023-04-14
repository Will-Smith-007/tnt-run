package de.will_smith_007.tntrun.listeners;

import lombok.NonNull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class CancelListener implements Listener {

    @EventHandler
    public void onFoodLevelChange(@NonNull FoodLevelChangeEvent foodLevelChangeEvent) {
        foodLevelChangeEvent.setCancelled(true);
    }

    @EventHandler
    public void onEntitySpawn(@NonNull EntitySpawnEvent entitySpawnEvent) {
        entitySpawnEvent.setCancelled(true);
    }
}
