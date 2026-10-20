package com.yourname.genericspawners.menus;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class FusionMenu {

    public static void openFusionMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§8Spawner Fusion (3:1)");

        ItemStack border = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.setDisplayName(" ");
        border.setItemMeta(borderMeta);

        for (int i = 0; i < 27; i++) {
            inv.setItem(i, border);
        }

        inv.setItem(13, new ItemStack(Material.AIR));

        ItemStack fuseButton = new ItemStack(Material.ANVIL);
        ItemMeta fuseMeta = fuseButton.getItemMeta();
        fuseMeta.setDisplayName("§e§lFuse Spawners");
        List<String> lore = new ArrayList<>();
        lore.add("§7Place §e3x §7identical spawners");
        lore.add("§7in the slot above.");
        lore.add("");
        lore.add("§a▶ Click to Fuse (3:1)!");
        fuseMeta.setLore(lore);
        fuseButton.setItemMeta(fuseMeta);
        inv.setItem(22, fuseButton);

        player.openInventory(inv);
    }
}