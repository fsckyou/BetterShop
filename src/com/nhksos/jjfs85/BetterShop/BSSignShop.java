/**
 * Programmer: Jacob Scott
 * Program Name: BSSignShop
 * Description: interface for adding a sign interface to bettershop
 * Date: Apr 6, 2011
 */
package com.nhksos.jjfs85.BetterShop;

import com.jascotty2.CSV;
import com.jascotty2.CheckInput;
import com.jascotty2.Item.ChestManip;
import com.jascotty2.Item.Item;
import com.jascotty2.Item.ItemStockEntry;
import com.jascotty2.Item.PriceListItem;
import com.jascotty2.MinecraftChatStr;
import com.jascotty2.Str;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
//import org.bukkit.event.block.BlockCanBuildEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;

/**
 * @author jacob
 */
public class BSSignShop extends PlayerListener {

    BetterShop plugin = null;
    HashMap<Location, Item> signs = new HashMap<Location, Item>();
    public SignDestroyListener signDestroy = new SignDestroyListener();
    protected SignSaver delaySave = null;
    //public StopBuild buildStopper = new StopBuild();

    public BSSignShop(BetterShop shop) {
        plugin = shop;
    } // end default constructor

    @Override
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (BetterShop.config.signShopEnabled
                && event.getClickedBlock() != null
                && (event.getClickedBlock().getType() == Material.WALL_SIGN
                || event.getClickedBlock().getType() == Material.SIGN_POST)) {
            Sign clickedSign = (Sign) event.getClickedBlock().getState();
            if (MinecraftChatStr.uncoloredStr(clickedSign.getLine(0)).equalsIgnoreCase("[BetterShop]")) {
                try {

                    boolean canEdit = BSutils.hasPermission(event.getPlayer(),
                            BSutils.BetterShopPermission.ADMIN_MAKESIGN);
                    String action = clickedSign.getLine(1).trim().replaceAll("  ", " ");
                    if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                        event.setCancelled(!canEdit);
                        // if sign is registered, update prices
                        if (signs.containsKey(event.getClickedBlock().getLocation())) {
                            try {
                                Item i = signs.get(event.getClickedBlock().getLocation());

                                String amt = action.contains(" ") ? action.substring(action.lastIndexOf(" ")).trim() : "1";
                                if (action.endsWith("all")) {
                                    amt = "all";
                                }
                                if (!(amt.equalsIgnoreCase("all") || CheckInput.IsInt(amt))) {
                                    BSutils.sendMessage(event.getPlayer(), "invalid amount");
                                    return;
                                }
                                boolean canBuyIllegal = BetterShop.config.allowbuyillegal || BSutils.hasPermission(event.getPlayer(), BSutils.BetterShopPermission.ADMIN_ILLEGAL, false);
                                boolean isBuy = action.toLowerCase().startsWith("buy");

                                PriceListItem price = null;
                                if (i != null) {
                                    price = BetterShop.pricelist.getItemPrice(i);
                                    if (price == null) {
                                        BSutils.sendMessage(event.getPlayer(), i.name + " cannot be bought or sold");
                                        return;
                                    }
                                    price.name = i.coloredName();
                                } else {
                                    price = new PriceListItem();
                                    price.buy = price.sell = 0;
                                    price.name = "(";
                                }

                                int numCheck = 1;
                                if (amt.equalsIgnoreCase("all")) {
                                    if (isBuy) {
                                        // numCheck = ChestManip.canHold(event.getPlayer().getInventory().getContents(),
                                        //         i, BetterShop.config.usemaxstack);
                                        //ArrayList<ItemStockEntry> sold = new ArrayList<ItemStockEntry>();
                                        ArrayList<ItemStockEntry> inv = BSutils.getTotalInventory(event.getPlayer(), false);
                                        int ipos = inv.indexOf(new ItemStockEntry(i));
                                        if (ipos >= 0) {
                                            numCheck += (int) inv.get(ipos).amount;
                                        } else {
                                            numCheck = 0;
                                        }
                                        price.buy *= numCheck;
                                        price.sell *= numCheck;
                                    } else {
                                        if (i != null) {
                                            numCheck = ChestManip.itemAmount(event.getPlayer().getInventory().getContents(), i);
                                        } else {
                                            ArrayList<ItemStockEntry> sellable = BSutils.getCanSell(event.getPlayer(), false, null);
                                            int tt = 0;
                                            for (ItemStockEntry ite : sellable) {
                                                tt += ite.amount;
                                                PriceListItem tprice = BetterShop.pricelist.getItemPrice(Item.findItem(ite.name));
                                                price.buy += tprice.buy > 0 ? tprice.buy * ite.amount : 0;
                                                price.sell += tprice.sell > 0 ? tprice.sell * ite.amount : 0;
                                                if (price.name.length() > 1) {
                                                    price.name += ", " + ite.name;
                                                } else {
                                                    price.name += ite.name;
                                                }
                                            }
                                            price.name += ")";
                                            numCheck = tt;
                                        }
                                    }
                                } else {
                                    numCheck = CheckInput.GetInt(amt, 1);
                                }

                                BSutils.sendMessage(event.getPlayer(), String.format(
                                        BetterShop.config.getString(isBuy ? "multipricecheckbuy" : "multipricechecksell"). //numCheck == 1 ? "pricecheck" : "multipricecheck"
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
                                        price.name,
                                        BetterShop.config.currency(),
                                        (price.IsLegal() || canBuyIllegal) && price.buy >= 0
                                        ? BSutils.formatCurrency(price.buy) : "No",
                                        price.sell >= 0 ? BSutils.formatCurrency(price.sell) : "No",
                                        BetterShop.stock == null || (i != null && BetterShop.stock.getItemAmount(i) < 0) ? "INF" : i == null ? "?" : BetterShop.stock.getItemAmount(i),
                                        numCheck));

                            } catch (Exception ex) {
                                BetterShop.Log(Level.SEVERE, ex);
                                BSutils.sendMessage(event.getPlayer(), "Failed to lookup the price");
                            }
                        } else if (canEdit) {// else, (if has permission) add sign
                            if (Str.count(action, " ") > 1
                                    || (action.contains(" ") && !Str.startIsIn(action, new String[]{
                                        "buy ", "buyall ", "buystack ",
                                        "sell ", "sellall ", "sellstack "}))
                                    || (!action.contains(" ") && !Str.startIsIn(action, new String[]{
                                        "buy", "buyall", "buystack",
                                        "sell", "sellall", "sellstack"}))) {
                                BSutils.sendMessage(event.getPlayer(), "invalid action");
                                return;
                            }
                            String amt = action.contains(" ") ? action.substring(action.lastIndexOf(" ")).trim() : "1";
                            if (action.endsWith("all")) {
                                amt = "all";
                            }
                            if (!(amt.equalsIgnoreCase("all") || CheckInput.GetInt(amt, -1) > 0)) {
                                BSutils.sendMessage(event.getPlayer(), "invalid amount");
                                return;
                            }
                            Item toAdd[] = new Item[]{Item.findItem(clickedSign.getLine(2))};
                            if (toAdd != null && toAdd[0] == null && clickedSign.getLine(2).length() > 0) {
                                toAdd = Item.findItems(clickedSign.getLine(2));
                            }
                            if (toAdd == null) {
                                BSutils.sendMessage(event.getPlayer(), "error");
                                return;
                            } else if (toAdd.length == 0) {
                                BSutils.sendMessage(event.getPlayer(), "no matching items");
                                return;
                            } else if (toAdd.length > 1) {
                                BSutils.sendMessage(event.getPlayer(), "more than one matching items");
                                return;
                            } else if (toAdd[0] != null && toAdd[0].ID() <= 0) {
                                BSutils.sendMessage(event.getPlayer(), toAdd[0].name + " cannot be bought or sold");
                                return;
                            } else if (toAdd[0] != null && action.startsWith("sell")) {
                                if (toAdd[0].isEntity()) {
                                    BSutils.sendMessage(event.getPlayer(), "entities cannot be sold");
                                    return;
                                } else if (toAdd[0].isKit()) {
                                    BSutils.sendMessage(event.getPlayer(), "kits cannot be sold");
                                    return;
                                } else if (!BetterShop.pricelist.isForSale(toAdd[0])) {
                                    BSutils.sendMessage(event.getPlayer(), "item cannot be sold");
                                    return;
                                }
                            } else if (action.startsWith("buy")) {
                                if (toAdd[0] == null) { //  && !amt.equalsIgnoreCase("all")
                                    BSutils.sendMessage(event.getPlayer(), "must provide an item to buy");
                                    return;
                                } else if (BetterShop.pricelist.getBuyPrice(toAdd[0]) < 0) {
                                    BSutils.sendMessage(event.getPlayer(), "item cannot be bought");
                                    return;
                                }
                            }
                            // all checks passed: create sign
                            signs.put(event.getClickedBlock().getLocation().clone(), toAdd[0]);
                            clickedSign.setLine(0, BetterShop.config.activeSignColor + MinecraftChatStr.uncoloredStr(clickedSign.getLine(0)));
                            clickedSign.update();
                            BSutils.sendMessage(event.getPlayer(), "new sign created");

                            if (delaySave != null) {
                                delaySave.cancel();
                            }
                            delaySave = new SignSaver();
                            delaySave.start(BetterShop.config.signDBsaveWait);

                        } else {
                            // let them know they can't make a sign
                            BSutils.hasPermission(event.getPlayer(), BSutils.BetterShopPermission.ADMIN_MAKESIGN, true);
                        }
                    } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                        if (signs.containsKey(event.getClickedBlock().getLocation())) {
                            event.setCancelled(true);
                            //buildStopper.stopPlace(event.getClickedBlock().getRelative(event.getBlockFace()).getLocation());
                            try {
                                Item i = signs.get(event.getClickedBlock().getLocation());
                                if (!BetterShop.config.allowbuyillegal && !i.IsLegal()
                                        && !BSutils.hasPermission(event.getPlayer(), BSutils.BetterShopPermission.ADMIN_ILLEGAL, true)) {
                                    return;
                                }

                                String amt = action.contains(" ") ? action.substring(action.lastIndexOf(" ")).trim() : "1";
                                if (action.endsWith("all")) {
                                    amt = "all";
                                } else if (action.contains("stack")) {
                                    amt = String.valueOf(i.getMaxStackSize());
                                }
                                if (!(amt.equalsIgnoreCase("all") || CheckInput.GetInt(amt, -1) > 0)) {
                                    BSutils.sendMessage(event.getPlayer(), "invalid amount");
                                    return;
                                }
                                boolean isBuy = action.toLowerCase().startsWith("buy");
                                if ((!isBuy && !BSutils.hasPermission(event.getPlayer(), BSutils.BetterShopPermission.USER_SELL, true))
                                        || isBuy && !BSutils.hasPermission(event.getPlayer(), BSutils.BetterShopPermission.USER_BUY, true)) {
                                    return;
                                }

                                if (i != null) {
                                    if (isBuy) {
                                        if (BetterShop.pricelist.getBuyPrice(i) < 0) {
                                            BSutils.sendMessage(event.getPlayer(), "item cannot be bought");
                                            return;
                                        }
                                    } else {
                                        if (BetterShop.pricelist.getSellPrice(i) < 0) {
                                            BSutils.sendMessage(event.getPlayer(), "item cannot be sold");
                                            return;
                                        }
                                    }
                                }

                                if (amt.equalsIgnoreCase("all")) {
                                    if (isBuy) {
                                        BSutils.buyAllItem(event.getPlayer(), i);
                                    } else {
                                        BSutils.sellItems(event.getPlayer(), false, null, -1);
                                    }
                                } else {
                                    if (isBuy) {
                                        BSutils.buyItem(event.getPlayer(), i, CheckInput.GetInt(amt, 0));
                                    } else {
                                        BSutils.sellItems(event.getPlayer(), false, i, CheckInput.GetInt(amt, 0));
                                    }
                                }

                            } catch (Exception ex) {
                                BetterShop.Log(Level.SEVERE, ex);
                            }
                            //(new UpdateInv(event.getPlayer())).start(2000);
                            // may be depricated, but only thing i could get to work :(
                            event.getPlayer().updateInventory();
                        } else {
                            BSutils.sendMessage(event.getPlayer(), "this is not a legal sign");
                        }
                    }
                } catch (Exception e) {
                    BetterShop.Log(Level.SEVERE, e);
                    event.getPlayer().sendMessage("An Error Occured");
                }
            }
        }
    }

    public boolean load() {
        if (BSConfig.signDBFile.exists()) {
            try {
                ArrayList<String[]> signdb = CSV.loadFile(BSConfig.signDBFile);
                for (String[] s : signdb) {
                    if (s.length >= 5 && plugin.getServer().getWorld(s[0]) != null) {
                        signs.put(new Location(plugin.getServer().getWorld(s[0]),
                                CheckInput.GetDouble(s[1], 0),
                                CheckInput.GetDouble(s[2], 0),
                                CheckInput.GetDouble(s[3], 0)), Item.findItem(s[4]));
                    }
                }
                // now scan & double-check these are all signs (and have correct color)
                for (Location l : signs.keySet().toArray(new Location[0])) {
                    if (!(l.getBlock().getState() instanceof Sign)) {
                        signs.remove(l);
                    } else {
                        // check color
                        Sign checkSign = (Sign) l.getBlock().getState();
                        if (!checkSign.getLine(0).startsWith(BetterShop.config.activeSignColor)) {
                            checkSign.setLine(0, BetterShop.config.activeSignColor + MinecraftChatStr.uncoloredStr(checkSign.getLine(0)));
                            checkSign.update();
                        }
                    }
                }
                return true;
            } catch (FileNotFoundException ex) {
                BetterShop.Log(Level.SEVERE, ex, false);
            } catch (IOException ex) {
                BetterShop.Log(Level.SEVERE, ex, false);
            } catch (Exception ex) {
                BetterShop.Log(Level.SEVERE, ex);
            }
            return false;
        }
        return true;
    }

    public boolean save() {
        try {
            if (delaySave != null) {
                delaySave.cancel();
                delaySave = null;
            }
        } catch (Exception e) {
            BetterShop.Log(Level.SEVERE, e);
        }
        try {
            ArrayList<String> file = new ArrayList<String>();
            for (Location l : signs.keySet()) {
                file.add(l.getWorld().getName() + ","
                        + l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ() + ","
                        + (signs.get(l) != null ? signs.get(l).IdDatStr() : " "));
            }
            return CSV.saveFile(BSConfig.signDBFile, file);
        } catch (Exception e) {
            BetterShop.Log(Level.SEVERE, e);
        }
        return false;
    }

    static BlockFace checkFaces[] = new BlockFace[]{BlockFace.SELF, BlockFace.UP,
        BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST, BlockFace.EAST};

    public class SignDestroyListener extends BlockListener {

        @Override
        public void onBlockBreak(BlockBreakEvent event) {
            boolean canBreak = BSutils.hasPermission(event.getPlayer(), BSutils.BetterShopPermission.ADMIN_MAKESIGN, false);
            /*
            if (event.getBlock().getState() instanceof Sign
                    && signs.containsKey(event.getBlock().getLocation())) {
                signs.remove(event.getBlock().getLocation());
            }*/
            
            for (BlockFace b : checkFaces) {
                if (event.getBlock().getRelative(b).getState() instanceof Sign
                        && signs.containsKey(event.getBlock().getRelative(b).getLocation())) {
                    if (!canBreak) {
                        event.setCancelled(true);
                        BSutils.hasPermission(event.getPlayer(), BSutils.BetterShopPermission.ADMIN_MAKESIGN, true);
                        return;
                    }else{
                        signs.remove(event.getBlock().getRelative(b).getLocation());
                    }
                }
            }
        }
    }

    protected class SignSaver extends TimerTask {

        public void start(long wait) {
            (new Timer()).schedule(this, wait);
        }

        @Override
        public void run() {
            save();
        }
    }
        /*
    public class StopBreak extends BlockListener {
]
        Location toStop = null;
        
        public void stopPlace(Location loc) {
        toStop = loc.clone();
        }
        
        @Override
        public void onBlockCanBuild(BlockCanBuildEvent event) {
        if (event.getBlock().getLocation().equals(toStop)) {
        event.setBuildable(false);
        }
        }
    }//*/
    /*
    public class UpdateInv extends TimerTask {
    
    Player toupdate = null;
    
    public UpdateInv(Player p) {
    toupdate = p;
    }
    
    public void start(long wait) {
    (new Timer()).schedule(this, wait);
    }
    
    @Override
    public void run() {
    toupdate.getInventory().setContents(toupdate.getInventory().getContents());
    }
    }//*/
} // end class BSSignShop

