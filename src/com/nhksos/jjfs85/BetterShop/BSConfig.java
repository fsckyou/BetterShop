
package com.nhksos.jjfs85.BetterShop;

import com.jascotty2.CheckInput;
import com.jascotty2.Item.Item;
import com.jascotty2.Item.ItemDB;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;

import org.bukkit.util.config.Configuration;
import org.bukkit.util.config.ConfigurationNode;


public class BSConfig {

    // chat messages
    private final HashMap<String, String> stringMap = new HashMap<String, String>();
    public String currency = "Coin";
    public int pagesize = 9;
    public boolean publicmarket = false;
    public String defColor = "white";
    // files used by plugin
    public final static String configname = "config.yml";
    public final static File pluginFolder = new File("plugins", BetterShop.name);
    public final static File configfile = new File(pluginFolder, configname);
    // plugin settings
    public boolean checkUpdates = true;
    // database information
    public String tableName = "BetterShop";
    public String sql_username = "root", sql_password = "root", sql_database = "minecraft", sql_hostName = "localhost", sql_portNum = "3306";
    private DBType databaseType = DBType.FLATFILE;
    // database caching (MySQL only)
    public boolean useDBCache = true;
    //public BigInteger priceListLifespan = new BigInteger("0"); // 0 = never update
    public long priceListLifespan = 0; // 0 = never update
    public int tempCacheTTL = 10; //how long before tempCache is considered outdated (seconds)
    // logging settings
    public boolean logUserTransactions = false, logTotalTransactions = false;
    //public BigInteger userTansactionLifespan = new BigInteger("172800"); // 2 days.. i'd like to use unsigned long, but java doesn't have it..
    public long userTansactionLifespan = 172800;
    public String transLogTablename = "BetterShopMarketActivity", recordTablename = "BetterShopTransactionTotals";
    // if pricelist is to have a custom sort order, what should be at the top
    public ArrayList<String> sortOrder = new ArrayList<String>();
    //item buying behavior
    //if someone without BetterShop.admin.illegal can buy illegal items
    public boolean allowbuyillegal = false;
    //whether maxstack should be honored
    public boolean usemaxstack = true;
    //used tools can be bought back?
    public boolean buybacktools = true;
    // global or region shops
    public boolean useRegionShops = false;
    // dynamic pricing options
    public boolean useDynamicPricing = false;
    //if a price is not set on a craftable item, can still sell if the materials to make are for sale
    public boolean sellcraftables= true;
    // if sellcraftables, how much % more a crafted item costs than the materials
    public double sellcraftableMarkup = .05;
    // if sellcraftables, if colored wool should sell for less than sellcraftableMarkup
    //   (otherwise, a player could buy dye, color a sheep, get 1-3 colored wool, make profit)
    public boolean woolsellweight= true;
    // stock items
    public boolean useItemStock = false;
    // table/file name to use
    public String stockTablename = "BetterShopItemStock";
    //  how much an added item has to start with
    public long startStock = 200;
    // max stock to carry (stock is increased with sales)
    public long maxStock =500;
    // deny sales if stock is full?
    public boolean noOverStock = true;
    // restock interval.. automatic, and stock will be reset to startStock value
    public long restock = 21600; //6h
    
    
    public enum DBType {

        MYSQL, SQLITE, FLATFILE
    }

    public BSConfig() {
        create();
        load();
    }

    public final boolean load() {
        try {
            Configuration config = new Configuration(configfile);
            config.load();
            ConfigurationNode n;
            
            checkUpdates=config.getBoolean("CheckForUpdates", checkUpdates);
            
            pagesize = config.getInt("ItemsPerPage", pagesize);
            publicmarket = config.getBoolean("publicmarket", publicmarket);

            allowbuyillegal = config.getBoolean("allowbuyillegal", allowbuyillegal);
            usemaxstack = config.getBoolean("usemaxstack", usemaxstack);
            buybacktools = config.getBoolean("buybacktools", buybacktools);

            tableName = config.getString("tablename", tableName);
            databaseType = config.getBoolean("useMySQL", config.getBoolean("useMySQLPricelist", false)) ? DBType.MYSQL : DBType.FLATFILE;
            
            defColor = config.getString("defaultItemColor", defColor);
            ItemDB.setDefaultColor(defColor);
            
            String customsort = config.getString("customsort");
            if (customsort != null) {
                // parse for items && add to custom sort arraylist
                String items[] = customsort.split(",");
                for (String i : items) {
                    Item toAdd = Item.findItem(i.trim());
                    if (toAdd != null) {
                        sortOrder.add(toAdd.IdDatStr());
                    } else {
                        BetterShop.Log("Invalid Item in customsort configuration: " + i);
                    }
                }
            }
            
            if (databaseType == DBType.MYSQL) {
                n = config.getNode("MySQL");
                if (n != null) {
                    sql_username = n.getString("username", sql_username);
                    sql_password = n.getString("password", sql_password);
                    sql_database = n.getString("database", sql_database).replace(" ", "_");
                    sql_hostName = n.getString("Hostname", sql_hostName);
                    sql_portNum = n.getString("Port", sql_portNum);
                    String lifespan = n.getString("tempCacheTTL");
                    if (lifespan != null) {
                        tempCacheTTL = CheckInput.GetInt(lifespan, tempCacheTTL);
                    }
                    useDBCache = n.getBoolean("cache", useDBCache);
                    lifespan = n.getString("cacheUpdate");
                    if (lifespan != null) {
                        try {
                            priceListLifespan = CheckInput.GetBigInt_TimeSpanInSec(lifespan, 'h').longValue();
                        } catch (Exception ex) {
                            BetterShop.Log(Level.WARNING, "cacheUpdate has an illegal value");
                            BetterShop.Log(Level.WARNING, ex);
                        }
                    }
                } else {
                    BetterShop.Log(Level.WARNING, "MySQL section in " + configname + " is missing");
                }
            }
            n = config.getNode("transactionLog");
            if (n != null) {
                logUserTransactions = n.getBoolean("enabled", logUserTransactions);
                transLogTablename = n.getString("logtablename", transLogTablename).replace(" ", "_");
                String lifespan = n.getString("userTansactionLifespan");
                if (lifespan != null) {
                    try {
                        userTansactionLifespan = CheckInput.GetBigInt_TimeSpanInSec(lifespan).longValue();
                    } catch (Exception ex) {
                        BetterShop.Log(Level.WARNING, "userTansactionLifespan has an illegal value", ex);
                    }
                }
            } else {
                BetterShop.Log(Level.WARNING, "transactionLog section in config not found");
            }

            n = config.getNode("totalsTransactionLog");
            if (n != null) {
                logTotalTransactions = n.getBoolean("enabled", logTotalTransactions);
                recordTablename = n.getString("logtablename", recordTablename).replace(" ", "_");
            } else {
                BetterShop.Log(Level.WARNING, "totalsTransactionLog section in config not found");
            }
            
            n = config.getNode("dynamicMarket");
            if (n != null) {
                useDynamicPricing = n.getBoolean("enabled", useDynamicPricing);
                sellcraftables= n.getBoolean("sellcraftables", sellcraftables);
                sellcraftableMarkup = n.getDouble("sellcraftableMarkup", sellcraftableMarkup/100)*100;
                woolsellweight=n.getBoolean("woolsellweight", woolsellweight) ;
            }
            
            n = config.getNode("itemStock");
            if (n != null) {
                useItemStock=n.getBoolean("enabled", useItemStock);
                stockTablename = n.getString("stockTablename", stockTablename);
                noOverStock = n.getBoolean("noOverStock", noOverStock);
                String num = n.getString("startStock");
                if(num!=null){
                    startStock = CheckInput.GetLong(num, startStock);//CheckInput.GetBigInt(num, startStock).longValue();
                }
                num = n.getString("maxStock");
                if(num!=null){
                    maxStock = CheckInput.GetLong(num, maxStock);//CheckInput.GetBigInt(num, maxStock).longValue();
                }
                num = n.getString("restock");
                if(num!=null){
                    restock = CheckInput.GetBigInt_TimeSpanInSec(num, 'h').longValue();
                }
            }
            
            
            n = config.getNode("strings");
            if (n != null) {
                stringMap.clear();
                for (Object k : config.getKeys("strings").toArray()) {
                    if (k instanceof String) {
                        String tmpString = n.getString(k.toString());
                        // double-check that recieved a value
                        if (tmpString != null) {
                            tmpString = tmpString.replace("&", "\u00A7").replace("%", "%%"); // tmpString.replaceAll("&.", "\u00A7").replace("%", "%%");//
                            // put the string in a HashMap for retrieval later
                            stringMap.put(k.toString(), tmpString);
                        }
                    }
                }
            } else {
                BetterShop.Log(Level.SEVERE, String.format("strings section missing from configuration file %s", configname));
            }
        } catch (Exception ex) {
            BetterShop.Log(Level.SEVERE, "Error parsing configuration file");
            BetterShop.Log(Level.SEVERE, ex);
            return false;
        }
        return true;
    }

    public boolean useMySQL() {
        return databaseType == DBType.MYSQL;
    }

    public boolean useFlatfile() {
        return databaseType == DBType.FLATFILE;
    }

    public String getString(String key) {
        String ret = stringMap.get(key);
        if (ret == null) {
            BetterShop.Log(Level.WARNING, String.format("%s missing from configuration file", key));
            ret = "(Error: Message is Missing)";
        }
        return ret;
    }

    public boolean hasString(String key) {
        String ret = stringMap.get(key);
        if (ret == null) {
            BetterShop.Log(Level.WARNING, String.format("%s missing from configuration file", key));
            return false;
        }
        return true;
    }

    private void create() {
        pluginFolder.mkdirs();
        if (!configfile.exists()) {
            try {
                BetterShop.Log(Level.WARNING, configname + " not found. Creating new file.");
                configfile.createNewFile();
                InputStream res = BetterShop.class.getResourceAsStream("/config.yml");
                FileWriter tx = new FileWriter(configfile);
                try {
                    for (int i = 0; (i = res.read()) > 0;) {
                        tx.write(i);
                    }
                } finally {
                    tx.flush();
                    tx.close();
                    res.close();
                }
            } catch (IOException ex) {
                BetterShop.Log(Level.SEVERE, "Failed creating new config file ", ex);
            }
        }
        //else logger.log(Level.INFO, configname + " found!");
    }
}
