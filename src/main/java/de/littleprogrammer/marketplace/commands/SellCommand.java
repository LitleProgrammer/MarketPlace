package de.littleprogrammer.marketplace.commands;

import de.littleprogrammer.marketplace.database.Database;
import de.littleprogrammer.marketplace.files.LanguageFile;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SellCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        //  /sell <price>
        LanguageFile languageFile = new LanguageFile();

        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(languageFile.getString("command.notAPlayer"));
            return false;
        }

        Player player = (Player) commandSender;
        if (args.length > 1) {
            player.sendMessage(languageFile.getString("command.sell.usage"));
            return false;
        }

        if (!player.hasPermission("marketplace.sell")) {
            player.sendMessage(languageFile.getString("command.noPermission"));
            return false;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.AIR) {
            player.sendMessage(languageFile.getString("command.sell.noItem"));
            return false;
        }

        new Database().addItem(item, Integer.parseInt(args[0]), player);
        player.getInventory().setItemInMainHand(null);
        player.sendMessage(languageFile.getString("command.sell.success"));

        return false;
    }
}
