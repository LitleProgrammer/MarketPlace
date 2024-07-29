package de.littleprogrammer.marketplace.commands;

import de.littleprogrammer.marketplace.database.Database;
import net.md_5.bungee.api.ChatColor;
import org.bson.Document;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class MarketPlaceCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {

        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(ChatColor.RED + "You must be a player to use this command!");
        }
        Player player = (Player) commandSender;

        if (!player.hasPermission("marketplace.view")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return false;
        }

        List<Document> items = new Database().getAllItems();
        System.out.println(items);

        return false;
    }
}
