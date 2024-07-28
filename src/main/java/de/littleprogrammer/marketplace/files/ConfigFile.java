package de.littleprogrammer.marketplace.files;

import de.littleprogrammer.marketplace.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ConfigFile {
    private File file = new File(Main.getInstance().getDataFolder(), "config.yml");
    private FileConfiguration configuration = YamlConfiguration.loadConfiguration(file);

    public String getString(String path) {
        return configuration.getString(path);
    }

    public boolean getBoolean(String path) {
        return configuration.getBoolean(path);
    }

    public void setBoolean(String path, boolean bool) {
        configuration.set(path, bool);
    }

    public int getInt(String path) {
        return configuration.getInt(path);
    }

    public double getDouble(String path) {
        return configuration.getDouble(path);
    }

    public Location locationBuilder(String path) {
        String world = configuration.getString(path + ".world");

        double x = configuration.getDouble(path + ".x");
        double y = configuration.getDouble(path + ".y");
        double z = configuration.getDouble(path + ".z");
        double yaw = 0;
        double pitch = 0;

        if (configuration.get(path + ".yaw") != null) {
            yaw = configuration.getDouble(path + ".yaw");
        }

        if (configuration.get(path + ".pitch") != null) {
            pitch = configuration.getDouble(path + ".pitch");
        }


        if (world != null) {
            Location location = new Location(Bukkit.getWorld(world), x, y, z, (float) yaw, (float) pitch);
            return location;
        } else {
            System.out.println("Didn't found location for " + path + ". Check if you set a valid location for this path.");

            return null;
        }

    }

    public List<?> getList(String path) {
        return configuration.getList(path);
    }

    public FileConfiguration getCfg() {
        return configuration;
    }

    public void save() {
        try {
            configuration.save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
