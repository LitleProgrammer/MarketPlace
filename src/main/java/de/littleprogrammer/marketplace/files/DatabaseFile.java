package de.littleprogrammer.marketplace.files;

import de.littleprogrammer.marketplace.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class DatabaseFile {
    private File file = new File(Main.getInstance().getDataFolder(), "database.yml");
    private FileConfiguration configuration = YamlConfiguration.loadConfiguration(file);

    public String getString(String path) {
        return configuration.getString(path);
    }

    public int getInt(String path) {
        return configuration.getInt(path);
    }
}
