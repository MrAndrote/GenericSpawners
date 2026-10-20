package com.yourname.genericspawners.listeners;

import com.yourname.genericspawners.GenericSpawners;
import com.yourname.genericspawners.menus.FusionMenu;
import com.yourname.genericspawners.menus.StackOverviewMenu;
import com.yourname.genericspawners.menus.TierGroupMenu;
import com.yourname.genericspawners.menus.UpgradeMenu;
import com.yourname.genericspawners.objects.IndividualSpawner;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class InventoryClickListener implements Listener {

    private static final NamespacedKey COUNT_KEY = new NamespacedKey(GenericSpawners.getInstance(), "spawn_count_lvl");
    private static final NamespacedKey SPEED_KEY = new NamespacedKey(GenericSpawners.getInstance(), "spawn_speed_lvl");
    private static final NamespacedKey DROP_KEY  = new NamespacedKey(GenericSpawners.getInstance(), "drop_rate_lvl");
    private static final NamespacedKey PRICE_KEY = new NamespacedKey(GenericSpawners.getInstance(), "spawner_price");
    private static final NamespacedKey FUSION_TIER_KEY = new NamespacedKey(GenericSpawners.getInstance(), "spawner_fusion_tier");

    private static final Map<String, String> FORMATTED_NAME_CACHE = new HashMap<>();

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
                        player.sendMessage(GenericSpawners.getInstance().getPrefix() + "§cPlease place spawners in the fusion slot!");
                        return;
                    }

                    int requiredAmount = GenericSpawners.getInstance().getConfig().getInt("fusion.required-amount", 3);
                    int maxTierConfig = GenericSpawners.getInstance().getConfig().getInt("fusion.max-tier", 4);

                    if (inputItem.getAmount() < requiredAmount) {
                        player.sendMessage(GenericSpawners.getInstance().getPrefix() + "§cYou need at least §e" + requiredAmount + "x §cspawners to fuse!");
                        return;
                    }

                    ItemMeta meta = inputItem.getItemMeta();
                    String entityType = null;
                    if (meta.hasLore()) {
                        for (String line : meta.getLore()) {
                            String stripped = ChatColor.stripColor(line);
                            if (stripped.startsWith("Type: ")) {
                                entityType = stripped.replace("Type: ", "").trim().toUpperCase();
                                break;
                            }
                        }
                    }

                    if (entityType == null) return;

                    int currentTier = meta.getPersistentDataContainer().getOrDefault(FUSION_TIER_KEY, PersistentDataType.INTEGER, 0);
                    if (currentTier >= maxTierConfig) {
                        player.sendMessage(GenericSpawners.getInstance().getPrefix() + "§cThis spawner is already at maximum rarity tier!");
                        return;
                    }

                    int newTier = currentTier + 1;
                    if (inputItem.getAmount() == requiredAmount) {
                        inv.setItem(13, new ItemStack(Material.AIR));
                    } else {
                        inputItem.setAmount(inputItem.getAmount() - requiredAmount);
                    }

                    ItemStack upgradedSpawner = createFusedSpawner(entityType, newTier);
                    if (player.getInventory().firstEmpty() != -1) {
                        player.getInventory().addItem(upgradedSpawner);
                    } else {
                        player.getWorld().dropItemNaturally(player.getLocation(), upgradedSpawner);
                    }

                    String formattedName = formatEntityName(entityType);
                    player.sendMessage(GenericSpawners.getInstance().getPrefix() + "§aSuccessfully fused §e" + requiredAmount + "x " + getRarityPrefix(currentTier) + " §e" + formattedName + " Spawners §ainto §e1x " + getRarityPrefix(newTier) + " §e" + formattedName + " Spawner§a!");
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
                FusionMenu.openFusionMenu(player);
                return;
            }

            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() != Material.SPAWNER || !clicked.hasItemMeta()) return;

            PersistentDataContainer shopPdc = clicked.getItemMeta().getPersistentDataContainer();
            if (!shopPdc.has(PRICE_KEY, PersistentDataType.DOUBLE)) return;
            double price = shopPdc.get(PRICE_KEY, PersistentDataType.DOUBLE);

            String entityType = null;
            for (String line : clicked.getItemMeta().getLore()) {
                String stripped = ChatColor.stripColor(line);
                if (stripped.startsWith("Type: ")) {
                    entityType = stripped.replace("Type: ", "").trim().toUpperCase();
                    break;
                }
            }
            if (entityType == null) return;

            Economy econ = GenericSpawners.getEconomy();
            if (econ != null) {
                if (econ.getBalance(player) < price) {
                    player.sendMessage(GenericSpawners.getInstance().getPrefix() + "§cYou cannot afford this spawner! Price: §a$" + String.format(Locale.US, "%,.2f", price));
                    return;
                }
                econ.withdrawPlayer(player, price);
            }

            ItemStack spawnerItem = createFusedSpawner(entityType, 0);
            player.getInventory().addItem(spawnerItem);
            player.sendMessage(GenericSpawners.getInstance().getPrefix() + "§aPurchased §e1x " + formatEntityName(entityType) + " Spawner §afor §e$" + String.format(Locale.US, "%,.2f", price) + "!");
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

                TierGroupMenu.openMenu(player, stacked, tier, 1);
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
            try {
                String pagePart = title.substring(title.indexOf("(") + 1, title.indexOf("/"));
                currentPage = Integer.parseInt(pagePart.trim());
            } catch (Exception ignored) {}

            if (slot == 45) {
                TierGroupMenu.openMenu(player, stacked, tier, currentPage - 1);
                return;
            }
            if (slot == 53) {
                TierGroupMenu.openMenu(player, stacked, tier, currentPage + 1);
                return;
            }
            if (slot == 49) {
                StackOverviewMenu.openMenu(player, stacked);
                return;
            }

            ItemStack clicked = event.getCurrentItem();
            if (clicked != null && clicked.getType() == Material.SPAWNER && clicked.hasItemMeta() && clicked.getItemMeta().hasLore()) {
                for (String line : clicked.getItemMeta().getLore()) {
                    String stripped = ChatColor.stripColor(line);
                    if (stripped.startsWith("Spawner Index: #")) {
                        try {
                            int index = Integer.parseInt(stripped.replace("Spawner Index: #", "").trim()) - 1;
                            if (index >= 0 && index < stacked.getSpawners().size()) {
                                UpgradeMenu.openUpgradeMenu(player, stacked, stacked.getSpawners().get(index), index + 1);
                                return;
                            }
                        } catch (NumberFormatException ignored) {}
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

            double baseCountCost = GenericSpawners.getInstance().getConfig().getDouble("upgrades.count.base-cost", 5000.0);
            double baseSpeedCost = GenericSpawners.getInstance().getConfig().getDouble("upgrades.speed.base-cost", 7500.0);
            double baseDropCost  = GenericSpawners.getInstance().getConfig().getDouble("upgrades.drops.base-cost", 10000.0);

            if (slot == 11) {
                if (ind.getSpawnCountLevel() >= maxAllowed) {
                    player.sendMessage(GenericSpawners.getInstance().getPrefix() + "§cSpawn Count is at maximum level for this rarity tier!");
                    return;
                }
                double cost = ind.getSpawnCountLevel() * baseCountCost;
                if (econ != null && econ.getBalance(player) < cost) {
                    player.sendMessage(GenericSpawners.getInstance().getPrefix() + "§cYou cannot afford this upgrade!");
                    return;
                }
                if (econ != null) econ.withdrawPlayer(player, cost);

                ind.setSpawnCountLevel(ind.getSpawnCountLevel() + 1);
                player.sendMessage(GenericSpawners.getInstance().getPrefix() + "§aUpgraded Spawn Count to Level " + ind.getSpawnCountLevel() + "!");
                UpgradeMenu.openUpgradeMenu(player, stacked, ind, extractSpawnerNumber(title));
            } else if (slot == 13) {
                if (ind.getSpawnSpeedLevel() >= maxAllowed) {
                    player.sendMessage(GenericSpawners.getInstance().getPrefix() + "§cSpawn Speed is at maximum level for this rarity tier!");
                    return;
                }
                double cost = ind.getSpawnSpeedLevel() * baseSpeedCost;
                if (econ != null && econ.getBalance(player) < cost) {
                    player.sendMessage(GenericSpawners.getInstance().getPrefix() + "§cYou cannot afford this upgrade!");
                    return;
                }
                if (econ != null) econ.withdrawPlayer(player, cost);

                ind.setSpawnSpeedLevel(ind.getSpawnSpeedLevel() + 1);
                player.sendMessage(GenericSpawners.getInstance().getPrefix() + "§aUpgraded Spawn Speed to Level " + ind.getSpawnSpeedLevel() + "!");
                UpgradeMenu.openUpgradeMenu(player, stacked, ind, extractSpawnerNumber(title));
            } else if (slot == 15) {
                if (ind.getDropRateLevel() >= maxAllowed) {
                    player.sendMessage(GenericSpawners.getInstance().getPrefix() + "§cDrop Rate is at maximum level for this rarity tier!");
                    return;
                }
                double cost = ind.getDropRateLevel() * baseDropCost;
                if (econ != null && econ.getBalance(player) < cost) {
                    player.sendMessage(GenericSpawners.getInstance().getPrefix() + "§cYou cannot afford this upgrade!");
                    return;
                }
                if (econ != null) econ.withdrawPlayer(player, cost);

                ind.setDropRateLevel(ind.getDropRateLevel() + 1);
                player.sendMessage(GenericSpawners.getInstance().getPrefix() + "§aUpgraded Drop Rate to Level " + ind.getDropRateLevel() + "!");
                UpgradeMenu.openUpgradeMenu(player, stacked, ind, extractSpawnerNumber(title));
            } else if (slot == 22) {
                TierGroupMenu.openMenu(player, stacked, ind.getFusionTier(), 1);
            }
        }
    }

    private ItemStack createFusedSpawner(String entityType, int tier) {
        ItemStack spawner = new ItemStack(Material.SPAWNER, 1);
        ItemMeta meta = spawner.getItemMeta();

        String formattedName = formatEntityName(entityType);
        meta.setDisplayName(getRarityPrefix(tier) + " " + getRarityColorCode(tier) + formattedName + " Spawner");

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(FUSION_TIER_KEY, PersistentDataType.INTEGER, tier);
        pdc.set(COUNT_KEY, PersistentDataType.INTEGER, 1);
        pdc.set(SPEED_KEY, PersistentDataType.INTEGER, 1);
        pdc.set(DROP_KEY, PersistentDataType.INTEGER, 1);

        int maxLvl = tier + 1;
        String bar = getProgressBar(1, maxLvl);
        List<String> lore = new ArrayList<>();
        lore.add("§8§m------------------------");
        lore.add("§7Type: §f" + entityType);
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

    private String formatEntityName(String entityType) {
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

    private int extractSpawnerNumber(String title) {
        try {
            return Integer.parseInt(title.replace("§8Upgrade Spawner #", "").trim());
        } catch (Exception e) {
            return 1;
        }
    }
}