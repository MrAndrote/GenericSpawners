package com.yourname.genericspawners.listeners;

import com.yourname.genericspawners.GenericSpawners;
import com.yourname.genericspawners.managers.SpawnerManager;
import com.yourname.genericspawners.menus.StackOverviewMenu;
import com.yourname.genericspawners.objects.StackedSpawner;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class SpawnerInteractListener implements Listener {

    @EventHandler
    public void onSpawnerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_BLOCK) return;

        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.SPAWNER) return;

        Player player = event.getPlayer();
        if (player.isSneaking()) {
            ItemStack tool = player.getInventory().getItemInMainHand();
            if (tool != null && tool.getType().name().endsWith("_PICKAXE")) return;

            event.setCancelled(true);

            StackedSpawner stacked = SpawnerManager.getStackedSpawner(block.getLocation());
            if (stacked == null) {
                player.sendMessage(GenericSpawners.getInstance().getPrefix() + "§cThis spawner is not registered in the system.");
                return;
            }

            StackOverviewMenu.openMenu(player, stacked);
        }
    }
}