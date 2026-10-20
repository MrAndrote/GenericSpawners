package com.yourname.genericspawners.commands;

import com.yourname.genericspawners.GenericSpawners;
import com.yourname.genericspawners.menus.FusionMenu;
import com.yourname.genericspawners.menus.ShopMenu;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpawnerCommand implements CommandExecutor, TabCompleter {

    private static final NamespacedKey COUNT_KEY = new NamespacedKey(GenericSpawners.getInstance(), "spawn_count_lvl");
    private static final NamespacedKey SPEED_KEY = new NamespacedKey(GenericSpawners.getInstance(), "spawn_speed_lvl");
    private static final NamespacedKey DROP_KEY  = new NamespacedKey(GenericSpawners.getInstance(), "drop_rate_lvl");
    private static final NamespacedKey FUSION_TIER_KEY = new NamespacedKey(GenericSpawners.getInstance(), "spawner_fusion_tier");

    private static final Map<String, String> FORMATTED_NAME_CACHE = new HashMap<>();

    private static final String[] VALID_MOBS = {
            "Zombie", "Skeleton", "Spider", "Creeper", "Blaze",
            "Enderman", "Witch", "Pig", "Cow", "Sheep", "Chicken",
            "Villager", "Iron_Golem"
    };

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String prefix = GenericSpawners.getInstance().getPrefix();

        if (args.length == 0 || args[0].equalsIgnoreCase("shop")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Only players can open the spawner shop.");
                return true;
            }
            ShopMenu.openShop((Player) sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("fuse")) {
            if (!(sender instanceof Player)) return true;
            FusionMenu.openFusionMenu((Player) sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("give")) {
            if (!sender.hasPermission("genericspawners.admin")) {
                sender.sendMessage(prefix + "§cYou do not have permission to use this command.");
                return true;
            }

            if (args.length < 2) {
                sender.sendMessage(prefix + "§cUsage: /spawner give [player] <mob> [amount]");
                return true;
            }

            Player target;
            int mobStartIndex;

            Player potentialTarget = Bukkit.getPlayer(args[1]);
            if (potentialTarget != null) {
                target = potentialTarget;
                mobStartIndex = 2;
            } else {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(prefix + "§cConsole must specify a target player!");
                    return true;
                }
                target = (Player) sender;
                mobStartIndex = 1;
            }

            if (args.length <= mobStartIndex) {
                sender.sendMessage(prefix + "§cPlease specify a valid spawner type.");
                return true;
            }

            StringBuilder mobBuilder = new StringBuilder();
            int amount = 1;

            for (int i = mobStartIndex; i < args.length; i++) {
                String arg = args[i];
                if (i == args.length - 1) {
                    try {
                        amount = Integer.parseInt(arg);
                        continue;
                    } catch (NumberFormatException ignored) {}
                }
                if (mobBuilder.length() > 0) mobBuilder.append(" ");
                mobBuilder.append(arg);
            }

            String entityType = matchFixedType(mobBuilder.toString().trim());
            if (entityType == null) {
                sender.sendMessage(prefix + "§cInvalid spawner type specified!");
                return true;
            }

            ItemStack spawnerItem = new ItemStack(Material.SPAWNER, amount);
            ItemMeta meta = spawnerItem.getItemMeta();
            String formattedName = formatEntityName(entityType);

            meta.setDisplayName("§7§lCOMMON §7" + formattedName + " Spawner");

            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(COUNT_KEY, PersistentDataType.INTEGER, 1);
            pdc.set(SPEED_KEY, PersistentDataType.INTEGER, 1);
            pdc.set(DROP_KEY,  PersistentDataType.INTEGER, 1);
            pdc.set(FUSION_TIER_KEY, PersistentDataType.INTEGER, 0);

            String bar1 = getProgressBar(1, 1);
            List<String> lore = new ArrayList<>();
            lore.add("§8§m------------------------");
            lore.add("§7Type: §f" + entityType);
            lore.add("");
            lore.add("§e§lUpgrades:");
            lore.add(" §7Count: " + bar1 + " §f1/1");
            lore.add(" §7Speed: " + bar1 + " §f1/1");
            lore.add(" §7Drops: " + bar1 + " §f1/1");
            lore.add("§8§m------------------------");
            lore.add("§e▶ Place to stack and upgrade!");
            meta.setLore(lore);
            spawnerItem.setItemMeta(meta);

            target.getInventory().addItem(spawnerItem);
            sender.sendMessage(prefix + "§aGave " + amount + "x " + formattedName + " spawner(s) to " + target.getName() + ".");
            if (!target.equals(sender)) {
                target.sendMessage(prefix + "§aYou received " + amount + "x " + formattedName + " spawner(s)!");
            }
            return true;
        }

        sender.sendMessage(prefix + "§cUnknown subcommand. Use /spawner, /spawner shop, or /spawner fuse.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            if ("shop".startsWith(args[0].toLowerCase())) completions.add("shop");
            if ("fuse".startsWith(args[0].toLowerCase())) completions.add("fuse");
            if ("give".startsWith(args[0].toLowerCase())) completions.add("give");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                    completions.add(player.getName());
                }
            }
            for (String mob : VALID_MOBS) {
                if (mob.toLowerCase().startsWith(args[1].toLowerCase())) {
                    completions.add(mob);
                }
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            for (String mob : VALID_MOBS) {
                if (mob.toLowerCase().startsWith(args[2].toLowerCase())) {
                    completions.add(mob);
                }
            }
        }

        return completions;
    }

    private String getProgressBar(int currentLevel, int maxLevel) {
        StringBuilder sb = new StringBuilder("§8[");
        for (int i = 1; i <= maxLevel; i++) {
            sb.append(i <= currentLevel ? "§a■" : "§7□");
        }
        return sb.append("§8]").toString();
    }

    private String matchFixedType(String input) {
        if (input == null || input.isEmpty()) return null;
        String name = input.toUpperCase().replace("_", " ").trim();

        if (name.contains("SKELETON")) return "SKELETON";
        if (name.contains("SPIDER")) return "SPIDER";
        if (name.contains("CREEPER")) return "CREEPER";
        if (name.contains("BLAZE")) return "BLAZE";
        if (name.contains("ENDERMAN") || name.contains("ENDER")) return "ENDERMAN";
        if (name.contains("WITCH")) return "WITCH";
        if (name.contains("PIG")) return "PIG";
        if (name.contains("COW")) return "COW";
        if (name.contains("SHEEP")) return "SHEEP";
        if (name.contains("CHICKEN")) return "CHICKEN";
        if (name.contains("VILLAGER")) return "VILLAGER";
        if (name.contains("IRON GOLEM") || name.contains("GOLEM") || name.contains("IRONGOLEM")) return "IRON_GOLEM";
        if (name.contains("ZOMBIE")) return "ZOMBIE";

        return null;
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