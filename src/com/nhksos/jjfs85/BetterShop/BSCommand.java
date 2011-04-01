package com.nhksos.jjfs85.BetterShop;

import com.jascotty2.CheckInput;
import com.jascotty2.Item.CreatureItem;
import com.jascotty2.Item.Item;
import com.jascotty2.Item.ItemDB;
import com.jascotty2.Item.ItemStockEntry;
import com.jascotty2.Item.Kit;
import com.jascotty2.Item.Kit.KitItem;
import com.jascotty2.Item.PriceListItem;
import com.jascotty2.Shop.UserTransaction;
import com.jascotty2.Shop.PriceList;
import com.jascotty2.Str;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.logging.Level;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class BSCommand {

    HashMap<String, String> userbuyHistory = new HashMap<String, String>();
    HashMap<String, String> usersellHistory = new HashMap<String, String>();

    public BSCommand() {
    }

    public boolean help(CommandSender player, String[] s) {
        if (!BSutils.hasPermission(player, "BetterShop.user.help", true)) {
            return true;
        }
        if (s.length > 0) {
            // more help
            if (Str.isIn(s[0], "shop")) {
                BSutils.sendMessage(player, "/shop   command alias to other commands");
                BSutils.sendMessage(player, "      ");
                if (BSutils.hasPermission(player, "BetterShop.admin.backup", false)) {
                    BSutils.sendMessage(player, "/shop backup   to backup current pricelist");
                }
                if (BSutils.hasPermission(player, "BetterShop.admin.info", false)) {
                    BSutils.sendMessage(player, "/shop ver[sion]   show the currently installed version");
                    BSutils.sendMessage(player, "        also shows if this is the most current avaliable");
                }
            } else if (Str.isIn(s[0], "shopcheck, scheck, sc")) {
                BSutils.sendMessage(player, "/shopcheck <item>  Check prices for an item");
                BSutils.sendMessage(player, " aliases: scheck, sc");
                BSutils.sendMessage(player, "-- will also run a name match search");
            } else if (Str.isIn(s[0], "shoplist, slist, sl")) {
                BSutils.sendMessage(player, "/shoplist [page]   Lists prices for the shop");
                BSutils.sendMessage(player, " aliases: slist, sl");
            } else if (Str.isIn(s[0], "shopitems, sitems")) {
                BSutils.sendMessage(player, "/shopitems  show listing of items in shop, without prices");
                BSutils.sendMessage(player, " aliases: sitems");
                BSutils.sendMessage(player, "-- coming soon: pages");
            } else if (Str.isIn(s[0], "shopbuy, sbuy, buy")) {
                BSutils.sendMessage(player, "/shopbuy <item> [amount] Buy an item for the price in the shop");
                BSutils.sendMessage(player, " aliases: sbuy, buy");
                BSutils.sendMessage(player, "-- \"all\" is a valid amount: will buy all you can hold/afford");
            } else if (Str.isIn(s[0], "shopbuyall, sbuyall, buyall")) {
                BSutils.sendMessage(player, "/shopbuyall <item>  buy all you can hold/afford");
                BSutils.sendMessage(player, " aliases: sbuyall, buyall");
            } else if (Str.isIn(s[0], "shopbuystack, buystack, sbuystack, sbuys, buys")) {
                BSutils.sendMessage(player, "/shopbuystack <item> [amount] buy items in stacks");
                BSutils.sendMessage(player, " aliases: buystack, sbuystack, sbuys, buys");
                BSutils.sendMessage(player, "-- can list multiple items, or give how many stacks to buy");
            } else if (Str.isIn(s[0], "shopsell, ssell, sell")) {
                BSutils.sendMessage(player, "/shopsell <item> [amount]");
                BSutils.sendMessage(player, " aliases: ssell, sell");
                BSutils.sendMessage(player, "-- \"all\" is a valid amount: will sell all you have");
            } else if (Str.isIn(s[0], "shopsellall, sellall, sell all")) {
                BSutils.sendMessage(player, "/shopsellall [inv] [item [item [...]]] ");
                BSutils.sendMessage(player, "-- Sell all of item from your inventory");
                BSutils.sendMessage(player, " aliases: sellall, sell all");
                BSutils.sendMessage(player, "-- inv will only sell from your inventory, not the lower 9");
                BSutils.sendMessage(player, "-- multiple items can be listed, or none for all sellable");
            } else if (Str.isIn(s[0], "shopadd, sadd")) {
                BSutils.sendMessage(player, "/shopadd <item> <buyprice> [sellprice]");
                BSutils.sendMessage(player, "--  Add an item to or update an item in the price list");
                BSutils.sendMessage(player, " aliases: sadd");
                BSutils.sendMessage(player, "-- price of -1 disables that action");
                BSutils.sendMessage(player, "-- if no sellprice is given, item will not be sellable");
            } else if (Str.isIn(s[0], "shopremove, sremove")) {
                BSutils.sendMessage(player, "/shopremove <item>  Remove an item from the price list");
                BSutils.sendMessage(player, " aliases: sremove");
            } else if (Str.isIn(s[0], "shopload, sload, shop load, shop reload")) {
                BSutils.sendMessage(player, "/shopload   reload prices from pricelist database");
                BSutils.sendMessage(player, " aliases: sload, shop [re]load");
            } else if (Str.isIn(s[0], "shophelp, shelp")) {
                BSutils.sendMessage(player, "/shophelp [command] Lists available commands");
                BSutils.sendMessage(player, " aliases: shelp");
                BSutils.sendMessage(player, "-- providing a command shows specific help for that command");
            } else if (Str.isIn(s[0], "shoplistkits, shopkits, skits")) {
                BSutils.sendMessage(player, "/shoplistkits [page] Lists available kits");
                BSutils.sendMessage(player, " aliases: shopkits, skits");
                BSutils.sendMessage(player, "-- toadd: show what each kit contains");
            } else if (Str.isIn(s[0], "shopbuyagain, sbuyagain, buyagain, sba")) {
                BSutils.sendMessage(player, "/shopbuyagain  repeat last purchase");
                BSutils.sendMessage(player, " aliases: sbuyagain, buyagain, sba");
            } else if (Str.isIn(s[0], "shopsellagain, ssellagain, sellagain, ssa")) {
                BSutils.sendMessage(player, "/shopsellagain  repeat last sale");
                BSutils.sendMessage(player, " aliases: ssellagain, sellagain, ssa");
            } /*else if (s[0].equalsIgnoreCase("")) {
            BSutils.sendMessage(player, "/");
            BSutils.sendMessage(player, " aliases: ");
            BSutils.sendMessage(player, "-- ");
            } */ else {
                BSutils.sendMessage(player, "Unknown Help Topic");
            }
            return true;
        }
        BSutils.sendMessage(player, "--------- Better Shop Usage --------");
        if (BSutils.hasPermission(player, "BetterShop.user.list", false)) {
            BSutils.sendMessage(player, "/shoplist [page] - List shop prices");
            BSutils.sendMessage(player, "/shopitems - show listing of items in shop, without prices");
            BSutils.sendMessage(player, "/shopkits [page] - show listing of kits in shop");
        }
        if (BSutils.hasPermission(player, "BetterShop.user.buy", false)) {
            BSutils.sendMessage(player, "/shopbuy <item> [amount] - Buy items");
            BSutils.sendMessage(player, "/shopbuyall <item> - Buy all that you can hold/afford");
            BSutils.sendMessage(player, "/shopbuystack <item> [amount] - Buy stacks of items");
            BSutils.sendMessage(player, "/shopbuyagain - repeat last purchase action");
        }
        if (BSutils.hasPermission(player, "BetterShop.user.sell", false)) {
            BSutils.sendMessage(player, "/shopsell <item> [amount] - Sell items ");
            BSutils.sendMessage(player, "/shopsellall <item> - Sell all of your items");
            BSutils.sendMessage(player, "/shopsellagain - Repeat last sell action");
        }
        if (BSutils.hasPermission(player, "BetterShop.user.check", false)) {
            BSutils.sendMessage(player, "/shopcheck <item> - Check prices of item");
        }
        BSutils.sendMessage(player, "/shophelp [command] - show help on commands");
        if (BSutils.hasPermission(player, "BetterShop.admin", false)) {
            BSutils.sendMessage(player, "**-------- Admin commands --------**");
            if (BSutils.hasPermission(player, "BetterShop.admin.add", false)) {
                BSutils.sendMessage(player, "/shopadd <item> <$buy> [$sell] - Add/Update an item");
            }
            if (BSutils.hasPermission(player, "BetterShop.admin.remove", false)) {
                BSutils.sendMessage(player, "/shopremove <item> - Remove an item from the shop");
            }
            if (BSutils.hasPermission(player, "BetterShop.admin.load", false)) {
                BSutils.sendMessage(player, "/shopload - Reload the Configuration & PriceList DB");
            }
        }
        BSutils.sendMessage(player, "----------------------------------");
        return true;
    }

    public boolean check(CommandSender player, String[] s) {
        if (!BSutils.hasPermission(player, "BetterShop.user.check", true)) {
            return true;
        }
        if (s == null || s.length != 1) {
            return false;
        }
        Item lookup[] = Item.findItems(s[0]);
        if (lookup == null || lookup.length == 0 || lookup[0] == null) {
            BSutils.sendMessage(player, String.format(BetterShop.config.getString("unkitem").
                    replace("<item>", "%s"), s[0]));
            return true;
        }
        boolean canBuyIllegal = BetterShop.config.allowbuyillegal || BSutils.hasPermission(player, "BetterShop.admin.illegal", false);
        int inStore = 0;
        try {
            for (Item i : lookup) {
                PriceListItem price = BetterShop.pricelist.getItemPrice(i);
                if (price != null) {
                    ++inStore;
                    BSutils.sendMessage(player, String.format(
                            BetterShop.config.getString("pricecheck").
                            replace("<buyprice>", "%1$s").
                            replace("<sellprice>", "%2$s").
                            replace("<item>", "%3$s").
                            replace("<curr>", "%4$s").
                            replace("<buycur>", "%5$s").
                            replace("<sellcur>", "%6$s").
                            replace("<avail>", "%7$s"),
                            (price.IsLegal() || canBuyIllegal) && price.buy >= 0 ? price.buy : "No",
                            price.sell >= 0 ? price.sell : "No",
                            i.coloredName(),
                            BetterShop.config.currency(),
                            (price.IsLegal() || canBuyIllegal) && price.buy >= 0
                            ? BetterShop.iConomy.getBank().format(price.buy) : "No",
                            price.sell >= 0 ? BetterShop.iConomy.getBank().format(price.sell) : "No",
                            BetterShop.stock == null ? "INF" : BetterShop.stock.getItemAmount(i)));
                } else if (lookup.length <= 5) { // only show nolisting if result page is 5 or less lines
                    BSutils.sendMessage(player,
                            String.format(BetterShop.config.getString("nolisting").
                            replace("<item>", "%s"), i.coloredName()));
                }
            }
            if (lookup.length > 5 && inStore == 0) {
                BSutils.sendMessage(player, String.format("No Sellable items found under \"%s\"", s[0]));
            }
            return true;
        } catch (Exception ex) {
            BetterShop.Log(Level.SEVERE, ex);
        }

        BSutils.sendMessage(player, "\u00A74An Error Occurred while looking up an item.. attemping to reload..");
        if (load(null)) {
            // ask to try command again.. don't want accidental infinite recursion & don't want to plan for recursion right now
            BSutils.sendMessage(player, "Success! Please try again.. ");
        } else {
            BSutils.sendMessage(player, "\u00A74Failed! Please let an OP know of this error");
        }
        return true;
    }

    public boolean list(CommandSender player, String[] s) {
        if (!BSutils.hasPermission(player, "BetterShop.user.list", true)) {
            return true;
        }
        int pagenum = 1;
        if ((s.length > 1)) {
            return false;
        } else if (s.length == 1) {
            if (s[0].equalsIgnoreCase("full") || s[0].equalsIgnoreCase("all")) {
                pagenum = -1;
            } else if (s[0].equalsIgnoreCase("item") || s[0].equalsIgnoreCase("items")) {
                return listitems(player, null);
            } else if (s[0].equalsIgnoreCase("kits")) {
                return listkits(player, null);
            } else if (!CheckInput.IsInt(s[0])) {
                BSutils.sendMessage(player, "That's not a page number.");
                return false;
            } else {
                pagenum = CheckInput.GetInt(s[0], 1);
            }
        }

        for (String line : BetterShop.pricelist.GetShopListPage(pagenum, player instanceof Player,
                BetterShop.config.allowbuyillegal || BSutils.hasPermission(player, "BetterShop.admin.illegal", false))) {
            BSutils.sendMessage(player, line);
        }

        return true;
    }

    public boolean listitems(CommandSender player, String[] s) {
        if (!BSutils.hasPermission(player, "BetterShop.user.list", true)) {
            return true;
        } else if (s != null || s.length > 0) {
            //return false;
        }
        try {
            LinkedList<String> items = BetterShop.pricelist.GetItemList(
                    BetterShop.config.allowbuyillegal || BSutils.hasPermission(player, "BetterShop.admin.illegal", false));
            String output = "\u00A72";
            for (int i = 0; i < items.size(); ++i) {
                output += items.get(i);
                if (i + 1 < items.size()) {
                    output += "\u00A72, ";
                }
            }
            BSutils.sendMessage(player, output);
            return true;
        } catch (Exception ex) {
            BetterShop.Log(Level.SEVERE, ex);
        }

        BSutils.sendMessage(player, "\u00A74An Error Occurred while looking up an item.. attemping to reload..");
        if (load(null)) {
            // ask to try command again.. don't want accidental infinite recursion & don't want to plan for recursion right now
            BSutils.sendMessage(player, "Success! Please try again.. ");
        } else {
            BSutils.sendMessage(player, "\u00A74Failed! Please let an OP know of this error");
        }
        return true;
    }

    public boolean load(CommandSender player) {
        if (player != null && !BSutils.hasPermission(player, "BetterShop.admin.load", true)) {
            return true;
        }
        boolean ok = true;
        if (ItemDB.load(BSConfig.pluginFolder)) {
            BSutils.sendMessage(player, ItemDB.size() + " items loaded.");
        } else {
            BetterShop.Log(Level.SEVERE, "Cannot Load Items db!", false);
            if (player != null) {
                BSutils.sendMessage(player, "\u00A74Item Database Load Error.");
            }
            // itemDB load error is pretty serious
            return player != null;
        }
        if (!BetterShop.config.load()) {
            if (player != null) {
                BSutils.sendMessage(player, "\u00A74Config loading error.");
            }
        } else {
            BSutils.sendMessage(player, "Config.yml loaded.");
            ok = false;
        }
        if (BetterShop.pricelist.load()) {
            BSutils.sendMessage(player, "Price Database " + BetterShop.pricelist.pricelistName() + " loaded.");
        } else {
            if (player != null) {
                BSutils.sendMessage(player, "\u00A74Price Database Load Error.");
                ok = false;
            }
        }
        if (BetterShop.config.logTotalTransactions || BetterShop.config.logUserTransactions) {
            if (BetterShop.transactions.load()) {
                BSutils.sendMessage(player, "Transactions Log Database loaded");
            } else {
                BSutils.sendMessage(player, "\u00A74Price Database Load Error.");
                ok = false;
            }
        }
        if (BetterShop.config.useItemStock) {
            if (BetterShop.stock == null) {
                BetterShop.stock = new BSItemStock();
            }
            if (BetterShop.stock.load()) {
                BSutils.sendMessage(player, "Stock Database loaded");
            } else {
                BSutils.sendMessage(player, "\u00A74Stock Database Load Error.");
                ok = false;
            }
        }
        return player != null || ok;
    }

    public boolean add(CommandSender player, String[] s) {
        if (s.length == 2) {
            // append -1 as sell price
            String ns[] = new String[3];
            for (int i = 0; i < 2; ++i) {
                ns[i] = s[i];
            }
            ns[2] = "-1";
            s = ns;
        }
        if (s.length != 3) {
            return false;
        }
        if (!BSutils.hasPermission(player, "BetterShop.admin.add", true)) {
            return true;
        }

        Item toAdd = Item.findItem(s[0]);
        if (toAdd == null) {
            BSutils.sendMessage(player, String.format(BetterShop.config.getString("unkitem").
                    replace("<item>", "%s"), s[0]));
            return false;
        }

        if (CheckInput.IsDouble(s[1]) && CheckInput.IsDouble(s[2])) {
            if (CheckInput.GetDouble(s[1], -1) > PriceList.MAX_PRICE
                    || CheckInput.GetDouble(s[2], -1) > PriceList.MAX_PRICE) {
                BSutils.sendMessage(player, "Price set too high. Max = " + BetterShop.iConomy.getBank().format(PriceList.MAX_PRICE));
                return true;
            } else if (toAdd.isKit() && CheckInput.GetDouble(s[2], -1) >= 0) {
                BSutils.sendMessage(player, "Note: Kits cannot be sold");
                s[2] = "-1";
            } else if (toAdd.isEntity() && CheckInput.GetDouble(s[2], -1) >= 0) {
                BSutils.sendMessage(player, "Note: Entities cannot be sold");
                s[2] = "-1";
            }
            try {
                boolean isChanged = BetterShop.pricelist.isForSale(toAdd);
                if (BetterShop.pricelist.setPrice(s[0], s[1], s[2])) {
                    if (isChanged) {
                        BSutils.sendMessage(player,
                                String.format(BetterShop.config.getString("chgmsg").
                                replace("<item>", "%1$s").
                                replace("<buyprice>", "%2$01.2f").
                                replace("<sellprice>", "%3$01.2f").
                                replace("<curr>", "%4$s").
                                replace("<buycur>", "%5$s").
                                replace("<sellcur>", "%6$s"),
                                toAdd.name,
                                BetterShop.pricelist.getBuyPrice(toAdd),
                                BetterShop.pricelist.getSellPrice(toAdd),
                                BetterShop.config.currency(),
                                BetterShop.iConomy.getBank().format(BetterShop.pricelist.getBuyPrice(toAdd)),
                                BetterShop.iConomy.getBank().format(BetterShop.pricelist.getSellPrice(toAdd))),
                                BetterShop.config.publicmarket);
                    } else {
                        if (BetterShop.config.useItemStock && BetterShop.stock != null) {
                            BetterShop.stock.setItemAmount(toAdd, BetterShop.config.startStock);
                        }
                        BSutils.sendMessage(player,
                                String.format(BetterShop.config.getString("addmsg").
                                replace("<item>", "%1$s").
                                replace("<buyprice>", "%2$01.2f").
                                replace("<sellprice>", "%3$01.2f").
                                replace("<curr>", "%4$s").
                                replace("<buycur>", "%5$s").
                                replace("<sellcur>", "%6$s"),
                                toAdd.name,
                                BetterShop.pricelist.getBuyPrice(toAdd),
                                BetterShop.pricelist.getSellPrice(toAdd),
                                BetterShop.config.currency(),
                                BetterShop.iConomy.getBank().format(BetterShop.pricelist.getBuyPrice(toAdd)),
                                BetterShop.iConomy.getBank().format(BetterShop.pricelist.getSellPrice(toAdd))),
                                BetterShop.config.publicmarket);
                    }
                    return true;
                }
            } catch (Exception ex) {
                BetterShop.Log(Level.SEVERE, ex);
            }
            BSutils.sendMessage(player, "\u00A74An Error Occurred While Adding.");
        } else {
            BSutils.sendMessage(player, BetterShop.config.getString("paramerror"));
            return false;
        }

        return true;
    }

    public boolean remove(CommandSender player, String[] s) {
        if ((!BSutils.hasPermission(player, "BetterShop.admin.remove", true))) {
            return true;
        } else if (s.length != 1) {
            return false;
        }
        Item toRem = Item.findItem(s[0]);
        if (toRem != null) {
            try {
                BetterShop.pricelist.remove(toRem);
                if (BetterShop.stock != null) {
                    BetterShop.stock.remove(toRem);
                }
                BSutils.sendMessage(player, String.format(BetterShop.config.getString("removemsg").
                        replace("<item>", "%1$s"), toRem.name), BetterShop.config.publicmarket);

                return true;
            } catch (Exception ex) {
                BetterShop.Log(Level.SEVERE, ex);
            }
            BSutils.sendMessage(player, "\u00A74Error removing item");
        } else {
            BSutils.sendMessage(player, String.format(BetterShop.config.getString("unkitem").
                    replace("<item>", "%s"), toRem.name));
        }
        return true;

    }

    public boolean buy(CommandSender player, String[] s) {
        if (!BSutils.hasPermission(player, "BetterShop.user.buy", true)) {
            return true;
        } else if ((s.length > 2) || (s.length == 0)) {
            BSutils.sendMessage(player, "What?");
            return false;
        } else if (BSutils.anonymousCheck(player)) {
            return true;
        }
        if (s.length == 2 && s[0].equalsIgnoreCase("all")) {
            // swap two indicies
            String t = s[0];
            s[0] = s[1];
            s[1] = t;
        }
        Item toBuy = Item.findItem(s[0]);
        if (toBuy == null) {
            BSutils.sendMessage(player, String.format(BetterShop.config.getString("unkitem").replace("<item>", "%1$s"), s[0]));
            return true;
        } else if (!BetterShop.config.allowbuyillegal && !toBuy.IsLegal() && !BSutils.hasPermission(player, "BetterShop.admin.illegal", false)) {
            BSutils.sendMessage(player, String.format(BetterShop.config.getString("illegalbuy").
                    replace("<item>", "%1$s"), toBuy.coloredName()));
            return true;
        } else if (toBuy.isKit()) {
            return buyKit(player, s);
        }

        // initial check complete: set as last action
        userbuyHistory.put(((Player) player).getDisplayName(), "shopbuy " + Str.argStr(s));

        double price = Double.NEGATIVE_INFINITY;

        try {
            price = BetterShop.pricelist.getBuyPrice(toBuy);
        } catch (Exception ex) {
            BetterShop.Log(Level.SEVERE, ex);
        }
        if (price < 0) {
            if (price == Double.NEGATIVE_INFINITY) {
                BSutils.sendMessage(player, "Error looking up price.. Attempting DB reload.. ");
                if (load(null)) {
                    // ask to try command again.. don't want accidental infinite recursion & don't want to plan for recursion right now
                    BSutils.sendMessage(player, "Success! Please try again.. ");
                } else {
                    BSutils.sendMessage(player, "\u00A74Failed! Please let an OP know of this error");
                }
            } else {
                BSutils.sendMessage(player, String.format(
                        BetterShop.config.getString("notforsale").
                        replace("<item>", "%1$s"), toBuy.coloredName()));
            }
            return true;
        }

        int amtbought = 1;
        int canHold = 0, maxStack = BetterShop.config.usemaxstack ? toBuy.getMaxStackSize() : 64;

        PlayerInventory inv = ((Player) player).getInventory();

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
        /*
        for (int i = 0; i <= 35; ++i) {
        ItemStack it = inv.getItem(i);
        if (toBuy.equals(it) || it.getAmount() == 0) {
        canHold += maxStack - it.getAmount();
        }
        }//*/
        if (s.length == 2) {
            if (s[1].equalsIgnoreCase("all")) {
                amtbought = canHold;
            } else if (!CheckInput.IsInt(s[1])) {
                BSutils.sendMessage(player, s[1] + " is definitely not a number.");
                return true;
            } else {
                amtbought = CheckInput.GetInt(s[1], -1);
                if (amtbought > canHold) {
                    BSutils.sendMessage(player, String.format(BetterShop.config.getString("outofroom").
                            replace("<item>", "%1$s").replace("<amt>", "%2$d").
                            replace("<priceper>", "%3$01.2f").replace("<leftover>", "%4$d").
                            replace("<curr>", "%5$s").replace("<free>", "%6$d"), toBuy.coloredName(),
                            amtbought, price, amtbought - canHold, BetterShop.config.currency(), canHold));
                    if (canHold == 0) {
                        return true;
                    }
                    amtbought = canHold;
                } else if (amtbought <= 0) {
                    BSutils.sendMessage(player, BetterShop.config.getString("nicetry"));
                    return true;
                }
            }
        }
        // now check if there are items avaliable for purchase
        long avail = -1;
        if (BetterShop.stock != null && BetterShop.config.useItemStock) {
            try {
                avail = BetterShop.stock.getItemAmount(toBuy);
            } catch (Exception ex) {
                BetterShop.Log(Level.SEVERE, ex);
            }
            if (avail == -1) {
                BSutils.sendMessage(player, "\u00A74Failed to lookup an item stock listing");
                return true;
            } else if (avail == 0) {
                BSutils.sendMessage(player, "this item is currently out of stock");
                return true;
            } else if (amtbought > avail) {
                BSutils.sendMessage(player, String.format("only %d avaliable for purchase", avail));
                amtbought = (int) avail;
            }
        }
        double cost = amtbought * price;
        try {
            if (cost == 0 || BSutils.debit(player, cost)) {
                if (!toBuy.isEntity()) {
                    if (maxStack == 64) { //((Player) player).getInventory().addItem(toBuy.toItemStack(amtbought));
                        inv.addItem(toBuy.toItemStack(amtbought));
                    } else {
                        int amtLeft = amtbought;
                        for (int i = 0; i <= 35; ++i) {
                            ItemStack it = inv.getItem(i);
                            if (it.getAmount() == 0 || (toBuy.equals(it) && it.getAmount() < maxStack)) {
                                inv.setItem(i, toBuy.toItemStack((maxStack < amtLeft ? maxStack : amtLeft) + it.getAmount()));
                                amtLeft -= maxStack;
                            }
                            if (amtLeft <= 0) {
                                break;
                            }
                        }
                    }
                    // drop in front of player?
                    //World w = player.getServer().getWorld(""); w.dropItem(player.getServer().getPlayer("").getLocation(), leftover.values());//.dropItem(
                } else {
                    CreatureItem.spawnNewWithOwner((Player) player, CreatureItem.getCreature(toBuy.ID()));
                }
                BSutils.sendMessage(player, String.format(BetterShop.config.getString("buymsg").
                        replace("<item>", "%1$s").
                        replace("<amt>", "%2$d").
                        replace("<priceper>", "%3$01.2f").
                        replace("<total>", "%4$01.2f").
                        replace("<curr>", "%5$s").
                        replace("<totcur>", "%6$s"),
                        toBuy.coloredName(), amtbought, price, cost,
                        BetterShop.config.currency(), BetterShop.iConomy.getBank().format(cost)));

                if (BetterShop.config.publicmarket && BetterShop.config.hasString("publicbuymsg")) {
                    BSutils.broadcastMessage(player, String.format(BetterShop.config.getString("publicbuymsg").
                            replace("<item>", "%1$s").
                            replace("<amt>", "%2$d").
                            replace("<priceper>", "%3$01.2f").
                            replace("<total>", "%4$01.2f").
                            replace("<curr>", "%5$s").
                            replace("<totcur>", "%6$s").
                            replace("<player>", "%7$s"),
                            toBuy.coloredName(), amtbought, price, cost,
                            BetterShop.config.currency(), BetterShop.iConomy.getBank().format(cost), ((Player) player).getDisplayName()), false);
                }

                if (BetterShop.stock != null && BetterShop.config.useItemStock) {
                    BetterShop.stock.changeItemAmount(toBuy, -amtbought);
                }

                BetterShop.transactions.addRecord(new UserTransaction(
                        toBuy, false, amtbought, price, ((Player) player).getDisplayName()));

                return true;
            } else {
                BSutils.sendMessage(player, String.format(BetterShop.config.getString("insuffunds").
                        replace("<item>", "%1$s").
                        replace("<amt>", "%2$d").
                        replace("<total>", "%3$01.2f").
                        replace("<curr>", "%5$s").
                        replace("<priceper>", "%4$01.2f").
                        replace("<totcur>", "%6$s"), toBuy.coloredName(),
                        amtbought, cost, price, BetterShop.config.currency(),
                        BetterShop.iConomy.getBank().format(price)));
                return true;
            }
        } catch (Exception e) {
            BetterShop.Log("Error while debiting player.. possible iConomy crash? (attempting to reload iConomy)");
            BetterShop.Log(Level.SEVERE, e);
            BSutils.sendMessage(player, "Error while debiting player.. possible iConomy crash? ");
            BSutils.sendMessage(player, "Attempting reload.. ");
            if (BSutils.reloadIConomy(player.getServer())) {
                BSutils.sendMessage(player, "Success! Please try again.. ");
                return true;
            }
            BSutils.sendMessage(player, "Failed.");
            return true;
        }
    }

    public boolean buystack(CommandSender player, String[] s) {
        if (!BSutils.hasPermission(player, "BetterShop.user.buy", true)) {
            return true;
        } else if (s.length == 0) {
            BSutils.sendMessage(player, "What?");
            return false;
        } else if (BSutils.anonymousCheck(player)) {
            return true;
        }
        if (s.length == 2 && CheckInput.IsInt(s[1])) {

            Item toBuy = Item.findItem(s[0]);
            if (toBuy == null) {
                BSutils.sendMessage(player, String.format(BetterShop.config.getString("unkitem").
                        replace("<item>", "%1$s"), s[0]));
                return true;
            } else if (!BetterShop.config.allowbuyillegal && !toBuy.IsLegal() && !BSutils.hasPermission(player, "BetterShop.admin.illegal", false)) {
                BSutils.sendMessage(player, String.format(BetterShop.config.getString("illegalbuy").
                        replace("<item>", "%1$s"), toBuy.coloredName()));
                return true;
            }
            // buy max. stackable
            buy(player, new String[]{toBuy.IdDatStr(), String.valueOf((BetterShop.config.usemaxstack ? toBuy.getMaxStackSize() : 64) * CheckInput.GetInt(s[1], 1))});
        } else {
            for (String is : s) {
                Item toBuy = Item.findItem(is);
                if (toBuy == null) {
                    BSutils.sendMessage(player, String.format(BetterShop.config.getString("unkitem").
                            replace("<item>", "%1$s"), is));
                    return true;
                } else if (!BetterShop.config.allowbuyillegal && !toBuy.IsLegal() && !BSutils.hasPermission(player, "BetterShop.admin.illegal", false)) {
                    BSutils.sendMessage(player, String.format(BetterShop.config.getString("illegalbuy").
                            replace("<item>", "%1$s"), toBuy.coloredName()));
                    return true;
                }
                // buy max. stackable
                buy(player, new String[]{toBuy.IdDatStr(), String.valueOf(BetterShop.config.usemaxstack ? toBuy.getMaxStackSize() : 64)});
            }
        }// overwrite history that buy wrote
        userbuyHistory.put(((Player) player).getDisplayName(), "shopbuystack " + Str.argStr(s));
        return true;
    }

    public boolean buyKit(CommandSender player, String[] s) {
        if (!BSutils.hasPermission(player, "BetterShop.user.buy", true)) {
            return true;
        } else if (s.length == 0 || s.length > 2) {
            BSutils.sendMessage(player, "What?");
            return false;
        } else if (s.length == 2 && !(s[1].equalsIgnoreCase("all") || CheckInput.IsInt(s[1]))) {
            BSutils.sendMessage(player, s[1] + " is not a number..");
            return true;
        } else if (BSutils.anonymousCheck(player)) {
            return true;
        }

        Item toBuy = Item.findItem(s[0]);
        if (toBuy == null) {
            BSutils.sendMessage(player, String.format(BetterShop.config.getString("unkitem").
                    replace("<item>", "%1$s"), s[0]));
            return true;
        } else if (!BetterShop.config.allowbuyillegal && !toBuy.IsLegal() && !BSutils.hasPermission(player, "BetterShop.admin.illegal", false)) {
            BSutils.sendMessage(player, String.format(BetterShop.config.getString("illegalbuy").
                    replace("<item>", "%1$s"), toBuy.coloredName()));
            return true;
        } else if (!toBuy.isKit()) {
            BSutils.sendMessage(player, toBuy.coloredName() + " is not a kit");
            return true;
        }// initial check complete: set as last action
        usersellHistory.put(((Player) player).getDisplayName(), "shopbuy " + Str.argStr(s));

        double price = Double.NEGATIVE_INFINITY;

        try {
            price = BetterShop.pricelist.getBuyPrice(toBuy);
        } catch (Exception ex) {
            BetterShop.Log(Level.SEVERE, ex);
        }
        if (price < 0) {
            if (price == Double.NEGATIVE_INFINITY) {
                BSutils.sendMessage(player, "Error looking up price.. Attempting DB reload.. ");
                if (load(null)) {
                    // ask to try command again.. don't want accidental infinite recursion & don't want to plan for recursion right now
                    BSutils.sendMessage(player, "Success! Please try again.. ");
                } else {
                    BSutils.sendMessage(player, "\u00A74Failed! Please let an OP know of this error");
                }
            } else {
                BSutils.sendMessage(player, String.format(
                        BetterShop.config.getString("notforsale").
                        replace("<item>", "%1$s"), toBuy.coloredName()));
            }
            return true;
        }

        Kit kitToBuy = ItemDB.getKit(toBuy);

        PlayerInventory inv = ((Player) player).getInventory();
        KitItem items[] = kitToBuy.getKitItems();

        int maxBuy = 0;
        //*
        ItemStack invCopy[] = new ItemStack[36];
        for (int i = 0; i <= 35; ++i) {
            invCopy[i] = new ItemStack(inv.getItem(i).getType(), inv.getItem(i).getAmount(), inv.getItem(i).getDurability());
        }

        while (true) {
            int numtoadd = 0;
            for (int itn = 0; itn < kitToBuy.numItems(); ++itn) {
                numtoadd = items[itn].itemAmount;
                int maxStack = BetterShop.config.usemaxstack ? items[itn].getMaxStackSize() : 64;
                // don't search armor slots
                for (int i = 0; i <= 35; ++i) {
                    //if (items[itn].equals(new Item(invCopy[i])) ||  (invCopy[i].getAmount() == 0 && invCopy[i].getAmount()<maxStack)) {
                    if ((items[itn].equals(new Item(invCopy[i])) && invCopy[i].getAmount() < maxStack)
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
        }//*/
        int numToBuy = 1;
        if (s.length == 2) {
            if (s[1].equalsIgnoreCase("all")) {
                numToBuy = maxBuy;
            } else {
                numToBuy = CheckInput.GetInt(s[1], 1);
                if (numToBuy <= 0) {
                    BSutils.sendMessage(player, BetterShop.config.getString("nicetry"));
                    return true;
                }
            }
        }

        if (numToBuy > maxBuy) {
            BSutils.sendMessage(player, String.format(BetterShop.config.getString("outofroom").
                    replace("<item>", "%1$s").
                    replace("<amt>", "%2$d").
                    replace("<priceper>", "%3$01.2f").
                    replace("<leftover>", "%4$d").
                    replace("<curr>", "%5$s").
                    replace("<free>", "%6$d"), toBuy.coloredName(),
                    numToBuy, price, numToBuy - maxBuy, BetterShop.config.currency(), maxBuy));
            if (maxBuy == 0) {
                return true;
            }
            numToBuy = maxBuy;
        }

        // now check if there are items avaliable for purchase
        long avail = -1;
        if (BetterShop.stock != null && BetterShop.config.useItemStock) {
            try {
                avail = BetterShop.stock.getItemAmount(toBuy);
            } catch (Exception ex) {
                BetterShop.Log(Level.SEVERE, ex);
            }
            if (avail == -1) {
                BSutils.sendMessage(player, "\u00A74Failed to lookup an item stock listing");
                return true;
            } else if (avail == 0) {
                BSutils.sendMessage(player, "this item is currently out of stock");
                return true;
            } else if (numToBuy > avail) {
                BSutils.sendMessage(player, String.format("only %d avaliable for purchase", avail));
                numToBuy = (int) avail;
            }
        }

        double cost = numToBuy * price;
        if (cost == 0 || BSutils.debit(player, cost)) {
            try {
                for (int num = 0; num < numToBuy; ++num) {
                    int numtoadd = 0;
                    for (int itn = 0; itn < kitToBuy.numItems(); ++itn) {
                        numtoadd = items[itn].itemAmount;
                        int maxStack = BetterShop.config.usemaxstack ? items[itn].getMaxStackSize() : 64;
                        // don't search armor slots
                        for (int i = 0; i <= 35; ++i) {
                            if ((items[itn].equals(new Item(inv.getItem(i))) && inv.getItem(i).getAmount() < maxStack) || inv.getItem(i).getAmount() == 0) {
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
                    BetterShop.stock.changeItemAmount(toBuy, -numToBuy);
                }
                BSutils.sendMessage(player, String.format(BetterShop.config.getString("buymsg").replace("<item>", "%1$s").replace("<amt>", "%2$d").replace("<priceper>", "%3$01.2f").replace("<total>", "%4$01.2f").replace("<curr>", "%5$s").replace("<totcur>", "%6$s"), toBuy.coloredName(), numToBuy, price, cost, BetterShop.config.currency(), BetterShop.iConomy.getBank().format(cost)));
                if (BetterShop.config.publicmarket && BetterShop.config.hasString("publicbuymsg")) {
                    BSutils.broadcastMessage(player, String.format(BetterShop.config.getString("publicbuymsg").replace("<item>", "%1$s").replace("<amt>", "%2$d").replace("<priceper>", "%3$01.2f").replace("<total>", "%4$01.2f").replace("<curr>", "%5$s").replace("<totcur>", "%6$s").replace("<player>", "%7$s"), toBuy.coloredName(), numToBuy, price, cost, BetterShop.config.currency(), BetterShop.iConomy.getBank().format(cost), ((Player) player).getDisplayName()), false);
                }
                BetterShop.transactions.addRecord(new UserTransaction(toBuy, false, numToBuy, price, ((Player) player).getDisplayName()));
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
                    numToBuy, cost, price, BetterShop.config.currency(),
                    BetterShop.iConomy.getBank().format(price)));
        }
        return true;
    }

    public boolean sellstack(CommandSender player, String[] s) {
        if (!BSutils.hasPermission(player, "BetterShop.user.sell", true)) {
            return true;
        } else if (s.length == 0) {
            BSutils.sendMessage(player, "What?");
            return false;
        } else if (BSutils.anonymousCheck(player)) {
            return true;
        }
        if (s.length == 2 && CheckInput.IsInt(s[1])) {

            Item toSell = Item.findItem(s[0]);
            if (toSell == null) {
                BSutils.sendMessage(player, String.format(BetterShop.config.getString("unkitem").
                        replace("<item>", "%1$s"), s[0]));
                return true;
            } else if (!BetterShop.config.allowbuyillegal && !toSell.IsLegal() && !BSutils.hasPermission(player, "BetterShop.admin.illegal", false)) {
                BSutils.sendMessage(player, String.format(BetterShop.config.getString("illegalbuy").
                        replace("<item>", "%1$s"), toSell.coloredName()));
                return true;
            }
            // sell max. stackable
            sell(player, new String[]{toSell.IdDatStr(), String.valueOf((BetterShop.config.usemaxstack ? toSell.getMaxStackSize() : 64) * CheckInput.GetInt(s[1], 1))});
        } else {
            for (String is : s) {
                Item toSell = Item.findItem(is);
                if (toSell == null) {
                    BSutils.sendMessage(player, String.format(BetterShop.config.getString("unkitem").
                            replace("<item>", "%1$s"), is));
                    return true;
                } else if (!BetterShop.config.allowbuyillegal && !toSell.IsLegal() && !BSutils.hasPermission(player, "BetterShop.admin.illegal", false)) {
                    BSutils.sendMessage(player, String.format(BetterShop.config.getString("illegalbuy").
                            replace("<item>", "%1$s"), toSell.coloredName()));
                    return true;
                }
                // sell max. stackable
                sell(player, new String[]{toSell.IdDatStr(), String.valueOf(BetterShop.config.usemaxstack ? toSell.getMaxStackSize() : 64)});
            }
        }// overwrite history that selll wrote
        usersellHistory.put(((Player) player).getDisplayName(), "shopsellstack " + Str.argStr(s));
        return true;
    }

    public boolean sell(CommandSender player, String[] s) {
        if (!BSutils.hasPermission(player, "BetterShop.user.sell", true) || BSutils.anonymousCheck(player)) {
            return true;
        } // "sell all", "sell all [item]" moved to own method ("sell [item] all" kept here)
        else if (s.length == 1 && s[0].equalsIgnoreCase("all")) {
            return sellall(player, null);
        } else if (s.length == 2) {
            if (s[0].equalsIgnoreCase("all")) {
                return sellall(player, new String[]{s[1]});
            } else if (s[1].equalsIgnoreCase("all")) {
                return sellall(player, new String[]{s[0]});
            }
        } else if (s.length == 0 || s.length > 2) {
            return false;
        }// initial check complete: set as last action
        usersellHistory.put(((Player) player).getDisplayName(), "shopsell " + Str.argStr(s));
        // expected syntax: item [amount]

        Item toSell = Item.findItem(s[0]);
        if (toSell == null) {
            BSutils.sendMessage(player, String.format(
                    BetterShop.config.getString("unkitem").
                    replace("<item>", "%1$s"), s[0]));
            return false;
        } else if (toSell.isKit()) {
            BSutils.sendMessage(player, "Kits cannot be sold");
            return true;
        } else if (toSell.isEntity()) {
            BSutils.sendMessage(player, "Entities cannot be sold");
            return true;
        }
        double price = Double.NEGATIVE_INFINITY;
        try {
            price = BetterShop.pricelist.getSellPrice(toSell);
        } catch (Exception ex) {
            BetterShop.Log(Level.SEVERE, ex);
        }
        if (price < 0) {
            if (price == Double.NEGATIVE_INFINITY) {
                BSutils.sendMessage(player, "Error looking up price.. Attempting DB reload..");
                if (load(null)) {
                    // ask to try command again.. don't want accidental infinite recursion & don't want to plan for recursion right now
                    BSutils.sendMessage(player, "Success! Please try again.. ");
                } else {
                    BSutils.sendMessage(player, "\u00A74Failed! Please let an OP know of this error");
                }
            } else {
                BSutils.sendMessage(player, String.format(
                        BetterShop.config.getString("donotwant").
                        replace("<item>", "%1$s"), toSell.coloredName()));
            }
            return true;
        }

        // go through inventory & find how much user has

        PlayerInventory inv = ((Player) player).getInventory();
        int amtSold = 1, amtHas = 0;

        for (ItemStack i : inv.getContents()) {
            if (toSell.equals(i)) {
                //System.out.println("found: " + i);
                if (!toSell.IsTool() || (toSell.IsTool()
                        && (i.getDurability() == 0 || BetterShop.config.buybacktools))) {
                    amtHas += i.getAmount();
                }
            }
        }

        if (amtHas <= 0) {
            BSutils.sendMessage(player, "You Don't have any " + (toSell == null ? "Sellable Items" : toSell.coloredName()));
            return true;
        }
        if (s.length == 2) {
            if (s[1].equalsIgnoreCase("all")) {
                amtSold = amtHas;
            } else if (CheckInput.IsInt(s[1])) {
                amtSold = CheckInput.GetInt(s[1], 1);
                if (amtSold > amtHas) {
                    BSutils.sendMessage(player, String.format(
                            BetterShop.config.getString("donthave").
                            replace("<hasamt>", "%1$d").
                            replace("<amt>", "%2$d").
                            replace("<item>", "%3$s"), amtHas, amtSold, toSell.coloredName()));
                    amtSold = amtHas;
                } else if (amtSold <= 0) {
                    BSutils.sendMessage(player, BetterShop.config.getString("nicetry"));
                    return true;
                }
            } else {
                BSutils.sendMessage(player, s[1] + " is definitely not a number.");
            }
        } // else  amtSold = 1


        // now check the remaining stock can sell back
        long avail = -1;
        if (BetterShop.config.useItemStock && BetterShop.stock != null) {
            avail = BetterShop.stock.freeStockRemaining(toSell);
            if (avail == -1) {
                BSutils.sendMessage(player, "\u00A74Failed to lookup an item stock listing");
                return true;
            } else if (avail == 0 && BetterShop.config.noOverStock) {
                BSutils.sendMessage(player, "this item is currently at max stock");
                return true;
            } else if (amtSold > avail && BetterShop.config.noOverStock) {
                BSutils.sendMessage(player, String.format("only %d can be sold back", avail));
                amtSold = (int) avail;
            }
        }

        double total = 0;//amtSold * price;

        int itemsLeft = amtSold;

        for (int i = 0; i <= 35; ++i) {
            ItemStack thisSlot = inv.getItem(i);
            //if (toSell.equals(thisSlot)) {
            if (toSell.equals(thisSlot) && (!toSell.IsTool() || (toSell.IsTool()
                    && (thisSlot.getDurability() == 0
                    || (thisSlot.getDurability() > 0 && BetterShop.config.buybacktools))))) {
                int amt = thisSlot.getAmount(), tamt = amt;

                if (itemsLeft >= amt) {
                    inv.setItem(i, null);
                } else {
                    // remove only whats left to remove
                    inv.setItem(i, toSell.toItemStack(amt - itemsLeft));
                    amt = itemsLeft;
                }
                if (toSell.IsTool()) {
                    //System.out.println("tool with " + thisSlot.getDurability() +"/"+ toSell.MaxDamage());
                    total += (price * (1 - ((double) thisSlot.getDurability() / toSell.MaxDamage()))) * amt;
                } else {
                    total += price * amt;
                }
                itemsLeft -= tamt;

                if (itemsLeft <= 0) {
                    break;
                }
            }
        }

        BSutils.credit(player, total);

        if (BetterShop.stock != null && BetterShop.config.useItemStock) {
            try {
                BetterShop.stock.changeItemAmount(toSell, amtSold);
            } catch (Exception ex) {
                BetterShop.Log(Level.SEVERE, ex);
            }
        }
        BSutils.sendMessage(player, String.format(BetterShop.config.getString("sellmsg").
                replace("<item>", "%1$s").
                replace("<amt>", "%2$d").
                replace("<priceper>", "%3$01.2f").
                replace("<total>", "%4$01.2f").
                replace("<curr>", "%5$s").
                replace("<totcur>", "%6$s"),
                toSell.coloredName(), amtSold, total / amtSold, total, BetterShop.config.currency(), BetterShop.iConomy.getBank().format(total)));
        //price
        if (BetterShop.config.publicmarket && BetterShop.config.hasString("publicsellmsg")) {
            BSutils.broadcastMessage(player, String.format(BetterShop.config.getString("publicsellmsg").
                    replace("<item>", "%1$s").
                    replace("<amt>", "%2$d").
                    replace("<priceper>", "%3$01.2f").
                    replace("<total>", "%4$01.2f").
                    replace("<curr>", "%5$s").
                    replace("<totcur>", "%6$s").
                    replace("<player>", "%7$s"),
                    toSell.coloredName(), amtSold, total / amtSold, total,
                    BetterShop.config.currency(), BetterShop.iConomy.getBank().format(total), ((Player) player).getDisplayName()), false);

        }
        try {
            BetterShop.transactions.addRecord(new UserTransaction(toSell, true, amtSold, total / amtSold, ((Player) player).getDisplayName()));
        } catch (Exception ex) {
            BetterShop.Log(Level.SEVERE, ex);
        }

        return true;
    }

    public boolean sellall(CommandSender player, String[] s) {
        Item toSell[] = null;
        boolean onlyInv = false;
        if (s != null) {
            if (!BSutils.hasPermission(player, "BetterShop.user.sell", true) || BSutils.anonymousCheck(player)) {
                return true;
            } /*else if (s.length < 1) {
            return false;
            }*/
            if (s.length > 0) {
                // expected syntax: [inv] [item [item [item [...]]]]
                int st = 0;
                if (s[0].equalsIgnoreCase("inv")) {
                    onlyInv = true;
                    st = 1;
                }
                toSell = new Item[s.length - st];
                for (int i = st; i < s.length; ++i) {
                    toSell[i - st] = Item.findItem(s[i]);
                    if (toSell[i - st] == null) {
                        BSutils.sendMessage(player, String.format(
                                BetterShop.config.getString("unkitem").
                                replace("<item>", "%1$s"), s[0]));
                        return false;
                    } else if (toSell[i - st].isKit()) {
                        BSutils.sendMessage(player, "Kits cannot be sold");
                        return true;
                    } else if (toSell[i - st].isEntity()) {
                        BSutils.sendMessage(player, "Entities cannot be sold");
                        return true;
                    }
                }
            } // "[All Sellable]"
        }// initial check complete: set as last action
        usersellHistory.put(((Player) player).getDisplayName(), "shopsellall " + Str.argStr(s));

        boolean err = false, overstock = false;
        //PlayerInventory inv = ((Player) player).getInventory();
        //ItemStack[] its = inv.getContents();
        ArrayList<ItemStockEntry> playerInv = BSutils.getTotalInventory((Player) player, onlyInv, toSell);
        int amtHas = 0;
        double total = 0;
        ArrayList<String> notwant = new ArrayList<String>();
        try {
            for (int i = 0; i < playerInv.size(); ++i) {
                Item check = Item.findItem(playerInv.get(i));
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
                        if (free == -1) {
                            err = true;
                        } else if (free == 0 && BetterShop.config.noOverStock) {
                            BSutils.sendMessage(player, String.format("%s is currently at max stock", check.coloredName()));
                            playerInv.get(i).amount = 0;
                            overstock = true;
                        } else if (playerInv.get(i).amount > free && BetterShop.config.noOverStock) {
                            BSutils.sendMessage(player, String.format("only %d %s can be sold back", free, check.coloredName()));
                            playerInv.get(i).amount = (int) free;
                        }
                    }
                    amtHas += playerInv.get(i).amount;
                }
            }
        } catch (Exception ex) {
            BetterShop.Log(Level.SEVERE, ex);
            err = true;
        }
        if (err) {
            BSutils.sendMessage(player, "Error looking up an item.. Attempting DB reload..");
            if (load(null)) {
                // ask to try command again.. don't want accidental infinite recursion & don't want to plan for recursion right now
                BSutils.sendMessage(player, "Success! Please try again.. ");
            } else {
                BSutils.sendMessage(player, "\u00A74Failed! Please let an OP know of this error");
            }
            return true;
        } else if (notwant.size() > 0) {
            BSutils.sendMessage(player, String.format(
                    BetterShop.config.getString("donotwant").
                    replace("<item>", "%1$s"), "(" + Str.argStr(notwant.toArray(new String[0]), ", ") + ")"));
            if (notwant.size() == toSell.length) {
                return true;
            }
        }
        if (amtHas <= 0) {
            if (!overstock) {
                BSutils.sendMessage(player, "You Don't have any " + (toSell == null || toSell.length == 0 ? "Sellable Items"
                        : (toSell.length == 1 ? toSell[0].coloredName() : "of those items")));
            }
            return true;
        }
        // make list of transactions made (or should make)
        LinkedList<UserTransaction> transactions = new LinkedList<UserTransaction>();
        try {
            for (ItemStockEntry ite : playerInv) {
                transactions.add(new UserTransaction(ite, true,
                        BetterShop.pricelist.getSellPrice(ite),
                        ((Player) player).getDisplayName()));
            }
        } catch (Exception ex) {
            BetterShop.Log(Level.SEVERE, ex);
        }
        // now scan through & remove the items
        total = sellItems((Player) player, onlyInv, playerInv);
        BSutils.credit(player, total);

        String itemN = ""; // "(All Sellable)"
        if (toSell != null && toSell.length == 1) {
            itemN = toSell[0].coloredName();
        } else {
            itemN = "(";
            for (UserTransaction it : transactions) {
                itemN += it.GetItem().coloredName() + " ";
            }
            itemN = itemN.trim().replace(" ", ", ") + ")";
        }

        BSutils.sendMessage(player, String.format(BetterShop.config.getString("sellmsg").
                replace("<item>", "%1$s").
                replace("<amt>", "%2$d").
                replace("<priceper>", "%3$01.2f").
                replace("<total>", "%4$01.2f").
                replace("<curr>", "%5$s").
                replace("<totcur>", "%6$s"),
                itemN, amtHas, total / amtHas, total, BetterShop.config.currency(), BetterShop.iConomy.getBank().format(total)));

        if (BetterShop.config.publicmarket && BetterShop.config.hasString("publicsellmsg")) {
            BSutils.broadcastMessage(player, String.format(BetterShop.config.getString("publicsellmsg").
                    replace("<item>", "%1$s").
                    replace("<amt>", "%2$d").
                    replace("<priceper>", "%3$01.2f").
                    replace("<total>", "%4$01.2f").
                    replace("<curr>", "%5$s").
                    replace("<totcur>", "%6$s").
                    replace("<player>", "%7$s"),
                    itemN, amtHas, total / amtHas, total,
                    BetterShop.config.currency(), BetterShop.iConomy.getBank().format(total), ((Player) player).getDisplayName()), false);

        }

        try {
            for (UserTransaction t : transactions) {
                BetterShop.transactions.addRecord(t);
            }
        } catch (Exception ex) {
            BetterShop.Log(Level.SEVERE, ex);
        }
        return true;
    }

    public boolean listkits(CommandSender player, String[] s) {
        try {
            BSutils.sendMessage(player, "Kit listing:");
            String kitNames = "";
            for (Item i : BetterShop.pricelist.getItems(BSutils.hasPermission(player, "BetterShop.admin.illegal"))) {
                if (i.isKit()) {
                    if (kitNames.length() > 0) {
                        kitNames += ", ";
                    }
                    kitNames += i.coloredName();
                }
            }
            BSutils.sendMessage(player, kitNames);
            return true;
        } catch (Exception ex) {
            BetterShop.Log(Level.SEVERE, ex);
        }
        BSutils.sendMessage(player, "Error looking up an item.. Attempting DB reload..");
        if (load(null)) {
            // ask to try command again.. don't want accidental infinite recursion & don't want to plan for recursion right now
            BSutils.sendMessage(player, "Success! Please try again.. ");
        } else {
            BSutils.sendMessage(player, "\u00A74Failed! Please let an OP know of this error");
        }
        return true;
    }

    protected static double sellItems(Player player, boolean onlyInv, ArrayList<ItemStockEntry> items) {
        double credit = 0;
        PlayerInventory inv = ((Player) player).getInventory();
        ItemStack[] its = inv.getContents();
        try {
            for (ItemStockEntry ite : items) {
                Item toSell = Item.findItem(ite);
                if (toSell != null) {
                    int amtLeft = (int) ite.amount;
                    for (int i = (onlyInv ? 9 : 0); i <= 35; ++i) {
                        Item it = Item.findItem(its[i]);
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
        return credit;
    }

    public boolean importDB(CommandSender player, String[] s) {
        if ((!BSutils.hasPermission(player, "BetterShop.admin.backup", true))) {
            return true;
        }
        String fname = Str.argStr(s);
        if (fname.length() > 6 && fname.substring(0, 7).equalsIgnoreCase("import ")) {
            fname = fname.substring(7);
        }
        if (fname.length() == 0) {
            BSutils.sendMessage(player, "Need a file to import");
            return true;
        }

        File toImport = new File(BSConfig.pluginFolder.getPath() + File.separatorChar + fname);
        if (!toImport.exists() && !fname.toLowerCase().endsWith(".csv")) {
            fname += ".csv";
            toImport = new File(BSConfig.pluginFolder.getPath() + File.separatorChar + fname);
        }
        if (!toImport.exists()) {
            BSutils.sendMessage(player, fname + " does not exist");
            return true;
        }
        if (BetterShop.pricelist.importDB(toImport)) {
            BSutils.sendMessage(player, "Database Imported");
        } else {
            BSutils.sendMessage(player, "\u00A74 An Error Occured while importing database");
        }
        return true;
    }

    public boolean restoreDB(CommandSender player, String[] s) {
        if ((!BSutils.hasPermission(player, "BetterShop.admin.backup", true))) {
            return true;
        }
        String fname = Str.argStr(s);
        if (fname.length() > 7 && fname.substring(0, 8).equalsIgnoreCase("restore ")) {
            fname = fname.substring(8);
        }
        if (fname.length() == 0) {
            BSutils.sendMessage(player, "Need a file to import");
            return true;
        }

        File toImport = new File(BSConfig.pluginFolder.getPath() + File.separatorChar + fname);
        if (!toImport.exists() && !fname.toLowerCase().endsWith(".csv")) {
            fname += ".csv";
            toImport = new File(BSConfig.pluginFolder.getPath() + File.separatorChar + fname);
        }
        if (!toImport.exists()) {
            BSutils.sendMessage(player, fname + " does not exist");
            return true;
        }
        if (BetterShop.pricelist.restoreDB(toImport)) {
            BSutils.sendMessage(player, "Database Imported");
        } else {
            BSutils.sendMessage(player, "\u00A74 An Error Occured while importing database");
        }
        return true;
    }
}
