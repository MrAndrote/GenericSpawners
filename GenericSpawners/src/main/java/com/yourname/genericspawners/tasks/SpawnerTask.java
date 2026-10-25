package com.yourname.genericspawners.tasks;

import com.yourname.genericspawners.managers.HologramManager;
import com.yourname.genericspawners.managers.SpawnerManager;
import com.yourname.genericspawners.objects.IndividualSpawner;
import com.yourname.genericspawners.objects.StackedSpawner;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

public class SpawnerTask extends BukkitRunnable {

    @Override
    public void run() {
        for (StackedSpawner stacked : SpawnerManager.getActiveSpawners().values()) {
            Location loc = stacked.getLocation();
            if (loc == null || loc.getWorld() == null || !loc.getChunk().isLoaded()) continue;

            HologramManager.updateHologram(loc, stacked);

            boolean playerNearby = loc.getWorld().getPlayers().stream()
                    .anyMatch(p -> p.getLocation().distanceSquared(loc) <= 576);

            if (!playerNearby) continue;

            for (IndividualSpawner ind : stacked.getSpawners()) {
                ind.tick(loc);
            }
        }
    }
}