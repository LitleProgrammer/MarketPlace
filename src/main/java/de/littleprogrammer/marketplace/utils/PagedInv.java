package de.littleprogrammer.marketplace.utils;

import de.littleprogrammer.marketplace.Main;
import de.littleprogrammer.marketplace.files.LanguageFile;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PagedInv implements Listener {

    //Classes
    private HeadUtils headUtils;
    private LanguageFile languageFile;


    //Integers
    private int page = 0;
    private int rows;
    private int[] slots;
    private int amountSlots;


    //Maps
    private Map<Integer, ItemStack> specialItems;


    //Lists
    private List<ItemStack> items = new ArrayList<>();
    private List<ItemStack> pageItems = new ArrayList<>();


    //Strings
    private String title;


    //Inventories
    private Inventory inv;


    //Booleans
    private boolean borderTop;


    //UUID's
    private UUID uuid;


    /**
     *
     * @param page
     * the page that should be opened (first page is 0, second page is 1...)
     * @param items
     * a list of the items filling the slots
     * @param slots
     * the slot ID's where the items should go in the inv (don't forget to count for if you have a top border)
     * @param rows
     * the amount of rows the inv should have (exclude the top and the bottom control row (only the rows where changing items are going))
     * @param title
     * the title of the gui
     * @param borderTop
     * if it should have a top border made of gray stained-glass pane
     */
    public Inventory buildPagedInv(int page, List<ItemStack> items, int[] slots, int rows, String title, boolean borderTop, UUID uuid, Map<Integer, ItemStack> specialItems) {
        this.page = page;
        this.items = items;
        this.slots = slots;
        this.rows = rows;
        this.title = title;
        this.borderTop = borderTop;
        this.uuid = uuid;
        this.specialItems = specialItems;
        amountSlots = slots.length;

        if(Main.getInstance().getPagedInv(uuid) != null) {
            Main.getInstance().removePagedInv(uuid);
        }
        Main.getInstance().addPagedInv(uuid, this);
        return createInv();
    }

    public Inventory nextPage() {
        page += 1;

        return createInv();
    }

    public Inventory prevPage() {
        page -= 1;

        return createInv();
    }

    private Inventory createInv() {
        languageFile = new LanguageFile();

        String pageString = languageFile.getString("common.page");
        inv = Bukkit.createInventory(null, (borderTop ? ((rows + 2) * 9) : ((rows + 1) * 9)), title + pageString + page);

        pageItems.clear();

        int startIndex = ((9 * rows) * (page)); //page + 1 ???

        for (int i = startIndex; i < (startIndex + (9 * rows)); i++) {
            //Looping through all items that should be on that page and adding them to pageItems arrayList
            if (items.size() <= i) {
                //Breaking, since if it's equal it reached the limit of index
                break;
            }
            pageItems.add(items.get(i));
        }

        ItemStack blankItem = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta blankMeta = blankItem.getItemMeta();
        blankMeta.setDisplayName(ChatColor.GRAY + "");
        blankItem.setItemMeta(blankMeta);
        ItemUtils.setPdc(blankItem,"fillItem");

        if (borderTop) {
            //Inv should have a top border
            for (int i = 0; i < 9; i++) {
                inv.setItem(i, blankItem);
            }
        }

        for (int i = 0; i < slots.length; i++) {
            //Adding one item per slot
            if (i >= pageItems.size()) {
                //Checks, if there are items left in the list
                break;
            }
            inv.setItem(slots[i], pageItems.get(i)); //(borderTop ? i + 9 : i)
        }

        for (int i = (rows + 1) * 9; i < ((rows + 1) * 9 + 9); i++) {
            //Looping through the last line and setting the blank item
            inv.setItem(i, blankItem);

            if (i == (rows + 1) * 9 + 3) {
                //It's the slot before
                if (page > 0) {
                    //There's a prev page
                    inv.setItem(i, backItem());
                }
            }
            if (i == ((rows + 1) * 9) + 5) {
                //It's the slot after
                if (page < Math.ceil((items.size() / slots.length))) {
                    //There is a next page
                    inv.setItem(i, forwardItem());
                }
            }
        }

        if (specialItems != null) {
            for (int i = 0; i < inv.getSize(); i++) {
                if (specialItems.containsKey(i)) {
                    //Slot should be filled
                    inv.setItem(i, specialItems.get(i));
                }
            }
        }
        return inv;
    }


    private ItemStack forwardItem() {
        headUtils = new HeadUtils();
        languageFile = new LanguageFile();

        String text;

        ItemStack forwardItem = headUtils.makeHead("69f61acec3aadc5e518259d5c1131ac149b427beeff5e57d879bd3131a172a7");
        ItemMeta forwardMeta = forwardItem.getItemMeta();
        text = languageFile.getString("playerTurn.buildHouse.next");
        forwardMeta.setDisplayName(text);
        forwardItem.setItemMeta(forwardMeta);
        ItemUtils.setPdc(forwardItem,"pagedInvForward:" + uuid);

        return forwardItem;
    }

    private ItemStack backItem() {
        headUtils = new HeadUtils();
        languageFile = new LanguageFile();

        String text;

        ItemStack backItem = headUtils.makeHead("3d4deef18397ded73888f2a44b1dba6e85c953b8afe7221deeff0ceeb6ac5e3");
        ItemMeta backMeta = backItem.getItemMeta();
        text = languageFile.getString("playerTurn.buildHouse.back");
        backMeta.setDisplayName(text);
        backItem.setItemMeta(backMeta);
        ItemUtils.setPdc(backItem,"pagedInvBack:" + uuid);

        return backItem;
    }


    @EventHandler
    public void onPlayerInteract(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack item = event.getCurrentItem();

        if (item == null) { return; }
        if (item.getItemMeta() == null) { return; }
        if (!ItemUtils.getPdc(item).contains("pagedInv")) { return; }

        if (ItemUtils.getPdc(item).contains("pagedInvForward")) {
            //It's forward item
            event.setCancelled(true);

            String input = ItemUtils.getPdc(item);
            UUID uuid = UUID.fromString(input.split(":")[1]);
            PagedInv pagedInv = Main.getInstance().getPagedInv(uuid);
            InvUtils.switchInv(pagedInv.nextPage(), player);
        }

        if (ItemUtils.getPdc(item).contains("pagedInvBack")) {
            //It's back item
            event.setCancelled(true);

            String input = ItemUtils.getPdc(item);
            UUID uuid = UUID.fromString(input.split(":")[1]);
            PagedInv pagedInv = Main.getInstance().getPagedInv(uuid);
            InvUtils.switchInv(pagedInv.nextPage(), player);
        }
    }
}
