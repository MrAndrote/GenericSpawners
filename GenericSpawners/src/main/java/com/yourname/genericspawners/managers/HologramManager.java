package com.yourname.genericspawners.managers;

import com.yourname.genericspawners.objects.StackedSpawner;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.HashMap;
import java.util.Map;

public class HologramManager {

    private static final Map<Location, ArmorStand> HOLOGRAM_CACHE = new HashMap<>();

    public static void updateHologram(Location location, StackedSpawner stack) {
        if (location == null || location.getWorld() == null) return;

        Location hologramLoc = location.clone().add(0.5, 1.2, 0.5);

        if (stack == null || stack.getStackSize() == 0) {
            removeHologram(location);
            return;
        }

        String mobType = stack.getSpawners().get(0).getEntityType().toUpperCase().replace("_", " ");
        String text = "§e§l" + stack.getStackSize() + "X §6§l" + mobType + " SPAWNER";

        ArmorStand stand = HOLOGRAM_CACHE.get(location);

        if (stand == null || !stand.isValid()) {
            removeExistingAtLoc(hologramLoc);
            stand = (ArmorStand) location.getWorld().spawnEntity(hologramLoc, EntityType.ARMOR_STAND);
            stand.setVisible(false);
            stand.setGravity(false);
            stand.setCustomNameVisible(true);
            stand.setMarker(true);
            HOLOGRAM_CACHE.put(location, stand);
        }

        stand.setCustomName(text);
    }

    public static void removeHologram(Location location) {
        ArmorStand stand = HOLOGRAM_CACHE.remove(location);
        if (stand != null && stand.isValid()) {
            stand.remove();
        } else if (location != null && location.getWorld() != null) {
            removeExistingAtLoc(location.clone().add(0.5, 1.2, 0.5));
        }
    }

    private static void removeExistingAtLoc(Location loc) {
        if (loc.getWorld() == null) return;
        for (Entity entity : loc.getWorld().getNearbyEntities(loc, 0.5, 0.5, 0.5)) {
            if (entity instanceof ArmorStand stand && stand.isMarker() && !stand.isVisible()) {
                stand.remove();
            }
        }
    }
}