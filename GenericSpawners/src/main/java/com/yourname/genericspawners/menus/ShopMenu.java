package com.yourname.genericspawners.menus;

import com.yourname.genericspawners.GenericSpawners;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ShopMenu {

    private static final NamespacedKey PRICE_KEY = new NamespacedKey(GenericSpawners.getInstance(), "spawner_price");
    private static final Map<String, String> FORMATTED_NAME_CACHE = new HashMap<>();

    public static void openShop(Player player) {
        Inventory shop = Bukkit.createInventory(null, 45, "§8Spawner Shop");

        ItemStack border = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.setDisplayName(" ");
        border.setItemMeta(borderMeta);

        for (int i = 0; i < 45; i++) {
            shop.setItem(i, border);
        }

        addShopSpawner(shop, 10, "PIG", getPrice("PIG"));
        addShopSpawner(shop, 11, "COW", getPrice("COW"));
        addShopSpawner(shop, 12, "SHEEP", getPrice("SHEEP"));
        addShopSpawner(shop, 13, "CHICKEN", getPrice("CHICKEN"));
        addShopSpawner(shop, 14, "ZOMBIE", getPrice("ZOMBIE"));
        addShopSpawner(shop, 15, "SKELETON", getPrice("SKELETON"));
        addShopSpawner(shop, 16, "SPIDER", getPrice("SPIDER"));

        addShopSpawner(shop, 19, "CREEPER", getPrice("CREEPER"));
        addShopSpawner(shop, 20, "BLAZE", getPrice("BLAZE"));
        addShopSpawner(shop, 21, "ENDERMAN", getPrice("ENDERMAN"));
        addShopSpawner(shop, 22, "WITCH", getPrice("WITCH"));
        addShopSpawner(shop, 23, "VILLAGER", getPrice("VILLAGER"));
        addShopSpawner(shop, 24, "IRON_GOLEM", getPrice("IRON_GOLEM"));

        ItemStack fusionButton = new ItemStack(Material.ANVIL);
        ItemMeta fusionMeta = fusionButton.getItemMeta();
        fusionMeta.setDisplayName("§e§lSpawner Fusion Menu");
        List<String> fusionLore = new ArrayList<>();
        fusionLore.add("§7Combine §e3x §7identical spawners");
        fusionLore.add("§7to upgrade their rarity tier!");
        fusionLore.add("");
        fusionLore.add("§a▶ Click to Open Fusion Menu");
        fusionMeta.setLore(fusionLore);
        fusionButton.setItemMeta(fusionMeta);
        shop.setItem(38, fusionButton);

        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        closeMeta.setDisplayName("§c§lClose Menu");
        close.setItemMeta(closeMeta);
        shop.setItem(42, close);

        player.openInventory(shop);
    }

    private static double getPrice(String entityType) {
        return GenericSpawners.getInstance().getConfig().getDouble("shop-prices." + entityType.toUpperCase(), 10000.0);
    }

    private static void addShopSpawner(Inventory inv, int slot, String entityType, double price) {
        ItemStack item = new ItemStack(Material.SPAWNER, 1);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName("§7§lCOMMON §7" + formatEntityName(entityType) + " Spawner");
        meta.getPersistentDataContainer().set(PRICE_KEY, PersistentDataType.DOUBLE, price);

        String bar = "§8[§a■§8]";
        List<String> lore = new ArrayList<>();
        lore.add("§8§m------------------------");
        lore.add("§7Type: §f" + entityType);
        lore.add("§7Price: §a$" + String.format(Locale.US, "%,.2f", price));
        lore.add("");
        lore.add("§e§lBase Upgrades:");
        lore.add(" §7Count: " + bar + " §f1/1");
        lore.add(" §7Speed: " + bar + " §f1/1");
        lore.add(" §7Drops: " + bar + " §f1/1");
        lore.add("§8§m------------------------");
        lore.add("§b▶ Click to purchase!");

        meta.setLore(lore);
        item.setItemMeta(meta);
        inv.setItem(slot, item);
    }

    private static String formatEntityName(String entityType) {
        if (entityType == null || entityType.isEmpty()) return "Zombie";
        return FORMATTED_NAME_CACHE.computeIfAbsent(entityType, k -> {
            String[] words = k.replace("_", " ").toLowerCase().split("\\s+");
            StringBuilder formatted = new StringBuilder();
            for (String word : words) {
                if (!word.isEmpty()) {
                    if (formatted.length() > 0) formatted.append(" ");
                    formatted.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
                }
            }
            return formatted.toString();
        });
    }
}