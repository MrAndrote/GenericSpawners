package com.yourname.genericspawners.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InventoryCloseListener implements Listener {

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().getTitle().equals("§8Spawner Fusion (3:1)")) {
            Inventory inv = event.getInventory();
            ItemStack inputItem = inv.getItem(13);

            if (inputItem != null && inputItem.getType() != Material.AIR) {
                Player player = (Player) event.getPlayer();
                inv.setItem(13, null);

                if (player.getInventory().firstEmpty() != -1) {
                    player.getInventory().addItem(inputItem);
                } else {
                    player.getWorld().dropItemNaturally(player.getLocation(), inputItem);
                }
            }
        }
    }
}