package com.yourname.genericspawners;

import com.yourname.genericspawners.commands.SpawnerCommand;
import com.yourname.genericspawners.listeners.*;
import com.yourname.genericspawners.managers.DataStorageManager;
import com.yourname.genericspawners.managers.HologramManager;
import com.yourname.genericspawners.managers.SpawnerManager;
import com.yourname.genericspawners.menus.*;
import com.yourname.genericspawners.objects.StackedSpawner;
import com.yourname.genericspawners.tasks.SpawnerTask;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Location;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class GenericSpawners extends JavaPlugin {

    private static GenericSpawners instance;
    private static Economy economy;

    private ShopMenu shopMenu;
    private FusionMenu fusionMenu;
    private StackOverviewMenu stackOverviewMenu;
    private TierGroupMenu tierGroupMenu;
    private UpgradeMenu upgradeMenu;

    private final String prefix = "§8[§eGenericSpawners§8] ";

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        if (!setupEconomy()) {
            getLogger().severe("Disabled due to no Vault dependency found!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.shopMenu = new ShopMenu(this);
        this.fusionMenu = new FusionMenu();
        this.stackOverviewMenu = new StackOverviewMenu(this);
        this.tierGroupMenu = new TierGroupMenu(this);
        this.upgradeMenu = new UpgradeMenu(this);

        DataStorageManager.setup();
        DataStorageManager.loadSpawners();

        if (getCommand("spawner") != null) {
            SpawnerCommand cmd = new SpawnerCommand(this);
            getCommand("spawner").setExecutor(cmd);
            getCommand("spawner").setTabCompleter(cmd);
        }

        var pm = getServer().getPluginManager();
        pm.registerEvents(new SpawnerPlaceBreakListener(this), this);
        pm.registerEvents(new SpawnerInteractListener(this), this);
        pm.registerEvents(new InventoryClickListener(this), this);
        pm.registerEvents(new ChunkLoadListener(), this);
        pm.registerEvents(new InventoryCloseListener(), this);

        new SpawnerTask().runTaskTimer(this, 0L, 20L);

        for (StackedSpawner stacked : SpawnerManager.getActiveSpawners().values()) {
            Location loc = stacked.getLocation();
            if (loc != null && loc.getWorld() != null && loc.getWorld().isChunkLoaded(loc.getBlockX() >> 4, loc.getBlockZ() >> 4)) {
                HologramManager.updateHologram(loc, stacked);
            }
        }

        getLogger().info("Plugin successfully loaded!");
    }

    @Override
    public void onDisable() {
        DataStorageManager.saveSpawners(SpawnerManager.getActiveSpawners());

        for (StackedSpawner stacked : SpawnerManager.getActiveSpawners().values()) {
            if (stacked.getLocation() != null) {
                HologramManager.removeHologram(stacked.getLocation());
            }
        }
        instance = null;
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) return false;
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        economy = rsp.getProvider();
        return economy != null;
    }

    public static GenericSpawners getInstance() { return instance; }
    public static Economy getEconomy() { return economy; }
    public String getPrefix() { return prefix; }

    public ShopMenu getShopMenu() { return shopMenu; }
    public FusionMenu getFusionMenu() { return fusionMenu; }
    public StackOverviewMenu getStackOverviewMenu() { return stackOverviewMenu; }
    public TierGroupMenu getTierGroupMenu() { return tierGroupMenu; }
    public UpgradeMenu getUpgradeMenu() { return upgradeMenu; }
}