package com.yourname.genericspawners.managers;

import com.yourname.genericspawners.GenericSpawners;
import com.yourname.genericspawners.objects.IndividualSpawner;
import com.yourname.genericspawners.objects.StackedSpawner;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class DataStorageManager {

    private static File file;
    private static FileConfiguration config;

    public static void setup() {
        file = new File(GenericSpawners.getInstance().getDataFolder(), "spawners.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException ignored) {}
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public static void saveSpawners(Map<Location, StackedSpawner> activeSpawners) {
        config.set("spawners", null);

        int index = 0;
        for (Map.Entry<Location, StackedSpawner> entry : activeSpawners.entrySet()) {
            Location loc = entry.getKey();
            StackedSpawner stacked = entry.getValue();
            if (loc == null || loc.getWorld() == null) continue;

            String path = "spawners." + index + ".";
            config.set(path + "world", loc.getWorld().getName());
            config.set(path + "x", loc.getBlockX());
            config.set(path + "y", loc.getBlockY());
            config.set(path + "z", loc.getBlockZ());

            List<IndividualSpawner> list = stacked.getSpawners();
            for (int i = 0; i < list.size(); i++) {
                IndividualSpawner ind = list.get(i);
                String subPath = path + "stack." + i + ".";
                config.set(subPath + "entityType", ind.getEntityType());
                config.set(subPath + "spawnCount", ind.getSpawnCountLevel());
                config.set(subPath + "spawnSpeed", ind.getSpawnSpeedLevel());
                config.set(subPath + "dropRate", ind.getDropRateLevel());
                config.set(subPath + "fusionTier", ind.getFusionTier());
            }
            index++;
        }

        try {
            config.save(file);
        } catch (IOException ignored) {}
    }

    public static void loadSpawners() {
        if (!config.contains("spawners")) return;

        for (String key : config.getConfigurationSection("spawners").getKeys(false)) {
            String path = "spawners." + key + ".";
            String worldName = config.getString(path + "world");
            if (worldName == null) continue;

            World world = Bukkit.getWorld(worldName);
            if (world == null) continue;

            Location loc = new Location(world, config.getInt(path + "x"), config.getInt(path + "y"), config.getInt(path + "z"));
            StackedSpawner stacked = new StackedSpawner(loc);
            stacked.getSpawners().clear();

            if (config.contains(path + "stack")) {
                for (String indexKey : config.getConfigurationSection(path + "stack").getKeys(false)) {
                    String subPath = path + "stack." + indexKey + ".";
                    String entityType = config.getString(subPath + "entityType", "ZOMBIE");

                    IndividualSpawner ind = new IndividualSpawner(
                            entityType,
                            config.getInt(subPath + "spawnCount", 1),
                            config.getInt(subPath + "spawnSpeed", 1),
                            config.getInt(subPath + "dropRate", 1),
                            config.getInt(subPath + "fusionTier", 0)
                    );

                    stacked.getSpawners().add(ind);
                }
            }

            stacked.setStackSize(stacked.getSpawners().size());
            SpawnerManager.registerLoadedSpawner(loc, stacked);
        }
    }
}