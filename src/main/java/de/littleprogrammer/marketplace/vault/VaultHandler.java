package de.littleprogrammer.marketplace.vault;

import de.littleprogrammer.marketplace.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class VaultHandler {
    public static void addBalance(Player player, double money) {
        Main.getInstance().getEconomy().depositPlayer(Bukkit.getOfflinePlayer(player.getUniqueId()), money);
    }

    public static void removeBalance(Player player, double money) {
        Main.getInstance().getEconomy().withdrawPlayer(Bukkit.getOfflinePlayer(player.getUniqueId()), money);
    }

    public static boolean hasBalance(Player player, double money) {
        return Main.getInstance().getEconomy().getBalance(player) >= money;
    }
}
