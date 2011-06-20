package com.nhksos.jjfs85.BetterShop;

import com.jascotty2.Item.CreatureItem;
import com.jascotty2.Item.JItem;
import com.jascotty2.Item.JItemDB;
import com.jascotty2.Item.ItemStockEntry;
import com.jascotty2.Item.JItems;
import com.jascotty2.Item.Kit;
import com.jascotty2.Item.Kit.KitItem;
import com.jascotty2.Shop.UserTransaction;
import com.jascotty2.util.Str;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.logging.Level;
import org.bukkit.Server;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

public class BSutils {

    public static enum BetterShopPermission {

        /**
         * generic user permissions
         */
        USER("BetterShop.user"),
        /**
         * look through shop listing of prices
         */
        USER_LIST("BetterShop.user.list"),
        /**
         * check the price of item(s)
         */
        USER_CHECK("BetterShop.user.check"),
        /**
         * view ingame help menu
         */
        USER_HELP("BetterShop.user.help"),
        /**
         * buy items from the shop
         */
        USER_BUY("BetterShop.user.buy"),
        /**
         * sell items to the shop
         */
        USER_SELL("BetterShop.user.sell"),
        /**
         * generic admin permissions
         */
        ADMIN("BetterShop.admin"),
        /**
         * add/edit items to/in the shop
         */
        ADMIN_ADD("BetterShop.admin.add"),
        /**
         * remove items from the shop
         */
        ADMIN_REMOVE("BetterShop.admin.remove"),
        /**
         * reload configuration & pricelist
         */
        ADMIN_LOAD("BetterShop.admin.load"),
        /**
         * show shop stats 
         */
        ADMIN_INFO("BetterShop.admin.info"),
        /**
         * gives the ability to purchase 'illegal' items
         */
        ADMIN_ILLEGAL("BetterShop.admin.illegal"),
        /**
         * backing up and restoring the pricelist
         */
        ADMIN_BACKUP("BetterShop.admin.backup"),
        /**
         * manually restock (if item stock is enabled)
         */
        ADMIN_RESTOCK("BetterShop.admin.restock"),
        /**
         * ability to add/remove shop signs
         */
        ADMIN_MAKESIGN("BetterShop.admin.makesign");
        String permissionNode = null;

        BetterShopPermission(String per) {
            permissionNode = per;
        }

        @Override
        public String toString() {
            return permissionNode;
        }
    }

    static boolean anonymousCheck(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sendMessage(sender, "Cannot execute that command, I don't know who you are!");
            return true;
        } else {
            return false;
        }
    }

    static boolean decimalSupported() {
        return BetterShop.iConomy != null;
    }

    static boolean credit(Player player, double amount) {
        if (amount <= 0) {
            return amount == 0;
        }
        if (BetterShop.iConomy != null || BetterShop.legacyIConomy != null) {
            try {
                if (iconomyEdit(player.getName(), amount)) {
                    return true;
                }
            } catch (Exception ex) {
            }
            // something seems to be wrong with iConomy: reload it
            BetterShop.Log(Level.SEVERE, "Failed to credit player: attempting iConomy reload", false);
            if (reloadIConomy(player.getServer())) {
                try {
                    if (iconomyEdit(player.getName(), amount)) {
                        return true;
                    }
                } catch (Exception ex) {
                }
            }
            BetterShop.Log(Level.SEVERE, "iConomy reload failed to resolve issue.", false);
        } else if (BetterShop.economy != null) {
            try {
                //long preAmt = BetterShop.economy.getPlayerMoney(player.getName());
                BetterShop.economy.addPlayerMoney(player.getName(), (int) Math.round(amount), false);
                if (BetterShop.config.BOSBank.length() > 0) {
                    BetterShop.economy.addBankMoney(BetterShop.config.BOSBank, -(int) Math.round(amount), false);
                }
            } catch (Exception ex) {
                BetterShop.Log(Level.SEVERE, "Failed to credit player (BOSEconomy)", ex);
            }
        } else if (BetterShop.essentials != null) {
            try {
                //User u = BetterShop.essentials.getUser(player);
                //double preAmt = u.getMoney();
                BetterShop.essentials.getUser(player).giveMoney(amount);
            } catch (Exception ex) {
                BetterShop.Log(Level.SEVERE, "Failed to credit player (Essentials)", ex);
            }
        } else {
            BetterShop.Log(Level.SEVERE, "Failed to credit player: no economy plugin", false);
            return false;
        }
        return true;
    }

    static boolean debit(Player player, double amount) {
        if (amount <= 0 || playerBalance(player) < amount) {
            return amount == 0;
        }
        if (BetterShop.iConomy != null || BetterShop.legacyIConomy != null) {
            try {
                if (iconomyEdit(player.getName(), -amount)) {
                    return true;
                }
            } catch (Exception ex) {
            }
            // something seems to be wrong with iConomy: reload it
            BetterShop.Log(Level.SEVERE, "Failed to debit player: attempting iConomy reload", false);
            if (reloadIConomy(player.getServer())) {
                try {
                    if (iconomyEdit(player.getName(), -amount)) {
                        return true;
                    }
                } catch (Exception ex) {
                }
            }
            BetterShop.Log(Level.SEVERE, "iConomy reload failed to resolve issue.", false);
        } else if (BetterShop.economy != null) {
            try {
                int preAmt = BetterShop.economy.getPlayerMoney(player.getName());
                // don't allow account to go negative
                if (preAmt < amount) {
                    return false;
                }
                BetterShop.economy.addPlayerMoney(player.getName(), -(int) Math.round(amount), false);
                if (BetterShop.config.BOSBank.length() > 0) {
                    BetterShop.economy.addBankMoney(BetterShop.config.BOSBank, (int) Math.round(amount), false);
                }
            } catch (Exception ex) {
                BetterShop.Log(Level.SEVERE, "Failed to debit player (BOSEconomy)", ex);
            }
        } else if (BetterShop.essentials != null) {
            try {
                //User u = BetterShop.essentials.getUser(player);
                //double preAmt = u.getMoney();
                BetterShop.essentials.getUser(player).takeMoney(amount);
            } catch (Exception ex) {
                BetterShop.Log(Level.SEVERE, "Failed to debit player (Essentials)", ex);
            }
        } else {
            BetterShop.Log(Level.SEVERE, "Failed to debit player: no economy plugin", false);
            return false;
        }
        return true;
    }

    public static double playerBalance(Player player) {
        if (player == null) {
            return 0;
        } else if (BetterShop.iConomy != null) {
            return BetterShop.iConomy.getAccount(player.getName()).getHoldings().balance();
        } else if (BetterShop.legacyIConomy != null) {
            return com.nijiko.coelho.iConomy.iConomy.getBank().getAccount(player.getName()).getBalance();
        } else if (BetterShop.economy != null) {
            return BetterShop.economy.getPlayerMoney(player.getName());
        } else if (BetterShop.essentials != null) {
            return BetterShop.essentials.getUser(player).getMoney();
        } else {
            return 0;
        }
    }

    private static boolean iconomyEdit(String player, double amount) {
        //Account account = BetterShop.iConomy.getBank().getAccount(player.getName());
        if (BetterShop.iConomy != null) {
            double preAmt = BetterShop.iConomy.getAccount(player).getHoldings().balance();// BetterShop.legacyIConomy ? BetterShop.iConomy.getBank().getAccount(player).getBalance() :
            // don't allow account to go negative
            if (amount > 0 || preAmt >= -amount) {
                BetterShop.iConomy.getAccount(player).getHoldings().add(amount);
                return BetterShop.iConomy.getAccount(player).getHoldings().balance() != preAmt;
            }
        } else if (BetterShop.legacyIConomy != null) {
            double preAmt = com.nijiko.coelho.iConomy.iConomy.getBank().getAccount(player).getBalance();
            // don't allow account to go negative
            if (amount > 0 || preAmt >= -amount) {
                com.nijiko.coelho.iConomy.iConomy.getBank().getAccount(player).add(amount);
                return com.nijiko.coelho.iConomy.iConomy.getBank().getAccount(player).getBalance() != preAmt;
            }
        }
        return false;
    }

    public static String formatCurrency(double amt) {
        try {
            if (BetterShop.iConomy != null) {
                return BetterShop.iConomy.format(amt);
            } else if (BetterShop.legacyIConomy != null) {
                return com.nijiko.coelho.iConomy.iConomy.getBank().format(amt);
            } else if (BetterShop.economy != null) {
                if (amt < 1 || amt > 1) {
                    return String.valueOf((int) Math.round(amt)) + " " + BetterShop.economy.getMoneyNamePlural();
                }
                return String.valueOf((int) Math.round(amt)) + " " + BetterShop.economy.getMoneyName();
            }
        } catch (Exception ex) {
            BetterShop.Log(Level.WARNING, "Error Formatting Currency", ex, false);
        }
        return String.format("%.2f", amt) + " "
                + (amt > 1 || amt < 1 ? BetterShop.config.pluralCurrency
                : BetterShop.config.defaultCurrency);
    }

    static boolean reloadIConomy(Server serv) {
        try {
            PluginManager m = serv.getPluginManager();
            Plugin icon = m.getPlugin("iConomy");
            if (icon != null) {
                m.disablePlugin(icon);
                m.enablePlugin(icon);

                return true;
            }
        } catch (Exception ex) {
            BetterShop.Log(Level.SEVERE, "Error reloading iConomy", ex);
        }
        return false;
    }

    public static boolean hasPermission(CommandSender player, BetterShopPermission node) {
        return hasPermission(player, node.toString(), false);
    }

    public static boolean hasPermission(CommandSender player, BetterShopPermission node, boolean notify) {
        return hasPermission(player, node.toString(), notify);
    }

    static boolean hasPermission(CommandSender player, String node) {
        return hasPermission(player, node, false);
    }

    static boolean hasPermission(CommandSender player, String node, boolean notify) {
        if (player == null || player.isOp() || !(player instanceof Player) || node == null || node.length() == 0) { // ops override permission check (double-check is a Player)
            return true;
        }/* else if (!node.toLowerCase().startsWith("bettershop")) {
        node = "BetterShop" + (node.codePointAt(0) == '.' ? "" : ".") + node;
        }*/
        try {
            if (BetterShop.Permissions == null || BetterShop.Permissions.Security == null) {
                // only ops have access to .admin
                if (node.length() < 16 // if invalid node, assume true
                        || !node.substring(0, 16).equalsIgnoreCase("BetterShop.admin")) {
                    return true;
                }
            } else {//if (player instanceof Player) {
                if (BetterShop.Permissions.Security.has((Player) player, node)) {
                    return true;
                }
            }
            if (notify) {
                //PermDeny(player, node);
                BSutils.sendMessage(player, BetterShop.config.getString("permdeny").replace("<perm>", node));
            }
            return false;
        } catch (Exception e) {
            BetterShop.Log(Level.SEVERE, e, false);
            return node.length() < 16 // if invalid node, assume true
                    || !node.substring(0, 16).equalsIgnoreCase("BetterShop.admin");
        }
    }

    public static void sendFormttedMessage(Player player, String key, String item, int amt, double total) {

        BSutils.sendMessage(player, BetterShop.config.getString(key).
                replace("<item>", item).
                replace("<amt>", Integer.toString(amt)).
                replace("<priceper>", BetterShop.config.intCurrency() ? String.format("%d", (int) Math.round(total / amt)) : String.format("%.2f", total / amt)).
                replace("<total>", BetterShop.config.intCurrency() ? String.format("%d", (int) Math.round(total)) : String.format("%.2f", total)).
                replace("<curr>", BetterShop.config.currency()).
                replace("<totcur>", BSutils.formatCurrency(total)));
        //price
        if (BetterShop.config.publicmarket && BetterShop.config.hasString("public" + key)) {
            BSutils.broadcastMessage(player, BetterShop.config.getString("public" + key).
                    replace("<item>", item).
                    replace("<amt>", String.valueOf(amt)).
                    replace("<priceper>", BetterShop.config.intCurrency() ? String.format("%d", (int) Math.round(total / amt)) : String.format("%.2f", total / amt)).
                    replace("<total>", BetterShop.config.intCurrency() ? String.format("%d", (int) Math.round(total)) : String.format("%.2f", total)).
                    replace("<curr>", BetterShop.config.currency()).
                    replace("<totcur>", BSutils.formatCurrency(total)).
                    replace("<player>", ((Player) player).getDisplayName()),
                    false);
        }
    }

    static void sendMessage(CommandSender player, String s) {
        if (player != null) {
            player.sendMessage(BetterShop.config.getString("prefix") + s);
        }
    }

    static void sendMessage(CommandSender player, String s, boolean isPublic) {
        if (player != null) {
            if (isPublic) {
                broadcastMessage(player, s);
            } else {
                player.sendMessage(BetterShop.config.getString("prefix") + s);
            }
        }
    }

    static void broadcastMessage(CommandSender player, String s) {
        if (player != null) {
            player.getServer().broadcastMessage(BetterShop.config.getString("prefix") + s);
        }
        BetterShop.Log("(public announcement) " + s.replaceAll("\\\u00A7.", ""));
    }

    static void broadcastMessage(CommandSender player, String s, boolean includePlayer) {
        if (player != null) {
            if (includePlayer) {
                broadcastMessage(player, s);
            } else {
                String name = player instanceof Player ? ((Player) player).getDisplayName() : "";
                for (Player p : player.getServer().getOnlinePlayers()) {
                    if (!p.getDisplayName().equals(name)) {
                        //player.getServer().broadcastMessage(BetterShop.config.getString("prefix") + s);
                        p.sendMessage(BetterShop.config.getString("prefix") + s);
                    }
                }
                BetterShop.Log("(public announcement) " + s.replaceAll("\\\u00A7.", ""));
            }
        }
    }

    /**
     * all items in the user's inventory
     * does not run check for tools, so tools with damage are returned separately
     * @param player Player to check
     * @param onlyInv if the lower 9 slots should be checked or not
     * @return
     */
    public static ArrayList<ItemStockEntry> getTotalInventory(Player player, boolean onlyInv) {
        if (player == null) {
            return null;
        }
        ArrayList<ItemStockEntry> inv = new ArrayList<ItemStockEntry>();

        ItemStack[] its = player.getInventory().getContents();
        for (int i = (onlyInv ? 9 : 0); i <= 35; ++i) {
            if (its[i] != null && its[i].getAmount() > 0) {
                ItemStockEntry find = new ItemStockEntry(its[i]);
                int pos = inv.indexOf(find);
                if (pos >= 0) {
                    inv.get(pos).AddAmount(its[i].getAmount());
                } else {
                    inv.add(find);
                }
            }
        }

        return inv;
    }

    public static ArrayList<ItemStockEntry> getTotalInventory(Player player, boolean onlyInv, JItem toFind) {
        if (toFind == null) {
            return getTotalInventory(player, onlyInv);
        }// else
        return getTotalInventory(player, onlyInv, new JItem[]{toFind});
    }

    public static ArrayList<ItemStockEntry> getTotalInventory(Player player, boolean onlyInv, JItem[] toFind) {
        if (toFind == null || toFind.length == 0) {
            return getTotalInventory(player, onlyInv);
        } else if (player == null) {
            return null;
        }
        ArrayList<ItemStockEntry> inv = new ArrayList<ItemStockEntry>();

        ItemStack[] its = player.getInventory().getContents();
        for (int i = (onlyInv ? 9 : 0); i <= 35; ++i) {
            if (its[i] != null && its[i].getAmount() > 0) {
                for (JItem it : toFind) {
                    if (it != null && its[i] != null && it.equals(its[i])) {
                        ItemStockEntry find = new ItemStockEntry(its[i]);
                        int pos = inv.indexOf(find);
                        if (pos >= 0) {
                            inv.get(pos).AddAmount(its[i].getAmount());
                        } else {
                            inv.add(find);
                        }
                        break;
                    }
                }
            }
        }
        return inv;
    }

    public static ArrayList<ItemStockEntry> getTotalInventory(Player player, boolean onlyInv, ArrayList<ItemStockEntry> toFind) {
        if (toFind == null) {
            return getTotalInventory(player, onlyInv);
        } else if (player == null) {
            return null;
        }
        ArrayList<ItemStockEntry> inv = new ArrayList<ItemStockEntry>();
        if (toFind.isEmpty()) {
            return inv;
        }
        ItemStack[] its = player.getInventory().getContents();
        for (int i = (onlyInv ? 9 : 0); i <= 35; ++i) {
            if (its[i] != null && its[i].getAmount() > 0) {
                for (ItemStockEntry it : toFind) {
                    if (it != null && its[i] != null && it.equals(its[i])) {
                        int pos = inv.indexOf(it);
                        if (pos >= 0) {
                            inv.get(pos).AddAmount(its[i].getAmount());
                        } else {
                            inv.add(new ItemStockEntry(it));
                        }
                        break;
                    }
                }
            }
        }
        return inv;
    }
    /*
    public static ArrayList<ItemStockEntry> getTotalSellableInventory(Player player, boolean onlyInv) {
    ArrayList<ItemStockEntry> inv = new ArrayList<ItemStockEntry>();
    try {
    ItemStack[] its = player.getInventory().getContents();
    for (int i = (onlyInv ? 9 : 0); i <= 35; ++i) {
    if (its[i].getAmount() > 0 && BetterShop.pricelist.isForSale(its[i])) {
    ItemStockEntry find = new ItemStockEntry(its[i]);
    int pos = inv.indexOf(find);
    if (pos >= 0) {
    inv.get(pos).AddAmount(its[i].getAmount());
    } else {
    inv.add(find);
    }
    }
    }
    } catch (Exception ex) {
    BetterShop.Log(Level.SEVERE, ex);
    return null;
    }
    return inv;
    }//*/

    public static UserTransaction buyAllItem(Player player, JItem toBuy) {
        if (toBuy == null || player == null) {
            return null;
        }
        double price = BetterShop.pricelist.itemBuyPrice(player, toBuy);
        if (price < 0) {
            if (price != Double.NEGATIVE_INFINITY) {
                BSutils.sendMessage(player,
                        BetterShop.config.getString("notforsale").
                        replace("<item>", toBuy.coloredName()));
            }
            return null;
        }
        int canHold = 0, maxStack = BetterShop.config.usemaxstack ? toBuy.getMaxStackSize() : 64;
        PlayerInventory inv = player.getInventory();
        if (!toBuy.isEntity()) {
            // don't search armor slots
            for (int i = 0; i <= 35; ++i) {
                ItemStack it = inv.getItem(i);
                if ((toBuy.equals(it) && it.getAmount() < maxStack) || it.getAmount() == 0) {
                    canHold += maxStack - it.getAmount();
                }
            }
        } else {
            canHold = BetterShop.config.maxEntityPurchase;
        }
        return _buyItem(player, toBuy, canHold, maxStack, price);
    }

    public static UserTransaction buyItem(Player player, JItem toBuy, int amt) {
        if (toBuy == null || player == null || amt <= 0) {
            return null;
        } else if (amt <= 0) {
            BSutils.sendMessage(player, BetterShop.config.getString("nicetry"));
            return null;
        }
        double price = BetterShop.pricelist.itemBuyPrice(player, toBuy);
        if (price < 0) {
            if (price != Double.NEGATIVE_INFINITY) {
                BSutils.sendMessage(player,
                        BetterShop.config.getString("notforsale").
                        replace("<item>", toBuy.coloredName()));
            }
            return null;
        }
        if (toBuy.isKit()) {
            return _buyKit(player, JItemDB.getKit(toBuy), amt, price);
        }
        int canHold = 0, maxStack = BetterShop.config.usemaxstack ? toBuy.getMaxStackSize() : 64;
        PlayerInventory inv = player.getInventory();
        if (!toBuy.isEntity()) {
            // don't search armor slots
            for (int i = 0; i <= 35; ++i) {
                ItemStack it = inv.getItem(i);
                if ((toBuy.equals(it) && it.getAmount() < maxStack) || it.getAmount() == 0) {
                    canHold += maxStack - it.getAmount();
                }
            }
        } else {
            canHold = BetterShop.config.maxEntityPurchase;
        }
        if (amt > canHold) {
            BSutils.sendMessage(player, String.format(BetterShop.config.getString("outofroom").
                    replace("<item>", "%1$s").replace("<amt>", "%2$d").
                    replace("<priceper>", "%3$01.2f").replace("<leftover>", "%4$d").
                    replace("<curr>", "%5$s").replace("<free>", "%6$d"), toBuy.coloredName(),
                    amt, price, amt - canHold, BetterShop.config.currency(), canHold));
            if (canHold == 0) {
                return null;
            }
            amt = canHold;
        }

        return _buyItem(player, toBuy, amt, maxStack, price);
    }

    private static UserTransaction _buyItem(Player player, JItem toBuy, int amt, int maxStack, double unitPrice) {
        UserTransaction ret = null; // new UserTransaction(toBuy, false);
        PlayerInventory inv = player.getInventory();
        // now check if there are items avaliable for purchase
        long avail = -1;
        if (BetterShop.stock != null && BetterShop.config.useItemStock) {
            try {
                avail = BetterShop.stock.getItemAmount(toBuy);
            } catch (Exception ex) {
                BetterShop.Log(Level.SEVERE, ex);
                avail = -1;
            }
            if (avail == 0) {
                BSutils.sendMessage(player, BetterShop.config.getString("outofstock").
                        replace("<item>", toBuy.coloredName()));
                return null;
            } else if (avail >= 0 && amt > avail) {
                BSutils.sendMessage(player, String.format(BetterShop.config.getString("lowstock").
                        replace("<item>", toBuy.coloredName()).
                        replace("<amt>", String.valueOf(avail))));
                amt = (int) avail;
            }
        }
        double cost = amt * unitPrice;

        if (cost == 0 || BSutils.debit(player, cost)) {
            if (!toBuy.isEntity()) {
                if (toBuy.equals(JItems.MAP)) {
                }
                //if (maxStack == 64) { //((Player) player).getInventory().addItem(toBuy.toItemStack(amtbought));
                //    inv.addItem(toBuy.toItemStack(amtbought));
                //} else {
                int amtLeft = amt;
                for (int i = 0; i <= 35; ++i) {
                    ItemStack it = inv.getItem(i);
                    if (it.getAmount() <= 0) {
                        inv.setItem(i, toBuy.toItemStack((maxStack < amtLeft ? maxStack : amtLeft)));
                        amtLeft -= maxStack;
                    } else if (toBuy.equals(it) && it.getAmount() < maxStack) {
                        int itAmt = it.getAmount();
                        inv.setItem(i, toBuy.toItemStack(amtLeft + itAmt > maxStack ? maxStack : amtLeft + itAmt));
                        amtLeft -= maxStack - itAmt;
                    }
                    if (amtLeft <= 0) {
                        break;
                    }
                }
                //}
                // drop in front of player?
                //World w = player.getServer().getWorld(""); w.dropItem(player.getServer().getPlayer("").getLocation(), leftover.values());//.dropItem(
            } else {
                CreatureItem c = CreatureItem.getCreature(toBuy.ID());
                if (c != null) {
                    for (int i = 0; i < amt; ++i) {
                        c.spawnNewWithOwner(player);
                    }
                }
            }

            BSutils.sendFormttedMessage(player, "buymsg", toBuy.coloredName(), amt, cost);
            // replace("<priceper>", unitPrice

            try {
                if (BetterShop.stock != null && BetterShop.config.useItemStock) {
                    BetterShop.stock.changeItemAmount(toBuy, -amt);
                }

                ret = new UserTransaction(
                        toBuy, false, amt, unitPrice, ((Player) player).getDisplayName());
                BetterShop.transactions.addRecord(ret);

            } catch (Exception ex) {
                BetterShop.Log(Level.SEVERE, ex);
            }
        } else {
            BSutils.sendMessage(player, String.format(BetterShop.config.getString("insuffunds").
                    replace("<item>", "%1$s").
                    replace("<amt>", "%2$d").
                    replace("<total>", "%3$01.2f").
                    replace("<curr>", "%5$s").
                    replace("<priceper>", "%4$01.2f").
                    replace("<totcur>", "%6$s"), toBuy.coloredName(),
                    amt, cost, unitPrice, BetterShop.config.currency(),
                    BSutils.formatCurrency(unitPrice)));
        }
        return ret;
    }

    private static UserTransaction _buyKit(Player player, Kit toBuy, int amt, double unitPrice) {
        UserTransaction ret = null;
        PlayerInventory inv = ((Player) player).getInventory();
        KitItem items[] = toBuy.getKitItems();

        int maxBuy = 0;

        ItemStack invCopy[] = new ItemStack[36];
        for (int i = 0; i <= 35; ++i) {
            invCopy[i] = new ItemStack(inv.getItem(i).getType(), inv.getItem(i).getAmount(), inv.getItem(i).getDurability());
        }

        while (true) {
            int numtoadd = 0;
            for (int itn = 0; itn < toBuy.numItems(); ++itn) {
                numtoadd = items[itn].itemAmount;
                int maxStack = BetterShop.config.usemaxstack ? items[itn].getMaxStackSize() : 64;
                // don't search armor slots
                for (int i = 0; i <= 35; ++i) {
                    //if (items[itn].equals(new JItem(invCopy[i])) ||  (invCopy[i].getAmount() == 0 && invCopy[i].getAmount()<maxStack)) {
                    if ((items[itn].iequals(invCopy[i]) && invCopy[i].getAmount() < maxStack)
                            || invCopy[i].getAmount() == 0) {
                        //System.out.println("can place " + items[itn] + " at " + i + " : " + invCopy[i]);
                        invCopy[i].setTypeId(items[itn].ID());
                        invCopy[i].setDurability(items[itn].Data());
                        invCopy[i].setAmount(invCopy[i].getAmount() + (maxStack < numtoadd ? maxStack : numtoadd));
                        numtoadd -= maxStack < numtoadd ? maxStack : numtoadd;
                        //System.out.println(invCopy[i]);
                        if (numtoadd <= 0) {
                            break;
                        }
                    }
                }
                if (numtoadd > 0) {
                    break;
                }
            }
            if (numtoadd <= 0) {
                //System.out.println("1 added: " + maxBuy);
                ++maxBuy;
            } else {
                break;
            }
        }

        if (amt > maxBuy) {
            BSutils.sendMessage(player, String.format(BetterShop.config.getString("outofroom").
                    replace("<item>", "%1$s").
                    replace("<amt>", "%2$d").
                    replace("<priceper>", "%3$01.2f").
                    replace("<leftover>", "%4$d").
                    replace("<curr>", "%5$s").
                    replace("<free>", "%6$d"), toBuy.coloredName(),
                    amt, unitPrice, amt - maxBuy, BetterShop.config.currency(), maxBuy));
            if (maxBuy == 0) {
                return null;
            }
            amt = maxBuy;
        }

        // now check if there are items avaliable for purchase
        long avail = -1;
        if (BetterShop.stock != null && BetterShop.config.useItemStock) {
            try {
                avail = BetterShop.stock.getItemAmount(toBuy.ID());
            } catch (Exception ex) {
                BetterShop.Log(Level.SEVERE, ex);
            }
            /*if (avail == -1) {
            BSutils.sendMessage(player, ChatColor.RED + "Failed to lookup an item stock listing");
            return true;
            } else */ if (avail == 0) {
                BSutils.sendMessage(player, BetterShop.config.getString("outofstock").
                        replace("<item>", toBuy.coloredName()));
                return null;
            } else if (amt > avail) {
                BSutils.sendMessage(player, String.format(BetterShop.config.getString("lowstock").
                        replace("<item>", toBuy.coloredName()).
                        replace("<amt>", String.valueOf(avail))));
                amt = (int) avail;
            }
        }

        double cost = amt * unitPrice;
        if (cost == 0 || BSutils.debit(player, cost)) {
            try {
                for (int num = 0; num < amt; ++num) {
                    int numtoadd = 0;
                    for (int itn = 0; itn < toBuy.numItems(); ++itn) {
                        numtoadd = items[itn].itemAmount;
                        int maxStack = BetterShop.config.usemaxstack ? items[itn].getMaxStackSize() : 64;
                        // don't search armor slots
                        for (int i = 0; i <= 35; ++i) {
                            if ((items[itn].iequals(inv.getItem(i)) && inv.getItem(i).getAmount() < maxStack) || inv.getItem(i).getAmount() == 0) {
                                //System.out.println("placing " + items[itn] + " at " + i + " (" + inv.getItem(i) + ")");
                                inv.setItem(i, items[itn].toItemStack(inv.getItem(i).getAmount() + (maxStack < numtoadd ? maxStack : numtoadd)));
                                numtoadd -= maxStack < numtoadd ? maxStack : numtoadd;
                                //System.out.println(inv.getItem(i));
                                if (numtoadd <= 0) {
                                    break;
                                }
                            }
                        }
                        if (numtoadd > 0) {
                            System.out.println("failed to add " + items[itn] + "!");
                            break;
                        }
                    }
                    if (numtoadd > 0) {
                        System.out.println("early exit while adding!");
                        BSutils.broadcastMessage(player, "An Error occurred.. contact an admin to resolve this issue");
                        break;
                    }
                }
                if (BetterShop.stock != null && BetterShop.config.useItemStock) {
                    BetterShop.stock.changeItemAmount(toBuy.ID(), toBuy.Name(), -amt);
                }

                BSutils.sendFormttedMessage(player, "buymsg", toBuy.coloredName(), amt, cost);

                ret = new UserTransaction(toBuy.ID(), 0, toBuy.Name(), false, amt, unitPrice, player.getDisplayName());
                BetterShop.transactions.addRecord(ret);
            } catch (Exception ex) {
                BetterShop.Log(Level.SEVERE, ex);
            }
        } else {
            BSutils.sendMessage(player, String.format(
                    BetterShop.config.getString("insuffunds").
                    replace("<item>", "%1$s").
                    replace("<amt>", "%2$d").
                    replace("<total>", "%3$01.2f").
                    replace("<curr>", "%5$s").
                    replace("<priceper>", "%4$01.2f").
                    replace("<totcur>", "%6$s"), toBuy.coloredName(),
                    amt, cost, unitPrice, BetterShop.config.currency(),
                    BSutils.formatCurrency(unitPrice)));
        }
        return ret;
    }

    public static ArrayList<ItemStockEntry> getCanSell(Player player, boolean onlyInv, JItem[] toSell) {
        ArrayList<ItemStockEntry> sellable = new ArrayList<ItemStockEntry>(),
                playerInv = getTotalInventory(player, onlyInv, toSell);
        if (toSell != null && toSell.length == 1 && toSell[0] == null) {
            toSell = null;
        }
        //ItemStack[] its = player.getInventory().getContents();
        boolean overstock = false;
        ArrayList<String> notwant = new ArrayList<String>();
        try {
            for (int i = 0; i < playerInv.size(); ++i) {
                JItem check = JItemDB.GetItem(playerInv.get(i));
                if (!BetterShop.pricelist.isForSale(check)
                        || (check.IsTool() && !BetterShop.config.buybacktools && playerInv.get(i).itemSub > 0)) {
                    if (toSell != null && toSell.length > 0) {
                        notwant.add(check.coloredName());
                    }
                    playerInv.remove(i);
                    --i;
                } else {
                    if (BetterShop.config.useItemStock && BetterShop.stock != null) {
                        // check if avaliable stock
                        long free = BetterShop.stock.freeStockRemaining(check);
                        if (free == 0 && BetterShop.config.noOverStock) {
                            BSutils.sendMessage(player, BetterShop.config.getString("maxstock").
                                    replace("<item>", check.coloredName()));
                            playerInv.get(i).amount = 0;
                            overstock = true;
                        } else if (free > 0 && playerInv.get(i).amount > free && BetterShop.config.noOverStock) {
                            BSutils.sendMessage(player, BetterShop.config.getString("highstock").
                                    replace("<item>", check.coloredName()).
                                    replace("<amt>", String.valueOf(free)));
                            playerInv.get(i).amount = (int) free;
                        }
                    }
                    //amtHas += playerInv.get(i).amount;
                    int isel = sellable.indexOf(playerInv.get(i));
                    if (isel >= 0) {
                        sellable.get(isel).SetAmount(sellable.get(isel).amount + playerInv.get(i).amount);
                    } else {
                        sellable.add(new ItemStockEntry(playerInv.get(i)));
                    }
                }
            }
        } catch (Exception ex) {
            BetterShop.Log(Level.SEVERE, ex);
            BSutils.sendMessage(player, "Error looking up an item");// .. Attempting DB reload..
            /*if (load(null)) {
            // ask to try command again.. don't want accidental infinite recursion & don't want to plan for recursion right now
            BSutils.sendMessage(player, "Success! Please try again.. ");
            } else {
            BSutils.sendMessage(player, "\u00A74Failed! Please let an OP know of this error");
            }*/
            return null;
        }
        if (notwant.size() > 0) {
            BSutils.sendMessage(player, String.format(
                    BetterShop.config.getString("donotwant").
                    replace("<item>", "%1$s"), "(" + Str.argStr(notwant.toArray(new String[0]), ", ") + ")"));
            if (notwant.size() == toSell.length) {
                return sellable;
            }
        }
        if (sellable.isEmpty() && !overstock) {
            BSutils.sendMessage(player, "You Don't have any "
                    + (toSell == null || toSell.length == 0 || (toSell.length == 1 && toSell[0] == null) ? "Sellable Items"
                    : (toSell.length == 1 ? toSell[0].coloredName() : "of those items")));
        }
        return sellable;
    }

    protected static double sellItems(Player player, boolean onlyInv, JItem item, int amt) {
        if (item == null || amt < 0) {
            ArrayList<ItemStockEntry> playerInv = getCanSell(player, onlyInv, item == null ? null : new JItem[]{item});
            if (playerInv == null || playerInv.isEmpty()) {
                return 0;
            }
            return sellItems(player, onlyInv, playerInv);
        } else {
            return sellItems(player, onlyInv, new ArrayList(Arrays.asList(new ItemStockEntry(item, (long) amt))));
        }
    }

    protected static double sellItems(Player player, boolean onlyInv, ArrayList<ItemStockEntry> items) {
        PlayerInventory inv = player.getInventory();
        ItemStack[] its = inv.getContents();

        ArrayList<ItemStockEntry> playerInv = getTotalInventory(player, onlyInv, items);
        // make list of transactions made (or should make)
        LinkedList<UserTransaction> transactions = new LinkedList<UserTransaction>();
        try {
            for (ItemStockEntry ite : playerInv) {
                transactions.add(new UserTransaction(ite, true,
                        BetterShop.pricelist.getSellPrice(ite),
                        player.getDisplayName()));
            }
        } catch (Exception ex) {
            BetterShop.Log(Level.SEVERE, ex);
        }

        double credit = 0;
        int amtSold = 0;
        try {
            for (ItemStockEntry ite : items) {
                JItem toSell = JItemDB.GetItem(ite);
                if (toSell != null) {
                    int amtLeft = (int) ite.amount;
                    for (int i = (onlyInv ? 9 : 0); i <= 35; ++i) {
                        JItem it = JItemDB.GetItem(its[i]);
                        if (it != null && it.equals(toSell)) {
                            if (BetterShop.pricelist.isForSale(it) && (!it.IsTool()
                                    || (its[i].getDurability() == 0 || BetterShop.config.buybacktools))) {
                                int amt = its[i].getAmount();
                                if (amtLeft < amt) {
                                    inv.setItem(i, it.toItemStack(amt - amtLeft));
                                    amt = amtLeft;
                                } else {
                                    inv.setItem(i, null);
                                }
                                if (it.IsTool()) {
                                    credit += (BetterShop.pricelist.getSellPrice(it) * (1 - ((double) its[i].getDurability() / it.MaxDamage()))) * amt;
                                } else {
                                    credit += BetterShop.pricelist.getSellPrice(it) * amt;
                                }
                                amtSold += amt;
                                amtLeft -= amt;
                                if (amtLeft <= 0) {
                                    if (BetterShop.config.useItemStock && BetterShop.stock != null) {
                                        BetterShop.stock.changeItemAmount(it, ite.amount);
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            BetterShop.Log(Level.SEVERE, ex);
            BSutils.sendMessage(player, "Error looking up an item");
        }

        try {
            for (UserTransaction t : transactions) {
                BetterShop.transactions.addRecord(t);
            }
        } catch (Exception ex) {
            BetterShop.Log(Level.SEVERE, ex);
        }
        BSutils.credit(player, credit);

        // name of item(s) sold
        String itemN = ""; // "(All Sellable)"
        if (items.size() == 1) {
            JItem i = JItemDB.GetItem(items.get(0));
            itemN = i != null ? i.coloredName() : items.get(0).name;
        } else {
            itemN = "(";
            for (ItemStockEntry ite : playerInv) {
                JItem i = JItemDB.GetItem(ite);
                itemN += i == null ? ite.name : i.coloredName() + " ";
            }
            itemN = itemN.trim().replace(" ", ", ") + ")";
        }

        BSutils.sendFormttedMessage((Player) player, "sellmsg", itemN, amtSold, credit);

        return credit;
    }
}
