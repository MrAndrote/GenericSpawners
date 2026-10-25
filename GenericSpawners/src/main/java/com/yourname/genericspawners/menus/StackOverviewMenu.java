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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StackOverviewMenu {

    private final GenericSpawners plugin;

    public StackOverviewMenu(GenericSpawners plugin) {
        this.plugin = plugin;
    }

    public void openMenu(Player player, StackedSpawner stacked) {
        player.setMetadata("CurrentStackedSpawner", new FixedMetadataValue(plugin, stacked));

        Inventory gui = Bukkit.createInventory(null, 36, "§8Stacked Spawner Dashboard");
        ItemStack border = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.setDisplayName(" ");
        border.setItemMeta(borderMeta);

        for (int i = 0; i < 36; i++) {
            gui.setItem(i, border);
        }

        Map<Integer, TierSummary> tierMap = new HashMap<>();
        for (IndividualSpawner ind : stacked.getSpawners()) {
            tierMap.computeIfAbsent(ind.getFusionTier(), t -> new TierSummary(ind.getEntityType())).count++;
        }

        int[] tierSlots = {11, 12, 13, 14, 15};
        for (int tier = 0; tier <= 4; tier++) {
            int targetSlot = tierSlots[tier];
            String rarityPrefix = getRarityPrefix(tier);

            if (tierMap.containsKey(tier)) {
                TierSummary summary = tierMap.get(tier);
                SpawnerType type = SpawnerType.match(summary.entityType);
                String formattedName = (type != null) ? type.getFormattedName() : summary.entityType;

                ItemStack item = new ItemStack(Material.SPAWNER, Math.min(summary.count, 64));
                ItemMeta meta = item.getItemMeta();

                meta.setDisplayName(rarityPrefix + " " + getRarityColorCode(tier) + formattedName + " Spawners");

                List<String> lore = new ArrayList<>();
                lore.add("§8§m------------------------");
                lore.add("§7Total in Stack: §e" + summary.count + "x");
                lore.add("§7Rarity Tier: " + rarityPrefix);
                lore.add("§8§m------------------------");
                lore.add("§a▶ Click to view all " + rarityPrefix + " §aSpawners!");

                meta.setLore(lore);
                item.setItemMeta(meta);
                gui.setItem(targetSlot, item);
            } else {
                ItemStack locked = new ItemStack(Material.GRAY_DYE);
                ItemMeta lockedMeta = locked.getItemMeta();
                lockedMeta.setDisplayName(rarityPrefix + " §7(None Stacked)");

                List<String> lockedLore = new ArrayList<>();
                lockedLore.add("§8§m------------------------");
                lockedLore.add("§7You have no " + rarityPrefix + " §7spawners");
                lockedLore.add("§7currently in this stacked block.");
                lockedLore.add("§8§m------------------------");

                lockedMeta.setLore(lockedLore);
                locked.setItemMeta(lockedMeta);
                gui.setItem(targetSlot, locked);
            }
        }

        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        closeMeta.setDisplayName("§c§lClose Menu");
        close.setItemMeta(closeMeta);
        gui.setItem(31, close);

        player.openInventory(gui);
    }

    private static class TierSummary {
        String entityType;
        int count;
        public TierSummary(String entityType) { this.entityType = entityType; this.count = 0; }
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
}