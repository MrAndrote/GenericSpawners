package com.yourname.genericspawners.managers;

import com.yourname.genericspawners.objects.IndividualSpawner;
import com.yourname.genericspawners.objects.StackedSpawner;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;

public class SpawnerManager {

    private static final Map<Location, StackedSpawner> activeSpawners = new HashMap<>();

    public static void addIndividualToLocation(Location loc, IndividualSpawner individual) {
        Location blockLoc = loc.getBlock().getLocation();
        StackedSpawner stacked = activeSpawners.computeIfAbsent(blockLoc, StackedSpawner::new);

        stacked.getSpawners().add(individual);
        stacked.setStackSize(stacked.getSpawners().size());

        HologramManager.updateHologram(blockLoc, stacked);
    }

    public static IndividualSpawner removeSpawnerFromLocation(Location loc) {
        Location blockLoc = loc.getBlock().getLocation();
        StackedSpawner stacked = activeSpawners.get(blockLoc);
        if (stacked == null || stacked.getSpawners().isEmpty()) {
            HologramManager.removeHologram(blockLoc);
            activeSpawners.remove(blockLoc);
            return null;
        }

        IndividualSpawner removed = stacked.getSpawners().remove(stacked.getSpawners().size() - 1);
        stacked.setStackSize(stacked.getSpawners().size());

        if (!stacked.getSpawners().isEmpty()) {
            HologramManager.updateHologram(blockLoc, stacked);
        } else {
            HologramManager.removeHologram(blockLoc);
            activeSpawners.remove(blockLoc);
        }

        return removed;
    }

    public static StackedSpawner getStackedSpawner(Location loc) {
        return loc == null ? null : activeSpawners.get(loc.getBlock().getLocation());
    }

    public static Map<Location, StackedSpawner> getActiveSpawners() {
        return activeSpawners;
    }

    public static void registerLoadedSpawner(Location loc, StackedSpawner stacked) {
        if (loc != null) {
            activeSpawners.put(loc.getBlock().getLocation(), stacked);
        }
    }
}