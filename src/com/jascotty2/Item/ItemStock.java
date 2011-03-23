/**
 * Programmer: Jacob Scott
 * Program Name: ItemStock
 * Description: provides a way to track how much of an item is avaliable
 * Date: Mar 22, 2011
 */
package com.jascotty2.Item;

import com.jascotty2.CSV;
import com.jascotty2.CheckInput;
import com.jascotty2.MySQL.MySQLItemStock;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.inventory.ItemStack;

/**
 * @author jacob
 */
public class ItemStock {

// set this to true to catch & log exceptions instead of throwing them
    public static boolean log_nothrow = false;
    // what to write to if logging
    protected final static Logger logger = Logger.getLogger("Minecraft");
    //how long before tempCache is considered outdated (seconds), used if MySQL & caching disabled
    public int tempCacheTTL = 10;
    // temporary storage for last queried item (primarily for when caching is disabled)
    ItemStockEntry tempCache = null;
    // use full db caching?
    public boolean useCache = false;
    // if using caching, how long before cache is considered outdated (seconds)
    public long dbCacheTTL = 0;
    // if false, will disconnect from db when now using it (use if have a high dbCacheTTL)
    //public boolean persistentMySQL = true;
    // last time the stockList was updated
    protected Date lastCacheUpdate = null;
    protected ArrayList<ItemStockEntry> stockList = new ArrayList<ItemStockEntry>();
    private boolean isLoaded = false;
    protected DBType databaseType = DBType.FLATFILE;
    //  current db connection, if using MySQL
    protected MySQLItemStock MySQLstockList = null;
    // file, if flatfile
    protected File flatFile = null;

    /*
    public static <T extends Comparable<? super T>> List<T> asSortedList(Collection<T> c) {
    List<T> list = new ArrayList<T>(c);
    java.util.Collections.sort(list);
    return list;
    }//*/
    public static class ItemStockEntryComparator implements Comparator<ItemStockEntry> {

        boolean descending = true;

        public ItemStockEntryComparator() {
        }

        public int compare(ItemStockEntry o1, ItemStockEntry o2) {
            return (o1.itemNum * 100 - o2.itemNum * 100 + o1.itemSub - o2.itemSub) * (descending ? 1 : -1);
        }
    }

    public void sort() {
        //stockList=Arrays.sort(stockList.toArray());
        java.util.Collections.sort(stockList, new ItemStockEntryComparator());
    }

    public enum DBType {

        MYSQL, FLATFILE //  SQLITE,
    }

    public ItemStock() {
    } // end default constructor

    public String stockListName() {
        return databaseType == DBType.MYSQL
                ? (MySQLstockList == null ? null
                : (MySQLstockList.GetHostName() + ":"
                + MySQLstockList.GetDatabaseName() + "/"
                + MySQLstockList.GetTableName())) : flatFile == null ? null : flatFile.getName();
    }

    public com.jascotty2.MySQL.MySQL getMySQLconnection() {
        return MySQLstockList == null ? null : MySQLstockList.getMySQLconnection();
    }

    public final boolean reloadList() throws SQLException, IOException, Exception {
        if (databaseType == DBType.MYSQL) {
            return reloadMySQL();
        } else {
            return loadFile(flatFile);
        }
    }

    private boolean reloadMySQL() throws SQLException, Exception { //
        if (MySQLstockList != null)// && MySQLdatabase.IsConnected())  MySQLdatabase.disconnect();
        {
            stockList.clear();
            try {
                MySQLstockList.connect();
            } catch (Exception ex) {
                if (log_nothrow) {
                    logger.log(Level.SEVERE, "Error connecting to MySQL database", ex);
                } else {
                    throw new Exception("Error connecting to MySQL database", ex);
                }
            }
            // load stockList data
            stockList.addAll(MySQLstockList.GetFullList());
        }

        return false;
    }

    public final boolean loadMySQL(String database, String tableName, String username, String password, String hostName, String portNum) throws SQLException, Exception {
        databaseType = DBType.MYSQL;
        try {
            if (MySQLstockList == null) {
                MySQLstockList = new MySQLItemStock(database, tableName, username, password, hostName, portNum);
            } else {
                MySQLstockList.connect(database, tableName, username, password, hostName, portNum);
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
        return isLoaded = MySQLstockList.IsConnected();
    }

    public boolean loadFile(File toload) throws IOException, Exception {
        if (toload != null) {
            databaseType = DBType.FLATFILE;
            flatFile = toload;
            stockList.clear();
            if (toload.exists()) {
                isLoaded = false;

                try {
                    ArrayList<String[]> actFile = CSV.loadFile(flatFile);
                    for (int n = 0; n < actFile.size(); ++n) {//String[] line : actFile) {
                        String[] line = actFile.get(n);
                        if (line.length == 4) {
                            Item plItem = Item.findItem(line[0] + ":" + (line[1].equals(" ") ? "0" : line[1]));
                            if (plItem != null) {
                                stockList.add(new ItemStockEntry(plItem, CheckInput.GetLong(line[3], 0)));
                            } else if (n > 0) { // first line is expected invalid: is title
                                logger.log(Level.WARNING, String.format("Invalid item on line %d in %s", (n + 1), toload.getName()));
                            }
                        } else {
                            logger.log(Level.WARNING, String.format("unexpected stockList line at %d in %s", (n + 1), toload.getName()));
                        }
                    }
                } catch (FileNotFoundException ex) {
                    if (!log_nothrow) {
                        throw new IOException("Unexpected Error: File not found: " + flatFile.getName(), ex);
                    }
                    logger.log(Level.SEVERE, "Unexpected Error: File not found: " + flatFile.getName(), ex);
                } catch (IOException ex) {
                    if (!log_nothrow) {
                        throw new IOException("Error opening " + toload.getName() + " for reading", ex);
                    }
                    logger.log(Level.SEVERE, "Error opening " + toload.getName() + " for reading", ex);
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
        // no need to save.. is saved after every edit
        //save();
        if (databaseType == DBType.MYSQL) {
            MySQLstockList.commit();
            MySQLstockList.disconnect();
            MySQLstockList = null;
        }
        stockList.clear();
        lastCacheUpdate = null;
        isLoaded = false;
    }

    public boolean IsLoaded() {
        if (databaseType == DBType.MYSQL) {
            return (dbCacheTTL == 0 && MySQLstockList.IsConnected()) || isLoaded;
        }
        return isLoaded;
    }

    public boolean save() throws IOException, SQLException {
        sort();
        if (databaseType == DBType.MYSQL) {
            try {
                MySQLstockList.commit();
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
                    throw new IOException("Error Saving " + (flatFile == null ? "flatfile stock database" : flatFile.getName()), ex);
                }
                logger.log(Level.SEVERE, "Error Saving " + (flatFile == null ? "flatfile stock database" : flatFile.getName()), ex);
                return false;
            }
        }
    }

    public boolean saveFile(File tosave) throws IOException {
        if (tosave != null && !tosave.isDirectory()) {
            if (!tosave.exists() || tosave.canWrite()) {
                try {
                    ArrayList<String> lines = new ArrayList<String>();
                    lines.add("id,subdata,name,amount");
                    for (ItemStockEntry i : stockList) {
                        // names provided for others to easily edit db
                        lines.add(i.itemNum + "," + i.itemSub + "," + i.name + "," + i.amount);
                    }
                    if (!CSV.saveFile(flatFile, lines)) {
                        if (!log_nothrow) {
                            throw new IOException("Error writing to " + flatFile.getName());
                        }
                        logger.log(Level.SEVERE, String.format("Error writing to %s", flatFile.getName()));
                    }
                } catch (FileNotFoundException ex) {
                    if (!log_nothrow) {
                        throw new IOException("Unexpected Error: File not found: " + tosave.getName(), ex);
                    }
                    logger.log(Level.SEVERE, "Unexpected Error: File not found: " + tosave.getName(), ex);
                } catch (IOException ex) {
                    if (!log_nothrow) {
                        throw new IOException("Error opening " + tosave.getName() + " for writing", ex);
                    }
                    logger.log(Level.SEVERE, "Error opening " + tosave.getName() + " for writing", ex);
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

    public void updateCache() throws SQLException, Exception {
        updateCache(true);
    }

    public void updateCache(boolean checkFirst) throws SQLException, Exception {
        if (databaseType != DBType.MYSQL) {
            // (flatfile is cached, manually updated)
            return;
        }
        //System.out.println("use c:" + useCache + "   " + dbCacheTTL);
        if (!checkFirst || (useCache && lastCacheUpdate == null)
                || (useCache && dbCacheTTL > 0 && lastCacheUpdate != null
                && ((new Date()).getTime() - lastCacheUpdate.getTime()) / 1000 > dbCacheTTL)) {
            //System.out.println("updating cache (" + (lastCacheUpdate != null?((new Date()).getTime() - lastCacheUpdate.getTime())/100:-1) + "s since last update)");
            // MySQL cache outdated: update
            LinkedList<ItemStockEntry> update = MySQLstockList.GetFullList();
            stockList.clear();
            stockList.addAll(update);
            lastCacheUpdate = new Date();
        }
        //else cache up-to-date, or disabled
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
            tempCache = MySQLstockList.GetItem(check);
        } else { // should be in cache
            int i = stockList.indexOf(new ItemStockEntry(check));
            if (i < 0) {
                return false;
            }//else
            if (tempCache == null) {
                tempCache = new ItemStockEntry(stockList.get(i));
            } else {
                tempCache.Set(stockList.get(i));
            }
        }
        return tempCache != null;
    }

    public long getItemAmount(ItemStack i) throws SQLException, Exception {
        return getItemAmount(Item.findItem(i));
    }

    public long getItemAmount(String s) throws SQLException, Exception {
        return getItemAmount(Item.findItem(s));
    }

    public long getItemAmount(Item it) throws SQLException, Exception {
        if (it == null) {
            return -1;
        }
        ItemStockEntry itp = getItemEntry(it);
        if (itp != null) {
            return itp.amount;
        }
        return -1;
    }

    public void changeItemAmount(ItemStack i, long delta) throws SQLException, Exception {
        changeItemAmount(Item.findItem(i), delta);
    }

    public void changeItemAmount(String s, long delta) throws SQLException, Exception {
        changeItemAmount(Item.findItem(s), delta);
    }

    public void changeItemAmount(Item it, long delta) throws SQLException, Exception {
        if (it == null) {
            return;
        }
        ItemStockEntry itp = getItemEntry(it);
        if (itp != null) {
            setItemAmount(it, itp.amount + delta);
        }
    }

    public ItemStockEntry getItemEntry(Item it) throws SQLException, Exception {
        if (it == null) {
            return null;
        }
        if (tempCache != null && tempCache.equals(it)
                && ((new Date()).getTime() - tempCache.getTime()) / 1000 < tempCacheTTL) {
            // use temp
            return tempCache;
        }
        if (databaseType == DBType.MYSQL && !useCache) {
            tempCache = MySQLstockList.GetItem(it);
        } else {
            updateCache(true);
            int i = stockList.indexOf(it);
            //System.out.println("found " + it + " at " + i);
            if (i < 0) {
                return null;
            } else if (tempCache == null) {
                tempCache = new ItemStockEntry(stockList.get(i));
            } else {
                tempCache.Set(stockList.get(i));
            }
            //System.out.println(tempCache);
        }
        return tempCache;
    }

    public boolean setItemAmount(String item, long a) throws SQLException, IOException, Exception {
        return setItemAmount(Item.findItem(item), a);
    }

    public boolean setItemAmount(Item it, long a) throws SQLException, IOException, Exception {
        if (it == null) {//  ||(b < 0 && s < 0)) {
            return false;
        }
        tempCache = null;
        if (databaseType == DBType.MYSQL) {
            MySQLstockList.SetAmount(it, a);
            updateCache(false);
            return true;
        } else {
            int i = stockList.indexOf(new ItemStockEntry(it));
            if (i < 0) {
                stockList.add(new ItemStockEntry(it, a));
            } else {
                stockList.get(i).SetAmount(a);
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
            MySQLstockList.RemoveItem(it);
            updateCache(false);
            return true;
        } else {
            if (stockList.remove(new ItemStockEntry(it))) {
                return save();
            }
        }
        return false;
    }

    public ItemStockEntry[] getstockListItems() throws SQLException, Exception {
        if (databaseType == DBType.MYSQL && !useCache) {
            updateCache(false);// manually update
        } else {
            updateCache(true);
        }
        return stockList.toArray(new ItemStockEntry[0]);
    }
} // end class ItemStock

