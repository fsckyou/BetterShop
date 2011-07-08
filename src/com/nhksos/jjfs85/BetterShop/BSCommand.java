package com.nhksos.jjfs85.BetterShop;

import com.jascotty2.util.ArrayManip;
import com.jascotty2.io.CheckInput;
import com.jascotty2.Item.JItem;
import com.jascotty2.Item.JItemDB;
import com.jascotty2.Item.ItemStockEntry;
import com.jascotty2.Item.PriceListItem;
import com.jascotty2.Shop.UserTransaction;
import com.jascotty2.Shop.PriceList;
import com.jascotty2.bukkit.MinecraftChatStr;
import com.jascotty2.util.Str;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.logging.Level;
import org.bukkit.ChatColor;

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
        if (!BSutils.hasPermission(player, BSutils.BetterShopPermission.USER_HELP, true)) {
            return true;
        }
        if (s.length > 0) {
            // more help
            if (Str.isIn(s[0], "shop")) {
                BSutils.sendMessage(player, "/shop   command alias to other commands");
                BSutils.sendMessage(player, "      ");
                if (BSutils.hasPermission(player, BSutils.BetterShopPermission.ADMIN_BACKUP, false)) {
                    BSutils.sendMessage(player, "/shop backup   to backup current pricelist");
                }
                if (BSutils.hasPermission(player, BSutils.BetterShopPermission.ADMIN_INFO, false)) {
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
        if (BSutils.hasPermission(player, BSutils.BetterShopPermission.USER_LIST, false)) {
            BSutils.sendMessage(player, "/shoplist [page] - List shop prices");
            BSutils.sendMessage(player, "/shopitems - show listing of items in shop, without prices");
            BSutils.sendMessage(player, "/shopkits [page] - show listing of kits in shop");
        }
        if (BSutils.hasPermission(player, BSutils.BetterShopPermission.USER_BUY, false)) {
            BSutils.sendMessage(player, "/shopbuy <item> [amount] - Buy items");
            BSutils.sendMessage(player, "/shopbuyall <item> - Buy all that you can hold/afford");
            BSutils.sendMessage(player, "/shopbuystack <item> [amount] - Buy stacks of items");
            BSutils.sendMessage(player, "/shopbuyagain - repeat last purchase action");
        }
        if (BSutils.hasPermission(player, BSutils.BetterShopPermission.USER_SELL, false)) {
            BSutils.sendMessage(player, "/shopsell <item> [amount] - Sell items ");
            BSutils.sendMessage(player, "/shopsellall <item> - Sell all of your items");
            BSutils.sendMessage(player, "/shopsellagain - Repeat last sell action");
        }
        if (BSutils.hasPermission(player, BSutils.BetterShopPermission.USER_CHECK, false)) {
            BSutils.sendMessage(player, "/shopcheck <item> - Check prices of item");
        }
        BSutils.sendMessage(player, "/shophelp [command] - show help on commands");
        if (BSutils.hasPermission(player, BSutils.BetterShopPermission.ADMIN, false)) {
            BSutils.sendMessage(player, "**-------- Admin commands --------**");
            if (BSutils.hasPermission(player, BSutils.BetterShopPermission.ADMIN_ADD, false)) {
                BSutils.sendMessage(player, "/shopadd <item> <$buy> [$sell] - Add/Update an item");
            }
            if (BSutils.hasPermission(player, BSutils.BetterShopPermission.ADMIN_REMOVE, false)) {
                BSutils.sendMessage(player, "/shopremove <item> - Remove an item from the shop");
            }
            if (BSutils.hasPermission(player, BSutils.BetterShopPermission.ADMIN_LOAD, false)) {
                BSutils.sendMessage(player, "/shopload - Reload the Configuration & PriceList DB");
            }
        }
        BSutils.sendMessage(player, "----------------------------------");
        return true;
    }

    public boolean check(CommandSender player, String[] s) {
        if (!BSutils.hasPermission(player, BSutils.BetterShopPermission.USER_CHECK, true)) {
            return true;
        }
        if (s == null || s.length == 0 || s.length > 2) {
            return false;
        } else if (s.length > 1 && !CheckInput.IsInt(s[1]) && !s[1].equalsIgnoreCase("all")) {
            BSutils.sendMessage(player, "Invalid amount");
            return true;
        }

        boolean canBuyIllegal = BetterShop.config.allowbuyillegal
                || BSutils.hasPermission(player, BSutils.BetterShopPermission.ADMIN_ILLEGAL, false);

        try {
            if (s[0].equalsIgnoreCase("all")) {
                ArrayList<ItemStockEntry> sellable = BSutils.getCanSell((Player) player, false, null);
                if (!sellable.isEmpty()) {
                    PriceListItem price = new PriceListItem();
                    price.buy = price.sell = 0;
                    String name = "("; // price.name = "(";
                    int numCheck = 0;
                    for (ItemStockEntry ite : sellable) {
                        numCheck += ite.amount;
                        PriceListItem tprice = BetterShop.pricelist.getItemPrice(JItemDB.findItem(ite.name));
                        price.buy += tprice.buy > 0 ? tprice.buy : 0;
                        price.sell += tprice.sell > 0 ? tprice.sell : 0;
                        if (name.length() > 1) {
                            name += ", " + ite.name;
                        } else {
                            name += ite.name;
                        }
                    }
                    name += ")";
                    BSutils.sendMessage(player, String.format(
                            BetterShop.config.getString(numCheck == 1 ? "pricecheck" : "multipricecheck").
                            replace("<buyprice>", "%1$s").
                            replace("<sellprice>", "%2$s").
                            replace("<item>", "%3$s").
                            replace("<curr>", "%4$s").
                            replace("<buycur>", "%5$s").
                            replace("<sellcur>", "%6$s").
                            replace("<avail>", "%7$s").
                            replace("<amt>", "%8$s"),
                            (price.IsLegal() || canBuyIllegal) && price.buy >= 0 ? price.buy : "No",
                            price.sell >= 0 ? price.sell : "No",
                            name,
                            BetterShop.config.currency(),
                            (price.IsLegal() || canBuyIllegal) && price.buy >= 0
                            ? BSutils.formatCurrency(price.buy) : "No",
                            price.sell >= 0 ? BSutils.formatCurrency(price.sell) : "No",
                            "?",
                            numCheck));
                }
                return true;
            } else {
                JItem lookup[] = JItemDB.findItems(s[0]);
                if (lookup == null || lookup.length == 0 || lookup[0] == null) {
                    lookup = JItemDB.getItemsByCategory(s[0]);
                    if (lookup == null || lookup.length == 0 || lookup[0] == null) {
                        BSutils.sendMessage(player, BetterShop.config.getString("unkitem").
                                replace("<item>", s[0]));
                        return true;
                    }
                }

                int inStore = 0,
                        numCheck = s.length > 1 && !s[1].equalsIgnoreCase("all") ? CheckInput.GetInt(s[1], 1) : 1;
                for (JItem i : lookup) {
                    PriceListItem price = BetterShop.pricelist.getItemPrice(i);
                    if (price != null) {
                        ++inStore;
                        BSutils.sendMessage(player, String.format(
                                BetterShop.config.getString(numCheck == 1 ? "pricecheck" : "multipricecheck").
                                replace("<buyprice>", "%1$s").
                                replace("<sellprice>", "%2$s").
                                replace("<item>", "%3$s").
                                replace("<curr>", "%4$s").
                                replace("<buycur>", "%5$s").
                                replace("<sellcur>", "%6$s").
                                replace("<avail>", "%7$s").
                                replace("<amt>", "%8$s"),
                                (price.IsLegal() || canBuyIllegal) && price.buy >= 0 ? numCheck * price.buy : "No",
                                price.sell >= 0 ? numCheck * price.sell : "No",
                                i.coloredName(),
                                BetterShop.config.currency(),
                                (price.IsLegal() || canBuyIllegal) && price.buy >= 0
                                ? BSutils.formatCurrency(numCheck * price.buy) : "No",
                                price.sell >= 0 ? BSutils.formatCurrency(numCheck * price.sell) : "No",
                                BetterShop.stock == null || BetterShop.stock.getItemAmount(i) < 0 ? "INF" : BetterShop.stock.getItemAmount(i),
                                numCheck));

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

            }
        } catch (Exception ex) {
            BetterShop.Log(Level.SEVERE, ex);
        }
        BSutils.sendMessage(player, ChatColor.RED + "An Error Occurred while looking up an item.. attemping to reload..");
        if (load(null)) {
            // ask to try command again.. don't want accidental infinite recursion & don't want to plan for recursion right now
            BSutils.sendMessage(player, "Success! Please try again.. ");
        } else {
            BSutils.sendMessage(player, ChatColor.RED + "Failed! Please let an OP know of this error");
        }
        return true;
    }

    public boolean list(CommandSender player, String[] s) {
        if (!BSutils.hasPermission(player, BSutils.BetterShopPermission.USER_LIST, true)) {
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

        if (BetterShop.pricelist == null) {
            BSutils.sendMessage(player, ChatColor.RED + "Pricelist Error: Notify Server Admin");
        } else {
            for (String line : BetterShop.pricelist.GetShopListPage(pagenum, player instanceof Player,
                    BetterShop.config.allowbuyillegal || BSutils.hasPermission(player, BSutils.BetterShopPermission.ADMIN_ILLEGAL, false))) {
                BSutils.sendMessage(player, line.replace("<curr>", BetterShop.config.currency()));
            }
        }

        return true;
    }

    public boolean listitems(CommandSender player, String[] s) {
        if (!BSutils.hasPermission(player, BSutils.BetterShopPermission.USER_LIST, true)) {
            return true;
        } else if (s != null || s.length > 0) {
            //return false;
        }
        try {
            LinkedList<String> items = BetterShop.pricelist.GetItemList(
                    BetterShop.config.allowbuyillegal || BSutils.hasPermission(player, BSutils.BetterShopPermission.ADMIN_ILLEGAL, false));
            StringBuilder output = new StringBuilder("\u00A72");
            if (items != null && items.size() > 0) {
                for (int i = 0; i < items.size(); ++i) {
                    output.append(items.get(i));
                    if (i + 1 < items.size()) {
                        output.append("\u00A72, ");
                    }
                }
            }
            BSutils.sendMessage(player, output.toString());
            return true;
        } catch (Exception ex) {
            BetterShop.Log(Level.SEVERE, ex);
        }

        BSutils.sendMessage(player, ChatColor.RED + "An Error Occurred while looking up an item.. attemping to reload..");
        if (load(null)) {
            // ask to try command again.. don't want accidental infinite recursion & don't want to plan for recursion right now
            BSutils.sendMessage(player, "Success! Please try again.. ");
        } else {
            BSutils.sendMessage(player, ChatColor.RED + "Failed! Please let an OP know of this error");
        }
        return true;
    }

    public boolean load(CommandSender player) {
        if (player != null && !BSutils.hasPermission(player, BSutils.BetterShopPermission.ADMIN_LOAD, true)) {
            return true;
        }
        boolean ok = true;

        if (BetterShop.config.signShopEnabled && BetterShop.signShop.delaySave != null) {
            if (BetterShop.signShop.save()) {
                if (player != null) {
                    BSutils.sendMessage(player, BetterShop.signShop.signs.size() + " signs saved.");
                }
            } else {
                if (player != null) {
                    BSutils.sendMessage(player, ChatColor.RED + "shop signs db save error");
                }
            }
        }
        if (BetterShop.config.signShopEnabled && BetterShop.config.tntSignDestroyProtection) {
            BetterShop.signShop.stopProtecting();
        }
        if (JItemDB.load(BSConfig.itemDBFile)) {
            if (player != null) {
                BSutils.sendMessage(player, JItemDB.size() + " items loaded.");
            }
        } else {
            BetterShop.Log(Level.SEVERE, "Cannot Load Items db!", false);
            if (player != null) {
                BSutils.sendMessage(player, ChatColor.RED + "Item Database Load Error.");
            }
            // itemDB load error is pretty serious
            //return player != null; // not anymore :)
        }
        if (!BetterShop.config.load()) {
            if (player != null) {
                BSutils.sendMessage(player, ChatColor.RED + "Config loading error.");
            }
        } else {
            BSutils.sendMessage(player, "Config.yml loaded.");
            ok = false;
        }
        if (BetterShop.pricelist.load()) {
            BSutils.sendMessage(player, "Price Database " + BetterShop.pricelist.pricelistName() + " loaded.");
        } else {
            if (player != null) {
                BSutils.sendMessage(player, ChatColor.RED + "Price Database Load Error.");
                ok = false;
            }
        }
        if (BetterShop.transactions.load()) {
            if (BetterShop.config.logTotalTransactions || BetterShop.config.logUserTransactions) {
                BSutils.sendMessage(player, "Transactions Log Database loaded");
            }
        } else if (BetterShop.config.logTotalTransactions || BetterShop.config.logUserTransactions) {
            BSutils.sendMessage(player, ChatColor.RED + "Price Database Load Error.");
            ok = false;
        }
        if (BetterShop.config.useItemStock) {
            if (BetterShop.stock == null) {
                BetterShop.stock = new BSItemStock();
            }
            if (BetterShop.stock.load()) {
                BSutils.sendMessage(player, "Stock Database loaded");
            } else {
                BSutils.sendMessage(player, ChatColor.RED + "Stock Database Load Error.");
                ok = false;
            }
        }

        if (BetterShop.config.signShopEnabled && BetterShop.signShop.load()) {
            if (player != null) {
                BSutils.sendMessage(player, BetterShop.signShop.signs.size() + " signs loaded.");
            }
        } else {
            if (player != null) {
                BSutils.sendMessage(player, ChatColor.RED + "shop signs db load error");
            }
        }
        if (BetterShop.config.signShopEnabled && BetterShop.config.tntSignDestroyProtection) {
            BetterShop.signShop.startProtecting();
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
        if (!BSutils.hasPermission(player, BSutils.BetterShopPermission.ADMIN_ADD, true)) {
            return true;
        }

        JItem toAdd = JItemDB.findItem(s[0]);
        if (toAdd == null) {
            BSutils.sendMessage(player,
                    BetterShop.config.getString("unkitem").replace("<item>", s[0]));
            return false;
        }

        if (CheckInput.IsDouble(s[1]) && CheckInput.IsDouble(s[2])) {
            double buy = CheckInput.GetDouble(s[1], -1),
                    sel = CheckInput.GetDouble(s[2], -1);
            if (buy > PriceList.MAX_PRICE
                    || sel > PriceList.MAX_PRICE) {
                BSutils.sendMessage(player, "Price set too high. Max = " + BSutils.formatCurrency(PriceList.MAX_PRICE));
                return true;
            } else if (toAdd.isKit() && sel >= 0) {
                BSutils.sendMessage(player, "Note: Kits cannot be sold");
                s[2] = "-1";
            } else if (toAdd.isEntity() && sel >= 0) {
                BSutils.sendMessage(player, "Note: Entities cannot be sold");
                s[2] = "-1";
            }
            try {
                boolean isChanged = BetterShop.pricelist.ItemExists(toAdd);
                if (BetterShop.pricelist.setPrice(toAdd, buy, sel)) {
                    PriceListItem nPrice = BetterShop.pricelist.getItemPrice(toAdd);
                    double by = nPrice == null ? -2 : nPrice.buy,
                            sl = nPrice == null ? -2 : nPrice.sell;

                    BSutils.sendMessage(player, BetterShop.config.getString(isChanged ? "chgmsg" : "addmsg").
                            replace("<item>", toAdd.coloredName()).
                            replace("<buyprice>", BetterShop.config.intCurrency() ? String.format("%d", (int) Math.round(by)) : String.format("%.2f", by)).
                            replace("<sellprice>", BetterShop.config.intCurrency() ? String.format("%d", (int) Math.round(sl)) : String.format("%.2f", sl)).
                            replace("<curr>", BetterShop.config.currency()).
                            replace("<buycur>", BSutils.formatCurrency(by)).
                            replace("<sellcur>", BSutils.formatCurrency(sl)),
                            BetterShop.config.publicmarket);

                    if (!isChanged && BetterShop.config.useItemStock && BetterShop.stock != null) {
                        BetterShop.stock.setItemAmount(toAdd, BetterShop.config.startStock);
                    }
                    return true;
                }
            } catch (Exception ex) {
                BetterShop.Log(Level.SEVERE, ex);
            }
            BSutils.sendMessage(player, ChatColor.RED + "An Error Occurred While Adding.");
        } else {
            BSutils.sendMessage(player, BetterShop.config.getString("paramerror"));
            return false;
        }

        return true;
    }

    public boolean remove(CommandSender player, String[] s) {
        if ((!BSutils.hasPermission(player, BSutils.BetterShopPermission.ADMIN_REMOVE, true))) {
            return true;
        } else if (s.length != 1) {
            return false;
        }
        JItem toRem = JItemDB.findItem(s[0]);
        if (toRem != null) {
            try {
                BetterShop.pricelist.remove(toRem);
                if (BetterShop.stock != null) {
                    BetterShop.stock.remove(toRem);
                }
                BSutils.sendMessage(player, String.format(BetterShop.config.getString("removemsg").
                        replace("<item>", "%1$s"), toRem.coloredName()), BetterShop.config.publicmarket);

                return true;
            } catch (Exception ex) {
                BetterShop.Log(Level.SEVERE, ex);
            }
            BSutils.sendMessage(player, ChatColor.RED + "Error removing item");
        } else {
            BSutils.sendMessage(player,
                    BetterShop.config.getString("unkitem").replace("<item>", s[0]));
        }
        return true;

    }

    public boolean buy(CommandSender player, String[] s) {
        if (!BSutils.hasPermission(player, BSutils.BetterShopPermission.USER_BUY, true)) {
            return true;
        } else if ((s.length > 2) || (s.length == 0)) {
            BSutils.sendMessage(player, "What?");
            return false;
        } else if (BSutils.anonymousCheck(player)) {
            return true;
        }
        if (s.length == 2 && (s[0].equalsIgnoreCase("all")
                || (CheckInput.IsInt(s[0]) && !CheckInput.IsInt(s[1])))) {
            // swap two indicies
            String t = s[0];
            s[0] = s[1];
            s[1] = t;
        }
        JItem toBuy = JItemDB.findItem(s[0]);
        if (toBuy == null) {
            BSutils.sendMessage(player, String.format(BetterShop.config.getString("unkitem").replace("<item>", "%1$s"), s[0]));
            return true;
        } else if (toBuy.ID() <= 0) {
            BSutils.sendMessage(player, toBuy.coloredName() + " Cannot be Bought");//, toBuy.coloredName());
            return true;
        } else if (!BetterShop.config.allowbuyillegal && !toBuy.IsLegal() && !BSutils.hasPermission(player, BSutils.BetterShopPermission.ADMIN_ILLEGAL, false)) {
            BSutils.sendMessage(player, BetterShop.config.getString("illegalbuy").
                    replace("<item>", toBuy.coloredName()));
            return true;
        }/* else if (toBuy.isKit()) {
        return buyKit(player, s);
        }*/

        // initial check complete: set as last action
        userbuyHistory.put(((Player) player).getDisplayName(), "shopbuy " + Str.argStr(s));

        //UserTransaction bought ;
        if (s.length == 2) {
            if (s[1].equalsIgnoreCase("all")) {
                BSutils.buyAllItem((Player) player, toBuy);
            } else if (!CheckInput.IsInt(s[1])) {
                BSutils.sendMessage(player, s[1] + " is definitely not a number.");
                return true;
            } else {
                BSutils.buyItem((Player) player, toBuy, CheckInput.GetInt(s[1], -1));
            }
        } else {
            BSutils.buyItem((Player) player, toBuy, 1);
        }
        return true;
    }

    public boolean buystack(CommandSender player, String[] s) {
        if (!BSutils.hasPermission(player, BSutils.BetterShopPermission.USER_BUY, true)) {
            return true;
        } else if (s.length == 0) {
            BSutils.sendMessage(player, "What?");
            return false;
        } else if (BSutils.anonymousCheck(player)) {
            return true;
        }
        if (s.length == 2 && CheckInput.IsInt(s[1])) {

            JItem toBuy = JItemDB.findItem(s[0]);
            if (toBuy == null) {
                BSutils.sendMessage(player, String.format(BetterShop.config.getString("unkitem").
                        replace("<item>", "%1$s"), s[0]));
                return true;
            } else if (!BetterShop.config.allowbuyillegal && !toBuy.IsLegal() && !BSutils.hasPermission(player, BSutils.BetterShopPermission.ADMIN_ILLEGAL, false)) {
                BSutils.sendMessage(player, String.format(BetterShop.config.getString("illegalbuy").
                        replace("<item>", "%1$s"), toBuy.coloredName()));
                return true;
            }
            // buy max. stackable
            buy(player, new String[]{toBuy.IdDatStr(), String.valueOf((BetterShop.config.usemaxstack ? toBuy.getMaxStackSize() : 64) * CheckInput.GetInt(s[1], 1))});
        } else {
            for (String is : s) {
                JItem toBuy = JItemDB.findItem(is);
                if (toBuy == null) {
                    BSutils.sendMessage(player, String.format(BetterShop.config.getString("unkitem").
                            replace("<item>", "%1$s"), is));
                    return true;
                } else if (!BetterShop.config.allowbuyillegal && !toBuy.IsLegal() && !BSutils.hasPermission(player, BSutils.BetterShopPermission.ADMIN_ILLEGAL, false)) {
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

    public boolean sellstack(CommandSender player, String[] s) {
        if (!BSutils.hasPermission(player, BSutils.BetterShopPermission.USER_SELL, true)) {
            return true;
        } else if (s.length == 0) {
            BSutils.sendMessage(player, "What?");
            return false;
        } else if (BSutils.anonymousCheck(player)) {
            return true;
        }
        if (s.length == 2 && CheckInput.IsInt(s[1])) {

            JItem toSell = JItemDB.findItem(s[0]);
            if (toSell == null) {
                BSutils.sendMessage(player, String.format(BetterShop.config.getString("unkitem").
                        replace("<item>", "%1$s"), s[0]));
                return true;
            } else if (!BetterShop.config.allowbuyillegal && !toSell.IsLegal()
                    && !BSutils.hasPermission(player, BSutils.BetterShopPermission.ADMIN_ILLEGAL, false)) {
                BSutils.sendMessage(player, String.format(BetterShop.config.getString("illegalbuy").
                        replace("<item>", "%1$s"), toSell.coloredName()));
                return true;
            }
            // sell max. stackable
            sell(player, new String[]{toSell.IdDatStr(),
                        String.valueOf((BetterShop.config.usemaxstack ? toSell.getMaxStackSize() : 64) * CheckInput.GetInt(s[1], 1))});
        } else {
            for (String is : s) {
                JItem toSell = JItemDB.findItem(is);
                if (toSell == null) {
                    BSutils.sendMessage(player, String.format(BetterShop.config.getString("unkitem").
                            replace("<item>", "%1$s"), is));
                    return true;
                } else if (!BetterShop.config.allowbuyillegal && !toSell.IsLegal()
                        && !BSutils.hasPermission(player, BSutils.BetterShopPermission.ADMIN_ILLEGAL, false)) {
                    BSutils.sendMessage(player, String.format(BetterShop.config.getString("illegalbuy").
                            replace("<item>", "%1$s"), toSell.coloredName()));
                    return true;
                }
                // sell max. stackable
                sell(player, new String[]{toSell.IdDatStr(), String.valueOf(
                            BetterShop.config.usemaxstack ? toSell.getMaxStackSize() : 64)});
            }
        }// overwrite history that selll wrote
        usersellHistory.put(((Player) player).getDisplayName(), "shopsellstack " + Str.argStr(s));
        return true;
    }

    public boolean sell(CommandSender player, String[] s) {
        if (!BSutils.hasPermission(player, BSutils.BetterShopPermission.USER_SELL, true) || BSutils.anonymousCheck(player)) {
            return true;
        } // "sell all", "sell all [item]" moved to own method ("sell [item] all" kept here)
        else if (s.length == 1 && s[0].equalsIgnoreCase("all")) {
            return sellall(player, null);
        } else if (s.length == 2) {
            if (s[0].equalsIgnoreCase("all")) {
                return sellall(player, new String[]{s[1]});
            } else if (s[1].equalsIgnoreCase("all")) {
                return sellall(player, new String[]{s[0]});
            } else if (CheckInput.IsInt(s[0]) && !CheckInput.IsInt(s[1])) {
                // swap two indicies
                String t = s[0];
                s[0] = s[1];
                s[1] = t;
            }
        } else if (s.length == 0 || s.length > 2) {
            return false;
        }// initial check complete: set as last action
        usersellHistory.put(((Player) player).getDisplayName(), "shopsell " + Str.argStr(s));
        // expected syntax: item [amount]

        JItem toSell = JItemDB.findItem(s[0]);
        if (toSell == null) {
            BSutils.sendMessage(player, BetterShop.config.getString("unkitem").
                    replace("<item>", s[0]));
            return false;
        } else if (toSell.ID() == 0) {
            BSutils.sendMessage(player, toSell.coloredName() + " Cannot be Sold");//, toSell.coloredName());
            return true;
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
                    BSutils.sendMessage(player, ChatColor.RED + "Failed! Please let an OP know of this error");
                }
            } else {
                BSutils.sendMessage(player, BetterShop.config.getString("donotwant").
                        replace("<item>", toSell.coloredName()));
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
            /*if (avail == -1) {
            BSutils.sendMessage(player, ChatColor.RED + "Failed to lookup an item stock listing");
            return true;
            } else */ if (avail == 0 && BetterShop.config.noOverStock) {
                BSutils.sendMessage(player, BetterShop.config.getString("maxstock").
                        replace("<item>", toSell.coloredName()));
                return true;
            } else if (avail > 0 && amtSold > avail && BetterShop.config.noOverStock) {
                BSutils.sendMessage(player, BetterShop.config.getString("highstock").
                        replace("<item>", toSell.coloredName()).
                        replace("<amt>", String.valueOf(avail)));
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

        BSutils.credit((Player) player, total);

        if (BetterShop.stock != null && BetterShop.config.useItemStock) {
            try {
                BetterShop.stock.changeItemAmount(toSell, amtSold);
            } catch (Exception ex) {
                BetterShop.Log(Level.SEVERE, ex);
            }
        }

        BSutils.sendFormttedMessage((Player) player, "sellmsg", toSell.coloredName(), amtSold, total);

        try {
            BetterShop.transactions.addRecord(new UserTransaction(toSell, true, amtSold, total / amtSold, ((Player) player).getDisplayName()));
        } catch (Exception ex) {
            BetterShop.Log(Level.SEVERE, ex);
        }

        return true;
    }

    public boolean sellall(CommandSender player, String[] s) {
        JItem toSell[] = null;
        boolean onlyInv = false;
        if (s != null) {
            if (!BSutils.hasPermission(player, BSutils.BetterShopPermission.USER_SELL, true)
                    || BSutils.anonymousCheck(player)) {
                return true;
            } else if (s.length > 0) {
                // expected syntax: [inv] [item [item [item [...]]]]
                int st = 0;
                if (s[0].equalsIgnoreCase("inv")) {
                    onlyInv = true;
                    st = 1;
                }
                toSell = new JItem[s.length - st];
                for (int i = st; i < s.length; ++i) {
                    toSell[i - st] = JItemDB.findItem(s[i]);
                    if (toSell[i - st] == null) {
                        JItem cts[] = JItemDB.getItemsByCategory(s[i]);
                        if (cts != null && cts.length > 0) {
                            toSell = ArrayManip.arrayConcat(toSell, cts);
                            //--i;
                        } else {
                            BSutils.sendMessage(player, String.format(
                                    BetterShop.config.getString("unkitem").
                                    replace("<item>", "%1$s"), s[i]));
                            return false;
                        }
                    } else if (toSell[i - st].ID() == 0) {
                        BSutils.sendMessage(player, toSell[i - st].coloredName() + " Cannot be Sold"); // toSell[i - st].coloredName()
                        return true; //toSell[i - st] = null; // 
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

        ArrayList<ItemStockEntry> playerInv = BSutils.getCanSell((Player) player, onlyInv, toSell);
        if (playerInv == null || playerInv.isEmpty()) {
            return true;
        }

        // now scan through & remove the items
        BSutils.sellItems((Player) player, onlyInv, playerInv);

        return true;
    }

    public boolean listkits(CommandSender player, String[] s) {
        if (!BSutils.hasPermission(player, BSutils.BetterShopPermission.USER_LIST, true)) {
            return true;
        }
        try {
            BSutils.sendMessage(player, "Kit listing:");
            String kitNames = "";
            for (JItem i : BetterShop.pricelist.getItems(BSutils.hasPermission(player, BSutils.BetterShopPermission.ADMIN_ILLEGAL))) {
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
            BSutils.sendMessage(player, "Error looking up an item.. Attempting DB reload..");
            if (load(null)) {
                // ask to try command again.. don't want accidental infinite recursion & don't want to plan for recursion right now
                BSutils.sendMessage(player, "Success! Please try again.. ");
            } else {
                BSutils.sendMessage(player, ChatColor.RED + "Failed! Please let an OP know of this error");
            }
            return true;
        }
    }

    public boolean listAlias(CommandSender player, String[] s) {
        if ((!BSutils.hasPermission(player, BSutils.BetterShopPermission.USER_HELP, true))) {
            return true;
        } else if (s.length != 1) {
            return false;
        }
        JItem it = JItemDB.findItem(s[0]);
        if (it == null) {
            BSutils.sendMessage(player, BetterShop.config.getString("unkitem").
                    replace("<item>", s[0]));
        } else {
            StringBuilder aliases = new StringBuilder();
            for (String a : it.Aliases()) {
                aliases.append(a).append(", ");
            }
            if (aliases.length() > 1) {//.indexOf(",") != -1) {
                aliases.delete(aliases.length() - 2, aliases.length());
            }

            BSutils.sendMessage(player,
                    MinecraftChatStr.strWordWrap(
                    BetterShop.config.getString("listalias").
                    replace("<item>", it.coloredName()).replace("<alias>", aliases.toString())));
        }
        return true;
    }

    public boolean importDB(CommandSender player, String[] s) {
        if ((!BSutils.hasPermission(player, BSutils.BetterShopPermission.ADMIN_BACKUP, true))) {
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
            BSutils.sendMessage(player, ChatColor.RED + " An Error Occured while importing database");
        }
        return true;
    }

    public boolean restoreDB(CommandSender player, String[] s) {
        if ((!BSutils.hasPermission(player, BSutils.BetterShopPermission.ADMIN_BACKUP, true))) {
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
            BSutils.sendMessage(player, ChatColor.RED + " An Error Occured while importing database");
        }
        return true;
    }
}
