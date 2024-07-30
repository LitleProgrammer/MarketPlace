package de.littleprogrammer.marketplace.files;

import de.littleprogrammer.marketplace.Main;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class LanguageFile {
    private File file = new File(Main.getInstance().getDataFolder(), "language.yml");
    private FileConfiguration configuration = YamlConfiguration.loadConfiguration(file);

    public String getString(String key) {
        return ChatColor.translateAlternateColorCodes('&', (configuration.getString(key) == null ? " " : configuration.getString(key)));
    }

    public String getInsertedString(String path, Object... placeholdersAndValues) {
        String message = configuration.getString(path);
        StringBuilder builder = new StringBuilder(message);

        if (!(placeholdersAndValues.length % 2 == 0)) {
            System.out.println("The amount of items in the list is not even. Please check for message: " + path);
        }

        for (int i = 0; i < placeholdersAndValues.length; i += 2) {
            String placeholder = String.valueOf(placeholdersAndValues[i]);
            String value = String.valueOf(placeholdersAndValues[i + 1]);

            int placeholderLength = placeholder.length();
            int startIndex = message.indexOf(placeholder);

            if (startIndex != -1) {
                builder.replace(startIndex, (startIndex + placeholderLength), value);
            }

            message = builder.toString();
        }

        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
