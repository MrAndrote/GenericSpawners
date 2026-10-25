package com.yourname.genericspawners.listeners;

import com.yourname.genericspawners.GenericSpawners;
import com.yourname.genericspawners.objects.IndividualSpawner;
import com.yourname.genericspawners.objects.SpawnerType;
import com.yourname.genericspawners.objects.StackedSpawner;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class InventoryClickListener implements Listener {

    private final GenericSpawners plugin;
    private final NamespacedKey countKey;
    private final NamespacedKey speedKey;
    private final NamespacedKey dropKey;
    private final NamespacedKey priceKey;
    private final NamespacedKey fusionTierKey;

    public InventoryClickListener(GenericSpawners plugin) {
        this.plugin = plugin;
        this.countKey = new NamespacedKey(plugin, "spawn_count_lvl");
        this.speedKey = new NamespacedKey(plugin, "spawn_speed_lvl");
        this.dropKey  = new NamespacedKey(plugin, "drop_rate_lvl");
        this.priceKey = new NamespacedKey(plugin, "spawner_price");
        this.fusionTierKey = new NamespacedKey(plugin, "spawner_fusion_tier");
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;
        String title = event.getView().getTitle();
        Player player = (Player) event.getWhoClicked();

        if (title.equals("§8Spawner Fusion (3:1)")) {
            if (event.getClickedInventory().equals(event.getView().getTopInventory())) {
                if (event.getSlot() == 13) return;
                event.setCancelled(true);

                if (event.getSlot() == 22) {
                    Inventory inv = event.getView().getTopInventory();
                    ItemStack inputItem = inv.getItem(13);

                    if (inputItem == null || inputItem.getType() != Material.SPAWNER || !inputItem.hasItemMeta()) {
                        player.sendMessage(plugin.getPrefix() + "§cPlease place spawners in the fusion slot!");
                        return;
                    }

                    int requiredAmount = plugin.getConfig().getInt("fusion.required-amount", 3);
                    int maxTierConfig = plugin.getConfig().getInt("fusion.max-tier", 4);

                    if (inputItem.getAmount() < requiredAmount) {
                        player.sendMessage(plugin.getPrefix() + "§cYou need at least §e" + requiredAmount + "x §cspawners to fuse!");
                        return;
                    }

                    ItemMeta meta = inputItem.getItemMeta();
                    String entityTypeRaw = null;
                    if (meta.hasLore()) {
                        for (String line : meta.getLore()) {
                            String stripped = ChatColor.stripColor(line);
                            if (stripped.startsWith("Type: ")) {
                                entityTypeRaw = stripped.replace("Type: ", "").trim();
                                break;
                            }
                        }
                    }

                    SpawnerType type = SpawnerType.match(entityTypeRaw);
                    if (type == null) return;

                    int currentTier = meta.getPersistentDataContainer().getOrDefault(fusionTierKey, PersistentDataType.INTEGER, 0);
                    if (currentTier >= maxTierConfig) {
                        player.sendMessage(plugin.getPrefix() + "§cThis spawner is already at maximum rarity tier!");
                        return;
                    }

                    int newTier = currentTier + 1;
                    if (inputItem.getAmount() == requiredAmount) {
                        inv.setItem(13, new ItemStack(Material.AIR));
                    } else {
                        inputItem.setAmount(inputItem.getAmount() - requiredAmount);
                    }

                    ItemStack upgradedSpawner = createFusedSpawner(type, newTier);
                    if (player.getInventory().firstEmpty() != -1) {
                        player.getInventory().addItem(upgradedSpawner);
                    } else {
                        player.getWorld().dropItemNaturally(player.getLocation(), upgradedSpawner);
                    }

                    player.sendMessage(plugin.getPrefix() + "§aSuccessfully fused §e" + requiredAmount + "x " + getRarityPrefix(currentTier) + " §e" + type.getFormattedName() + " Spawners §ainto §e1x " + getRarityPrefix(newTier) + " §e" + type.getFormattedName() + " Spawner§a!");
                }
            }
            return;
        }

        if (title.equals(ChatColor.DARK_GRAY + "Spawner Shop")) {
            if (!event.getClickedInventory().equals(event.getView().getTopInventory())) return;
            event.setCancelled(true);

            if (event.getSlot() == 42) {
                player.closeInventory();
                return;
            }
            if (event.getSlot() == 38) {
                plugin.getFusionMenu().openFusionMenu(player);
                return;
            }

            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() != Material.SPAWNER || !clicked.hasItemMeta()) return;

            PersistentDataContainer shopPdc = clicked.getItemMeta().getPersistentDataContainer();
            if (!shopPdc.has(priceKey, PersistentDataType.DOUBLE)) return;
            double price = shopPdc.get(priceKey, PersistentDataType.DOUBLE);

            String entityTypeRaw = null;
            for (String line : clicked.getItemMeta().getLore()) {
                String stripped = ChatColor.stripColor(line);
                if (stripped.startsWith("Type: ")) {
                    entityTypeRaw = stripped.replace("Type: ", "").trim();
                    break;
                }
            }

            SpawnerType type = SpawnerType.match(entityTypeRaw);
            if (type == null) return;

            Economy econ = GenericSpawners.getEconomy();
            if (econ != null) {
                if (econ.getBalance(player) < price) {
                    player.sendMessage(plugin.getPrefix() + "§cYou cannot afford this spawner! Price: §a$" + String.format(Locale.US, "%,.2f", price));
                    return;
                }
                econ.withdrawPlayer(player, price);
            }

            ItemStack spawnerItem = createFusedSpawner(type, 0);
            player.getInventory().addItem(spawnerItem);
            player.sendMessage(plugin.getPrefix() + "§aPurchased §e1x " + type.getFormattedName() + " Spawner §afor §e$" + String.format(Locale.US, "%,.2f", price) + "!");
            return;
        }

        if (title.equals("§8Stacked Spawner Dashboard")) {
            event.setCancelled(true);
            if (!player.hasMetadata("CurrentStackedSpawner")) return;

            StackedSpawner stacked = (StackedSpawner) player.getMetadata("CurrentStackedSpawner").get(0).value();
            int slot = event.getSlot();

            if (slot == 31) {
                player.closeInventory();
                return;
            }

            ItemStack clicked = event.getCurrentItem();
            if (clicked != null && clicked.getType() == Material.SPAWNER && clicked.hasItemMeta()) {
                String displayName = clicked.getItemMeta().getDisplayName();
                int tier = 0;
                if (displayName.contains("UNCOMMON")) tier = 1;
                else if (displayName.contains("RARE")) tier = 2;
                else if (displayName.contains("EPIC")) tier = 3;
                else if (displayName.contains("LEGENDARY")) tier = 4;

                plugin.getTierGroupMenu().openMenu(player, stacked, tier, 1);
            }
            return;
        }

        if (title.contains("Spawners (") && title.startsWith("§8")) {
            event.setCancelled(true);
            if (!player.hasMetadata("CurrentStackedSpawner")) return;

            StackedSpawner stacked = (StackedSpawner) player.getMetadata("CurrentStackedSpawner").get(0).value();
            int slot = event.getSlot();

            int tier = 0;
            if (title.contains("UNCOMMON")) tier = 1;
            else if (title.contains("RARE")) tier = 2;
            else if (title.contains("EPIC")) tier = 3;
            else if (title.contains("LEGENDARY")) tier = 4;

            int currentPage = 1;
            if (title.contains("(") && title.contains("/")) {
                String pagePart = title.substring(title.indexOf("(") + 1, title.indexOf("/")).trim();
                if (pagePart.matches("^-?\\d+$")) {
                    currentPage = Integer.parseInt(pagePart);
                }
            }

            if (slot == 45) {
                plugin.getTierGroupMenu().openMenu(player, stacked, tier, currentPage - 1);
                return;
            }
            if (slot == 53) {
                plugin.getTierGroupMenu().openMenu(player, stacked, tier, currentPage + 1);
                return;
            }
            if (slot == 49) {
                plugin.getStackOverviewMenu().openMenu(player, stacked);
                return;
            }

            ItemStack clicked = event.getCurrentItem();
            if (clicked != null && clicked.getType() == Material.SPAWNER && clicked.hasItemMeta() && clicked.getItemMeta().hasLore()) {
                for (String line : clicked.getItemMeta().getLore()) {
                    String stripped = ChatColor.stripColor(line);
                    if (stripped.startsWith("Spawner Index: #")) {
                        String rawIndex = stripped.replace("Spawner Index: #", "").trim();
                        if (rawIndex.matches("^-?\\d+$")) {
                            int index = Integer.parseInt(rawIndex) - 1;
                            if (index >= 0 && index < stacked.getSpawners().size()) {
                                plugin.getUpgradeMenu().openUpgradeMenu(player, stacked, stacked.getSpawners().get(index), index + 1);
                                return;
                            }
                        }
                    }
                }
            }
            return;
        }

        if (title.startsWith("§8Upgrade Spawner #")) {
            event.setCancelled(true);
            if (!player.hasMetadata("CurrentStackedSpawner") || !player.hasMetadata("CurrentIndividualSpawner")) return;

            StackedSpawner stacked = (StackedSpawner) player.getMetadata("CurrentStackedSpawner").get(0).value();
            IndividualSpawner ind = (IndividualSpawner) player.getMetadata("CurrentIndividualSpawner").get(0).value();
            Economy econ = GenericSpawners.getEconomy();

            int slot = event.getSlot();
            int maxAllowed = ind.getFusionTier() + 1;

            double baseCountCost = plugin.getConfig().getDouble("upgrades.count.base-cost", 5000.0);
            double baseSpeedCost = plugin.getConfig().getDouble("upgrades.speed.base-cost", 7500.0);
            double baseDropCost  = plugin.getConfig().getDouble("upgrades.drops.base-cost", 10000.0);

            if (slot == 11) {
                if (ind.getSpawnCountLevel() >= maxAllowed) {
                    player.sendMessage(plugin.getPrefix() + "§cSpawn Count is at maximum level for this rarity tier!");
                    return;
                }
                double cost = ind.getSpawnCountLevel() * baseCountCost;
                if (econ != null && econ.getBalance(player) < cost) {
                    player.sendMessage(plugin.getPrefix() + "§cYou cannot afford this upgrade!");
                    return;
                }
                if (econ != null) econ.withdrawPlayer(player, cost);

                ind.setSpawnCountLevel(ind.getSpawnCountLevel() + 1);
                player.sendMessage(plugin.getPrefix() + "§aUpgraded Spawn Count to Level " + ind.getSpawnCountLevel() + "!");
                plugin.getUpgradeMenu().openUpgradeMenu(player, stacked, ind, extractSpawnerNumber(title));
            } else if (slot == 13) {
                if (ind.getSpawnSpeedLevel() >= maxAllowed) {
                    player.sendMessage(plugin.getPrefix() + "§cSpawn Speed is at maximum level for this rarity tier!");
                    return;
                }
                double cost = ind.getSpawnSpeedLevel() * baseSpeedCost;
                if (econ != null && econ.getBalance(player) < cost) {
                    player.sendMessage(plugin.getPrefix() + "§cYou cannot afford this upgrade!");
                    return;
                }
                if (econ != null) econ.withdrawPlayer(player, cost);

                ind.setSpawnSpeedLevel(ind.getSpawnSpeedLevel() + 1);
                player.sendMessage(plugin.getPrefix() + "§aUpgraded Spawn Speed to Level " + ind.getSpawnSpeedLevel() + "!");
                plugin.getUpgradeMenu().openUpgradeMenu(player, stacked, ind, extractSpawnerNumber(title));
            } else if (slot == 15) {
                if (ind.getDropRateLevel() >= maxAllowed) {
                    player.sendMessage(plugin.getPrefix() + "§cDrop Rate is at maximum level for this rarity tier!");
                    return;
                }
                double cost = ind.getDropRateLevel() * baseDropCost;
                if (econ != null && econ.getBalance(player) < cost) {
                    player.sendMessage(plugin.getPrefix() + "§cYou cannot afford this upgrade!");
                    return;
                }
                if (econ != null) econ.withdrawPlayer(player, cost);

                ind.setDropRateLevel(ind.getDropRateLevel() + 1);
                player.sendMessage(plugin.getPrefix() + "§aUpgraded Drop Rate to Level " + ind.getDropRateLevel() + "!");
                plugin.getUpgradeMenu().openUpgradeMenu(player, stacked, ind, extractSpawnerNumber(title));
            } else if (slot == 22) {
                plugin.getTierGroupMenu().openMenu(player, stacked, ind.getFusionTier(), 1);
            }
        }
    }

    private ItemStack createFusedSpawner(SpawnerType type, int tier) {
        ItemStack spawner = new ItemStack(Material.SPAWNER, 1);
        ItemMeta meta = spawner.getItemMeta();

        meta.setDisplayName(getRarityPrefix(tier) + " " + getRarityColorCode(tier) + type.getFormattedName() + " Spawner");

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(fusionTierKey, PersistentDataType.INTEGER, tier);
        pdc.set(countKey, PersistentDataType.INTEGER, 1);
        pdc.set(speedKey, PersistentDataType.INTEGER, 1);
        pdc.set(dropKey, PersistentDataType.INTEGER, 1);

        int maxLvl = tier + 1;
        String bar = getProgressBar(1, maxLvl);
        List<String> lore = new ArrayList<>();
        lore.add("§8§m------------------------");
        lore.add("§7Type: §f" + type.name());
        lore.add("");
        lore.add("§e§lUpgrades:");
        lore.add(" §7Count: " + bar + " §f1/" + maxLvl);
        lore.add(" §7Speed: " + bar + " §f1/" + maxLvl);
        lore.add(" §7Drops: " + bar + " §f1/" + maxLvl);
        lore.add("§8§m------------------------");
        lore.add("§e▶ Place to stack and upgrade!");
        meta.setLore(lore);

        spawner.setItemMeta(meta);
        return spawner;
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

    private int extractSpawnerNumber(String title) {
        String numStr = title.replace("§8Upgrade Spawner #", "").trim();
        if (numStr.matches("^-?\\d+$")) {
            return Integer.parseInt(numStr);
        }
        return 1;
    }
}