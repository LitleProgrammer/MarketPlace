package de.littleprogrammer.marketplace.commands;

import de.littleprogrammer.marketplace.database.Database;
import de.littleprogrammer.marketplace.database.DatabaseTransaction;
import de.littleprogrammer.marketplace.files.LanguageFile;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class TransactionsCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        LanguageFile languageFile = new LanguageFile();

        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(languageFile.getString("command.notAPlayer"));
        }

        Player player = (Player) commandSender;
        if (!player.hasPermission("marketplace.history")) {
            player.sendMessage(languageFile.getString("command.noPermission"));
            return false;
        }

        List<DatabaseTransaction> transactions = new Database().getPlayerTransactions(player.getUniqueId());
        player.sendMessage(ChatColor.DARK_GRAY + "-----------------------------------");
        if (!transactions.isEmpty()) {
            for (DatabaseTransaction transaction : transactions) {
                if (transaction.getBuyer().toString().equals(player.getUniqueId().toString())) {
                    OfflinePlayer seller = Bukkit.getOfflinePlayer(transaction.getSeller());
                    player.sendMessage(languageFile.getInsertedString("messages.transactionLog.bought", "%itemName%", transaction.getItem().getType().toString(), "%player%", seller.getName(), "%price%", transaction.getPrice(), "%date%", transaction.getDate().toString(), "%market%", (transaction.isBlackMarket() ? "black-market" : "normal market")));
                } else {
                    OfflinePlayer buyer = Bukkit.getOfflinePlayer(transaction.getBuyer());
                    player.sendMessage(languageFile.getInsertedString("messages.transactionLog.sold", "%itemName%", transaction.getItem().getType().toString(), "%player%", buyer.getName(), "%price%", transaction.getPrice(), "%date%", transaction.getDate().toString(), "%market%", (transaction.isBlackMarket() ? "black-market" : "normal market")));
                }
            }
        } else {
            player.sendMessage(languageFile.getString("messages.transactionLog.noTransactions"));
        }
        player.sendMessage(ChatColor.DARK_GRAY + "-----------------------------------");

        return false;
    }
}
