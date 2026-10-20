package com.yourname.genericspawners.objects;

import com.yourname.genericspawners.GenericSpawners;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.concurrent.ThreadLocalRandom;

public class IndividualSpawner {

    private final String entityType;
    private int spawnCountLevel;
    private int spawnSpeedLevel;
    private int dropRateLevel;
    private int fusionTier;
    private int timer;

    public IndividualSpawner(String entityType) {
        this(entityType, 1, 1, 1, 0);
    }

    public IndividualSpawner(String entityType, int spawnCountLevel, int spawnSpeedLevel, int dropRateLevel, int fusionTier) {
        this.entityType = entityType;
        this.fusionTier = Math.min(4, Math.max(0, fusionTier));
        int maxLvl = this.fusionTier + 1;
        this.spawnCountLevel = Math.min(maxLvl, Math.max(1, spawnCountLevel));
        this.spawnSpeedLevel = Math.min(maxLvl, Math.max(1, spawnSpeedLevel));
        this.dropRateLevel = Math.min(maxLvl, Math.max(1, dropRateLevel));
        this.timer = getRequiredTicks();
    }

    public void tick(Location loc) {
        if (loc == null || loc.getWorld() == null) return;

        timer -= 20;
        if (timer <= 0) {
            spawnEntities(loc);
            timer = getRequiredTicks();
        }
    }

    private void spawnEntities(Location loc) {
        try {
            EntityType type = matchEntityType(entityType);
            int totalToSpawn = calculateSpawnAmount();

            for (int i = 0; i < totalToSpawn; i++) {
                double offsetX = ThreadLocalRandom.current().nextDouble(-1.5, 1.5);
                double offsetZ = ThreadLocalRandom.current().nextDouble(-1.5, 1.5);
                Location spawnLoc = loc.clone().add(0.5 + offsetX, 1.0, 0.5 + offsetZ);

                Entity entity = loc.getWorld().spawnEntity(spawnLoc, type);
                entity.setMetadata("DropRateLevel", new FixedMetadataValue(GenericSpawners.getInstance(), dropRateLevel));
            }
        } catch (IllegalArgumentException ignored) {}
    }

    private EntityType matchEntityType(String typeStr) {
        if (typeStr == null || typeStr.isEmpty()) return EntityType.ZOMBIE;
        String clean = typeStr.toUpperCase().replace(" ", "_").replace("-", "_");
        try {
            return EntityType.valueOf(clean);
        } catch (IllegalArgumentException ignored) {}

        if (clean.contains("IRON") || clean.contains("GOLEM")) return EntityType.IRON_GOLEM;
        if (clean.contains("SPIDER")) return EntityType.SPIDER;
        if (clean.contains("PIG")) return EntityType.PIG;
        if (clean.contains("COW")) return EntityType.COW;
        if (clean.contains("CHICKEN")) return EntityType.CHICKEN;
        if (clean.contains("SHEEP")) return EntityType.SHEEP;
        if (clean.contains("SKELETON")) return EntityType.SKELETON;
        if (clean.contains("CREEPER")) return EntityType.CREEPER;
        if (clean.contains("BLAZE")) return EntityType.BLAZE;
        if (clean.contains("ENDER")) return EntityType.ENDERMAN;
        if (clean.contains("WITCH")) return EntityType.WITCH;
        if (clean.contains("VILLAGER")) return EntityType.VILLAGER;

        return EntityType.ZOMBIE;
    }

    private int calculateSpawnAmount() {
        return switch (spawnCountLevel) {
            case 1 -> 1;
            case 2 -> ThreadLocalRandom.current().nextInt(1, 3);
            case 3 -> ThreadLocalRandom.current().nextInt(2, 4);
            case 4 -> ThreadLocalRandom.current().nextInt(2, 5);
            default -> ThreadLocalRandom.current().nextInt(3, 6);
        };
    }

    private int getRequiredTicks() {
        return switch (spawnSpeedLevel) {
            case 1 -> 600;
            case 2 -> 450;
            case 3 -> 300;
            case 4 -> 200;
            default -> 120;
        };
    }

    public String getEntityType() { return entityType; }
    public int getSpawnCountLevel() { return spawnCountLevel; }

    public void setSpawnCountLevel(int spawnCountLevel) {
        this.spawnCountLevel = Math.min(fusionTier + 1, Math.max(1, spawnCountLevel));
    }

    public int getSpawnSpeedLevel() { return spawnSpeedLevel; }

    public void setSpawnSpeedLevel(int spawnSpeedLevel) {
        this.spawnSpeedLevel = Math.min(fusionTier + 1, Math.max(1, spawnSpeedLevel));
        this.timer = getRequiredTicks();
    }

    public int getDropRateLevel() { return dropRateLevel; }

    public void setDropRateLevel(int dropRateLevel) {
        this.dropRateLevel = Math.min(fusionTier + 1, Math.max(1, dropRateLevel));
    }

    public int getFusionTier() { return fusionTier; }

    public void setFusionTier(int fusionTier) {
        this.fusionTier = Math.min(4, Math.max(0, fusionTier));
    }
}