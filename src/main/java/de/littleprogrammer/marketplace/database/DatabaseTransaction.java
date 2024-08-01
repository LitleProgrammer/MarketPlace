package de.littleprogrammer.marketplace.database;

import org.bukkit.inventory.ItemStack;

import java.util.Date;
import java.util.UUID;

public class DatabaseTransaction {

    private int price;
    private UUID buyer;
    private UUID seller;
    private ItemStack item;
    private Date date;
    private boolean blackMarket;

    public DatabaseTransaction(int price, UUID buyer, UUID seller, ItemStack item, Date date, boolean blackMarket) {
        this.price = price;
        this.buyer = buyer;
        this.seller = seller;
        this.item = item;
        this.date = date;
        this.blackMarket = blackMarket;
    }

    public int getPrice() {
        return price;
    }

    public UUID getBuyer() {
        return buyer;
    }

    public UUID getSeller() {
        return seller;
    }

    public ItemStack getItem() {
        return item;
    }

    public Date getDate() {
        return date;
    }

    public boolean isBlackMarket() {
        return blackMarket;
    }
}
