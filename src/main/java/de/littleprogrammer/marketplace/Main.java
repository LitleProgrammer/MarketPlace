package de.littleprogrammer.marketplace;

import de.littleprogrammer.marketplace.commands.MarketPlaceCommand;
import de.littleprogrammer.marketplace.commands.SellCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class Main extends JavaPlugin {

    private static Main instance;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;

        //Commands
        getCommand("sell").setExecutor(new SellCommand());
        getCommand("marketplace").setExecutor(new MarketPlaceCommand());

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

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static Main getInstance() {
        return instance;
    }
}
