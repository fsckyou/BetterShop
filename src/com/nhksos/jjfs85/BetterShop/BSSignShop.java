/**
 * Programmer: Jacob Scott
 * Program Name: BSSignShop
 * Description: for adding a sign interface to bettershop
 * Date: Apr 6, 2011
 */
package com.nhksos.jjfs85.BetterShop;

import com.jascotty2.io.FileIO;
import com.jascotty2.io.CheckInput;
import com.jascotty2.Item.ChestManip;
import com.jascotty2.Item.JItem;
import com.jascotty2.Item.ItemStockEntry;
import com.jascotty2.Item.JItemDB;
import com.jascotty2.Item.PriceListItem;
import com.jascotty2.bukkit.MinecraftChatStr;
import com.jascotty2.util.Str;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;

/**
 * @author jacob
 */
public class BSSignShop extends PlayerListener {

    BetterShop plugin = null;
    HashMap<Location, JItem> signs = new HashMap<Location, JItem>();
    HashMap<Location, Sign> savedSigns = new HashMap<Location, Sign>();
    HashMap<Location, BlockState> signBlocks = new HashMap<Location, BlockState>();
    HashMap<Player, Long> playerInteractTime = new HashMap<Player, Long>();
    public SignDestroyListener signDestroy = new SignDestroyListener();
    protected SignSaver delaySave = null;
    public long signResWait = 5000;
    protected SignRestore checkSigns = null;
    //public StopBuild buildStopper = new StopBuild();

    public BSSignShop(BetterShop shop) {
        plugin = shop;
    } // end default constructor

    @Override
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.isCancelled()) {
            return;
        }
        //if(event.getAction() == Action.RIGHT_CLICK_BLOCK) event.getPlayer().getWorld().strikeLightning(event.getClickedBlock().getLocation());
        if (BetterShop.config.signShopEnabled
                && event.getClickedBlock() != null
                && (event.getClickedBlock().getType() == Material.WALL_SIGN
                || event.getClickedBlock().getType() == Material.SIGN_POST)) {
            Sign clickedSign = (Sign) event.getClickedBlock().getState();
            if (MinecraftChatStr.uncoloredStr(clickedSign.getLine(0)).equalsIgnoreCase("[BetterShop]")) {
                BetterShop.lastCommand = "Sign: [" + clickedSign.getLine(1) + "][" + clickedSign.getLine(2) + "][" + clickedSign.getLine(3) + "]";
                try {
                    Long lt = playerInteractTime.get(event.getPlayer());
                    if (lt != null && System.currentTimeMillis() - lt < BSConfig.signInteractWait) {
                        event.setCancelled(event.getAction() == Action.RIGHT_CLICK_BLOCK);
                        return;
                    }
                    playerInteractTime.put(event.getPlayer(), System.currentTimeMillis());
                    boolean canEdit = BSutils.hasPermission(event.getPlayer(),
                            BSutils.BetterShopPermission.ADMIN_MAKESIGN);
                    String action = MinecraftChatStr.uncoloredStr(clickedSign.getLine(1)).trim().replace("  ", " ");
                    if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                        //event.setCancelled(!canEdit);
                        // if sign is registered, update prices
                        if (signs.containsKey(event.getClickedBlock().getLocation())) {
                            try {
                                JItem i = signs.get(event.getClickedBlock().getLocation());
                                boolean isInv = false;
                                if (i == null) {
                                    String in = ((Sign) event.getClickedBlock().getState()).getLine(2).toLowerCase().replaceAll(" ", "");
                                    isInv = in.equals("inv");
                                    if (!isInv && in.length() > 0) {
                                        if (in.equals("hand")
                                                || in.equals("inhand")) {
                                            if (event.getPlayer().getItemInHand() == null
                                                    || event.getPlayer().getItemInHand().getAmount() == 0) {
                                                BSutils.sendMessage(event.getPlayer(), "you don't have anything in your hand");
                                                return;
                                            }
                                            i = JItemDB.findItem(event.getPlayer().getItemInHand());
                                        }
                                    }
                                }
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
                                String pname = "";
                                if (i != null) {
                                    price = BetterShop.pricelist.getItemPrice(i);
                                    if (price == null) {
                                        BSutils.sendMessage(event.getPlayer(), i.Name() + " cannot be bought or sold");
                                        return;
                                    }
                                    pname = i.coloredName();
                                } else {
                                    price = new PriceListItem();
                                    price.buy = price.sell = 0;
                                    pname = "(";
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
                                                PriceListItem tprice = BetterShop.pricelist.getItemPrice(JItemDB.findItem(ite.name));
                                                price.buy += tprice.buy > 0 ? tprice.buy * ite.amount : 0;
                                                price.sell += tprice.sell > 0 ? tprice.sell * ite.amount : 0;
                                                if (pname.length() > 1) {
                                                    pname += ", " + ite.name;
                                                } else {
                                                    pname += ite.name;
                                                }
                                            }
                                            pname += ")";
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
                                        pname,
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
                            String in = MinecraftChatStr.uncoloredStr(clickedSign.getLine(2)).replace(" ", "").toLowerCase();
                            JItem toAdd[] = new JItem[]{JItemDB.findItem(in)};
                            if (toAdd != null && toAdd[0] == null && clickedSign.getLine(2).length() > 0) {
                                if (!(in.equals("inv")
                                        || in.equals("hand")
                                        || in.equals("inhand"))) {
                                    toAdd = JItemDB.findItems(clickedSign.getLine(2));
                                } else {
                                    toAdd = new JItem[1];
                                }
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
                                BSutils.sendMessage(event.getPlayer(), toAdd[0].Name() + " cannot be bought or sold");
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
                            } else if (action.startsWith("buy")
                                    && !(in.equals("hand") || in.equals("inhand"))) {
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
                            savedSigns.put(event.getClickedBlock().getLocation().clone(), clickedSign);
                            Block a = getSignAnchor(event.getClickedBlock());
                            if (a != null) {
                                signBlocks.put(a.getLocation(), a.getState());
                            }
                            clickedSign.setLine(0, BetterShop.config.activeSignColor + MinecraftChatStr.uncoloredStr(clickedSign.getLine(0)));

                            if (toAdd[0] != null && toAdd[0].color != null) {
                                if (BetterShop.config.signItemColorBWswap && ChatColor.BLACK.toString().equals(toAdd[0].color)) {
                                    clickedSign.setLine(2, ChatColor.WHITE.toString() + clickedSign.getLine(2));
                                } else if (BetterShop.config.signItemColorBWswap && ChatColor.WHITE.toString().equals(toAdd[0].color)) {
                                    clickedSign.setLine(2, ChatColor.BLACK.toString() + clickedSign.getLine(2));
                                } else {
                                    clickedSign.setLine(2, toAdd[0].color + clickedSign.getLine(2));
                                }
                            }

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
                                JItem i = signs.get(event.getClickedBlock().getLocation());

                                boolean isInv = false;
                                if (i == null) {
                                    String in = ((Sign) event.getClickedBlock().getState()).getLine(2).toLowerCase().replaceAll(" ", "");
                                    isInv = in.equals("inv");
                                    if (!isInv && in.length() > 0) {
                                        if (in.equals("hand")
                                                || in.equals("inhand")) {
                                            if (event.getPlayer().getItemInHand() == null
                                                    || event.getPlayer().getItemInHand().getAmount() == 0) {
                                                BSutils.sendMessage(event.getPlayer(), "you don't have anything in your hand");
                                                return;
                                            }
                                            i = JItemDB.findItem(event.getPlayer().getItemInHand());
                                        }
                                    }
                                }
                                if (i != null && !BetterShop.config.allowbuyillegal && !i.IsLegal()
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
                                        BSutils.sellItems(event.getPlayer(), isInv, i, -1);
                                    }
                                } else {
                                    if (isBuy) {
                                        BSutils.buyItem(event.getPlayer(), i, CheckInput.GetInt(amt, 0));
                                    } else {
                                        BSutils.sellItems(event.getPlayer(), isInv, i, CheckInput.GetInt(amt, 0));
                                    }
                                }

                            } catch (Exception ex) {
                                BetterShop.Log(Level.SEVERE, ex);
                            }
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
                ArrayList<String[]> signdb = FileIO.loadCSVFile(BSConfig.signDBFile);
                for (String[] s : signdb) {
                    if (s.length >= 5 && plugin.getServer().getWorld(s[0]) != null) {
                        signs.put(new Location(plugin.getServer().getWorld(s[0]),
                                CheckInput.GetDouble(s[1], 0),
                                CheckInput.GetDouble(s[2], 0),
                                CheckInput.GetDouble(s[3], 0)), JItemDB.findItem(s[4]));
                    }
                }
                // now scan & double-check these are all signs (and have correct color)
                for (Location l : signs.keySet().toArray(new Location[0])) {
                    if (!(l.getBlock().getState() instanceof Sign)) {
                        signs.remove(l);
                    } else {
                        Sign checkSign = (Sign) l.getBlock().getState();
                        // save sign
                        savedSigns.put(l.clone(), checkSign);//plugin.getServer().getWorld(l.getWorld().getName()).getBlockAt(l));
                        // save block that anchors it
                        if (l.getBlock().getType() == Material.SIGN_POST) {
                            signBlocks.put(l.getBlock().getRelative(BlockFace.DOWN).getLocation(),
                                    l.getBlock().getRelative(BlockFace.DOWN).getState());
                        } else {
                            Block a = getSignAnchor(l.getBlock());
                            if (a != null) {
                                signBlocks.put(a.getLocation(), a.getState());
                            }
                        }

                        // check color
                        boolean up = false;
                        if (!checkSign.getLine(0).startsWith(BetterShop.config.activeSignColor)) {
                            checkSign.setLine(0, BetterShop.config.activeSignColor + MinecraftChatStr.uncoloredStr(checkSign.getLine(0)));
                            up = true;
                        }
                        if (BetterShop.config.signItemColor) {
                            JItem i = signs.get(l);
                            if (i != null && i.color != null && !checkSign.getLine(2).startsWith(i.color)) {
                                if (BetterShop.config.signItemColorBWswap && ChatColor.BLACK.toString().equals(i.color)) {
                                    checkSign.setLine(2, ChatColor.WHITE + MinecraftChatStr.uncoloredStr(checkSign.getLine(2)));
                                } else if (BetterShop.config.signItemColorBWswap && ChatColor.WHITE.toString().equals(i.color)) {
                                    checkSign.setLine(2, ChatColor.BLACK + MinecraftChatStr.uncoloredStr(checkSign.getLine(2)));
                                } else {
                                    checkSign.setLine(2, i.color + MinecraftChatStr.uncoloredStr(checkSign.getLine(2)));
                                }
                                up = true;
                            }
                        }

                        if (up) {
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
            return FileIO.saveFile(BSConfig.signDBFile, file);
        } catch (Exception e) {
            BetterShop.Log(Level.SEVERE, e);
        }
        return false;
    }

    public void startProtecting() {
        if (checkSigns != null) {
            checkSigns.cancel();
        }
        checkSigns = new SignRestore();
        checkSigns.start(signResWait);
    }

    public void stopProtecting() {
        if (checkSigns != null) {
            checkSigns.cancel();
            checkSigns = null;
        }
    }
    static BlockFace checkFaces[] = new BlockFace[]{BlockFace.SELF, BlockFace.UP,
        BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST, BlockFace.EAST};

    public class SignDestroyListener extends BlockListener {

        @Override
        public void onBlockPlace(BlockPlaceEvent event) {
            if (signs.containsKey(event.getBlock().getLocation())) {
                signs.remove(event.getBlock().getLocation());
            }
        }

        @Override
        public void onBlockBreak(BlockBreakEvent event) {
            if (signBlocks.containsKey(event.getBlock().getLocation())
                    || savedSigns.containsKey(event.getBlock().getLocation())) {
                if (BetterShop.config.signDestroyProtection
                        && !BSutils.hasPermission(event.getPlayer(), BSutils.BetterShopPermission.ADMIN_MAKESIGN, true)) {
                    event.setCancelled(true);
                } else {
                    if (event.getBlock().getState() instanceof Sign) {
                        signs.remove(event.getBlock().getLocation());
                        savedSigns.remove(event.getBlock().getLocation());
                        Block b = getSignAnchor(event.getBlock());
                        if (b != null) {
                            signBlocks.remove(b.getLocation());
                        }
                    } else {
                        ArrayList<Block> list = getShopSigns(event.getBlock());
                        for (Block b : list) {
                            signs.remove(b.getLocation());
                            savedSigns.remove(b.getLocation());
                        }
                        signBlocks.remove(event.getBlock().getLocation());
                    }
                }
            }
        }
    }

    public static Block getSignAnchor(Block b) {
        if (b.getState() instanceof Sign) {
            switch (b.getData()) {
                case 2: // w
                    return b.getRelative(BlockFace.WEST);
                case 3: // e
                    return b.getRelative(BlockFace.EAST);
                case 4: // s
                    return b.getRelative(BlockFace.SOUTH);
                case 5: // n
                    return b.getRelative(BlockFace.NORTH);
            }
        }
        return null;
    }

    public ArrayList<Block> getShopSigns(Block b) {
        ArrayList<Block> list = getSigns(b);
        for (int i = 0; i < list.size(); ++i) {
            if (!signs.containsKey(list.get(i).getLocation())) {
                list.remove(i);
                --i;
            }
        }
        return list;
    }

    public static ArrayList<Block> getSigns(Block b) {
        ArrayList<Block> list = new ArrayList<Block>();
        if (b.getState() instanceof Sign) {
            list.add(b);
        } else {
            if (b.getRelative(BlockFace.UP).getType() == Material.SIGN_POST) {
                list.add(b.getRelative(BlockFace.UP));
            }
            if (b.getRelative(BlockFace.NORTH).getType() == Material.WALL_SIGN
                    && b.getRelative(BlockFace.NORTH).getData() == 4) {
                list.add(b.getRelative(BlockFace.NORTH));
            }
            if (b.getRelative(BlockFace.SOUTH).getType() == Material.WALL_SIGN
                    && b.getRelative(BlockFace.SOUTH).getData() == 5) {
                list.add(b.getRelative(BlockFace.SOUTH));
            }

            if (b.getRelative(BlockFace.WEST).getType() == Material.WALL_SIGN
                    && b.getRelative(BlockFace.WEST).getData() == 3) {
                list.add(b.getRelative(BlockFace.WEST));
            }

            if (b.getRelative(BlockFace.EAST).getType() == Material.WALL_SIGN
                    && b.getRelative(BlockFace.EAST).getData() == 2) {
                list.add(b.getRelative(BlockFace.EAST));
            }

            //BlockFace.UP, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST, BlockFace.EAST
        }
        return list;
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

    protected class SignRestore extends TimerTask {

        public void start(long wait) {
            (new Timer()).scheduleAtFixedRate(this, wait, wait);
        }

        @Override
        public void run() {
            for (BlockState b : signBlocks.values()) {
                if (b.getBlock().getLocation().getBlock().getTypeId() != b.getTypeId()) {
                    b.getBlock().getLocation().getBlock().setTypeIdAndData(b.getTypeId(), b.getRawData(), false);
                }
            }
            for (Sign b : savedSigns.values()) {
                if (b.getBlock().getLocation().getBlock().getTypeId() != b.getTypeId()) {
                    b.getBlock().getLocation().getBlock().setTypeIdAndData(b.getTypeId(), b.getRawData(), false);
                    Sign dest = (Sign) b.getBlock().getLocation().getBlock().getState();
                    for (int i = 0; i < 4; ++i) {
                        dest.setLine(i, b.getLine(i));
                    }
                    dest.update();
                }
            }
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

