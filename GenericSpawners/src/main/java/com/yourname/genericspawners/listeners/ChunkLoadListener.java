package com.yourname.genericspawners.listeners;

import com.yourname.genericspawners.managers.HologramManager;
import com.yourname.genericspawners.managers.SpawnerManager;
import com.yourname.genericspawners.objects.StackedSpawner;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

public class ChunkLoadListener implements Listener {

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        Chunk chunk = event.getChunk();
        int chunkX = chunk.getX();
        int chunkZ = chunk.getZ();

        for (StackedSpawner stacked : SpawnerManager.getActiveSpawners().values()) {
            Location loc = stacked.getLocation();
            if (loc != null && loc.getWorld() != null && loc.getWorld().equals(chunk.getWorld())) {
                if ((loc.getBlockX() >> 4) == chunkX && (loc.getBlockZ() >> 4) == chunkZ) {
                    HologramManager.updateHologram(loc, stacked);
                }
            }
        }
    }
}