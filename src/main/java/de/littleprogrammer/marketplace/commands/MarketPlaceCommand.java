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

public class MarketPlaceCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        LanguageFile languageFile = new LanguageFile();

        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(languageFile.getString("command.notAPlayer"));
        }
        Player player = (Player) commandSender;

        if (!player.hasPermission("marketplace.view")) {
            player.sendMessage(languageFile.getString("command.noPermission"));
            return false;
        }

        try {
            List<Document> items = new Database().getAllItems();
            new MarketPlaceGUI(player, items, false);
        } catch (Exception e) {
            e.printStackTrace();
            player.sendMessage(languageFile.getString("command.errorGeneral"));
        }

        return false;
    }
}
