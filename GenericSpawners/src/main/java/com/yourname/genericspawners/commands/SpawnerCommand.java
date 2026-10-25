package com.yourname.genericspawners.commands;

import com.yourname.genericspawners.GenericSpawners;
import com.yourname.genericspawners.objects.SpawnerType;
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
import java.util.List;

public class SpawnerCommand implements CommandExecutor, TabCompleter {

    private final GenericSpawners plugin;
    private final NamespacedKey countKey;
    private final NamespacedKey speedKey;
    private final NamespacedKey dropKey;
    private final NamespacedKey fusionKey;

    public SpawnerCommand(GenericSpawners plugin) {
        this.plugin = plugin;
        this.countKey = new NamespacedKey(plugin, "spawn_count_lvl");
        this.speedKey = new NamespacedKey(plugin, "spawn_speed_lvl");
        this.dropKey = new NamespacedKey(plugin, "drop_rate_lvl");
        this.fusionKey = new NamespacedKey(plugin, "spawner_fusion_tier");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String prefix = plugin.getPrefix();

        if (args.length == 0) {
            sendHelp(sender, prefix);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        if (subCommand.equals("shop")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Only players can open the spawner shop.");
                return true;
            }
            plugin.getShopMenu().openShop(player);
            return true;
        }

        if (subCommand.equals("fuse")) {
            if (!(sender instanceof Player player)) return true;
            plugin.getFusionMenu().openFusionMenu(player);
            return true;
        }

        if (subCommand.equals("give")) {
            handleGiveCommand(sender, args, prefix);
            return true;
        }

        sendHelp(sender, prefix);
        return true;
    }

    private void sendHelp(CommandSender sender, String prefix) {
        sender.sendMessage(prefix + "§e--- Spawner Commands ---");
        sender.sendMessage(prefix + "§a/spawner shop §7- Åbn spawner-shoppen");
        sender.sendMessage(prefix + "§a/spawner fuse §7- Åbn fusion-menuen");
        if (sender.hasPermission("genericspawners.admin")) {
            sender.sendMessage(prefix + "§c/spawner give [player] <mob> [amount] §7- Giv en spawner");
        }
    }

    private void handleGiveCommand(CommandSender sender, String[] args, String prefix) {
        if (!sender.hasPermission("genericspawners.admin")) {
            sender.sendMessage(prefix + "§cYou do not have permission to use this command.");
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(prefix + "§cUsage: /spawner give [player] <mob> [amount]");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        int mobStartIndex = 1;

        if (target != null) {
            mobStartIndex = 2;
        } else {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(prefix + "§cConsole must specify a target player!");
                return;
            }
            target = player;
            mobStartIndex = 1;
        }

        if (args.length <= mobStartIndex) {
            sender.sendMessage(prefix + "§cPlease specify a valid spawner type.");
            return;
        }

        int amount = 1;
        int mobEndIndex = args.length;

        String lastArg = args[args.length - 1];
        if (lastArg.matches("^-?\\d+$")) {
            amount = Math.max(1, Integer.parseInt(lastArg));
            mobEndIndex = args.length - 1;
        }

        StringBuilder mobBuilder = new StringBuilder();
        for (int i = mobStartIndex; i < mobEndIndex; i++) {
            if (mobBuilder.length() > 0) mobBuilder.append(" ");
            mobBuilder.append(args[i]);
        }

        SpawnerType spawnerType = SpawnerType.match(mobBuilder.toString().trim());
        if (spawnerType == null) {
            sender.sendMessage(prefix + "§cInvalid spawner type specified!");
            return;
        }

        ItemStack spawnerItem = createSpawnerItem(spawnerType, 1, 1, 1, 0, amount);
        target.getInventory().addItem(spawnerItem);

        sender.sendMessage(prefix + "§aGave " + amount + "x " + spawnerType.getFormattedName() + " spawner(s) to " + target.getName() + ".");
        if (!target.equals(sender)) {
            target.sendMessage(prefix + "§aYou received " + amount + "x " + spawnerType.getFormattedName() + " spawner(s)!");
        }
    }

    private ItemStack createSpawnerItem(SpawnerType type, int countLvl, int speedLvl, int dropLvl, int tier, int amount) {
        ItemStack item = new ItemStack(Material.SPAWNER, amount);
        ItemMeta meta = item.getItemMeta();
        String rarityPrefix = getRarityPrefix(tier);

        meta.setDisplayName(rarityPrefix + " " + getRarityColorCode(tier) + type.getFormattedName() + " Spawner");

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(countKey, PersistentDataType.INTEGER, countLvl);
        pdc.set(speedKey, PersistentDataType.INTEGER, speedLvl);
        pdc.set(dropKey, PersistentDataType.INTEGER, dropLvl);
        pdc.set(fusionKey, PersistentDataType.INTEGER, tier);

        int maxLvl = tier + 1;
        String bar = getProgressBar(1, maxLvl);
        List<String> lore = new ArrayList<>();
        lore.add("§8§m------------------------");
        lore.add("§7Type: §f" + type.name());
        lore.add("");
        lore.add("§e§lUpgrades:");
        lore.add(" §7Count: " + bar + " §f" + countLvl + "/" + maxLvl);
        lore.add(" §7Speed: " + bar + " §f" + speedLvl + "/" + maxLvl);
        lore.add(" §7Drops: " + bar + " §f" + dropLvl + "/" + maxLvl);
        lore.add("§8§m------------------------");
        lore.add("§e▶ Place to stack and upgrade!");

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
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
            for (SpawnerType type : SpawnerType.values()) {
                if (type.name().toLowerCase().startsWith(args[1].toLowerCase())) {
                    completions.add(type.name());
                }
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            for (SpawnerType type : SpawnerType.values()) {
                if (type.name().toLowerCase().startsWith(args[2].toLowerCase())) {
                    completions.add(type.name());
                }
            }
        }

        return completions;
    }
}