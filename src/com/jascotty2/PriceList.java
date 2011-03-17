/**
 * Programmer: Jacob Scott
 * Program Name: PriceList
 * Description: tracks items in a database with buy/sell prices
 * Date: Mar 14, 2011
 */
package com.jascotty2;

import com.jascotty2.MySQL.MySQLPriceList;
import com.jascotty2.Item.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.inventory.ItemStack;

public class PriceList {
    // set this to true to catch & log exceptions instead of throwing them

    public static boolean log_nothrow = false;
    // what to write to if logging
    protected final static Logger logger = Logger.getLogger("Minecraft");
    //how long before tempCache is considered outdated (seconds), used if MySQL & caching disabled
    public int tempCacheTTL = 10;
    // temporary storage for last queried item (primarily for when caching is disabled)
    PriceListItem tempCache = null;
    // if using caching, how long before cache is considered outdated (seconds)
    public int dbCacheTTL = 0;
    // if false, will disconnect from db when now using it (use if have a high dbCacheTTL)
    public boolean persistentMySQL = true;
    // last time the pricelist was updated
    protected Date lastCacheUpdate = null;
    ArrayList<PriceListItem> priceList = new ArrayList<PriceListItem>();
    private boolean isLoaded = false;
    private DBType databaseType = DBType.FLATFILE;
    //  current db connection, if using MySQL
    protected MySQLPriceList MySQLpricelist = null;
    // file, if flatfile
    File flatFile = null;
    // max buy/sell price
    public static final int MAX_PRICE = 999999999; // int max=2,147,483,647
    // uses the Item.IdDatStr() (like "5:0")
    public ArrayList<String> sortOrder = new ArrayList<String>();

    /*
    public static <T extends Comparable<? super T>> List<T> asSortedList(Collection<T> c) {
    List<T> list = new ArrayList<T>(c);
    java.util.Collections.sort(list);
    return list;
    }//*/
    public static class PriceListItemComparator implements Comparator<PriceListItem> {

        public ArrayList<String> sortOrder = new ArrayList<String>();
        boolean descending = true;

        public PriceListItemComparator() {
        }

        public PriceListItemComparator(ArrayList<String> sortOrder) {
            this.sortOrder.addAll(sortOrder);
        }

        public int compare(PriceListItem o1, PriceListItem o2) {
            if (sortOrder.size() > 0) {
                int o1i = sortOrder.indexOf(o1.IdDatStr());
                int o2i = sortOrder.indexOf(o2.IdDatStr());
                if (o1i != -1 && o2i != -1) {
                    return o2i - o1i;
                } else if (o1i != -1) {
                    return -1;
                } else if (o2i != -1) {
                    return 1;
                }
            }
            return (o1.ID() * 100 - o2.ID() * 100 + o1.Data() - o2.Data()) * (descending ? 1 : -1);
        }
    }

    public void sort() {
        //priceList=Arrays.sort(priceList.toArray());
        java.util.Collections.sort(priceList, new PriceListItemComparator(sortOrder));
    }

    public enum DBType {

        MYSQL, FLATFILE //  SQLITE,
    }

    public PriceList() {
    } // end default constructor

    public String pricelistName() {
        return databaseType == DBType.MYSQL
                ? (MySQLpricelist == null ? null
                : (MySQLpricelist.GetHostName() + ":"
                + MySQLpricelist.GetDatabaseName() + "/"
                + MySQLpricelist.GetTableName())) : flatFile == null ? null : flatFile.getName();
    }

    public com.jascotty2.MySQL.MySQL getMySQLconnection() {
        return MySQLpricelist == null ? null : MySQLpricelist.getMySQLconnection();
    }

    public final boolean reloadList() throws SQLException, IOException, Exception {
        lastCacheUpdate = new Date();
        if (databaseType == DBType.MYSQL) {
            return reloadMySQL();
        } else {
            return loadFile(flatFile);
        }
    }

    private boolean reloadMySQL() throws SQLException, Exception { //
        if (MySQLpricelist != null)// && MySQLdatabase.IsConnected())  MySQLdatabase.disconnect();
        {
            priceList.clear();
            try {
                MySQLpricelist.connect();
            } catch (Exception ex) {
                if (log_nothrow) {
                    logger.log(Level.SEVERE, "Error connecting to MySQL database", ex);
                } else {
                    throw new Exception("Error connecting to MySQL database", ex);
                }
            }
            // load pricelist data
            priceList.addAll(MySQLpricelist.GetFullList());
        }

        return false;
    }

    public final boolean loadMySQL(String database, String tableName, String username, String password, String hostName, String portNum) throws SQLException, Exception {
        databaseType = DBType.MYSQL;
        try {
            if (MySQLpricelist == null) {
                MySQLpricelist = new MySQLPriceList(database, tableName, username, password, hostName, portNum);
            } else {
                MySQLpricelist.connect(database, tableName, username, password, hostName, portNum);
            }
        } catch (SQLException ex) {
            if (log_nothrow) {
                logger.log(Level.SEVERE, "Error connecting to MySQL database or while retrieving table list", ex);
            } else {
                throw ex; // "Error connecting to MySQL database or while retrieving table list"
            }
        } catch (Exception ex) {
            if (log_nothrow) {
                logger.log(Level.SEVERE, "Failed to start database connection", ex);
            } else {
                throw ex; // "Failed to start database connection"
            }
        }
        return isLoaded = MySQLpricelist.IsConnected();
    }

    public boolean loadFile(File toload) throws IOException, Exception {
        if (toload != null) {
            databaseType = DBType.FLATFILE;
            flatFile = toload;
            priceList.clear();
            if (toload.exists()) {
                isLoaded = false;
                FileReader fstream = null;
                try {
                    fstream = new FileReader(toload.getAbsolutePath());
                    BufferedReader in = new BufferedReader(fstream);
                    try {
                        int n = 0;
                        for (String line = null; (line = in.readLine()) != null && line.length() > 0; ++n) {
                            // if was edited in openoffice, will instead have semicolins..
                            String fields[] = line.replace(";", ",").replace(",,", ", ,").split(",");
                            // if (fields.length == 1) fields = line.split(";");
                            if (fields.length > 3) {
                                //Item plItem = Item.findItem(fields[0] + ":" + (fields[1].length() == 0 ? "0" : fields[1]));
                                Item plItem = Item.findItem(fields[0] + ":" + (fields[1].equals(" ") ? "0" : fields[1]));
                                if (plItem != null) {
                                    //priceList.add(new PriceListItem(plItem, fields[2].length() == 0 ? -1 : CheckInput.GetDouble(fields[2], -1), fields[3].length() == 0 ? -1 : CheckInput.GetDouble(fields[3], -1)));
                                    priceList.add(new PriceListItem(plItem,
                                            fields[2].equals(" ") ? -1 : CheckInput.GetDouble(fields[2], -1),
                                            fields[3].equals(" ") ? -1 : CheckInput.GetDouble(fields[3], -1)));
                                    //System.out.println("added: " + priceList.get(priceList.size()-1));
                                } else if (n > 0) { // first line is expected invalid: is title
                                    logger.log(Level.WARNING, String.format("Invalid item on line %d in %s", (n + 1), toload.getName()));
                                }
                            } else {
                                logger.log(Level.WARNING, String.format("unexpected pricelist line at %d in %s", (n + 1), toload.getName()));
                            }
                        }
                    } finally {
                        in.close();
                    }
                } catch (IOException ex) {
                    if (!log_nothrow) {
                        throw new IOException("Error opening " + toload.getName() + " for reading", ex);
                    }
                    logger.log(Level.SEVERE, "Error opening " + toload.getName() + " for reading", ex);
                } finally {
                    try {
                        fstream.close();
                    } catch (IOException ex) {
                        if (!log_nothrow) {
                            throw new IOException("Error closing " + toload.getName(), ex);
                        }
                        logger.log(Level.SEVERE, "Error closing " + toload.getName(), ex);
                    }
                }
                return isLoaded = true && save();
            } else { // !toload.exists()
                // still set as loaded
                isLoaded = true;
                return save();
            }
        }
        return false;
    }

    /**
     * closes connections & frees up used memory
     */
    public void close() throws IOException, SQLException {
        save();
        if (databaseType == DBType.MYSQL) {
            MySQLpricelist.disconnect();
            MySQLpricelist = null;
        }
        priceList.clear();
        lastCacheUpdate = null;
        isLoaded = false;
    }

    public boolean IsLoaded() {
        if (databaseType == DBType.MYSQL) {
            return (dbCacheTTL == 0 && MySQLpricelist.IsConnected()) || isLoaded;
        }
        return isLoaded;
    }

    public boolean save() throws IOException, SQLException {
        sort();
        if (databaseType == DBType.MYSQL) {
            try {
                MySQLpricelist.commit();
                return true;
            } catch (SQLException ex) {
                if (!log_nothrow) {
                    throw new SQLException("Error executing COMMIT", ex);
                }
                logger.log(Level.SEVERE, "Error executing COMMIT", ex);
                return false;
            }
        } else {
            try {
                return saveFile(flatFile);
            } catch (IOException ex) {
                if (!log_nothrow) {
                    throw new IOException("Error Saving " + (flatFile == null ? "flatfile price database" : flatFile.getName()), ex);
                }
                logger.log(Level.SEVERE, "Error Saving " + (flatFile == null ? "flatfile price database" : flatFile.getName()), ex);
                return false;
            }
        }
    }

    public boolean saveFile(File tosave) throws IOException {
        if (tosave != null && !tosave.isDirectory()) {
            if (!tosave.exists() || tosave.canWrite()) {
                FileWriter fstream = null;
                try {
                    fstream = new FileWriter(tosave.getAbsolutePath());
                    //System.out.println("writing to " + tosave.getAbsolutePath());
                    BufferedWriter out = new BufferedWriter(fstream);
                    out.write("id,subdata,buyprice,sellprice,name");
                    out.newLine();
                    for (PriceListItem i : priceList) {
                        // names provided for others to easily edit db
                        out.write(i.ID() + "," + i.Data() + "," + i.buy + "," + i.sell + "," + i.name);
                        out.newLine();
                    }
                    out.flush();
                    out.close();
                } catch (IOException ex) {
                    if (!log_nothrow) {
                        throw new IOException("Error opening " + tosave.getName() + " for writing", ex);
                    }
                    logger.log(Level.SEVERE, "Error opening " + tosave.getName() + " for writing", ex);
                } finally {
                    try {
                        fstream.close();
                    } catch (IOException ex) {
                        if (!log_nothrow) {
                            throw new IOException("Error closing " + tosave.getName(), ex);
                        }
                        logger.log(Level.SEVERE, "Error closing " + tosave.getName(), ex);
                    }
                }
                return true;
            }
        } else if (tosave == null) {
            throw new IOException("no file to save to");
        } else if (tosave.isDirectory()) {
            throw new IOException("file to save is a directory");
        }
        return false;
    }

    public void updateCache(boolean checkFirst) throws SQLException, Exception {
        if (databaseType != DBType.MYSQL || dbCacheTTL == 0) {
            // (flatfile is cached, manually updated)
            // caching disabled
            return;
        }
        if (!checkFirst
                || (checkFirst && databaseType == DBType.MYSQL && dbCacheTTL > 0 && lastCacheUpdate != null
                && ((new Date()).getTime() - lastCacheUpdate.getTime()) < dbCacheTTL * 1000)) {
            // MySQL cache outdated: update
            LinkedList<PriceListItem> update = MySQLpricelist.GetFullList();
            priceList.clear();
            priceList.addAll(update);
        }
    }

    public boolean ItemExists(String check) throws SQLException, Exception {
        return ItemExists(Item.findItem(check));
    }

    public boolean ItemExists(ItemStack check) throws SQLException, Exception {
        return ItemExists(Item.findItem(check));
    }

    public boolean ItemExists(Item check) throws SQLException, Exception {
        if (check == null) {
            return false;
        }
        updateCache(true);
        if (databaseType == DBType.MYSQL && dbCacheTTL == 0) {
            tempCache = MySQLpricelist.GetItem(check);
        } else { // should be in cache
            int i = priceList.indexOf(new PriceListItem(check));
            if (i < 0) {
                return false;
            }//else
            if (tempCache == null) {
                tempCache = new PriceListItem(priceList.get(i));
            } else {
                tempCache.Set(priceList.get(i));
            }
        }
        return tempCache != null;
    }

    public boolean isForSale(String s) throws SQLException, Exception {
        if (tempCache != null && tempCache.name.equals(s)) {
            // has been retrieved recently
            return tempCache.sell >= 0;
        }
        //if ItemExists, tempCache will contain the item
        return ItemExists(s) && tempCache.sell >= 0;//getSellPrice(s) >= 0;
    }

    public boolean isForSale(Item i) throws SQLException, Exception {
        if (tempCache != null && tempCache.equals(i)) {
            // has been retrieved recently
            return tempCache.sell >= 0;
        }
        //if ItemExists, tempCache will contain the item
        return ItemExists(i) && tempCache.sell >= 0;//getSellPrice(i) >= 0;
    }

    public boolean isForSale(ItemStack i) throws SQLException, Exception {
        if (tempCache != null && tempCache.equals(i)) {
            // has been retrieved recently
            return tempCache.sell >= 0;
        }
        //if ItemExists, tempCache will contain the item
        return ItemExists(i) && tempCache.sell >= 0;//getSellPrice(i) >= 0;
    }

    public double getSellPrice(ItemStack i) throws SQLException, Exception {
        return getSellPrice(Item.findItem(i));
    }

    public double getSellPrice(String s) throws SQLException, Exception {
        return getSellPrice(Item.findItem(s));
    }

    public double getSellPrice(Item it) throws SQLException, Exception {
        if (it == null) {
            return -1;
        }
        if (tempCache != null && tempCache.equals(it)
                && ((new Date()).getTime() - tempCache.getTime()) < tempCacheTTL * 1000) {
            // use temp
            return tempCache.sell;
        }
        updateCache(true);
        if (databaseType == DBType.MYSQL && dbCacheTTL == 0) {
            tempCache = MySQLpricelist.GetItem(it);
        } else {
            int i = priceList.indexOf(it);
            if (i < 0) {
                return -1;
            }//else
            if (tempCache == null) {
                tempCache = new PriceListItem(priceList.get(i));
            } else {
                tempCache.Set(priceList.get(i));
            }
        }
        return tempCache == null ? -1 : tempCache.sell;
    }

    public double getBuyPrice(String s) throws SQLException, Exception {
        return getBuyPrice(Item.findItem(s));
    }

    public double getBuyPrice(Item it) throws SQLException, Exception {
        if (it == null) {
            return -1;
        }
        if (tempCache != null && tempCache.equals(it)
                && ((new Date()).getTime() - tempCache.getTime()) < tempCacheTTL * 1000) {
            // use temp
            return tempCache.buy;
        }
        updateCache(true);
        if (databaseType == DBType.MYSQL && dbCacheTTL == 0) {
            tempCache = MySQLpricelist.GetItem(it);
        } else {
            int i = priceList.indexOf(it);
            if (i < 0) {
                return -1;
            }//else
            if (tempCache == null) {
                tempCache = new PriceListItem(priceList.get(i));
            } else {
                tempCache.Set(priceList.get(i));
            }
        }
        return tempCache == null ? -1 : tempCache.buy;
    }

    public PriceListItem getItemPrice(Item it) throws SQLException, Exception {
        if (it == null) {
            return null;
        }
        if (tempCache != null && tempCache.equals(it)
                && ((new Date()).getTime() - tempCache.getTime()) < tempCacheTTL * 1000) {
            // use temp
            return tempCache;
        }
        updateCache(true);
        if (databaseType == DBType.MYSQL && dbCacheTTL == 0) {
            tempCache = MySQLpricelist.GetItem(it);
        } else {
            int i = priceList.indexOf(it);
            if (i < 0) {
                return null;
            }//else
            if (tempCache == null) {
                tempCache = new PriceListItem(priceList.get(i));
            } else {
                tempCache.Set(priceList.get(i));
            }
        }
        return tempCache;
    }

    public boolean setPrice(String item, double b, double s) throws SQLException, IOException, Exception {
        return setPrice(Item.findItem(item), b, s);
    }

    public boolean setPrice(Item it, double b, double s) throws SQLException, IOException, Exception {
        if (it == null || (b < 0 && s < 0)) {
            return false;
        }
        tempCache = null;
        if (databaseType == DBType.MYSQL) {
            MySQLpricelist.SetPrice(it, b, s);
            updateCache(false);
            return true;
        } else {
            int i = priceList.indexOf(new PriceListItem(it));
            if (i < 0) {
                priceList.add(new PriceListItem(it, b, s));
            } else {
                priceList.get(i).SetPrice(b, s);
            }
            return save();
        }
    }

    public boolean remove(String s) throws SQLException, Exception {
        return remove(Item.findItem(s));
    }

    public boolean remove(Item it) throws SQLException, Exception {
        tempCache = null;
        if (databaseType == DBType.MYSQL) {
            MySQLpricelist.RemoveItem(it);
            updateCache(false);
            return true;
        } else {
            if (priceList.remove((PriceListItem) it)) {
                return save();
            }
        }
        return false;
    }

    public LinkedList<String> GetItemList(boolean showIllegal) throws SQLException, Exception {
        LinkedList<String> items = new LinkedList<String>();

        for (int i = 0; i < priceList.size(); ++i) {
            if (!showIllegal && !priceList.get(i).IsLegal()) {
                continue;
            }
            items.add(priceList.get(i).coloredName());
        }
        return items;
    }

    public LinkedList<String> GetItemList(boolean showIllegal, String itemTail) throws SQLException, Exception {
        LinkedList<String> items = new LinkedList<String>();

        for (int i = 0; i < priceList.size(); ++i) {
            if (!showIllegal && !priceList.get(i).IsLegal()) {
                continue;
            }
            items.add(priceList.get(i).coloredName() + itemTail);
        }
        return items;
    }

    /**
     * returns a page of prices
     * @param pageNum page to lookup (-1 will print all pages)
     * @param isPlayer if should use minecraft font spacing
     * @param pageSize how many on a page
     * @param listing format to output listing with
     * @param header page header (<page> of <pages>)
     * @param footer page footer
     * @return a list of formatted lines
     * @throws SQLException if using MySQL database & there was some database connection error
     * @throws Exception some serious error occurred (details in message)
     */
    public LinkedList<String> GetShopListPage(int pageNum, boolean isPlayer, int pageSize, String listing, String header, String footer) throws SQLException, Exception {
        return GetShopListPage(pageNum, isPlayer, pageSize, listing, header, footer, false);
    }

    /**
     * returns a page of prices
     * @param pageNum page to lookup (-1 will print all pages)
     * @param isPlayer if should use minecraft font spacing
     * @param pageSize how many on a page
     * @param listing format to output listing with
     * @param header page header (<page> of <pages>)
     * @param footer page footer
     * @param showIllegal whether illegal items should be included in the listing
     * @return a list of formatted lines
     * @throws SQLException if using MySQL database & there was some database connection error
     * @throws Exception some serious error occurred (details in message)
     */
    public LinkedList<String> GetShopListPage(int pageNum, boolean isPlayer, int pageSize, String listing, String header, String footer, boolean showIllegal) throws SQLException, Exception {
        LinkedList<String> ret = new LinkedList<String>();
        if (databaseType == DBType.MYSQL && dbCacheTTL == 0) {
            // manually update
            priceList.clear();
            priceList.addAll(MySQLpricelist.GetFullList());
        }
        int pricelistsize = priceList.size();
        if (!showIllegal) {
            for (PriceListItem i : priceList) {
                if (!i.IsLegal()) {
                    --pricelistsize;
                }
            }
        }
        int pages = (int) Math.ceil((double) pricelistsize / pageSize);
        String listhead = header == null || header.length() == 0 ? ""
                : header.replace("<page>", pageNum < 0 ? "(All)" : String.valueOf(pageNum)).
                replace("<pages>", String.valueOf(pages));
        if (pageNum > pages) {
            ret.add("There is no page " + pageNum + ". (" + pages + " pages total)");
        } else {
            if (listhead.length() > 0) {
                ret.add(String.format(listhead, pageNum, pages));
            }
            listing = listing.replace("<item>", "%1$s").replace("<buyprice>", "%2$s").replace("<sellprice>", "%3$s");
            if (pageNum <= 0) {
                pageNum = 1;
                pageSize = priceList.size();
            }
            for (int i = pageSize * (pageNum - 1), n = 0; n < pageSize && i < priceList.size(); ++i, ++n) {
                if (!showIllegal && !priceList.get(i).IsLegal()) {
                    --n;
                    continue;
                }
                ret.add(String.format(listing, priceList.get(i).coloredName(),
                        String.format("%5s", priceList.get(i).buy <= 0 ? " No " : String.format("%01.2f", priceList.get(i).buy)),
                        String.format("%5s", priceList.get(i).sell <= 0 ? " No " : String.format("%01.2f", priceList.get(i).sell))));
            }
            if (footer != null && footer.length() > 0) {
                ret.add(footer);
            }
        }
        if (ret.size() > 2) {
            // format spaces
            return MinecraftFontWidthCalculator.alignTags(ret, isPlayer);
        }
        return ret;
    }

    /**
     * returns a page of prices, with discounts (if applicable)
     * @param pageNum page to lookup (-1 will print all pages)
     * @param playerName player to use for discount pricing
     * @param pageSize how many on a page
     * @param listing format to output listing with
     * @param header page header (<page> of <pages>)
     * @param footer page footer
     * @param showIllegal whether illegal items should be included in the listing
     * @return a list of formatted lines
     * @throws SQLException if using MySQL database & there was some database connection error
     * @throws Exception some serious error occurred (details in message)
     */
    public LinkedList<String> GetShopListPage(int pageNum, String playerName, int pageSize, String listing, String header, String footer, boolean showIllegal) throws SQLException, Exception {
        LinkedList<String> ret = new LinkedList<String>();
        if (databaseType == DBType.MYSQL && dbCacheTTL == 0) {
            // manually update
            priceList.clear();
            priceList.addAll(MySQLpricelist.GetFullList());
        }
        int pricelistsize = priceList.size();
        if (!showIllegal) {
            for (PriceListItem i : priceList) {
                if (!i.IsLegal()) {
                    --pricelistsize;
                }
            }
        }
        int pages = (int) Math.ceil((double) pricelistsize / pageSize);
        String listhead = header == null || header.length() == 0 ? ""
                : header.replace("<page>", pageNum < 0 ? "(All)" : String.valueOf(pageNum)).
                replace("<pages>", String.valueOf(pages));
        if (pageNum > pages) {
            ret.add("There is no page " + pageNum + ". (" + pages + " pages total)");
        } else {
            if (listhead.length() > 0) {
                ret.add(String.format(listhead, pageNum, pages));
            }
            listing = listing.replace("<item>", "%1$s").replace("<buyprice>", "%2$s").replace("<sellprice>", "%3$s");
            if (pageNum <= 0) {
                pageNum = 1;
                pageSize = priceList.size();
            }
            for (int i = pageSize * (pageNum - 1), n = 0; n < pageSize && i < priceList.size(); ++i, ++n) {
                if (!showIllegal && !priceList.get(i).IsLegal()) {
                    --n;
                    continue;
                }
                ret.add(String.format(listing, priceList.get(i).coloredName(),
                        String.format("%5s", priceList.get(i).buy <= 0 ? " No " : String.format("%01.2f", priceList.get(i).buy)),
                        String.format("%5s", priceList.get(i).sell <= 0 ? " No " : String.format("%01.2f", priceList.get(i).sell))));
            }
            if (footer != null && footer.length() > 0) {
                ret.add(footer);
            }
        }
        if (ret.size() > 2) {
            // format spaces
            return MinecraftFontWidthCalculator.alignTags(ret, true);
        }
        return ret;
    }
    
    public Item[] getItems() throws SQLException, Exception{
        return (Item[])getPricelistItems();
    }
    public PriceListItem[] getPricelistItems() throws SQLException, Exception{
        if (databaseType == DBType.MYSQL && dbCacheTTL == 0) {
            // manually update
            priceList.clear();
            priceList.addAll(MySQLpricelist.GetFullList());
        }else{
            updateCache(true);
        }
        return priceList.toArray(new PriceListItem[0]);
    }
    
    public Item[] getItems(boolean showIllegal) throws SQLException, Exception{
        //for(Item i :((Item[])getPricelistItems())) System.out.println(i);
        if(showIllegal) return getPricelistItems();
        //else
        return (Item[])getPricelistItems(showIllegal);
    }
    public PriceListItem[] getPricelistItems(boolean showIllegal) throws SQLException, Exception{
        if (databaseType == DBType.MYSQL && dbCacheTTL == 0) {
            // manually update
            priceList.clear();
            priceList.addAll(MySQLpricelist.GetFullList());
        }else{
            updateCache(true);
        }
        ArrayList<PriceListItem> items = new ArrayList<PriceListItem>();
        for (int i = 0; i < priceList.size(); ++i) {
            if (!showIllegal && !priceList.get(i).IsLegal()) {
                continue;
            }
            items.add(priceList.get(i));
        }
        return items.toArray(new PriceListItem[0]);
    }
} // end class PriceList

