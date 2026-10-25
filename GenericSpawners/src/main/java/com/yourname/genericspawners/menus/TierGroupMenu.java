package com.yourname.genericspawners.menus;

import com.yourname.genericspawners.GenericSpawners;
import com.yourname.genericspawners.objects.IndividualSpawner;
import com.yourname.genericspawners.objects.SpawnerType;
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

public class TierGroupMenu {

    private final GenericSpawners plugin;

    public TierGroupMenu(GenericSpawners plugin) {
        this.plugin = plugin;
    }

    public void openMenu(Player player, StackedSpawner stacked, int tier) {
        openMenu(player, stacked, tier, 1);
    }

    public void openMenu(Player player, StackedSpawner stacked, int tier, int page) {
        player.setMetadata("CurrentStackedSpawner", new FixedMetadataValue(plugin, stacked));

        List<SpawnerIndexPair> tierSpawners = new ArrayList<>();
        List<IndividualSpawner> spawners = stacked.getSpawners();
        for (int i = 0; i < spawners.size(); i++) {
            IndividualSpawner ind = spawners.get(i);
            if (ind.getFusionTier() == tier) {
                tierSpawners.add(new SpawnerIndexPair(ind, i + 1));
            }
        }

        int maxItemsPerPage = 28;
        int totalItems = tierSpawners.size();
        int maxPages = Math.max(1, (int) Math.ceil((double) totalItems / maxItemsPerPage));
        page = Math.max(1, Math.min(page, maxPages));

        String rarityPrefix = getRarityPrefix(tier);
        Inventory gui = Bukkit.createInventory(null, 54, "§8" + rarityPrefix + " §8Spawners (" + page + "/" + maxPages + ")");

        ItemStack border = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.setDisplayName(" ");
        border.setItemMeta(borderMeta);
        for (int i = 0; i < 54; i++) gui.setItem(i, border);

        int[] availableSlots = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43
        };

        int startIndex = (page - 1) * maxItemsPerPage;
        int endIndex = Math.min(startIndex + maxItemsPerPage, totalItems);

        for (int i = startIndex, slotIdx = 0; i < endIndex; i++, slotIdx++) {
            SpawnerIndexPair pair = tierSpawners.get(i);
            IndividualSpawner ind = pair.spawner;
            SpawnerType type = SpawnerType.match(ind.getEntityType());
            String formattedName = (type != null) ? type.getFormattedName() : ind.getEntityType();

            ItemStack item = new ItemStack(Material.SPAWNER, 1);
            ItemMeta meta = item.getItemMeta();
            int maxLvl = tier + 1;

            meta.setDisplayName(rarityPrefix + " " + getRarityColorCode(tier) + formattedName + " Spawner");

            List<String> lore = new ArrayList<>();
            lore.add("§8§m------------------------");
            lore.add("§7Spawner Index: §f#" + pair.actualIndex);
            lore.add("");
            lore.add("§e§lUpgrades:");
            lore.add(" §7Count: " + getProgressBar(ind.getSpawnCountLevel(), maxLvl) + " §f" + ind.getSpawnCountLevel() + "/" + maxLvl);
            lore.add(" §7Speed: " + getProgressBar(ind.getSpawnSpeedLevel(), maxLvl) + " §f" + ind.getSpawnSpeedLevel() + "/" + maxLvl);
            lore.add(" §7Drops: " + getProgressBar(ind.getDropRateLevel(), maxLvl) + " §f" + ind.getDropRateLevel() + "/" + maxLvl);
            lore.add("§8§m------------------------");
            lore.add("§a▶ Click to upgrade this spawner!");

            meta.setLore(lore);
            item.setItemMeta(meta);
            gui.setItem(availableSlots[slotIdx], item);
        }

        if (page > 1) {
            ItemStack prev = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = prev.getItemMeta();
            prevMeta.setDisplayName("§e◀ Previous Page");
            prev.setItemMeta(prevMeta);
            gui.setItem(45, prev);
        }

        if (page < maxPages) {
            ItemStack next = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = next.getItemMeta();
            nextMeta.setDisplayName("§eNext Page ▶");
            next.setItemMeta(nextMeta);
            gui.setItem(53, next);
        }

        ItemStack back = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName("§a§l◀ Back to Dashboard");
        back.setItemMeta(backMeta);
        gui.setItem(49, back);

        player.openInventory(gui);
    }

    private static class SpawnerIndexPair {
        IndividualSpawner spawner;
        int actualIndex;
        public SpawnerIndexPair(IndividualSpawner spawner, int actualIndex) {
            this.spawner = spawner;
            this.actualIndex = actualIndex;
        }
    }

    private String getRarityPrefix(int tier) {
        return switch (tier) {
            case 4 -> "§6§lLEGENDARY";
            case 3 -> "§d§lEPIC";
            case 2 -> "§9§lRARE";
            case 1 -> "§a§lUNCOMMON";
            default -> "§7§lCOMMON";
        };
    }

    private String getRarityColorCode(int tier) {
        return switch (tier) {
            case 4 -> "§6";
            case 3 -> "§d";
            case 2 -> "§9";
            case 1 -> "§a";
            default -> "§7";
        };
    }

    private String getProgressBar(int currentLevel, int maxLevel) {
        StringBuilder sb = new StringBuilder("§8[");
        for (int i = 1; i <= maxLevel; i++) {
            sb.append(i <= currentLevel ? "§a■" : "§7□");
        }
        return sb.append("§8]").toString();
    }
}