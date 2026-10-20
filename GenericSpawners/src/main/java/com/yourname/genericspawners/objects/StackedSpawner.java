package com.yourname.genericspawners.objects;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;

import java.util.ArrayList;
import java.util.List;

public class StackedSpawner {
    private final Location location;
    private int stackSize;
    private final List<IndividualSpawner> spawners;
    private ArmorStand hologram;

    public StackedSpawner(Location location) {
        this.location = location;
        this.stackSize = 1;
        this.spawners = new ArrayList<>();
    }

    public Location getLocation() { return location; }
    public int getStackSize() { return stackSize; }
    public void setStackSize(int stackSize) { this.stackSize = stackSize; }
    public List<IndividualSpawner> getSpawners() { return spawners; }
    public ArmorStand getHologram() { return hologram; }
    public void setHologram(ArmorStand hologram) { this.hologram = hologram; }
}