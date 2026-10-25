package com.yourname.genericspawners.menus;

import com.yourname.genericspawners.GenericSpawners;
import com.yourname.genericspawners.objects.SpawnerType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ShopMenu {

    private final GenericSpawners plugin;
    private final NamespacedKey priceKey;

    public ShopMenu(GenericSpawners plugin) {
        this.plugin = plugin;
        this.priceKey = new NamespacedKey(plugin, "spawner_price");
    }

    public void openShop(Player player) {
        Inventory shop = Bukkit.createInventory(null, 45, "§8Spawner Shop");

        ItemStack border = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.setDisplayName(" ");
        border.setItemMeta(borderMeta);

        for (int i = 0; i < 45; i++) {
            shop.setItem(i, border);
        }

        addShopSpawner(shop, 10, SpawnerType.PIG);
        addShopSpawner(shop, 11, SpawnerType.COW);
        addShopSpawner(shop, 12, SpawnerType.SHEEP);
        addShopSpawner(shop, 13, SpawnerType.CHICKEN);
        addShopSpawner(shop, 14, SpawnerType.ZOMBIE);
        addShopSpawner(shop, 15, SpawnerType.SKELETON);
        addShopSpawner(shop, 16, SpawnerType.SPIDER);

        addShopSpawner(shop, 19, SpawnerType.CREEPER);
        addShopSpawner(shop, 20, SpawnerType.BLAZE);
        addShopSpawner(shop, 21, SpawnerType.ENDERMAN);
        addShopSpawner(shop, 22, SpawnerType.WITCH);
        addShopSpawner(shop, 23, SpawnerType.VILLAGER);
        addShopSpawner(shop, 24, SpawnerType.IRON_GOLEM);

        ItemStack fusionButton = new ItemStack(Material.ANVIL);
        ItemMeta fusionMeta = fusionButton.getItemMeta();
        fusionMeta.setDisplayName("§e§lSpawner Fusion Menu");
        List<String> fusionLore = List.of(
                "§7Combine §e3x §7identical spawners",
                "§7to upgrade their rarity tier!",
                "",
                "§a▶ Click to Open Fusion Menu"
        );
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

    private void addShopSpawner(Inventory inv, int slot, SpawnerType spawnerType) {
        double price = plugin.getConfig().getDouble("shop-prices." + spawnerType.name(), 10000.0);

        ItemStack item = new ItemStack(Material.SPAWNER, 1);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName("§7§lCOMMON §7" + spawnerType.getFormattedName() + " Spawner");
        meta.getPersistentDataContainer().set(priceKey, PersistentDataType.DOUBLE, price);

        String bar = "§8[§a■§8]";
        List<String> lore = new ArrayList<>();
        lore.add("§8§m------------------------");
        lore.add("§7Type: §f" + spawnerType.name());
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
}