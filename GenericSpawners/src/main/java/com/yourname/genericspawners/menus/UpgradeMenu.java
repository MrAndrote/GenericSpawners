package com.yourname.genericspawners.menus;

import com.yourname.genericspawners.GenericSpawners;
import com.yourname.genericspawners.objects.IndividualSpawner;
import com.yourname.genericspawners.objects.StackedSpawner;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.ArrayList;
import java.util.List;

public class UpgradeMenu {

    public static void openUpgradeMenu(Player player, StackedSpawner stacked, IndividualSpawner ind, int spawnerNumber) {
        player.setMetadata("CurrentStackedSpawner", new FixedMetadataValue(GenericSpawners.getInstance(), stacked));
        player.setMetadata("CurrentIndividualSpawner", new FixedMetadataValue(GenericSpawners.getInstance(), ind));

        Inventory gui = Bukkit.createInventory(null, 27, "§8Upgrade Spawner #" + spawnerNumber);
        ItemStack glass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(" ");
        glass.setItemMeta(glassMeta);
        for (int i = 0; i < 27; i++) gui.setItem(i, glass);

        int maxAllowedLvl = ind.getFusionTier() + 1;
        double baseCountCost = GenericSpawners.getInstance().getConfig().getDouble("upgrades.count.base-cost", 5000.0);
        double baseSpeedCost = GenericSpawners.getInstance().getConfig().getDouble("upgrades.speed.base-cost", 7500.0);
        double baseDropCost  = GenericSpawners.getInstance().getConfig().getDouble("upgrades.drops.base-cost", 10000.0);

        ItemStack countItem = new ItemStack(Material.EMERALD);
        ItemMeta countMeta = countItem.getItemMeta();
        countMeta.setDisplayName("§a§lUpgrade Spawn Amount");
        List<String> countLore = new ArrayList<>();
        countLore.add("§7Increases how many mobs spawn per cycle.");
        countLore.add("§7Current Level: §e" + ind.getSpawnCountLevel() + " / " + maxAllowedLvl);
        if (ind.getSpawnCountLevel() < maxAllowedLvl) {
            countLore.add("§7Upgrade Cost: §a$" + String.format("%,.2f", ind.getSpawnCountLevel() * baseCountCost));
            countLore.add("");
            countLore.add("§a▶ Click to Upgrade");
        } else {
            countLore.add("§cMAX TIER REACHED (Fuse to unlock higher levels)");
        }
        countMeta.setLore(countLore);
        countItem.setItemMeta(countMeta);
        gui.setItem(11, countItem);

        ItemStack speedItem = new ItemStack(Material.SUGAR);
        ItemMeta speedMeta = speedItem.getItemMeta();
        speedMeta.setDisplayName("§b§lUpgrade Spawn Speed");
        List<String> speedLore = new ArrayList<>();
        speedLore.add("§7Reduces spawner cooldown delay.");
        speedLore.add("§7Current Level: §e" + ind.getSpawnSpeedLevel() + " / " + maxAllowedLvl);
        if (ind.getSpawnSpeedLevel() < maxAllowedLvl) {
            speedLore.add("§7Upgrade Cost: §a$" + String.format("%,.2f", ind.getSpawnSpeedLevel() * baseSpeedCost));
            speedLore.add("");
            speedLore.add("§a▶ Click to Upgrade");
        } else {
            speedLore.add("§cMAX TIER REACHED (Fuse to unlock higher levels)");
        }
        speedMeta.setLore(speedLore);
        speedItem.setItemMeta(speedMeta);
        gui.setItem(13, speedItem);

        ItemStack dropItem = new ItemStack(Material.GOLD_INGOT);
        ItemMeta dropMeta = dropItem.getItemMeta();
        dropMeta.setDisplayName("§6§lUpgrade Drop Multiplier");
        List<String> dropLore = new ArrayList<>();
        dropLore.add("§7Multiplies item drops when mobs die.");
        dropLore.add("§7Current Level: §e" + ind.getDropRateLevel() + " / " + maxAllowedLvl);
        if (ind.getDropRateLevel() < maxAllowedLvl) {
            dropLore.add("§7Upgrade Cost: §a$" + String.format("%,.2f", ind.getDropRateLevel() * baseDropCost));
            dropLore.add("");
            dropLore.add("§a▶ Click to Upgrade");
        } else {
            dropLore.add("§cMAX TIER REACHED (Fuse to unlock higher levels)");
        }
        dropMeta.setLore(dropLore);
        dropItem.setItemMeta(dropMeta);
        gui.setItem(15, dropItem);

        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName("§c§lBack to Overview");
        back.setItemMeta(backMeta);
        gui.setItem(22, back);

        player.openInventory(gui);
    }
}