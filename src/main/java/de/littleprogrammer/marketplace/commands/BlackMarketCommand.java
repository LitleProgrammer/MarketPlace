package de.littleprogrammer.marketplace.commands;

import de.littleprogrammer.marketplace.database.Database;
import de.littleprogrammer.marketplace.files.LanguageFile;
import de.littleprogrammer.marketplace.guis.MarketPlaceGUI;
import org.bson.Document;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class BlackMarketCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        LanguageFile languageFile = new LanguageFile();

        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(languageFile.getString("command.notAPlayer"));
        }
        Player player = (Player) commandSender;

        if (!player.hasPermission("marketplace.blackmarket")) {
            player.sendMessage(languageFile.getString("command.noPermission"));
            return false;
        }

        List<Document> items = new Database().getAllItems();
        new MarketPlaceGUI(player, items, true);

        return false;
    }
}
