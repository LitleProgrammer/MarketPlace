package de.littleprogrammer.marketplace.utils;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class ItemUtils {

    public static String serializeItem(ItemStack item) {
        try
        {
            final ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
            final BukkitObjectOutputStream objectOutputStream = new BukkitObjectOutputStream(arrayOutputStream);
            objectOutputStream.writeObject(item);
            return Base64Coder.encodeLines(arrayOutputStream.toByteArray());
        }
        catch (final Exception exception)
        {
            throw new RuntimeException("Failed to convert item into base64", exception);
        }
    }

    public static ItemStack deserializeItem(String base64) {
        try
        {
            final ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(Base64Coder.decodeLines(base64));
            final BukkitObjectInputStream objectInputStream = new BukkitObjectInputStream(arrayInputStream);
            return (ItemStack) objectInputStream.readObject();
        }
        catch (final Exception exception)
        {
            throw new RuntimeException("Failed to convert bas64 to a valid itemStack", exception);
        }
    }

}
