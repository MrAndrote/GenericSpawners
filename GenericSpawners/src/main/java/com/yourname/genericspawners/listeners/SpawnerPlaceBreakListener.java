package com.yourname.genericspawners.listeners;

import com.yourname.genericspawners.GenericSpawners;
import com.yourname.genericspawners.managers.HologramManager;
import com.yourname.genericspawners.managers.SpawnerManager;
import com.yourname.genericspawners.objects.IndividualSpawner;
import com.yourname.genericspawners.objects.StackedSpawner;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpawnerPlaceBreakListener implements Listener {

    private final NamespacedKey countKey = new NamespacedKey(GenericSpawners.getInstance(), "spawn_count_lvl");
    private final NamespacedKey speedKey = new NamespacedKey(GenericSpawners.getInstance(), "spawn_speed_lvl");
    private final NamespacedKey dropKey  = new NamespacedKey(GenericSpawners.getInstance(), "drop_rate_lvl");
    private final NamespacedKey fusionKey = new NamespacedKey(GenericSpawners.getInstance(), "spawner_fusion_tier");

    private static final Map<String, String> FORMATTED_NAME_CACHE = new HashMap<>();

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Block placedBlock = event.getBlockPlaced();
        if (placedBlock.getType() != Material.SPAWNER) return;

        ItemStack handItem = event.getItemInHand();
        if (handItem == null || !handItem.hasItemMeta()) return;

        String entityType = null;
        if (handItem.getItemMeta().hasLore()) {
            for (String line : handItem.getItemMeta().getLore()) {
                String stripped = ChatColor.stripColor(line);
                if (stripped.startsWith("Type: ")) {
                    entityType = stripped.replace("Type: ", "").trim().toUpperCase().replace(" ", "_");
                    break;
                }
            }
        }
        if (entityType == null) return;

        ItemMeta meta = handItem.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        int fusionTier = pdc.getOrDefault(fusionKey, PersistentDataType.INTEGER, 0);
        int countLvl   = pdc.getOrDefault(countKey, PersistentDataType.INTEGER, 1);
        int speedLvl   = pdc.getOrDefault(speedKey, PersistentDataType.INTEGER, 1);
        int dropLvl    = pdc.getOrDefault(dropKey, PersistentDataType.INTEGER, 1);

        Block clickedBlock = event.getBlockAgainst();
        Location targetLoc = placedBlock.getLocation();
        Player player = event.getPlayer();

        if (clickedBlock != null && clickedBlock.getType() == Material.SPAWNER) {
            StackedSpawner existingStack = SpawnerManager.getStackedSpawner(clickedBlock.getLocation());
            if (existingStack != null) {
                String stackType = existingStack.getSpawners().get(0).getEntityType();

                if (!stackType.equalsIgnoreCase(entityType)) {
                    event.setCancelled(true);
                    player.sendMessage(GenericSpawners.getInstance().getPrefix() + "§cYou can only stack spawners of the same type! (§e" + formatEntityName(stackType) + "§c)");
                    return;
                }

                int maxStackSize = GenericSpawners.getInstance().getConfig().getInt("spawners.max-stack-size", 250);
                int currentStackSize = existingStack.getStackSize();

                if (currentStackSize >= maxStackSize) {
                    event.setCancelled(true);
                    player.sendMessage(GenericSpawners.getInstance().getPrefix() + "§cThis spawner stack has reached the maximum limit of §e" + maxStackSize + " §cspawners!");
                    return;
                }

                int attemptedAdd = player.isSneaking() ? handItem.getAmount() : 1;
                int amountToAdd = Math.min(attemptedAdd, maxStackSize - currentStackSize);

                for (int i = 0; i < amountToAdd; i++) {
                    SpawnerManager.addIndividualToLocation(clickedBlock.getLocation(), new IndividualSpawner(entityType, countLvl, speedLvl, dropLvl, fusionTier));
                }

                event.setCancelled(true);
                if (player.getGameMode() != GameMode.CREATIVE) {
                    if (amountToAdd == handItem.getAmount()) {
                        if (event.getHand() == EquipmentSlot.HAND) {
                            player.getInventory().setItemInMainHand(null);
                        } else {
                            player.getInventory().setItemInOffHand(null);
                        }
                    } else {
                        handItem.setAmount(handItem.getAmount() - amountToAdd);
                    }
                }

                HologramManager.updateHologram(clickedBlock.getLocation(), existingStack);
                player.sendMessage(GenericSpawners.getInstance().getPrefix() + "§aAdded §e" + amountToAdd + "x §aspawner(s) to stack! (§e" + existingStack.getStackSize() + "x Total)");
                return;
            }
        }

        SpawnerManager.addIndividualToLocation(targetLoc, new IndividualSpawner(entityType, countLvl, speedLvl, dropLvl, fusionTier));
        HologramManager.updateHologram(targetLoc, SpawnerManager.getStackedSpawner(targetLoc));

        if (placedBlock.getState() instanceof CreatureSpawner cs) {
            cs.setSpawnedType(matchEntityType(entityType));
            cs.setMaxSpawnDelay(999999);
            cs.setMinSpawnDelay(999999);
            cs.setDelay(999999);
            cs.setRequiredPlayerRange(0);
            cs.setSpawnCount(0);
            cs.setMaxNearbyEntities(0);
            cs.update(true, true);
        }

        player.sendMessage(GenericSpawners.getInstance().getPrefix() + "§aPlaced new §e" + formatEntityName(entityType) + " Spawner§a!");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() != Material.SPAWNER) return;

        StackedSpawner stacked = SpawnerManager.getStackedSpawner(block.getLocation());
        if (stacked == null) return;

        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();
        boolean hasSilkTouch = tool != null && tool.hasItemMeta() && tool.containsEnchantment(Enchantment.SILK_TOUCH);

        event.setCancelled(true);

        int previousSize = stacked.getStackSize();
        int amountToBreak = player.isSneaking() ? previousSize : 1;
        List<IndividualSpawner> removedSpawners = new ArrayList<>();

        for (int i = 0; i < amountToBreak; i++) {
            IndividualSpawner removed = SpawnerManager.removeSpawnerFromLocation(block.getLocation());
            if (removed != null) removedSpawners.add(removed);
        }

        if (stacked.getStackSize() == 0) {
            block.setType(Material.AIR);
            HologramManager.removeHologram(block.getLocation());
        } else {
            HologramManager.updateHologram(block.getLocation(), stacked);
        }

        if (hasSilkTouch && player.getGameMode() != GameMode.CREATIVE) {
            for (IndividualSpawner removedIndividual : removedSpawners) {
                int countLvl = removedIndividual.getSpawnCountLevel();
                int speedLvl = removedIndividual.getSpawnSpeedLevel();
                int dropLvl  = removedIndividual.getDropRateLevel();
                int tier     = removedIndividual.getFusionTier();
                int maxLvl   = tier + 1;

                ItemStack dropItem = new ItemStack(Material.SPAWNER, 1);
                ItemMeta meta = dropItem.getItemMeta();
                String formattedName = formatEntityName(removedIndividual.getEntityType());

                meta.setDisplayName(getRarityPrefix(tier) + " " + getRarityColorCode(tier) + formattedName + " Spawner");

                PersistentDataContainer pdc = meta.getPersistentDataContainer();
                pdc.set(countKey, PersistentDataType.INTEGER, countLvl);
                pdc.set(speedKey, PersistentDataType.INTEGER, speedLvl);
                pdc.set(dropKey, PersistentDataType.INTEGER, dropLvl);
                pdc.set(fusionKey, PersistentDataType.INTEGER, tier);

                List<String> lore = new ArrayList<>();
                lore.add("§8§m------------------------");
                lore.add("§7Type: §f" + removedIndividual.getEntityType());
                lore.add("");
                lore.add("§e§lUpgrades:");
                lore.add(" §7Count: " + getProgressBar(countLvl, maxLvl) + " §f" + countLvl + "/" + maxLvl);
                lore.add(" §7Speed: " + getProgressBar(speedLvl, maxLvl) + " §f" + speedLvl + "/" + maxLvl);
                lore.add(" §7Drops: " + getProgressBar(dropLvl, maxLvl) + " §f" + dropLvl + "/" + maxLvl);
                lore.add("§8§m------------------------");
                lore.add("§e▶ Place to stack and upgrade!");

                meta.setLore(lore);
                dropItem.setItemMeta(meta);

                block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5), dropItem);
            }

            if (amountToBreak > 1) {
                player.sendMessage(GenericSpawners.getInstance().getPrefix() + "§eBroke all §a" + amountToBreak + "x §espawners from the stack!");
            } else if (stacked.getStackSize() == 0) {
                player.sendMessage(GenericSpawners.getInstance().getPrefix() + "§eBroke the final spawner from the stack.");
            } else {
                player.sendMessage(GenericSpawners.getInstance().getPrefix() + "§eRemoved 1 spawner from stack! (§a" + (previousSize - 1) + "x §eremaining)");
            }
        } else if (!hasSilkTouch) {
            player.sendMessage(GenericSpawners.getInstance().getPrefix() + "§cBroke spawner without Silk Touch (no spawner dropped).");
        }
    }

    private String getProgressBar(int currentLevel, int maxLevel) {
        StringBuilder sb = new StringBuilder("§8[");
        for (int i = 1; i <= maxLevel; i++) {
            sb.append(i <= currentLevel ? "§a■" : "§7□");
        }
        return sb.append("§8]").toString();
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

    private EntityType matchEntityType(String entityTypeStr) {
        if (entityTypeStr == null || entityTypeStr.isEmpty()) return EntityType.ZOMBIE;
        String clean = entityTypeStr.toUpperCase().replace(" ", "_").replace("-", "_");
        try {
            return EntityType.valueOf(clean);
        } catch (IllegalArgumentException ignored) {}

        if (clean.contains("IRON") || clean.contains("GOLEM")) return EntityType.IRON_GOLEM;
        if (clean.contains("SPIDER")) return EntityType.SPIDER;
        if (clean.contains("PIG")) return EntityType.PIG;
        if (clean.contains("COW")) return EntityType.COW;
        if (clean.contains("CHICKEN")) return EntityType.CHICKEN;
        if (clean.contains("SHEEP")) return EntityType.SHEEP;
        if (clean.contains("SKELETON")) return EntityType.SKELETON;
        if (clean.contains("CREEPER")) return EntityType.CREEPER;
        if (clean.contains("BLAZE")) return EntityType.BLAZE;
        if (clean.contains("ENDER")) return EntityType.ENDERMAN;
        if (clean.contains("WITCH")) return EntityType.WITCH;
        if (clean.contains("VILLAGER")) return EntityType.VILLAGER;

        return EntityType.ZOMBIE;
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
}