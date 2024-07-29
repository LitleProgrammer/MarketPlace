package de.littleprogrammer.marketplace.commands;

import de.littleprogrammer.marketplace.database.Database;
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

        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(ChatColor.RED + "You must be a player to use this command!");
            return false;
        }

        Player player = (Player) commandSender;
        if (args.length > 1) {
            player.sendMessage(ChatColor.RED + "Usage: /sell <price>");
            return false;
        }

        if (!player.hasPermission("marketplace.sell")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return false;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.AIR) {
            player.sendMessage(ChatColor.RED + "You are not holding an item!");
            return false;
        }

        new Database().addItem(item, Integer.parseInt(args[0]), player);
        player.getInventory().setItemInMainHand(null);
        player.sendMessage(ChatColor.GREEN + "You listed your item on the marketplace");

        return false;
    }
}
