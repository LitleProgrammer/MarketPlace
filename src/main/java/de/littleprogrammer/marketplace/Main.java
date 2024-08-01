package de.littleprogrammer.marketplace;

import de.littleprogrammer.marketplace.commands.BlackMarketCommand;
import de.littleprogrammer.marketplace.commands.MarketPlaceCommand;
import de.littleprogrammer.marketplace.commands.SellCommand;
import de.littleprogrammer.marketplace.commands.TransactionsCommand;
import de.littleprogrammer.marketplace.listeners.InventoryClickListener;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class Main extends JavaPlugin {

    private static Main instance;
    private static Economy economy = null;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;

        //Commands
        getCommand("sell").setExecutor(new SellCommand());
        getCommand("marketplace").setExecutor(new MarketPlaceCommand());
        getCommand("blackmarket").setExecutor(new BlackMarketCommand());
        getCommand("transactions").setExecutor(new TransactionsCommand());

        //Listeners
        Bukkit.getPluginManager().registerEvents(new InventoryClickListener(), this);

        //Files
        File databaseConfig = new File(getDataFolder(), "database.yml");
        if (!databaseConfig.exists()) {
            //File doesn't exist -> creating it from resources
            saveResource("database.yml", false);
        }

        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            //File doesn't exist -> creating it from resources
            saveResource("config.yml", false);
        }

        File langFile = new File(getDataFolder(), "language.yml");
        if (!langFile.exists()) {
            saveResource("language.yml", false);
        }

        if (!setupEconomy()) {
            System.out.println("Failed to setup vault connection!");
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }

    public static Main getInstance() {
        return instance;
    }

    public Economy getEconomy() {
        return economy;
    }
}
